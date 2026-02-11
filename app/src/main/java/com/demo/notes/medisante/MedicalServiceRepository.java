package com.demo.notes.medisante;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicalServiceRepository extends JpaRepository<MedicalService, Long> {
  List<MedicalService> findAllByOrderByIdAsc();
}
