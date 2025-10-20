package lk.gov.mohe.adminsystem.user;

import lk.gov.mohe.adminsystem.division.Division;
import lk.gov.mohe.adminsystem.division.DivisionRepository;
import lk.gov.mohe.adminsystem.notification.EmailService;
import lk.gov.mohe.adminsystem.role.Role;
import lk.gov.mohe.adminsystem.role.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private DivisionRepository divisionRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EmailService emailService;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User existingUser;
    private CreateUserRequest createUserRequest;
    private AccountSetupRequestDto accountSetupRequest;

    @BeforeEach
    void setUp() {
        existingUser = new User();
        existingUser.setId(1);
        existingUser.setUsername("johndoe");
        existingUser.setPassword("encodedOldPassword");
        existingUser.setAccountSetupRequired(true);

        createUserRequest = new CreateUserRequest("newuser", "new@example.com", 1, 1);
        accountSetupRequest = new AccountSetupRequestDto("John Doe", "john.d@example.com", "12345", "oldPassword123", "newPassword456");
    }

    @Test
    void createUser_ShouldSucceedAndSendEmail_WhenDataIsValid() {
        // Given
        when(userRepository.existsByUsername(createUserRequest.username())).thenReturn(false);
        when(userRepository.existsByEmail(createUserRequest.email())).thenReturn(false);
        when(roleRepository.findById(createUserRequest.roleId())).thenReturn(Optional.of(new Role()));
        when(divisionRepository.findById(createUserRequest.divisionId())).thenReturn(Optional.of(new Division()));
        when(passwordEncoder.encode("123")).thenReturn("encodedPassword");

        // Mock the save to return the user object, so we can verify the email content
        User savedUser = new User();
        savedUser.setEmail(createUserRequest.email());
        savedUser.setFullName("New User");
        savedUser.setUsername(createUserRequest.username());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        doNothing().when(emailService).sendEmailWithTemplate(anyString(), anyString(), anyString(), any());

        // When
        userService.createUser(createUserRequest);

        // Then
        verify(userRepository, times(1)).save(any(User.class));

        // Capture and verify the arguments passed to the email service
        ArgumentCaptor<Map<String, Object>> emailModelCaptor = ArgumentCaptor.forClass(Map.class);
        verify(emailService, times(1)).sendEmailWithTemplate(
                eq(savedUser.getEmail()),
                anyString(),
                eq("email/welcome-email"),
                emailModelCaptor.capture()
        );

        Map<String, Object> capturedModel = emailModelCaptor.getValue();
        assertEquals("New User", capturedModel.get("fullName"));
        assertEquals("newuser", capturedModel.get("username"));
        assertEquals("123", capturedModel.get("password")); // Verify plain password is sent
    }

    @Test
    void createUser_ShouldThrowConflict_WhenUsernameExists() {
        // Given
        when(userRepository.existsByUsername(createUserRequest.username())).thenReturn(true);

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.createUser(createUserRequest));
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_ShouldSucceed_WhenUserExists() {
        // Given
        when(userRepository.existsById(1)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1);

        // When
        userService.deleteUser(1);

        // Then
        verify(userRepository, times(1)).deleteById(1);
    }

    @Test
    void deleteUser_ShouldThrowNotFound_WhenUserDoesNotExist() {
        // Given
        when(userRepository.existsById(1)).thenReturn(false);

        // When & Then
        assertThrows(ResponseStatusException.class, () -> userService.deleteUser(1));
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    void accountSetup_ShouldSucceed_WhenPasswordsAreValid() {
        when(userRepository.findById(1)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("oldPassword123", "encodedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword456")).thenReturn("encodedNewPassword");

        userService.accountSetup(1, accountSetupRequest);

        verify(userRepository, times(1)).save(existingUser);
        assertEquals("encodedNewPassword", existingUser.getPassword());
        // Corrected: Use getAccountSetupRequired() for Boolean wrapper type
        assertFalse(existingUser.getAccountSetupRequired());
        assertEquals("John Doe", existingUser.getFullName());
    }

    @Test
    void accountSetup_ShouldThrowBadRequest_WhenOldPasswordIsIncorrect() {
        // Given
        when(userRepository.findById(1)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("oldPassword123", "encodedOldPassword")).thenReturn(false);

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.accountSetup(1, accountSetupRequest));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("incorrect"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void accountSetup_ShouldThrowBadRequest_WhenNewPasswordIsSameAsOld() {
        // Given
        AccountSetupRequestDto samePasswordRequest = new AccountSetupRequestDto("John Doe", "john.d@example.com", "12345", "oldPassword123", "oldPassword123");
        when(userRepository.findById(1)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("oldPassword123", "encodedOldPassword")).thenReturn(true);

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.accountSetup(1, samePasswordRequest));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("different"));
        verify(userRepository, never()).save(any());
    }
}