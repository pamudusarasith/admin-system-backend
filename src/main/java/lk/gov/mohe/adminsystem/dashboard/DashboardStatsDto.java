package lk.gov.mohe.adminsystem.dashboard;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DashboardStatsDto(
    UserStats userStats,
    LetterStats letterStats,
    CabinetPaperStats cabinetPaperStats,
    DivisionStats divisionStats,
    RoleStats roleStats) {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record UserStats(Long totalUsers, Long activeUsers, Long inactiveUsers) {}

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record LetterStats(
      Long totalLetters,
      Long unassignedLetters,
      Map<String, Long> lettersByStatus,
      Map<String, Long> lettersByPriority,
      Map<String, Long> lettersByDivision) {}

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record CabinetPaperStats(
      Long totalPapers,
      Map<String, Long> papersByStatus,
      Map<String, Long> papersByCategory,
      Long papersWithDecisions) {}

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record DivisionStats(Long totalDivisions) {}

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record RoleStats(Long totalRoles) {}
}
