package lk.gov.mohe.adminsystem.cabinetpaper.category;

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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CabinetPaperCategoryControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CabinetPaperCategoryService service;

    @InjectMocks
    private CabinetPaperCategoryController controller;

    private CabinetPaperCategoryDto categoryDto;
    private CreateCabinetPaperCategoryRequestDto createDto;
    private UpdateCabinetPaperCategoryRequestDto updateDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        categoryDto = new CabinetPaperCategoryDto(1, "Test Category", "Description");
        createDto = new CreateCabinetPaperCategoryRequestDto("New Category", "Description");
        updateDto = new UpdateCabinetPaperCategoryRequestDto("Updated Category", "Updated Description");
    }

    @Test
    void createCategory_ShouldReturnSuccessMessage() throws Exception {
        // Given
        when(service.createCategory(any(CreateCabinetPaperCategoryRequestDto.class))).thenReturn(new CabinetPaperCategory());

        // When & Then
        mockMvc.perform(post("/cabinet-papers/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Category created successfully"));
    }

    @Test
    void updateCategory_ShouldReturnSuccessMessage() throws Exception {
        // Given
        when(service.updateCategory(eq(1), any(UpdateCabinetPaperCategoryRequestDto.class))).thenReturn(new CabinetPaperCategory());

        // When & Then
        mockMvc.perform(put("/cabinet-papers/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Category updated successfully"));
    }

    @Test
    void deleteCategory_ShouldReturnSuccessMessage() throws Exception {
        // Given
        doNothing().when(service).deleteCategory(1);

        // When & Then
        mockMvc.perform(delete("/cabinet-papers/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Category deleted successfully"));
    }
}