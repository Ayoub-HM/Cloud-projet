package com.demo.notes.medisante;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;
import java.util.List;

@Configuration
public class MediSanteBootstrap {

  @Bean
  CommandLineRunner seedMediSanteData(
      MedicalServiceRepository medicalServiceRepository,
      OfficeRepository officeRepository,
      TeleconsultationRepository teleconsultationRepository
  ) {
    return args -> {
      if (medicalServiceRepository.count() == 0) {
        medicalServiceRepository.saveAll(List.of(
            new MedicalService(
                "Consultations video securisees",
                "Mise en relation rapide patient-medecin avec historique de consultation.",
                "Teleconsultation",
                "https://images.unsplash.com/photo-1584515933487-779824d29309?auto=format&fit=crop&w=1200&q=80"
            ),
            new MedicalService(
                "Gestion des dossiers medicaux",
                "Centralisation des comptes rendus, analyses et ordonnances.",
                "Dossier medical",
                "https://images.unsplash.com/photo-1576091160550-2173dba999ef?auto=format&fit=crop&w=1200&q=80"
            ),
            new MedicalService(
                "Prescriptions electroniques",
                "Emission d'ordonnances numeriques et suivi du renouvellement.",
                "Prescription",
                "https://images.unsplash.com/photo-1585435557343-3b092031a831?auto=format&fit=crop&w=1200&q=80"
            )
        ));
      }

      medicalServiceRepository.findAllByOrderByIdAsc().forEach(service -> {
        if (service.getImageUrl() == null || service.getImageUrl().isBlank()) {
          service.setImageUrl(imageUrlForCategory(service.getCategory()));
          medicalServiceRepository.save(service);
        }
      });

      if (officeRepository.count() == 0) {
        officeRepository.saveAll(List.of(
            new Office("Paris", "Siege social", 65),
            new Office("Lyon", "Pole operations medicales", 32),
            new Office("Bordeaux", "Support patient et qualite", 23)
        ));
      }

      if (teleconsultationRepository.count() == 0) {
        teleconsultationRepository.saveAll(List.of(
            new Teleconsultation(
                "Nora Benali",
                "Dr Martin Legrand",
                "Medecine generale",
                Instant.parse("2026-02-14T09:30:00Z"),
                "PLANIFIEE",
                "Suivi post-consultation et renouvellement de traitement."
            ),
            new Teleconsultation(
                "Leo Moreau",
                "Dr Clara Vidal",
                "Dermatologie",
                Instant.parse("2026-02-14T13:15:00Z"),
                "PLANIFIEE",
                "Evaluation d'une irritation cutanee persistante."
            )
        ));
      }
    };
  }

  private String imageUrlForCategory(String category) {
    if ("Teleconsultation".equalsIgnoreCase(category)) {
      return "https://images.unsplash.com/photo-1584515933487-779824d29309?auto=format&fit=crop&w=1200&q=80";
    }
    if ("Dossier medical".equalsIgnoreCase(category)) {
      return "https://images.unsplash.com/photo-1576091160550-2173dba999ef?auto=format&fit=crop&w=1200&q=80";
    }
    if ("Prescription".equalsIgnoreCase(category)) {
      return "https://images.unsplash.com/photo-1585435557343-3b092031a831?auto=format&fit=crop&w=1200&q=80";
    }
    return "https://images.unsplash.com/photo-1579684385127-1ef15d508118?auto=format&fit=crop&w=1200&q=80";
  }
}
