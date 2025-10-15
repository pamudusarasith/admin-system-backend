package lk.gov.mohe.adminsystem.division;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import lk.gov.mohe.adminsystem.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
  public ApiResponse<Void> createDivision(
      @Valid @RequestBody CreateOrUpdateDivisionRequestDto dto) {
    divisionService.createDivision(dto);
    return ApiResponse.message("Division created successfully");
  }

  @DeleteMapping("/divisions/{id}")
  public ResponseEntity<Void> deleteDivision(@PathVariable Integer id) {
    Optional<Division> division = divisionRepository.findById(id);

    if (division.isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    divisionRepository.deleteById(id);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/divisions/{id}")
  public ApiResponse<Void> updateDivision(
      @PathVariable Integer id, @Valid @RequestBody CreateOrUpdateDivisionRequestDto dto) {
    divisionService.updateDivision(id, dto);
    return ApiResponse.message("Division updated successfully");
  }
}
