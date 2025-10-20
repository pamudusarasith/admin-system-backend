package lk.gov.mohe.adminsystem.division;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DivisionController.class)
@Import(DivisionControllerTest.TestConfig.class)
public class DivisionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DivisionRepository divisionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    static class TestConfig {
        @Bean
        public DivisionRepository divisionRepository() {
            return Mockito.mock(DivisionRepository.class);
        }
    }

    @Test
    void testGetAllDivisions() throws Exception {
        Division division = new Division();
        division.setId(1);
        division.setName("Finance");
        division.setDescription("Handles finances");

        Mockito.when(divisionRepository.findAll()).thenReturn(List.of(division));

        mockMvc.perform(get("/divisions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Finance"));
    }

    @Test
    void testCreateDivision() throws Exception {
        Division division = new Division();
        division.setName("HR");
        division.setDescription("Human Resources");

        Division savedDivision = new Division();
        savedDivision.setId(2);
        savedDivision.setName("HR");
        savedDivision.setDescription("Human Resources");

        Mockito.when(divisionRepository.save(any(Division.class))).thenReturn(savedDivision);

        mockMvc.perform(post("/divisions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(division)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("HR"));
    }

    @Test
    void testDeleteDivision_Found() throws Exception {
        Division division = new Division();
        division.setId(3);
        division.setName("IT");

        Mockito.when(divisionRepository.findById(3)).thenReturn(Optional.of(division));

        mockMvc.perform(delete("/divisions/3"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteDivision_NotFound() throws Exception {
        Mockito.when(divisionRepository.findById(99)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/divisions/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateDivision_Found() throws Exception {
        Division existing = new Division();
        existing.setId(4);
        existing.setName("Old Name");
        existing.setDescription("Old Desc");

        Division updated = new Division();
        updated.setId(4);
        updated.setName("New Name");
        updated.setDescription("New Desc");

        Mockito.when(divisionRepository.findById(4)).thenReturn(Optional.of(existing));
        Mockito.when(divisionRepository.save(any(Division.class))).thenReturn(updated);

        Division divisionDetails = new Division();
        divisionDetails.setName("New Name");
        divisionDetails.setDescription("New Desc");

        mockMvc.perform(put("/divisions/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(divisionDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"))
                .andExpect(jsonPath("$.description").value("New Desc"));
    }

    @Test
    void testUpdateDivision_NotFound() throws Exception {
        Mockito.when(divisionRepository.findById(100)).thenReturn(Optional.empty());

        Division divisionDetails = new Division();
        divisionDetails.setName("Doesn't Matter");
        divisionDetails.setDescription("Doesn't Matter");

        mockMvc.perform(put("/divisions/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(divisionDetails)))
                .andExpect(status().isNotFound());
    }

}