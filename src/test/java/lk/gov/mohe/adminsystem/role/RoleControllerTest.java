package lk.gov.mohe.adminsystem.role;

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
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RoleControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private RoleService roleService;

    @InjectMocks
    private RoleController roleController;

    private RoleDto roleDto;
    private CreateOrUpdateRoleRequestDto createDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(roleController).build();

        // Corrected: Create a RoleDto instance matching the new class structure
        roleDto = new RoleDto();
        roleDto.setId(1);
        roleDto.setName("Admin");
        roleDto.setDescription("Admin role");
        roleDto.setPermissions(Collections.emptyList());
        roleDto.setUserCount(5L);

        createDto = new CreateOrUpdateRoleRequestDto("New Role", "Description", List.of("user:read"));    }

    @Test
    void createRole_ShouldReturnSuccessMessage() throws Exception {
        doNothing().when(roleService).createRole(any(CreateOrUpdateRoleRequestDto.class));

        mockMvc.perform(post("/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Role created successfully"));
    }

    @Test
    void updateRole_ShouldReturnSuccessMessage() throws Exception {
        doNothing().when(roleService).updateRole(eq(1), any(CreateOrUpdateRoleRequestDto.class));

        mockMvc.perform(put("/roles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Role updated successfully"));
    }

    @Test
    void deleteRole_ShouldReturnSuccessMessage() throws Exception {
        doNothing().when(roleService).deleteRole(1);

        mockMvc.perform(delete("/roles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Role deleted successfully"));
    }
}