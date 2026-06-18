package com.velsis.speedviolation.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ViolationControllerIT {

    private static final String EVALUATE = "/api/v1/violations/evaluate";
    private static final String QUERY = "/api/v1/violations";
    private static final String PAST_TIMESTAMP = "2020-06-08T14:30:00Z";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String body(String plate, Integer measured, Integer limit, String equipmentId, String timestamp)
            throws Exception {
        Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("licensePlate", plate);
        payload.put("measuredSpeed", measured);
        payload.put("speedLimit", limit);
        payload.put("equipmentId", equipmentId);
        payload.put("captureTimestamp", timestamp);
        return objectMapper.writeValueAsString(payload);
    }

    @Test
    @DisplayName("POST com infracao -> 200 e classificacao SERIOUS")
    void evaluateWithViolation() throws Exception {
        mockMvc.perform(post(EVALUATE)
                        .header("x-origin", "FIXED")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("ABC1D23", 92, 60, "RAD-CWB-001", PAST_TIMESTAMP)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasViolation").value(true))
                .andExpect(jsonPath("$.consideredSpeed").value(85))
                .andExpect(jsonPath("$.excessPercentage").value(41.67))
                .andExpect(jsonPath("$.violation.severity").value("SERIOUS"))
                .andExpect(jsonPath("$.violation.ctbCode").value("218-II"));
    }

    @Test
    @DisplayName("POST sem infracao -> 200 e violation nulo")
    void evaluateWithoutViolation() throws Exception {
        mockMvc.perform(post(EVALUATE)
                        .header("x-origin", "MOBILE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("ABC1D23", 64, 60, "RAD-CWB-001", PAST_TIMESTAMP)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasViolation").value(false))
                .andExpect(jsonPath("$.consideredSpeed").value(57))
                .andExpect(jsonPath("$.violation").doesNotExist());
    }

    @Test
    @DisplayName("POST com placa invalida -> 400 INVALID_LICENSE_PLATE")
    void evaluateInvalidPlate() throws Exception {
        mockMvc.perform(post(EVALUATE)
                        .header("x-origin", "FIXED")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("INVALID", 92, 60, "RAD-CWB-001", PAST_TIMESTAMP)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_LICENSE_PLATE"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("POST sem header x-origin -> 400 INVALID_ORIGIN")
    void evaluateMissingOrigin() throws Exception {
        mockMvc.perform(post(EVALUATE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("ABC1D23", 92, 60, "RAD-CWB-001", PAST_TIMESTAMP)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_ORIGIN"));
    }

    @Test
    @DisplayName("POST com timestamp no futuro -> 400 INVALID_CAPTURE_TIMESTAMP")
    void evaluateFutureTimestamp() throws Exception {
        mockMvc.perform(post(EVALUATE)
                        .header("x-origin", "FIXED")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("ABC1D23", 92, 60, "RAD-CWB-001", "2099-01-01T00:00:00Z")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_CAPTURE_TIMESTAMP"));
    }

    @Test
    @DisplayName("Corpo malformado -> 400 MALFORMED_REQUEST")
    void evaluateMalformedBody() throws Exception {
        mockMvc.perform(post(EVALUATE)
                        .header("x-origin", "FIXED")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ not valid json "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("MALFORMED_REQUEST"));
    }

    @Test
    @DisplayName("Infracao apurada fica disponivel na consulta por placa")
    void persistsAndQueriesViolation() throws Exception {
        String plate = "QRY1A11";

        mockMvc.perform(post(EVALUATE)
                        .header("x-origin", "HANDHELD")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(plate, 92, 60, "RAD-CWB-009", PAST_TIMESTAMP)))
                .andExpect(status().isOk());

        mockMvc.perform(get(QUERY).param("licensePlate", plate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].licensePlate").value(plate))
                .andExpect(jsonPath("$[0].severity").value("SERIOUS"))
                .andExpect(jsonPath("$[0].ctbCode").value("218-II"));
    }

    @Test
    @DisplayName("Consulta de placa sem infracoes -> lista vazia")
    void queryUnknownPlateReturnsEmptyList() throws Exception {
        mockMvc.perform(get(QUERY).param("licensePlate", "ZZZ0Z00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Consulta sem parametro licensePlate -> 400 MISSING_PARAMETER")
    void queryMissingParameter() throws Exception {
        mockMvc.perform(get(QUERY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("MISSING_PARAMETER"));
    }
}
