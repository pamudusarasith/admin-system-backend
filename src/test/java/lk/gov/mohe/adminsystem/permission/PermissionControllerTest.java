package lk.gov.mohe.adminsystem.permission;

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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PermissionController.class)
@Import(PermissionControllerTest.TestConfig.class)
public class PermissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    static class TestConfig {
        @Bean
        public PermissionRepository permissionRepository() {
            return Mockito.mock(PermissionRepository.class);
        }
    }

    @Test
    void testGetAllPermission() throws Exception {
        Permission permission = new Permission();
        permission.setId(1);
        permission.setName("READ_USER");
        permission.setLabel("Read User");
        permission.setDescription("Allows reading user data");

        when(permissionRepository.findAll()).thenReturn(List.of(permission));

        mockMvc.perform(get("/permissions")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("READ_USER"))
                .andExpect(jsonPath("$[0].label").value("Read User"))
                .andExpect(jsonPath("$[0].description").value("Allows reading user data"));
    }
}