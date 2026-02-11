package com.demo.notes.medisante;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "medical_services")
public class MedicalService {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 120)
  private String title;

  @Column(nullable = false, length = 2000)
  private String description;

  @Column(nullable = false, length = 80)
  private String category;

  @Column(nullable = false, length = 500)
  private String imageUrl;

  protected MedicalService() {
    // JPA
  }

  public MedicalService(String title, String description, String category, String imageUrl) {
    this.title = title;
    this.description = description;
    this.category = category;
    this.imageUrl = imageUrl;
  }

  public Long getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public String getCategory() {
    return category;
  }

  public String getImageUrl() {
    return imageUrl;
  }
}
