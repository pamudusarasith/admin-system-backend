package lk.gov.mohe.adminsystem.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CsrfController.class)
public class CsrfControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetCsrfToken() throws Exception {
        mockMvc.perform(get("/csrf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }
}