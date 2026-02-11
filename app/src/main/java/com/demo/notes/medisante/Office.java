package com.demo.notes.medisante;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "offices")
public class Office {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 80, unique = true)
  private String city;

  @Column(nullable = false, length = 100)
  private String role;

  @Column(nullable = false)
  private int employees;

  protected Office() {
    // JPA
  }

  public Office(String city, String role, int employees) {
    this.city = city;
    this.role = role;
    this.employees = employees;
  }

  public Long getId() {
    return id;
  }

  public String getCity() {
    return city;
  }

  public String getRole() {
    return role;
  }

  public int getEmployees() {
    return employees;
  }
}
