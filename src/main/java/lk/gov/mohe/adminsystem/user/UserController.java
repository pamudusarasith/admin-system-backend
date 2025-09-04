package lk.gov.mohe.adminsystem.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lk.gov.mohe.adminsystem.division.Division;
import lk.gov.mohe.adminsystem.division.DivisionRepository;
import lk.gov.mohe.adminsystem.role.Role;
import lk.gov.mohe.adminsystem.role.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final DivisionRepository divisionRepository;
    private final UserMapper userMapper;

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('user:view')")
    public ResponseEntity<List<UserDto>> getUsers() {
        List<User> users = userRepository.findAll();
        List<UserDto> userDtos = users.stream().map(userMapper::toUserDto).toList();
        return ResponseEntity.ok(userDtos);
    }

    @PostMapping("/users")
    @PreAuthorize("hasAuthority('user:create')")
    public ResponseEntity<String> createUser(@Valid @RequestBody CreateUserRequest createUserRequest) {
        String encodedPassword =
            this.passwordEncoder.encode("123");
        User user = new User();
        user.setUsername(createUserRequest.username());
        user.setPassword(encodedPassword);
        user.setEmail(createUserRequest.email());
        Role role =
            roleRepository.findById(createUserRequest.roleId()).orElseThrow(() -> new IllegalArgumentException("Role not found: " + createUserRequest.roleId()));
        user.setRole(role);
        Division division =
            divisionRepository.findById(createUserRequest.divisionId()).orElseThrow(() -> new IllegalArgumentException("Division not found: " + createUserRequest.divisionId()));
        user.setDivision(division);
        userRepository.save(user);
        return new ResponseEntity<>(createUserRequest.username(), HttpStatus.CREATED);
    }

    public record CreateUserRequest(@Size(min = 6, max = 50) String username,
//                                    @Size(min = 6, max = 50) String password,
                                    @Email String email, @NotNull Integer roleId,
                                    @NotNull Integer divisionId) {
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasAuthority('user:update')")
    public ResponseEntity<String> updateUser(@PathVariable Long id,
                                             @Valid @RequestBody UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        user.setUsername(request.username());
        user.setEmail(request.email());

        if (request.password() != null && !request.password().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }

        Role role = roleRepository.findByName(request.role())
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));
        user.setRole(role);

        userRepository.save(user);
        return ResponseEntity.ok("User updated successfully");
    }

    public record UpdateUserRequest(
            @Size(min = 6, max = 50) String username,
            @Email String email,
            @Size(min = 6)String password,
            @NotEmpty String role
    ) {}

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasAuthority('user:delete')")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        userRepository.deleteById(id);
        return ResponseEntity.ok("User deleted successfully");
    }

    @GetMapping("/profile")
    public ResponseEntity<UserDto> getProfile(@AuthenticationPrincipal Jwt jwt) {

        User user = userRepository.findById(jwt.getClaim("user_id"))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        UserDto userDto = userMapper.toUserDto(user);
        return ResponseEntity.ok(userDto);
    }



}