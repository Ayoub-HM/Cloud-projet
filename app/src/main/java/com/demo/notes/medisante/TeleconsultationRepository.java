package com.demo.notes.medisante;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeleconsultationRepository extends JpaRepository<Teleconsultation, Long> {
  List<Teleconsultation> findAllByOrderByScheduledAtAsc();
}
