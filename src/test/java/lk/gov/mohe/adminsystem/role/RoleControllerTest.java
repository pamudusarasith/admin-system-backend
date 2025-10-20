package lk.gov.mohe.adminsystem.role;

import com.fasterxml.jackson.databind.ObjectMapper;
import lk.gov.mohe.adminsystem.util.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class RoleControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RoleService roleService;

    @InjectMocks
    private RoleController roleController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(roleController).build();
    }

    @Test
    void testCreateRole() throws Exception {
        RoleController.CreateRoleRequest request = new RoleController.CreateRoleRequest("Admin", "desc", List.of("READ"));
        ApiResponse<Void> response = ApiResponse.message("Role created successfully");
        Mockito.when(roleService.createRole(anyString(), anyString(), anyList())).thenReturn(response);

        mockMvc.perform(post("/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Role created successfully"));
    }

    @Test
    void testUpdateRole() throws Exception {
        RoleController.CreateRoleRequest request = new RoleController.CreateRoleRequest("User", "desc", List.of("WRITE"));
        ApiResponse<Void> response = ApiResponse.message("Role updated successfully");
        Mockito.when(roleService.updateRole(anyInt(), anyString(), anyString(), anyList())).thenReturn(response);

        mockMvc.perform(put("/roles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Role updated successfully"));
    }

    @Test
    void testGetAllRoles() throws Exception {
        Mockito.when(roleService.getAllRoles()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/roles"))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteRole() throws Exception {
        ApiResponse<Void> response = ApiResponse.message("Role deleted successfully");
        Mockito.when(roleService.deleteRole(anyInt())).thenReturn(response);

        mockMvc.perform(delete("/roles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Role deleted successfully"));
    }
}