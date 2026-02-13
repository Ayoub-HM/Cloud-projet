package com.demo.notes.medisante;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api/medisante")
public class MediSanteController {
  private static final String REQUIRED_TELECONSULTATION_FIELDS_MESSAGE =
      "patientName, doctorName, speciality, scheduledAt and reason are required";
  private static final String DEFAULT_SERVICE_IMAGE =
      "https://images.unsplash.com/photo-1579684385127-1ef15d508118?auto=format&fit=crop&w=1200&q=80";
  private static final String TELECONSULT_IMAGE =
      "https://images.unsplash.com/photo-1584515933487-779824d29309?auto=format&fit=crop&w=1200&q=80";
  private static final String RECORDS_IMAGE =
      "https://images.unsplash.com/photo-1576091160550-2173dba999ef?auto=format&fit=crop&w=1200&q=80";
  private static final String PRESCRIPTION_IMAGE =
      "https://images.unsplash.com/photo-1585435557343-3b092031a831?auto=format&fit=crop&w=1200&q=80";

  private final MedicalServiceRepository medicalServiceRepository;
  private final OfficeRepository officeRepository;
  private final TeleconsultationRepository teleconsultationRepository;

  public MediSanteController(
      MedicalServiceRepository medicalServiceRepository,
      OfficeRepository officeRepository,
      TeleconsultationRepository teleconsultationRepository
  ) {
    this.medicalServiceRepository = medicalServiceRepository;
    this.officeRepository = officeRepository;
    this.teleconsultationRepository = teleconsultationRepository;
  }

  @GetMapping("/home")
  public MediSanteHomeResponse home() {
    List<MedicalServiceCard> services = medicalServiceRepository.findAllByOrderByIdAsc()
        .stream()
        .map(service -> new MedicalServiceCard(
            service.getTitle(),
            service.getDescription(),
            service.getCategory(),
            resolveServiceImage(service)
        ))
        .toList();

    List<OfficeCard> offices = officeRepository.findAllByOrderByEmployeesDesc()
        .stream()
        .map(office -> new OfficeCard(
            office.getCity(),
            office.getRole(),
            office.getEmployees()
        ))
        .toList();

    return new MediSanteHomeResponse(
        "MediSante+",
        "Entreprise francaise de telemedecine",
        2018,
        120,
        "https://images.unsplash.com/photo-1631815588090-d1bcbe9a24b2?auto=format&fit=crop&w=1800&q=80",
        "MediSante+ est une entreprise francaise de telemedecine fondee en 2018, employant 120 collaborateurs repartis entre Paris (siege), Lyon et Bordeaux.",
        "L'entreprise propose une plateforme permettant aux patients de consulter des medecins a distance, de gerer leurs dossiers medicaux et de recevoir des prescriptions electroniques.",
        services,
        offices
    );
  }

  @GetMapping("/teleconsultations")
  public List<TeleconsultationCard> teleconsultations() {
    return teleconsultationRepository.findAllByOrderByScheduledAtAsc()
        .stream()
        .map(this::toCard)
        .toList();
  }

  @PostMapping("/teleconsultations")
  public ResponseEntity<Object> createTeleconsultation(@RequestBody TeleconsultationUpsertRequest req) {
    if (req == null
        || isBlank(req.patientName())
        || isBlank(req.doctorName())
        || isBlank(req.speciality())
        || req.scheduledAt() == null
        || isBlank(req.reason())) {
      return ResponseEntity.badRequest().body(REQUIRED_TELECONSULTATION_FIELDS_MESSAGE);
    }

    Teleconsultation saved = teleconsultationRepository.save(new Teleconsultation(
        req.patientName().trim(),
        req.doctorName().trim(),
        req.speciality().trim(),
        req.scheduledAt(),
        resolveStatus(req.status(), "PLANIFIEE"),
        req.reason().trim()
    ));

    URI location = ServletUriComponentsBuilder.fromCurrentRequest()
        .path("/{id}")
        .buildAndExpand(Objects.requireNonNull(saved.getId()))
        .toUri();

    return ResponseEntity.created(location).body(toCard(saved));
  }

  @PutMapping("/teleconsultations/{id}")
  public ResponseEntity<Object> updateTeleconsultation(@PathVariable("id") long id, @RequestBody TeleconsultationUpsertRequest req) {
    if (req == null
        || isBlank(req.patientName())
        || isBlank(req.doctorName())
        || isBlank(req.speciality())
        || req.scheduledAt() == null
        || isBlank(req.reason())) {
      return ResponseEntity.badRequest().body(REQUIRED_TELECONSULTATION_FIELDS_MESSAGE);
    }

    Optional<Teleconsultation> existingOpt = teleconsultationRepository.findById(id);
    if (existingOpt.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    Teleconsultation existing = existingOpt.get();

    existing.setPatientName(req.patientName().trim());
    existing.setDoctorName(req.doctorName().trim());
    existing.setSpeciality(req.speciality().trim());
    existing.setScheduledAt(req.scheduledAt());
    existing.setReason(req.reason().trim());
    existing.setStatus(resolveStatus(req.status(), existing.getStatus()));

    Teleconsultation saved = teleconsultationRepository.save(existing);
    return ResponseEntity.ok(toCard(saved));
  }

  @DeleteMapping("/teleconsultations/{id}")
  public ResponseEntity<Void> deleteTeleconsultation(@PathVariable("id") long id) {
    if (!teleconsultationRepository.existsById(id)) {
      return ResponseEntity.notFound().build();
    }
    teleconsultationRepository.deleteById(id);
    return ResponseEntity.noContent().build();
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  private String resolveStatus(String status, String fallback) {
    if (isBlank(status)) {
      return fallback;
    }
    return status.trim().toUpperCase();
  }

  private TeleconsultationCard toCard(Teleconsultation teleconsultation) {
    return new TeleconsultationCard(
        teleconsultation.getId(),
        teleconsultation.getPatientName(),
        teleconsultation.getDoctorName(),
        teleconsultation.getSpeciality(),
        teleconsultation.getScheduledAt(),
        teleconsultation.getStatus(),
        teleconsultation.getReason()
    );
  }

  private String resolveServiceImage(MedicalService service) {
    if ("Teleconsultation".equalsIgnoreCase(service.getCategory())) {
      return TELECONSULT_IMAGE;
    }
    if ("Dossier medical".equalsIgnoreCase(service.getCategory())) {
      return RECORDS_IMAGE;
    }
    if ("Prescription".equalsIgnoreCase(service.getCategory())) {
      return PRESCRIPTION_IMAGE;
    }
    return service.getImageUrl() == null || service.getImageUrl().isBlank()
        ? DEFAULT_SERVICE_IMAGE
        : service.getImageUrl();
  }

  public record MediSanteHomeResponse(
      String company,
      String label,
      int foundedYear,
      int employees,
      String heroImageUrl,
      String companyStory,
      String platformOverview,
      List<MedicalServiceCard> services,
      List<OfficeCard> offices
  ) {
  }

  public record MedicalServiceCard(String title, String description, String category, String imageUrl) {
  }

  public record OfficeCard(String city, String role, int employees) {
  }

  public record TeleconsultationUpsertRequest(
      String patientName,
      String doctorName,
      String speciality,
      Instant scheduledAt,
      String reason,
      String status
  ) {
  }

  public record TeleconsultationCard(
      Long id,
      String patientName,
      String doctorName,
      String speciality,
      Instant scheduledAt,
      String status,
      String reason
  ) {
  }
}
