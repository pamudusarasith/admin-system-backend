package lk.gov.mohe.adminsystem.permission;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PermissionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PermissionService permissionService;

    @InjectMocks
    private PermissionController permissionController;

    private PermissionCategoryDto categoryDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(permissionController).build();

        // --- Corrected Mock DTOs ---
        // Use the new 4-argument constructor for PermissionDto
        PermissionDto permissionDto = new PermissionDto(101, "user:read", "Read Users", "Allows reading user data");

        // Use the no-arg constructor and setters for the PermissionCategoryDto class
        categoryDto = new PermissionCategoryDto();
        categoryDto.setId(1);
        categoryDto.setName("User Management");
        categoryDto.setPermissions(Collections.singletonList(permissionDto));
        categoryDto.setSubCategories(Collections.emptyList());
    }

    @Test
    void getPermissionHierarchy_ShouldReturnApiResponseWithData() throws Exception {
        // Given
        when(permissionService.getPermissions()).thenReturn(Collections.singletonList(categoryDto));

        // When & Then
        mockMvc.perform(get("/permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("User Management"))
                .andExpect(jsonPath("$.data[0].permissions[0].name").value("user:read"));
    }
}