package com.demo.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final UserAccountRepository userAccountRepository;
  private final PasswordEncoder passwordEncoder;
  private final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();
  private final long tokenTtlSeconds;

  public AuthController(
      UserAccountRepository userAccountRepository,
      @Value("${auth.token-ttl-seconds:3600}") long tokenTtlSeconds
  ) {
    this.userAccountRepository = userAccountRepository;
    this.passwordEncoder = new BCryptPasswordEncoder();
    this.tokenTtlSeconds = tokenTtlSeconds;
  }

  @PostMapping("/signup")
  public ResponseEntity<?> signUp(@RequestBody SignupRequest request) {
    if (request == null || isBlank(request.username()) || isBlank(request.password())) {
      return ResponseEntity.badRequest().body(Map.of("error", "username and password are required"));
    }

    String username = request.username().trim().toLowerCase();
    String password = request.password().trim();

    if (username.length() < 3 || username.length() > 50) {
      return ResponseEntity.badRequest().body(Map.of("error", "username must be between 3 and 50 characters"));
    }

    if (password.length() < 8) {
      return ResponseEntity.badRequest().body(Map.of("error", "password must be at least 8 characters"));
    }

    if (userAccountRepository.existsByUsername(username)) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "username already exists"));
    }

    userAccountRepository.save(new UserAccount(username, passwordEncoder.encode(password)));
    return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "account created"));
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody LoginRequest request) {
    if (request == null || isBlank(request.username()) || isBlank(request.password())) {
      return ResponseEntity.badRequest().body(Map.of("error", "username and password are required"));
    }

    String username = request.username().trim().toLowerCase();
    String password = request.password().trim();

    UserAccount user = userAccountRepository.findByUsername(username).orElse(null);
    if (user == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "invalid credentials"));
    }

    Instant expiresAt = Instant.now().plusSeconds(tokenTtlSeconds);
    String token = UUID.randomUUID().toString();
    sessions.put(token, new Session(username, expiresAt));

    return ResponseEntity.ok(Map.of(
        "token", token,
        "type", "Bearer",
        "expiresAt", expiresAt.toString()
    ));
  }

  @GetMapping("/validate")
  public ResponseEntity<?> validate(@RequestParam String token) {
    Session session = sessions.get(token);
    if (session == null || session.expiresAt().isBefore(Instant.now())) {
      sessions.remove(token);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("valid", false));
    }
    return ResponseEntity.ok(Map.of("valid", true, "username", session.username()));
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  public record SignupRequest(
      @NotBlank @Size(min = 3, max = 50) String username,
      @NotBlank @Size(min = 8, max = 72) String password
  ) {
  }

  public record LoginRequest(
      @NotBlank String username,
      @NotBlank String password
  ) {
  }

  private record Session(String username, Instant expiresAt) {
  }
}
