package lk.gov.mohe.adminsystem.user;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Map;
import lk.gov.mohe.adminsystem.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
public class UserController {

  private static final String USER_ID_CLAIM = "userId";
  private final UserService userService;

  @GetMapping("/users")
  @PreAuthorize("hasAnyAuthority('user:read', 'letter:assign:user')")
  public ApiResponse<List<UserDto>> getUsers(@ModelAttribute UserSearchParams searchParams) {
    return ApiResponse.paged(userService.getUsers(searchParams));
  }

  @PostMapping("/users")
  @PreAuthorize("hasAuthority('user:create')")
  public ResponseEntity<ApiResponse<Void>> createUser(
      @Valid @RequestBody CreateUserRequest createUserRequest) {
    User user = userService.createUser(createUserRequest);
    return ResponseEntity.created(URI.create("/users/" + user.getId()))
        .body(ApiResponse.message("User created successfully"));
  }

  @PutMapping("/users/{id}")
  @PreAuthorize("hasAuthority('user:update')")
  public ApiResponse<Void> updateUser(
      @PathVariable Integer id, @Valid @RequestBody UserUpdateRequestDto request) {
    userService.updateUser(id, request);
    return ApiResponse.message("User updated successfully");
  }

  @DeleteMapping("/users/{id}")
  @PreAuthorize("hasAuthority('user:delete')")
  public ApiResponse<Void> deleteUser(
      @PathVariable Integer id, @AuthenticationPrincipal Jwt jwt) {
    // Get current user ID to prevent self-deletion
    Integer currentUserId = jwt.getClaim(USER_ID_CLAIM);
    
    userService.deleteUser(id, currentUserId);
    return ApiResponse.message("User deleted successfully");
  }

  @PostMapping("/users/{id}/reset-password")
  @PreAuthorize("hasAuthority('user:update')")
  public ApiResponse<Void> resetUserPassword(@PathVariable Integer id) {
    // Validate path variable
    if (id == null || id <= 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user ID");
    }
    
    userService.resetUserPassword(id);
    return ApiResponse.message("Password reset successfully. New password sent to user's email.");
  }

  @GetMapping("/profile")
  public ApiResponse<UserDto> getProfile(@AuthenticationPrincipal Jwt jwt) {
    Integer userId = jwt.getClaim(USER_ID_CLAIM);
    return ApiResponse.of(userService.getProfile(userId));
  }

  @PutMapping("/profile")
  public ApiResponse<Map<String, String>> updateProfile(
      @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody UserProfileUpdateRequestDto request) {
    Integer userId = jwt.getClaim(USER_ID_CLAIM);

    userService.updateProfile(userId, request);
    return ApiResponse.of(Map.of("message", "Profile updated successfully"));
  }

  @PostMapping("/account-setup")
  public ApiResponse<Void> accountSetup(
      @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody AccountSetupRequestDto request) {
    Integer userId = jwt.getClaim(USER_ID_CLAIM);
    userService.accountSetup(userId, request);
    return ApiResponse.message("Account setup completed successfully");
  }
}
