package lk.gov.mohe.adminsystem.user;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lk.gov.mohe.adminsystem.cabinetpaper.CabinetPaperRepository;
import lk.gov.mohe.adminsystem.cabinetpaper.decision.CabinetDecisionRepository;
import lk.gov.mohe.adminsystem.division.Division;
import lk.gov.mohe.adminsystem.division.DivisionRepository;
import lk.gov.mohe.adminsystem.letter.LetterEventRepository;
import lk.gov.mohe.adminsystem.letter.LetterRepository;
import lk.gov.mohe.adminsystem.notification.EmailService;
import lk.gov.mohe.adminsystem.role.Role;
import lk.gov.mohe.adminsystem.role.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

  private static final String USER_NOT_FOUND = "User not found";
  private static final String UPPERCASE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  private static final String LOWERCASE_CHARS = "abcdefghijklmnopqrstuvwxyz";
  private static final String DIGIT_CHARS = "0123456789";
  private static final String SPECIAL_CHARS = "!@#$%^&*";
  private static final String ALL_CHARS = UPPERCASE_CHARS + LOWERCASE_CHARS + DIGIT_CHARS + SPECIAL_CHARS;
  private static final int PASSWORD_LENGTH = 12;
  private static final SecureRandom RANDOM = new SecureRandom();
  
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;
  private final DivisionRepository divisionRepository;
  private final UserMapper userMapper;
  private final EmailService emailService;
  private final LetterRepository letterRepository;
  private final LetterEventRepository letterEventRepository;
  private final CabinetPaperRepository cabinetPaperRepository;
  private final CabinetDecisionRepository cabinetDecisionRepository;

  @Value("${custom.frontend.url}")
  private String frontendUrl;

  @Transactional(readOnly = true)
  public Page<UserDto> getUsers(UserSearchParams searchParams) {
    // Create sort with explicit ordering by id
    Sort sort = Sort.by(Sort.Order.asc("id"));
    Pageable pageable = PageRequest.of(
        searchParams.getPage(), 
        searchParams.getPageSize(),
        sort);
    Specification<User> spec = buildSearchSpec(searchParams);
    
    // Use a specification that matches all if spec is null
    if (spec == null) {
      spec = (root, query, cb) -> cb.conjunction(); // Matches all users
    }
    
    Page<User> users = userRepository.findAll(spec, pageable);
    return users.map(userMapper::toUserDto);
  }

  private Specification<User> buildSearchSpec(UserSearchParams searchParams) {
    if (searchParams == null) {
      return null;
    }

    Specification<User> spec = null;

    // General query search (searches username, email, fullName, phoneNumber)
    spec = withText(spec, searchParams.getQuery(), UserSpecs::matchesQuery);
    
    // Role and division filters
    spec = withText(spec, searchParams.getRoleName(), UserSpecs::hasRoleNameContaining);
    spec = withText(spec, searchParams.getDivisionName(), UserSpecs::hasDivisionNameContaining);
    spec = withValue(spec, searchParams.getDivisionId(), UserSpecs::hasDivisionId);
    
    // Assignable users filter (users with letter:own:manage permission)
    if (Boolean.TRUE.equals(searchParams.getAssignableOnly())) {
      spec = andSpec(spec, UserSpecs.hasPermission("letter:own:manage"));
    }

    return spec;
  }

  private Specification<User> withText(
      Specification<User> spec, String value, java.util.function.Function<String, Specification<User>> specBuilder) {
    if (StringUtils.hasText(value)) {
      return andSpec(spec, specBuilder.apply(value));
    }
    return spec;
  }

  private <T> Specification<User> withValue(
      Specification<User> spec, T value, java.util.function.Function<T, Specification<User>> specBuilder) {
    if (value != null) {
      return andSpec(spec, specBuilder.apply(value));
    }
    return spec;
  }

  private Specification<User> andSpec(Specification<User> spec, Specification<User> toAdd) {
    return (spec == null) ? toAdd : spec.and(toAdd);
  }

  @Transactional
  public User createUser(CreateUserRequest createUserRequest) {
    if (userRepository.existsByUsername(createUserRequest.username())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
    }
    if (userRepository.existsByEmail(createUserRequest.email())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
    }

    // Generate a random secure password
    String generatedPassword = generateRandomPassword();

    User user = new User();
    user.setUsername(createUserRequest.username());
    user.setPassword(passwordEncoder.encode(generatedPassword)); // Encode it for the database
    user.setEmail(createUserRequest.email());
    user.setFullName("New User"); // The template will use this name
    user.setAccountSetupRequired(true);
    user.setIsActive(true);

    Role role =
        roleRepository
            .findById(createUserRequest.roleId())
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role not found"));
    user.setRole(role);

    Division division =
        divisionRepository
            .findById(createUserRequest.divisionId())
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Division not found"));
    user.setDivision(division);

    // Save the user to the database first
    User savedUser = userRepository.save(user);

    // Prepare the variables for the email template
    Map<String, Object> emailModel = new HashMap<>();
    emailModel.put("fullName", savedUser.getFullName());
    emailModel.put("username", savedUser.getUsername());
    emailModel.put("password", generatedPassword); // Send the plain password to the user
    emailModel.put("loginUrl", frontendUrl + "/login"); // Add login URL for the button

    // Call the email service
    emailService.sendEmailWithTemplate(
        savedUser.getEmail(),
        "Welcome to the Admin System!",
        "email/welcome-email", // Path to the template: resources/templates/email/welcome-email.html
        emailModel);

    return savedUser;
  }

  @Transactional
  public void updateUser(Integer id, UserUpdateRequestDto request) {
    // Validate id
    if (id == null || id <= 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user ID");
    }

    // Find user
    User user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND));

    // Check if username is being changed and if it conflicts with another user
    if (!user.getUsername().equals(request.username())
        && userRepository.existsByUsername(request.username())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
    }

    // Check if email is being changed and if it conflicts with another user
    if (!user.getEmail().equals(request.email())
        && userRepository.existsByEmail(request.email())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
    }

    // Validate and fetch role by ID
    Role role =
        roleRepository
            .findById(request.roleId())
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Role not found with ID: " + request.roleId()));

    // Validate and fetch division by ID
    Division division =
        divisionRepository
            .findById(request.divisionId())
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Division not found with ID: " + request.divisionId()));

    // Update user fields
    user.setUsername(request.username());
    user.setEmail(request.email());
    user.setFullName(request.fullName());
    user.setPhoneNumber(request.phoneNumber());
    user.setRole(role);
    user.setDivision(division);

    userRepository.save(user);
  }

  @Transactional
  public void deleteUser(Integer id, Integer currentUserId) {
    // Validate id
    if (id == null || id <= 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user ID");
    }

    // Prevent self-deletion
    if (id.equals(currentUserId)) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Cannot delete your own account");
    }

    // Find user
    User user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND));

    // Check for user references in the system
    List<String> references = new ArrayList<>();
    
    // Check for assigned letters
    long assignedLettersCount = letterRepository.countActiveLettersByUserId(id);
    if (assignedLettersCount > 0) {
      references.add(assignedLettersCount + " assigned letter(s)");
    }
    
    // Check for letter events (actions performed by the user)
    long letterEventsCount = letterEventRepository.countByUserId(id);
    if (letterEventsCount > 0) {
      references.add(letterEventsCount + " letter event(s)");
    }
    
    // Check for submitted cabinet papers
    long submittedPapersCount = cabinetPaperRepository.countBySubmittedByUserId(id);
    if (submittedPapersCount > 0) {
      references.add(submittedPapersCount + " submitted cabinet paper(s)");
    }
    
    // Check for recorded cabinet decisions
    long recordedDecisionsCount = cabinetDecisionRepository.countByRecordedByUserId(id);
    if (recordedDecisionsCount > 0) {
      references.add(recordedDecisionsCount + " recorded cabinet decision(s)");
    }
    
    // If there are references, prevent deletion
    if (!references.isEmpty()) {
      String message = String.format(
          "Cannot delete user: This user has %s in the system. " +
          "Please reassign or remove these references before deleting the user.",
          String.join(", ", references));
      throw new ResponseStatusException(HttpStatus.CONFLICT, message);
    }

    // Delete the user
    userRepository.delete(user);
  }

  public UserDto getProfile(Integer userId) {
    return userRepository
        .findById(userId)
        .map(userMapper::toUserDto)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND));
  }

  @Transactional
  public void updateProfile(Integer userId, UserProfileUpdateRequestDto request) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND));

    // Check if email is being changed and if it conflicts with another user
    if (!user.getEmail().equals(request.email()) 
        && userRepository.existsByEmail(request.email())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
    }

    // Update user fields
    user.setFullName(request.fullName());
    user.setEmail(request.email());
    user.setPhoneNumber(request.phoneNumber());

    userRepository.save(user);
  }

  @Transactional
  public void accountSetup(Integer userId, AccountSetupRequestDto request) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND));

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

  @Transactional
  public void resetUserPassword(Integer userId) {
    // Validate userId
    if (userId == null || userId <= 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user ID");
    }

    // Find user
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND));

    // Generate a new random password
    String newPassword = generateRandomPassword();

    // Update user password and set account setup required
    user.setPassword(passwordEncoder.encode(newPassword));
    user.setAccountSetupRequired(true);

    userRepository.save(user);

    // Prepare the variables for the email template
    Map<String, Object> emailModel = new HashMap<>();
    emailModel.put("fullName", user.getFullName());
    emailModel.put("username", user.getUsername());
    emailModel.put("password", newPassword); // Send the plain password to the user
    emailModel.put("loginUrl", frontendUrl + "/login"); // Add login URL for the button

    // Send password reset email
    emailService.sendEmailWithTemplate(
        user.getEmail(),
        "Password Reset - MOHE Admin System",
        "email/password-reset-email",
        emailModel);
  }

  /**
   * Generates a secure random password with the following criteria:
   * - Length: 12 characters
   * - Contains at least one uppercase letter
   * - Contains at least one lowercase letter
   * - Contains at least one digit
   * - Contains at least one special character (!@#$%^&*)
   * 
   * @return A randomly generated secure password
   */
  private String generateRandomPassword() {
    StringBuilder password = new StringBuilder(PASSWORD_LENGTH);
    
    // Ensure at least one character from each category
    password.append(UPPERCASE_CHARS.charAt(RANDOM.nextInt(UPPERCASE_CHARS.length())));
    password.append(LOWERCASE_CHARS.charAt(RANDOM.nextInt(LOWERCASE_CHARS.length())));
    password.append(DIGIT_CHARS.charAt(RANDOM.nextInt(DIGIT_CHARS.length())));
    password.append(SPECIAL_CHARS.charAt(RANDOM.nextInt(SPECIAL_CHARS.length())));
    
    // Fill the remaining characters randomly from all character sets
    for (int i = 4; i < PASSWORD_LENGTH; i++) {
      password.append(ALL_CHARS.charAt(RANDOM.nextInt(ALL_CHARS.length())));
    }
    
    // Shuffle the password to avoid predictable patterns
    return shuffleString(password.toString());
  }

  /**
   * Shuffles the characters in a string using Fisher-Yates algorithm
   * 
   * @param input The string to shuffle
   * @return A new string with characters shuffled randomly
   */
  private String shuffleString(String input) {
    char[] characters = input.toCharArray();
    for (int i = characters.length - 1; i > 0; i--) {
      int j = RANDOM.nextInt(i + 1);
      char temp = characters[i];
      characters[i] = characters[j];
      characters[j] = temp;
    }
    return new String(characters);
  }
}
