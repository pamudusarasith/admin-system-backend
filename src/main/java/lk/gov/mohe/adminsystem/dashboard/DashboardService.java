package lk.gov.mohe.adminsystem.dashboard;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lk.gov.mohe.adminsystem.cabinetpaper.CabinetPaperRepository;
import lk.gov.mohe.adminsystem.cabinetpaper.CabinetPaperStatusEnum;
import lk.gov.mohe.adminsystem.cabinetpaper.category.CabinetPaperCategoryRepository;
import lk.gov.mohe.adminsystem.cabinetpaper.decision.CabinetDecisionRepository;
import lk.gov.mohe.adminsystem.dashboard.DashboardStatsDto.*;
import lk.gov.mohe.adminsystem.division.Division;
import lk.gov.mohe.adminsystem.division.DivisionRepository;
import lk.gov.mohe.adminsystem.letter.LetterRepository;
import lk.gov.mohe.adminsystem.letter.PriorityEnum;
import lk.gov.mohe.adminsystem.letter.StatusEnum;
import lk.gov.mohe.adminsystem.role.RoleRepository;
import lk.gov.mohe.adminsystem.security.CurrentUserProvider;
import lk.gov.mohe.adminsystem.user.User;
import lk.gov.mohe.adminsystem.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {
  private final UserRepository userRepository;
  private final LetterRepository letterRepository;
  private final CabinetPaperRepository cabinetPaperRepository;
  private final CabinetDecisionRepository cabinetDecisionRepository;
  private final DivisionRepository divisionRepository;
  private final RoleRepository roleRepository;
  private final CabinetPaperCategoryRepository categoryRepository;
  private final CurrentUserProvider currentUserProvider;

  @Transactional(readOnly = true)
  public DashboardStatsDto getDashboardStats(Collection<String> authorities) {
    User currentUser = currentUserProvider.getCurrentUserOrThrow();
    UserStats userStats = null;
    LetterStats letterStats = null;
    CabinetPaperStats cabinetPaperStats = null;
    DivisionStats divisionStats = null;
    RoleStats roleStats = null;

    // Check permissions and gather stats
    if (hasPermission(authorities, "user:read")) {
      userStats = getUserStats();
    }

    // Letter statistics based on scope
    if (hasPermission(authorities, "letter:all:read")) {
      // Full access - all letter statistics
      letterStats = getAllLetterStats();
    } else if (hasPermission(authorities, "letter:division:read")) {
      // Division scope - only their division's letters
      letterStats = getDivisionLetterStats(currentUser);
    } else if (hasPermission(authorities, "letter:unassigned:read")) {
      // Unassigned scope - only unassigned letters
      letterStats = getUnassignedLetterStats();
    } else if (hasPermission(authorities, "letter:own:manage")) {
      // Own letters scope - only letters assigned to them
      letterStats = getOwnLetterStats(currentUser);
    }

    if (hasPermission(authorities, "cabinet:read")) {
      cabinetPaperStats = getCabinetPaperStats();
    }

    if (hasPermission(authorities, "division:read")) {
      divisionStats = getDivisionStats();
    }

    if (hasPermission(authorities, "role:read")) {
      roleStats = getRoleStats();
    }

    return new DashboardStatsDto(
        userStats, letterStats, cabinetPaperStats, divisionStats, roleStats);
  }

  private UserStats getUserStats() {
    long totalUsers = userRepository.count();
    long activeUsers = userRepository.countByIsActive(true);
    long inactiveUsers = totalUsers - activeUsers;
    return new UserStats(totalUsers, activeUsers, inactiveUsers);
  }

  private LetterStats getAllLetterStats() {
    long totalLetters = letterRepository.count();
    long unassignedLetters = letterRepository.countUnassignedLetters();

    // Initialize all statuses with 0
    Map<String, Long> lettersByStatus = initializeStatusMap();
    letterRepository
        .countByStatus()
        .forEach(result -> lettersByStatus.put(result[0].toString(), (Long) result[1]));

    // Initialize all priorities with 0
    Map<String, Long> lettersByPriority = initializePriorityMap();
    letterRepository
        .countByPriority()
        .forEach(result -> lettersByPriority.put(result[0].toString(), (Long) result[1]));

    // Initialize all divisions with 0
    Map<String, Long> lettersByDivision = initializeDivisionMap();
    letterRepository
        .countByDivision()
        .forEach(result -> lettersByDivision.put(result[0].toString(), (Long) result[1]));

    return new LetterStats(
        totalLetters, unassignedLetters, lettersByStatus, lettersByPriority, lettersByDivision);
  }

  private LetterStats getDivisionLetterStats(User currentUser) {
    if (currentUser.getDivision() == null) {
      // User has no division, return zero stats with all keys
      return new LetterStats(
          0L, 0L, initializeStatusMap(), initializePriorityMap(), initializeDivisionMap());
    }

    long totalLetters =
        letterRepository.countByAssignedDivisionId(currentUser.getDivision().getId());
    // Unassigned letters don't belong to this scope
    long unassignedLetters = 0L;

    // Initialize all statuses with 0, then populate
    Map<String, Long> lettersByStatus = initializeStatusMap();
    letterRepository
        .countByStatusAndDivisionId(currentUser.getDivision().getId())
        .forEach(result -> lettersByStatus.put(result[0].toString(), (Long) result[1]));

    // Initialize all priorities with 0, then populate
    Map<String, Long> lettersByPriority = initializePriorityMap();
    letterRepository
        .countByPriorityAndDivisionId(currentUser.getDivision().getId())
        .forEach(result -> lettersByPriority.put(result[0].toString(), (Long) result[1]));

    // Initialize all divisions with 0, then set their division
    Map<String, Long> lettersByDivision = initializeDivisionMap();
    lettersByDivision.put(currentUser.getDivision().getName(), totalLetters);

    return new LetterStats(
        totalLetters, unassignedLetters, lettersByStatus, lettersByPriority, lettersByDivision);
  }

  private LetterStats getUnassignedLetterStats() {
    long unassignedLetters = letterRepository.countUnassignedLetters();

    // Initialize all statuses with 0, then populate
    Map<String, Long> lettersByStatus = initializeStatusMap();
    letterRepository
        .countUnassignedByStatus()
        .forEach(result -> lettersByStatus.put(result[0].toString(), (Long) result[1]));

    // Initialize all priorities with 0, then populate
    Map<String, Long> lettersByPriority = initializePriorityMap();
    letterRepository
        .countUnassignedByPriority()
        .forEach(result -> lettersByPriority.put(result[0].toString(), (Long) result[1]));

    // Initialize all divisions with 0, then set unassigned
    Map<String, Long> lettersByDivision = initializeDivisionMap();
    lettersByDivision.put("Unassigned", unassignedLetters);

    return new LetterStats(
        unassignedLetters,
        unassignedLetters,
        lettersByStatus,
        lettersByPriority,
        lettersByDivision);
  }

  private LetterStats getOwnLetterStats(User currentUser) {
    long totalLetters = letterRepository.countByAssignedUserId(currentUser.getId());
    // Own letters are already assigned
    long unassignedLetters = 0L;

    // Initialize all statuses with 0, then populate
    Map<String, Long> lettersByStatus = initializeStatusMap();
    letterRepository
        .countByStatusAndUserId(currentUser.getId())
        .forEach(result -> lettersByStatus.put(result[0].toString(), (Long) result[1]));

    // Initialize all priorities with 0, then populate
    Map<String, Long> lettersByPriority = initializePriorityMap();
    letterRepository
        .countByPriorityAndUserId(currentUser.getId())
        .forEach(result -> lettersByPriority.put(result[0].toString(), (Long) result[1]));

    // Initialize all divisions with 0, then set user's division if they have one
    Map<String, Long> lettersByDivision = initializeDivisionMap();
    if (currentUser.getDivision() != null) {
      lettersByDivision.put(currentUser.getDivision().getName(), totalLetters);
    }

    return new LetterStats(
        totalLetters, unassignedLetters, lettersByStatus, lettersByPriority, lettersByDivision);
  }

  private CabinetPaperStats getCabinetPaperStats() {
    long totalPapers = cabinetPaperRepository.count();
    long papersWithDecisions = cabinetDecisionRepository.count();

    // Initialize all statuses with 0, then populate
    Map<String, Long> papersByStatus = initializeCabinetPaperStatusMap();
    cabinetPaperRepository
        .countByStatus()
        .forEach(result -> papersByStatus.put(result[0].toString(), (Long) result[1]));

    // Initialize all categories with 0, then populate
    Map<String, Long> papersByCategory = initializeCategoryMap();
    cabinetPaperRepository
        .countByCategory()
        .forEach(result -> papersByCategory.put(result[0].toString(), (Long) result[1]));

    return new CabinetPaperStats(
        totalPapers, papersByStatus, papersByCategory, papersWithDecisions);
  }

  private DivisionStats getDivisionStats() {
    long totalDivisions = divisionRepository.count();
    return new DivisionStats(totalDivisions);
  }

  private RoleStats getRoleStats() {
    long totalRoles = roleRepository.count();
    return new RoleStats(totalRoles);
  }

  private boolean hasPermission(Collection<String> authorities, String permission) {
    return authorities.contains(permission);
  }

  // Helper methods to initialize maps with all possible keys set to 0
  private Map<String, Long> initializeStatusMap() {
    return Arrays.stream(StatusEnum.values())
        .collect(Collectors.toMap(Enum::name, status -> 0L, (a, b) -> a, LinkedHashMap::new));
  }

  private Map<String, Long> initializePriorityMap() {
    return Arrays.stream(PriorityEnum.values())
        .collect(Collectors.toMap(Enum::name, priority -> 0L, (a, b) -> a, LinkedHashMap::new));
  }

  private Map<String, Long> initializeDivisionMap() {
    Map<String, Long> divisionMap =
        divisionRepository.findAll().stream()
            .collect(
                Collectors.toMap(Division::getName, div -> 0L, (a, b) -> a, LinkedHashMap::new));
    // Always include "Unassigned" as a key
    divisionMap.put("Unassigned", 0L);
    return divisionMap;
  }

  private Map<String, Long> initializeCabinetPaperStatusMap() {
    return Arrays.stream(CabinetPaperStatusEnum.values())
        .collect(Collectors.toMap(Enum::name, status -> 0L, (a, b) -> a, LinkedHashMap::new));
  }

  private Map<String, Long> initializeCategoryMap() {
    return categoryRepository.findAll().stream()
        .collect(
            Collectors.toMap(
                category -> category.getName(), cat -> 0L, (a, b) -> a, LinkedHashMap::new));
  }
}
