package com.demo.notes.note;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "notes")
public class Note {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 120)
  private String title;

  @Column(nullable = false, columnDefinition = "text")
  private String content;

  @Column(nullable = false)
  private Instant createdAt = Instant.now();

  protected Note() {
    // JPA
  }

  public Note(String title, String content) {
    this.title = title;
    this.content = content;
  }

  public Long getId() { return id; }
  public String getTitle() { return title; }
  public String getContent() { return content; }
  public Instant getCreatedAt() { return createdAt; }

  public void setTitle(String title) { this.title = title; }
  public void setContent(String content) { this.content = content; }
}
