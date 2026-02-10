package com.demo.notes.note;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

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
    return repo.findById(id)
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
    return ResponseEntity.created(URI.create("/api/notes/" + saved.getId())).body(saved);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    if (!repo.existsById(id)) return ResponseEntity.notFound().build();
    repo.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}
