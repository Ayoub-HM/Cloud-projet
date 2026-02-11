package com.demo.notes.medisante;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MediSanteController.class)
class MediSanteControllerWebTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private MedicalServiceRepository medicalServiceRepository;

  @MockBean
  private OfficeRepository officeRepository;

  @MockBean
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
            .contentType(MediaType.APPLICATION_JSON)
            .content(payload))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.patientName").value("Samir Haddad"))
        .andExpect(jsonPath("$.status").value("PLANIFIEE"));
  }
}
