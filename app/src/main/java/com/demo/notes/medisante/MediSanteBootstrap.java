package com.demo.notes.medisante;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class MediSanteBootstrap {

  @Bean
  CommandLineRunner seedMediSanteData(MedicalServiceRepository medicalServiceRepository, OfficeRepository officeRepository) {
    return args -> {
      if (medicalServiceRepository.count() == 0) {
        medicalServiceRepository.saveAll(List.of(
            new MedicalService(
                "Consultations video securisees",
                "Mise en relation rapide patient-medecin avec historique de consultation.",
                "Teleconsultation"
            ),
            new MedicalService(
                "Gestion des dossiers medicaux",
                "Centralisation des comptes rendus, analyses et ordonnances.",
                "Dossier medical"
            ),
            new MedicalService(
                "Prescriptions electroniques",
                "Emission d'ordonnances numeriques et suivi du renouvellement.",
                "Prescription"
            )
        ));
      }

      if (officeRepository.count() == 0) {
        officeRepository.saveAll(List.of(
            new Office("Paris", "Siege social", 65),
            new Office("Lyon", "Pole operations medicales", 32),
            new Office("Bordeaux", "Support patient et qualite", 23)
        ));
      }
    };
  }
}
