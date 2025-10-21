package lk.gov.mohe.adminsystem.cabinetpaper.decision;

import jakarta.validation.Valid;
import lk.gov.mohe.adminsystem.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class CabinetDecisionController {
  private final CabinetDecisionService decisionService;

  @GetMapping("/cabinet-papers/{paperId}/decision")
  @PreAuthorize("hasAuthority('cabinet_decision:read')")
  public ApiResponse<CabinetDecisionDto> getDecisionByPaperId(@PathVariable Integer paperId) {
    CabinetDecisionDto decision = decisionService.getDecisionByPaperId(paperId);
    return ApiResponse.of(decision);
  }

  @PostMapping("/cabinet-papers/{paperId}/decision")
  @PreAuthorize("hasAuthority('cabinet_decision:create')")
  public ResponseEntity<ApiResponse<Void>> createDecision(
      @PathVariable Integer paperId,
      @Valid @RequestBody CreateCabinetDecisionRequestDto request) {
    decisionService.createDecision(paperId, request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.message("Cabinet decision created successfully"));
  }

  @PutMapping("/cabinet-papers/{paperId}/decision")
  @PreAuthorize("hasAuthority('cabinet_decision:update')")
  public ApiResponse<Void> updateDecision(
      @PathVariable Integer paperId, 
      @Valid @RequestBody UpdateCabinetDecisionRequestDto request) {
    decisionService.updateDecision(paperId, request);
    return ApiResponse.message("Cabinet decision updated successfully");
  }

  @DeleteMapping("/cabinet-papers/{paperId}/decision")
  @PreAuthorize("hasAuthority('cabinet_decision:delete')")
  public ApiResponse<Void> deleteDecision(@PathVariable Integer paperId) {
    decisionService.deleteDecision(paperId);
    return ApiResponse.message("Cabinet decision deleted successfully");
  }
}
