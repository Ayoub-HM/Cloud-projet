package com.demo.notes.medisante;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

  @Test
  void homeEndpointReturnsMediSanteData() throws Exception {
    given(medicalServiceRepository.findAllByOrderByIdAsc())
        .willReturn(List.of(new MedicalService(
            "Consultations video securisees",
            "Mise en relation rapide patient-medecin",
            "Teleconsultation"
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
}
