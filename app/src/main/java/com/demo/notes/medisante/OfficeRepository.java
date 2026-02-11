package com.demo.notes.medisante;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OfficeRepository extends JpaRepository<Office, Long> {
  List<Office> findAllByOrderByEmployeesDesc();
}
