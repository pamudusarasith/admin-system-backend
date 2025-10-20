package lk.gov.mohe.adminsystem.division;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Use MockitoExtension to enable @Mock and @InjectMocks annotations.
@ExtendWith(MockitoExtension.class)
class DivisionControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Create a mock of the DivisionService.
    @Mock
    private DivisionService divisionService;

    // Create an instance of the controller and inject the mocks into it.
    @InjectMocks
    private DivisionController divisionController;

    @BeforeEach
    void setUp() {
        // Build a MockMvc instance for our controller for standalone testing.
        mockMvc = MockMvcBuilders.standaloneSetup(divisionController).build();
    }


    @Test
    void createDivision_ShouldReturnBadRequest_WhenNameIsBlank() throws Exception {
        // Given
        CreateOrUpdateDivisionRequestDto requestDto = new CreateOrUpdateDivisionRequestDto("", "Desc");

        // When & Then
        mockMvc.perform(post("/divisions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

}