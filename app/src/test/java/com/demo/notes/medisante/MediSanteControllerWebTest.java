package com.demo.notes.medisante;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MediSanteController.class)
@SuppressWarnings("null")
class MediSanteControllerWebTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private MedicalServiceRepository medicalServiceRepository;

  @MockitoBean
  private OfficeRepository officeRepository;

  @MockitoBean
  private TeleconsultationRepository teleconsultationRepository;

  @Test
  void homeEndpointReturnsMediSanteData() throws Exception {
    given(medicalServiceRepository.findAllByOrderByIdAsc())
        .willReturn(List.of(new MedicalService(
            "Consultations video securisees",
            "Mise en relation rapide patient-medecin",
            "Teleconsultation",
            "https://example.com/service.jpg"
        )));

    given(officeRepository.findAllByOrderByEmployeesDesc())
        .willReturn(List.of(new Office("Paris", "Siege social", 65)));

    mockMvc.perform(get("/api/medisante/home"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.company").value("MediSante+"))
        .andExpect(jsonPath("$.foundedYear").value(2018))
        .andExpect(jsonPath("$.employees").value(120))
        .andExpect(jsonPath("$.services[0].category").value("Teleconsultation"))
        .andExpect(jsonPath("$.offices[0].city").value("Paris"));
  }

  @Test
  void createTeleconsultationReturnsCreated() throws Exception {
    Instant instant = Instant.parse("2026-02-15T10:30:00Z");
    Teleconsultation saved = new Teleconsultation(
        "Samir Haddad",
        "Dr Aline Perez",
        "Cardiologie",
        instant,
        "PLANIFIEE",
        "Controle annuel"
    );
    ReflectionTestUtils.setField(saved, "id", 10L);

    given(teleconsultationRepository.save(any(Teleconsultation.class)))
        .willReturn(saved);

    String payload = """
        {
          "patientName": "Samir Haddad",
          "doctorName": "Dr Aline Perez",
          "speciality": "Cardiologie",
          "scheduledAt": "2026-02-15T10:30:00Z",
          "reason": "Controle annuel"
        }
        """;

    mockMvc.perform(post("/api/medisante/teleconsultations")
            .contentType(APPLICATION_JSON_VALUE)
            .content(payload))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.patientName").value("Samir Haddad"))
        .andExpect(jsonPath("$.status").value("PLANIFIEE"));
  }

  @Test
  void updateTeleconsultationReturnsUpdatedPayload() throws Exception {
    Instant instant = Instant.parse("2026-02-15T10:30:00Z");
    Teleconsultation existing = new Teleconsultation(
        "Samir Haddad",
        "Dr Aline Perez",
        "Cardiologie",
        instant,
        "PLANIFIEE",
        "Controle annuel"
    );
    ReflectionTestUtils.setField(existing, "id", 10L);

    Teleconsultation saved = new Teleconsultation(
        "Samir Haddad",
        "Dr Emilie Roy",
        "Dermatologie",
        Instant.parse("2026-02-18T09:00:00Z"),
        "EN_COURS",
        "Suivi cutane"
    );
    ReflectionTestUtils.setField(saved, "id", 10L);

    given(teleconsultationRepository.findById(10L)).willReturn(Optional.of(existing));
    given(teleconsultationRepository.save(any(Teleconsultation.class))).willReturn(saved);

    String payload = """
        {
          "patientName": "Samir Haddad",
          "doctorName": "Dr Emilie Roy",
          "speciality": "Dermatologie",
          "scheduledAt": "2026-02-18T09:00:00Z",
          "reason": "Suivi cutane",
          "status": "EN_COURS"
        }
        """;

    mockMvc.perform(put("/api/medisante/teleconsultations/10")
            .contentType(APPLICATION_JSON_VALUE)
            .content(payload))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.doctorName").value("Dr Emilie Roy"))
        .andExpect(jsonPath("$.status").value("EN_COURS"));
  }

  @Test
  void deleteTeleconsultationReturnsNoContent() throws Exception {
    given(teleconsultationRepository.existsById(10L)).willReturn(true);

    mockMvc.perform(delete("/api/medisante/teleconsultations/10"))
        .andExpect(status().isNoContent());

    then(teleconsultationRepository).should().deleteById(10L);
  }

  @Test
  void deleteTeleconsultationReturnsNotFoundWhenMissing() throws Exception {
    given(teleconsultationRepository.existsById(99L)).willReturn(false);

    mockMvc.perform(delete("/api/medisante/teleconsultations/99"))
        .andExpect(status().isNotFound());

    then(teleconsultationRepository).should(never()).deleteById(99L);
  }
}
