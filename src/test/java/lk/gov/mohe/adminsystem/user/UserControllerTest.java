package lk.gov.mohe.adminsystem.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lk.gov.mohe.adminsystem.division.Division;
import lk.gov.mohe.adminsystem.division.DivisionRepository;
import lk.gov.mohe.adminsystem.role.Role;
import lk.gov.mohe.adminsystem.role.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private DivisionRepository divisionRepository;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserController userController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    void testGetUsers() throws Exception {
        Mockito.when(userRepository.findAll()).thenReturn(Collections.emptyList());
        Mockito.when(userMapper.toUserDto(any())).thenReturn(new UserDto(1L, "testuser", "test@email.com", "Test User", "0771234567", "Admin", "Division A", true));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk());
    }

    @Test
    void testCreateUser() throws Exception {
        UserController.CreateUserRequest request = new UserController.CreateUserRequest("testuser", "test@email.com", 1, 1);
        Role role = new Role();
        Division division = new Division();
        Mockito.when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        Mockito.when(roleRepository.findById(anyInt())).thenReturn(Optional.of(role));
        Mockito.when(divisionRepository.findById(anyInt())).thenReturn(Optional.of(division));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().string("testuser"));
    }

    @Test
    void testUpdateUser() throws Exception {
        User user = new User();
        Mockito.when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        Mockito.when(roleRepository.findByName(anyString())).thenReturn(Optional.of(new Role()));

        UserController.UpdateUserRequest request = new UserController.UpdateUserRequest("updateduser", "updated@email.com", "newpass", "Admin");

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("User updated successfully"));
    }

    @Test
    void testDeleteUser_UserExists() throws Exception {
        Mockito.when(userRepository.existsById(anyLong())).thenReturn(true);

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("User deleted successfully"));
    }

    @Test
    void testDeleteUser_UserNotFound() throws Exception {
        Mockito.when(userRepository.existsById(anyLong())).thenReturn(false);

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found"));
    }
}