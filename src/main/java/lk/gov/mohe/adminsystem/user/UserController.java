package lk.gov.mohe.adminsystem.user;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Map;
import lk.gov.mohe.adminsystem.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @GetMapping("/users")
  @PreAuthorize("hasAuthority('user:read')")
  public ApiResponse<List<UserDto>> getUsers() {
    return ApiResponse.of(userService.getUsers());
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
  public ApiResponse<Void> deleteUser(@PathVariable Integer id) {
    userService.deleteUser(id);
    return ApiResponse.message("User deleted successfully");
    }

  @GetMapping("/profile")
  public ApiResponse<UserDto> getProfile(@AuthenticationPrincipal Jwt jwt) {
    Integer userId = jwt.getClaim("userId");
    return ApiResponse.of(userService.getProfile(userId));
  }

  @PutMapping("/profile")
  public ApiResponse<Map<String, String>> updateProfile(
      @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody UserProfileUpdateRequestDto request) {
    Integer userId = jwt.getClaim("userId");
    userService.updateProfile(userId, request);
    return ApiResponse.of(Map.of("message", "Profile updated successfully"));
  }
}
