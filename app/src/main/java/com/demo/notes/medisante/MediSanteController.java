package com.demo.notes.medisante;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/medisante")
public class MediSanteController {

  private final MedicalServiceRepository medicalServiceRepository;
  private final OfficeRepository officeRepository;

  public MediSanteController(MedicalServiceRepository medicalServiceRepository, OfficeRepository officeRepository) {
    this.medicalServiceRepository = medicalServiceRepository;
    this.officeRepository = officeRepository;
  }

  @GetMapping("/home")
  public MediSanteHomeResponse home() {
    List<MedicalServiceCard> services = medicalServiceRepository.findAllByOrderByIdAsc()
        .stream()
        .map(service -> new MedicalServiceCard(
            service.getTitle(),
            service.getDescription(),
            service.getCategory()
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
        "MediSante+ est une entreprise francaise de telemedecine fondee en 2018, employant 120 collaborateurs repartis entre Paris (siege), Lyon et Bordeaux.",
        "L'entreprise propose une plateforme permettant aux patients de consulter des medecins a distance, de gerer leurs dossiers medicaux et de recevoir des prescriptions electroniques.",
        services,
        offices
    );
  }

  public record MediSanteHomeResponse(
      String company,
      String label,
      int foundedYear,
      int employees,
      String companyStory,
      String platformOverview,
      List<MedicalServiceCard> services,
      List<OfficeCard> offices
  ) {
  }

  public record MedicalServiceCard(String title, String description, String category) {
  }

  public record OfficeCard(String city, String role, int employees) {
  }
}
