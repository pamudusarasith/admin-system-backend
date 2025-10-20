package lk.gov.mohe.adminsystem.dashboard;

import java.util.stream.Collectors;
import lk.gov.mohe.adminsystem.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {
  private final DashboardService dashboardService;

  @GetMapping
  @PreAuthorize("isAuthenticated()")
  public ApiResponse<DashboardStatsDto> getDashboardStats(Authentication authentication) {
    var authorities =
        authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toSet());

    DashboardStatsDto stats = dashboardService.getDashboardStats(authorities);
    return ApiResponse.of(stats);
  }
}
