package lk.gov.mohe.adminsystem.user;

import lk.gov.mohe.adminsystem.division.Division;
import lk.gov.mohe.adminsystem.division.DivisionRepository;
import lk.gov.mohe.adminsystem.role.Role;
import lk.gov.mohe.adminsystem.role.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;
  private final DivisionRepository divisionRepository;
  private final UserMapper userMapper;

  @Transactional(readOnly = true)
  public Page<UserDto> getUsers(String query, Integer divisionId, Integer page, Integer pageSize,
      Boolean assignableOnly) {
    Pageable pageable = PageRequest.of(page, pageSize);
    Specification<User> spec = null;

    if (StringUtils.hasText(query)) {
      String likeQuery = "%" + query.toLowerCase() + "%";
      spec = (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.or(
          criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), likeQuery),
          criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), likeQuery),
          criteriaBuilder.like(criteriaBuilder.lower(root.get("fullName")), likeQuery),
          criteriaBuilder.like(criteriaBuilder.lower(root.get("phoneNumber")), likeQuery));
    }

    if (divisionId != null) {
      Specification<User> divisionSpec = (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder
          .equal(root.get("division").get("id"), divisionId);
      spec = (spec == null) ? divisionSpec : spec.and(divisionSpec);
    }

    if (assignableOnly != null && assignableOnly) {
      Specification<User> assignableSpec = (root, criteriaQuery, criteriaBuilder) -> {
        var roleJoin = root.join("role");
        var permissionsJoin = roleJoin.join("permissions");
        return criteriaBuilder.equal(permissionsJoin.get("name"), "letter:own:manage");
      };
      spec = (spec == null) ? assignableSpec : spec.and(assignableSpec);
    }
    return userRepository.findAll(spec, pageable).map(userMapper::toUserDto);
  }

  @Transactional
  public User createUser(CreateUserRequest createUserRequest) {
    if (userRepository.existsByUsername(createUserRequest.username())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
    }
    if (userRepository.existsByEmail(createUserRequest.email())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
    }

    User user = new User();
    user.setUsername(createUserRequest.username());
    user.setPassword(passwordEncoder.encode("123")); // Default password
    user.setEmail(createUserRequest.email());
    user.setFullName("New User");
    user.setAccountSetupRequired(true);
    user.setIsActive(true);

    Role role = roleRepository
        .findById(createUserRequest.roleId())
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role not found"));
    user.setRole(role);

    Division division = divisionRepository
        .findById(createUserRequest.divisionId())
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Division not found"));
    user.setDivision(division);

    return userRepository.save(user);
  }

  @Transactional
  public void updateUser(Integer id, UserUpdateRequestDto request) {
    User user = userRepository
        .findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    user.setUsername(request.username());
    user.setEmail(request.email());
    user.setFullName(request.fullName());
    user.setPhoneNumber(request.phoneNumber());
    user.setIsActive(request.isActive());

    Role role = roleRepository
        .findByName(request.role())
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role not found"));
    user.setRole(role);

    Division division = divisionRepository
        .findByName(request.division())
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Division not found"));
    user.setDivision(division);

    userRepository.save(user);
  }

  public void deleteUser(Integer id) {
    if (!userRepository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
    }
    userRepository.deleteById(id);
  }

  public UserDto getProfile(Integer userId) {
    return userRepository
        .findById(userId)
        .map(userMapper::toUserDto)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
  }

  @Transactional
  public void updateProfile(Integer userId, UserProfileUpdateRequestDto request) {
    User user = userRepository
        .findById(userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    user.setFullName(request.fullName());
    user.setEmail(request.email());
    user.setPhoneNumber(request.phoneNumber());

    userRepository.save(user);
  }

  @Transactional
  public void accountSetup(Integer userId, AccountSetupRequestDto request) {
    User user = userRepository
        .findById(userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Old password is incorrect");
    }

    if (request.oldPassword().equals(request.newPassword())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "New password must be different from old password");
    }

    user.setFullName(request.fullName());
    user.setEmail(request.email());
    user.setPhoneNumber(request.phoneNumber());
    user.setPassword(passwordEncoder.encode(request.newPassword()));
    user.setAccountSetupRequired(false);

    userRepository.save(user);
  }
}
