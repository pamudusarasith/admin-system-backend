package lk.gov.mohe.adminsystem.cabinetpaper.decision;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lk.gov.mohe.adminsystem.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cabinet-decisions")
public class CabinetDecisionController {
  private final CabinetDecisionService decisionService;

  @GetMapping
  @PreAuthorize("hasAuthority('cabinet_decision:read')")
  public ApiResponse<List<CabinetDecisionDto>> getAllDecisions(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int pageSize) {
    Page<CabinetDecisionDto> decisions = decisionService.getAllDecisions(page, pageSize);
    return ApiResponse.paged(decisions);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAuthority('cabinet_decision:read')")
  public ApiResponse<CabinetDecisionDto> getDecisionById(@PathVariable Integer id) {
    CabinetDecisionDto decision = decisionService.getDecisionById(id);
    return ApiResponse.of(decision);
  }

  @GetMapping("/by-paper/{paperId}")
  @PreAuthorize("hasAuthority('cabinet_decision:read')")
  public ApiResponse<CabinetDecisionDto> getDecisionByPaperId(@PathVariable Integer paperId) {
    CabinetDecisionDto decision = decisionService.getDecisionByPaperId(paperId);
    return ApiResponse.of(decision);
  }

  @PostMapping
  @PreAuthorize("hasAuthority('cabinet_decision:create')")
  public ResponseEntity<ApiResponse<Void>> createDecision(
      @Valid @RequestBody CreateCabinetDecisionRequestDto request) {
    CabinetDecision decision = decisionService.createDecision(request);
    return ResponseEntity.created(URI.create("/cabinet-decisions/" + decision.getId()))
        .body(ApiResponse.message("Cabinet decision created successfully"));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAuthority('cabinet_decision:update')")
  public ApiResponse<Void> updateDecision(
      @PathVariable Integer id, @Valid @RequestBody UpdateCabinetDecisionRequestDto request) {
    decisionService.updateDecision(id, request);
    return ApiResponse.message("Cabinet decision updated successfully");
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAuthority('cabinet_decision:delete')")
  public ApiResponse<Void> deleteDecision(@PathVariable Integer id) {
    decisionService.deleteDecision(id);
    return ApiResponse.message("Cabinet decision deleted successfully");
  }
}
