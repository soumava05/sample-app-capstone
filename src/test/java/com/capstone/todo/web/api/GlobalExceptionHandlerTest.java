package com.capstone.todo.web.api;

import com.capstone.todo.web.filter.CorrelationIdFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = GlobalExceptionHandlerTest.ValidationController.class)
@Import({GlobalExceptionHandler.class, CorrelationIdFilter.class})
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @WithMockUser
    @org.testng.annotations.Test
    void validationError_withoutHeader_returns400_andGeneratedCorrelationId_andDetails() throws Exception {
        mockMvc.perform(post("/api/test/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.details").isArray())
            .andExpect(jsonPath("$.error.correlationId").isNotEmpty());
    }

    @WithMockUser
    @org.testng.annotations.Test
    void validationError_withHeader_returns400_andSameCorrelationId() throws Exception {
        mockMvc.perform(post("/api/test/validate")
                .header(CorrelationIdFilter.CORRELATION_ID_HEADER, "client-uuid")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.correlationId").value("client-uuid"));
    }

    @RestController
    @RequestMapping("/api/test")
    static class ValidationController {

        @PostMapping("/validate")
        public void validate(@jakarta.validation.Valid @RequestBody Payload payload) {
            // no-op
        }
    }

    static class Payload {
        @jakarta.validation.constraints.NotBlank
        public String name;
    }
}
