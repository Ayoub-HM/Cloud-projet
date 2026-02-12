package com.demo.auth;

import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final String expectedUsername;
  private final String expectedPassword;
  private final String staticToken;

  public AuthController(
      @Value("${auth.username:admin}") String expectedUsername,
      @Value("${auth.password:admin123}") String expectedPassword,
      @Value("${auth.static-token:medisante-token}") String staticToken
  ) {
    this.expectedUsername = expectedUsername;
    this.expectedPassword = expectedPassword;
    this.staticToken = staticToken;
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody LoginRequest request) {
    if (request == null || isBlank(request.username()) || isBlank(request.password())) {
      return ResponseEntity.badRequest().body(Map.of("error", "username and password are required"));
    }

    if (!expectedUsername.equals(request.username().trim()) || !expectedPassword.equals(request.password().trim())) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "invalid credentials"));
    }

    return ResponseEntity.ok(Map.of("token", staticToken, "type", "Bearer"));
  }

  @GetMapping("/validate")
  public ResponseEntity<?> validate(@RequestParam String token) {
    if (isBlank(token) || !staticToken.equals(token.trim())) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("valid", false));
    }
    return ResponseEntity.ok(Map.of("valid", true));
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  public record LoginRequest(
      @NotBlank String username,
      @NotBlank String password
  ) {
  }
}
