package com.demo.notes.note;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

  private final NoteRepository repo;

  public NoteController(NoteRepository repo) {
    this.repo = repo;
  }

  @GetMapping
  public List<Note> list() {
    return repo.findAll();
  }

  @GetMapping("/{id}")
  public ResponseEntity<Note> get(@PathVariable Long id) {
    return repo.findById(Objects.requireNonNull(id))
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping
  public ResponseEntity<?> create(@RequestBody NoteCreateRequest req) {
    if (req == null || req.title() == null || req.content() == null ||
        req.title().isBlank() || req.content().isBlank()) {
      return ResponseEntity.badRequest().body("title/content required");
    }

    Note saved = repo.save(new Note(req.title().trim(), req.content().trim()));
    Long id = Objects.requireNonNull(saved.getId(), "ID should be generated after save");

    URI location = ServletUriComponentsBuilder
        .fromCurrentRequest()
        .path("/{id}")
        .buildAndExpand(id)
        .toUri();

    return ResponseEntity.created(location).body(saved);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    if (id == null || !repo.existsById(id)) return ResponseEntity.notFound().build();
    repo.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}
