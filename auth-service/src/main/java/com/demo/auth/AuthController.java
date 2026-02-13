package com.demo.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.dao.DataIntegrityViolationException;
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
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
  private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();
  private static final String HMAC_ALGORITHM = "HmacSHA256";

  private final UserAccountRepository userAccountRepository;
  private final PasswordEncoder passwordEncoder;
  private final ObjectMapper objectMapper;
  private final long tokenTtlSeconds;
  private final String appBaseUrl;
  private final String tokenIssuer;
  private final byte[] jwtSecret;

  public AuthController(
      UserAccountRepository userAccountRepository,
      ObjectMapper objectMapper,
      @Value("${auth.token-ttl-seconds:3600}") long tokenTtlSeconds,
      @Value("${auth.app-base-url:http://localhost:8080}") String appBaseUrl,
      @Value("${auth.token-issuer:auth-service}") String tokenIssuer,
      @Value("${auth.jwt-secret:dev-only-please-change-this-jwt-secret-32chars}") String jwtSecret
  ) {
    this.userAccountRepository = userAccountRepository;
    this.passwordEncoder = new BCryptPasswordEncoder();
    this.objectMapper = objectMapper;
    this.tokenTtlSeconds = Math.max(60, tokenTtlSeconds);
    this.appBaseUrl = appBaseUrl;
    this.tokenIssuer = tokenIssuer;
    this.jwtSecret = normalizeJwtSecret(jwtSecret);
  }

  @PostMapping("/signup")
  public ResponseEntity<?> signUp(@Valid @RequestBody SignupRequest request) {
    String username = request.username().trim().toLowerCase(Locale.ROOT);
    String password = request.password();

    if (username.length() < 3 || username.length() > 50) {
      return ResponseEntity.badRequest().body(Map.of("error", "username must be between 3 and 50 characters"));
    }

    if (password.length() < 8) {
      return ResponseEntity.badRequest().body(Map.of("error", "password must be at least 8 characters"));
    }

    if (userAccountRepository.existsByUsername(username)) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "username already exists"));
    }

    try {
      userAccountRepository.save(new UserAccount(username, passwordEncoder.encode(password)));
    } catch (DataIntegrityViolationException ignored) {
      // Protect against duplicate signup races on the unique username constraint.
      return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "username already exists"));
    }
    return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "account created"));
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
    String username = request.username().trim().toLowerCase(Locale.ROOT);
    String password = request.password();

    UserAccount user = userAccountRepository.findByUsername(username).orElse(null);
    if (user == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "invalid credentials"));
    }

    Instant issuedAt = Instant.now();
    Instant expiresAt = issuedAt.plusSeconds(tokenTtlSeconds);
    String token;
    try {
      token = issueToken(username, issuedAt, expiresAt);
    } catch (JsonProcessingException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "token generation failed"));
    }

    return ResponseEntity.ok(Map.of(
        "token", token,
        "type", "Bearer",
        "expiresAt", expiresAt.toString()
    ));
  }

  @GetMapping("/validate")
  public ResponseEntity<?> validate(@RequestParam String token) {
    Optional<TokenClaims> claims = parseAndValidateToken(token);
    if (claims.isEmpty()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("valid", false));
    }
    return ResponseEntity.ok(Map.of("valid", true, "username", claims.get().username()));
  }

  @GetMapping("/config")
  public ResponseEntity<?> config() {
    return ResponseEntity.ok(Map.of("appBaseUrl", appBaseUrl));
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  private String issueToken(String username, Instant issuedAt, Instant expiresAt) throws JsonProcessingException {
    String header = encodeJson(Map.of("alg", "HS256", "typ", "JWT"));
    String payload = encodeJson(Map.of(
        "sub", username,
        "iss", tokenIssuer,
        "iat", issuedAt.getEpochSecond(),
        "exp", expiresAt.getEpochSecond()
    ));
    String signature = sign(header + "." + payload);
    return header + "." + payload + "." + signature;
  }

  private Optional<TokenClaims> parseAndValidateToken(String token) {
    if (isBlank(token)) {
      return Optional.empty();
    }

    String[] parts = token.trim().split("\\.");
    if (parts.length != 3) {
      return Optional.empty();
    }

    String signedValue = parts[0] + "." + parts[1];
    String expectedSignature = sign(signedValue);
    if (!MessageDigest.isEqual(
        expectedSignature.getBytes(StandardCharsets.US_ASCII),
        parts[2].getBytes(StandardCharsets.US_ASCII))) {
      return Optional.empty();
    }

    try {
      String payloadJson = new String(BASE64_URL_DECODER.decode(parts[1]), StandardCharsets.UTF_8);
      JsonNode payloadNode = objectMapper.readTree(payloadJson);
      String username = payloadNode.path("sub").asText("");
      String issuer = payloadNode.path("iss").asText("");
      long expiresAtEpoch = payloadNode.path("exp").asLong(0);

      if (isBlank(username) || isBlank(issuer) || !tokenIssuer.equals(issuer)) {
        return Optional.empty();
      }
      if (expiresAtEpoch <= Instant.now().getEpochSecond()) {
        return Optional.empty();
      }

      return Optional.of(new TokenClaims(username));
    } catch (IllegalArgumentException | JsonProcessingException e) {
      return Optional.empty();
    }
  }

  private String encodeJson(Map<String, Object> payload) throws JsonProcessingException {
    return BASE64_URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(payload));
  }

  private String sign(String value) {
    try {
      Mac mac = Mac.getInstance(HMAC_ALGORITHM);
      mac.init(new SecretKeySpec(jwtSecret, HMAC_ALGORITHM));
      return BASE64_URL_ENCODER.encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      throw new IllegalStateException("Unable to sign JWT", e);
    }
  }

  private byte[] normalizeJwtSecret(String rawSecret) {
    if (isBlank(rawSecret)) {
      throw new IllegalArgumentException("auth.jwt-secret is required");
    }
    byte[] secretBytes = rawSecret.trim().getBytes(StandardCharsets.UTF_8);
    if (secretBytes.length < 32) {
      throw new IllegalArgumentException("auth.jwt-secret must be at least 32 characters");
    }
    return secretBytes;
  }

  public record SignupRequest(
      @NotBlank @Size(min = 3, max = 50) String username,
      @NotBlank @Size(min = 8, max = 72) String password
  ) {
  }

  public record LoginRequest(
      @NotBlank @Size(min = 3, max = 50) String username,
      @NotBlank @Size(min = 8, max = 72) String password
  ) {
  }

  private record TokenClaims(String username) {
  }
}
