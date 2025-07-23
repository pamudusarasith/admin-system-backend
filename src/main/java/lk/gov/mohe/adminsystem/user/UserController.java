package lk.gov.mohe.adminsystem.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lk.gov.mohe.adminsystem.division.Division;
import lk.gov.mohe.adminsystem.division.DivisionRepository;
import lk.gov.mohe.adminsystem.role.Role;
import lk.gov.mohe.adminsystem.role.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final DivisionRepository divisionRepository;

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<String> getUsers(Authentication authentication) {
        // This method will return a list of users
        // For now, we can return a placeholder response
        return ResponseEntity.ok("List of users will be here ");
    }

    @PostMapping("/users")
    @PreAuthorize("hasAuthority('user:create')")
    public ResponseEntity<String> createUser(@Valid @RequestBody CreateUserRequest createUserRequest) {
        String encodedPassword =
            this.passwordEncoder.encode(createUserRequest.password());
        User user = new User();
        user.setUsername(createUserRequest.username());
        user.setPassword(encodedPassword);
        user.setEmail(createUserRequest.email());
        Role role =
            roleRepository.findByName(createUserRequest.role()).orElseThrow(() -> new IllegalArgumentException("Role not found: " + createUserRequest.role()));
        user.setRole(role);
        Division division =
                divisionRepository.findByName(createUserRequest.division()).orElseThrow(() -> new IllegalArgumentException("Division not found: " + createUserRequest.division()));
        user.setDivision(division);
        userRepository.save(user);
        return new ResponseEntity<>(createUserRequest.username(), HttpStatus.CREATED);
    }

    public record CreateUserRequest(@Size(min = 6, max = 50) String username,
                                    @Size(min = 6, max = 50) String password, 
                                    @Email String email, @NotEmpty String role,
                                    @NotEmpty String division) {
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasAuthority('user:update')")
    public ResponseEntity<String> updateUser(@PathVariable Integer id,
                                             @Valid @RequestBody CreateUserRequest createUserRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        user.setUsername(createUserRequest.username());
        user.setEmail(createUserRequest.email());

        if (createUserRequest.password() != null && !createUserRequest.password().isEmpty()) {
            user.setPassword(passwordEncoder.encode(createUserRequest.password()));
        }

        Role role = roleRepository.findByName(createUserRequest.role())
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + createUserRequest.role()));
        user.setRole(role);

        userRepository.save(user);
        return ResponseEntity.ok("User updated successfully");
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasAuthority('user:delete')")
    public ResponseEntity<String> deleteUser(@PathVariable Integer id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        userRepository.deleteById(id);
        return ResponseEntity.ok("User deleted successfully");
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<User> getUserById(@PathVariable Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return ResponseEntity.ok(user);
    }

}