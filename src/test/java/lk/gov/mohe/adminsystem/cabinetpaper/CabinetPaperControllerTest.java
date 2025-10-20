package lk.gov.mohe.adminsystem.cabinetpaper;

import com.fasterxml.jackson.databind.ObjectMapper;
import lk.gov.mohe.adminsystem.cabinetpaper.category.CabinetPaperCategoryDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Use MockitoExtension for a pure unit test without Spring Context
@ExtendWith(MockitoExtension.class)
class CabinetPaperControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Create a mock of the service, which is the controller's only dependency
    @Mock
    private CabinetPaperService cabinetPaperService;

    // Create an instance of the controller and inject the mock service into it
    @InjectMocks
    private CabinetPaperController cabinetPaperController;

    private final CabinetPaperCategoryDto categoryDto = new CabinetPaperCategoryDto(1, "Test Category", "Desc");
    private final CabinetPaperDto paperDto = new CabinetPaperDto(1, "REF123", "Title", "Summary", categoryDto, CabinetPaperStatusEnum.DRAFT, null, 1L, Collections.emptyList(), null, null);
    private final CreateCabinetPaperRequestDto createDto = new CreateCabinetPaperRequestDto("REF456", "New Subject", "Summary text", 1, CabinetPaperStatusEnum.DRAFT);
    private final UpdateCabinetPaperRequestDto updateDto = new UpdateCabinetPaperRequestDto("REF123", "Updated Subject", "Updated summary", 1, CabinetPaperStatusEnum.SUBMITTED);

    @BeforeEach
    void setUp() {
        // Build a standalone MockMvc instance for the controller, no Spring context loaded
        mockMvc = MockMvcBuilders.standaloneSetup(cabinetPaperController).build();
    }
    @Test
    void createCabinetPaper_ShouldReturnCreatedAndLocationHeader() throws Exception {
        CabinetPaper mockSavedPaper = new CabinetPaper();
        mockSavedPaper.setId(5);
        MockMultipartFile file = new MockMultipartFile("attachments", "file.pdf", "application/pdf", "data".getBytes());
        MockMultipartFile details = new MockMultipartFile("details", "", "application/json", objectMapper.writeValueAsBytes(createDto));

        when(cabinetPaperService.createCabinetPaper(any(CreateCabinetPaperRequestDto.class), any())).thenReturn(mockSavedPaper);

        mockMvc.perform(multipart("/cabinet-papers")
                        .file(details)
                        .file(file))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/cabinet-papers/5"))
                .andExpect(jsonPath("$.message").value("Cabinet paper created successfully"));
    }
    @Test
    void deleteCabinetPaper_ShouldReturnSuccessMessage() throws Exception {
        doNothing().when(cabinetPaperService).deleteCabinetPaper(1);

        mockMvc.perform(delete("/cabinet-papers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Cabinet paper deleted successfully"));
    }
}