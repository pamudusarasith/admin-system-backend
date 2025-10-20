package lk.gov.mohe.adminsystem.user;

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
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private CreateUserRequest createUserRequest;
    private UserDto userDto;

    /**
     * Helper to create a mock Authentication object with a JWT principal.
     */
    private Authentication createMockAuthentication(int userId) {
        Jwt jwt = Jwt.withTokenValue("mock-token")
                .header("alg", "none")
                .claim("userId", userId)
                .build();
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(jwt);
        return auth;
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();

        createUserRequest = new CreateUserRequest("newuser", "new@example.com", 1, 1);

        // Corrected: Use the new 9-argument constructor for UserDto
        userDto = new UserDto(1, "johndoe", "john@example.com", "John Doe", "12345", "Admin", "IT", false, true);
    }

    @Test
    void createUser_ShouldReturnCreated() throws Exception {
        // Given
        User savedUser = new User();
        savedUser.setId(100);
        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(savedUser);

        // When & Then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/users/100"))
                .andExpect(jsonPath("$.message").value("User created successfully"));
    }

    @Test
    void deleteUser_ShouldReturnSuccessMessage() throws Exception {
        // Given
        doNothing().when(userService).deleteUser(1);

        // When & Then
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User deleted successfully"));
    }
}