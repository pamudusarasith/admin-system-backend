package lk.gov.mohe.adminsystem.cabinetpaper;

import com.fasterxml.jackson.databind.ObjectMapper;
import lk.gov.mohe.adminsystem.cabinetpaper.category.CabinetPaperCategory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CabinetPaperController.class)
class CabinetPaperControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CabinetPaperService cabinetPaperService;
    // Mock the other dependencies from the controller's constructor for @WebMvcTest
    @MockBean
    private CabinetPaperRepository cabinetPaperRepository; // Assuming this is a required mock if you hadn't mocked ALL dependencies

    private final CabinetPaperDto paperDto = new CabinetPaperDto(1, "REF123", "Title", null, null, null, null, null);
    private final CreateCabinetPaperRequestDto createDto = new CreateCabinetPaperRequestDto("REF456", "Title", 1);
    private final UpdateCabinetPaperRequestDto updateDto = new UpdateCabinetPaperRequestDto("REF456", "New Title", 1);

    // --- GET /cabinet-papers Tests ---

    @Test
    @WithMockUser(authorities = "cabinet:read")
    void getCabinetPapers_ShouldReturnPagedApiResponse() throws Exception {
        // Given
        Page<CabinetPaperDto> pagedResponse = new PageImpl<>(Collections.singletonList(paperDto));
        when(cabinetPaperService.getCabinetPapers(anyInt(), anyInt())).thenReturn(pagedResponse);

        // When & Then
        mockMvc.perform(get("/cabinet-papers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].referenceId").value("REF123"));
    }

    @Test
    @WithMockUser(authorities = "cabinet:read")
    void getCabinetPaperById_ShouldReturnSingleDto() throws Exception {
        // Given
        when(cabinetPaperService.getCabinetPaperById(1)).thenReturn(paperDto);

        // When & Then
        mockMvc.perform(get("/cabinet-papers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.referenceId").value("REF123"));
    }

    @Test
    void getCabinetPaperById_ShouldReturnForbidden_WhenNoAuth() throws Exception {
        // When & Then
        mockMvc.perform(get("/cabinet-papers/1"))
                .andExpect(status().isForbidden());
    }

    // --- POST /cabinet-papers Tests (Multipart/File Upload) ---

    @Test
    @WithMockUser(authorities = "cabinet:create")
    void createCabinetPaper_ShouldReturnCreatedAndLocationHeader_WhenValid() throws Exception {
        // Given
        CabinetPaper mockSavedPaper = new CabinetPaper();
        mockSavedPaper.setId(5);
        // Create mock files and DTO parts
        MockMultipartFile file = new MockMultipartFile("attachments", "file.pdf", "application/pdf", "data".getBytes());
        MockMultipartFile details = new MockMultipartFile("details", "", "application/json",
                objectMapper.writeValueAsBytes(createDto));

        when(cabinetPaperService.createCabinetPaper(any(CreateCabinetPaperRequestDto.class), any())).thenReturn(mockSavedPaper);

        // When & Then
        mockMvc.perform(multipart("/cabinet-papers")
                        .file(details)
                        .file(file)
                        .with(csrf())) // Required for POST/PUT/DELETE
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/cabinet-papers/5"))
                .andExpect(jsonPath("$.message").value("Cabinet paper created successfully"));
    }

    @Test
    @WithMockUser(authorities = "cabinet:create")
    void createCabinetPaper_ShouldReturnBadRequest_WhenValidationFails() throws Exception {
        // Given: DTO with invalid data (e.g., blank reference ID)
        CreateCabinetPaperRequestDto invalidDto = new CreateCabinetPaperRequestDto("", "Title", 1);
        MockMultipartFile invalidDetails = new MockMultipartFile("details", "", "application/json",
                objectMapper.writeValueAsBytes(invalidDto));

        // When & Then
        mockMvc.perform(multipart("/cabinet-papers")
                        .file(invalidDetails)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    // --- PUT /cabinet-papers/{id} Tests (Multipart/File Upload) ---

    @Test
    @WithMockUser(authorities = "cabinet:update")
    void updateCabinetPaper_ShouldReturnSuccessMessage_WhenValid() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile("attachments", "file.pdf", "application/pdf", "data".getBytes());
        MockMultipartFile details = new MockMultipartFile("details", "", "application/json",
                objectMapper.writeValueAsBytes(updateDto));

        doNothing().when(cabinetPaperService).updateCabinetPaper(anyInt(), any(UpdateCabinetPaperRequestDto.class), any());

        // When & Then
        mockMvc.perform(multipart("/cabinet-papers/1")
                        .file(details)
                        .file(file)
                        .with(csrf()) // Required for PUT
                        .with(_method("PUT"))) // Simulates a PUT request via multipart
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Cabinet paper updated successfully"));
    }

    // --- DELETE /cabinet-papers/{id} Tests ---

    @Test
    @WithMockUser(authorities = "cabinet:delete")
    void deleteCabinetPaper_ShouldReturnSuccessMessage() throws Exception {
        // Given
        doNothing().when(cabinetPaperService).deleteCabinetPaper(1);

        // When & Then
        mockMvc.perform(delete("/cabinet-papers/1")
                        .with(csrf())) // Required for DELETE
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Cabinet paper deleted successfully"));
    }
}