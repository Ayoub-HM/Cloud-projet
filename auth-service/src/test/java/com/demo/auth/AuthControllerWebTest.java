package com.demo.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    value = AuthController.class,
    properties = {
        "auth.token-ttl-seconds=3600",
        "auth.app-base-url=http://localhost:8080",
        "auth.token-issuer=auth-service-test",
        "auth.jwt-secret=this-is-a-test-jwt-secret-with-32-characters"
    }
)
@SuppressWarnings("null")
class AuthControllerWebTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private UserAccountRepository userAccountRepository;

  @Test
  void signupCreatesAccount() throws Exception {
    given(userAccountRepository.existsByUsername(eq("alice"))).willReturn(false);
    given(userAccountRepository.save(any(UserAccount.class))).willAnswer(invocation -> invocation.getArgument(0));

    String payload = """
        {
          "username": "alice",
          "password": "super-secure-password"
        }
        """;

    mockMvc.perform(post("/api/auth/signup")
            .contentType(APPLICATION_JSON_VALUE)
            .content(payload))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.message").value("account created"));

    then(userAccountRepository).should().save(any(UserAccount.class));
  }

  @Test
  void loginReturnsJwtAndValidateAcceptsIt() throws Exception {
    UserAccount user = new UserAccount("alice", new BCryptPasswordEncoder().encode("super-secure-password"));
    given(userAccountRepository.findByUsername(eq("alice"))).willReturn(Optional.of(user));

    String payload = """
        {
          "username": "alice",
          "password": "super-secure-password"
        }
        """;

    MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
            .contentType(APPLICATION_JSON_VALUE)
            .content(payload))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").isString())
        .andReturn();

    JsonNode loginBody = objectMapper.readTree(loginResult.getResponse().getContentAsString());
    String token = loginBody.path("token").asText();

    mockMvc.perform(get("/api/auth/validate").queryParam("token", token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.valid").value(true))
        .andExpect(jsonPath("$.username").value("alice"));
  }

  @Test
  void validateRejectsTamperedToken() throws Exception {
    UserAccount user = new UserAccount("alice", new BCryptPasswordEncoder().encode("super-secure-password"));
    given(userAccountRepository.findByUsername(eq("alice"))).willReturn(Optional.of(user));

    String payload = """
        {
          "username": "alice",
          "password": "super-secure-password"
        }
        """;

    MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
            .contentType(APPLICATION_JSON_VALUE)
            .content(payload))
        .andExpect(status().isOk())
        .andReturn();

    JsonNode loginBody = objectMapper.readTree(loginResult.getResponse().getContentAsString());
    String tamperedToken = loginBody.path("token").asText() + "x";

    mockMvc.perform(get("/api/auth/validate").queryParam("token", tamperedToken))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.valid").value(false));
  }
}
