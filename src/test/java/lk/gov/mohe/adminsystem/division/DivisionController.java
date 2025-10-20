package lk.gov.mohe.adminsystem.division;

import jakarta.validation.Valid;
import lk.gov.mohe.adminsystem.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DivisionController {

  private final DivisionRepository divisionRepository;
  private final DivisionService divisionService;

  @GetMapping("/divisions")
  // Searching divisions is required for assigning letters to divisions
  @PreAuthorize("hasAnyAuthority('division:read', 'letter:assign:division')")
  public ApiResponse<List<DivisionDto>> getDivisions(
      @RequestParam(defaultValue = "") String query,
      @RequestParam(defaultValue = "0") Integer page,
      @RequestParam(defaultValue = "10") Integer pageSize) {
    Page<DivisionDto> divisions = divisionService.getDivisions(query, page, pageSize);
    return ApiResponse.paged(divisions);
  }

  @PostMapping("/divisions")
  @PreAuthorize("hasAuthority('division:create')")
  public ApiResponse<Void> createDivision(
      @Valid @RequestBody CreateOrUpdateDivisionRequestDto dto) {
    divisionService.createDivision(dto);
    return ApiResponse.message("Division created successfully");
  }

  @PutMapping("/divisions/{id}")
  @PreAuthorize("hasAuthority('division:update')")
  public ApiResponse<Void> updateDivision(
      @PathVariable Integer id, @Valid @RequestBody CreateOrUpdateDivisionRequestDto dto) {
    divisionService.updateDivision(id, dto);
    return ApiResponse.message("Division updated successfully");
  }

  @DeleteMapping("/divisions/{id}")
  @PreAuthorize("hasAuthority('division:delete')")
  public ApiResponse<Void> deleteDivision(@PathVariable Integer id) {
    divisionService.deleteDivision(id);
    return ApiResponse.message("Division deleted successfully");
  }
}
