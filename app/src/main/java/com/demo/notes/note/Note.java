package com.demo.notes.note;

import jakarta.persistence.*;

@Entity
@Table(name = "notes")
public class Note {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false, length = 2000)
  private String content;

  public Note() {}

  public Note(String title, String content) {
    this.title = title;
    this.content = content;
  }

  public Long getId() { return id; }
  public String getTitle() { return title; }
  public String getContent() { return content; }

  public void setId(Long id) { this.id = id; }
  public void setTitle(String title) { this.title = title; }
  public void setContent(String content) { this.content = content; }
}
