package com.demo.notes.medisante;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "teleconsultations")
public class Teleconsultation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 120)
  private String patientName;

  @Column(nullable = false, length = 120)
  private String doctorName;

  @Column(nullable = false, length = 120)
  private String speciality;

  @Column(nullable = false)
  private Instant scheduledAt;

  @Column(nullable = false, length = 40)
  private String status;

  @Column(nullable = false, length = 500)
  private String reason;

  protected Teleconsultation() {
    // JPA
  }

  public Teleconsultation(
      String patientName,
      String doctorName,
      String speciality,
      Instant scheduledAt,
      String status,
      String reason
  ) {
    this.patientName = patientName;
    this.doctorName = doctorName;
    this.speciality = speciality;
    this.scheduledAt = scheduledAt;
    this.status = status;
    this.reason = reason;
  }

  public Long getId() {
    return id;
  }

  public String getPatientName() {
    return patientName;
  }

  public String getDoctorName() {
    return doctorName;
  }

  public String getSpeciality() {
    return speciality;
  }

  public Instant getScheduledAt() {
    return scheduledAt;
  }

  public String getStatus() {
    return status;
  }

  public String getReason() {
    return reason;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
