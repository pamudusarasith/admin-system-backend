package lk.gov.mohe.adminsystem.cabinetpaper.category;

import jakarta.validation.Valid;
import java.util.List;
import lk.gov.mohe.adminsystem.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class CabinetPaperCategoryController {

  private final CabinetPaperCategoryService service;

  @GetMapping("/cabinet-papers/categories")
  @PreAuthorize("hasAnyAuthority('cabinet_paper_category:read')")
  public ApiResponse<List<CabinetPaperCategoryDto>> getAllCategories(
      @RequestParam(defaultValue = "") String query,
      @RequestParam(defaultValue = "0") Integer page,
      @RequestParam(defaultValue = "10") Integer pageSize) {
    Page<CabinetPaperCategoryDto> categories = service.getAllCategories(query, page, pageSize);
        return ApiResponse.paged(categories);
    }

  @PostMapping("/cabinet-papers/categories")
  @PreAuthorize("hasAuthority('cabinet_paper_category:create')")
  public ApiResponse<Void> createCategory(
      @Valid @RequestBody CreateCabinetPaperCategoryRequestDto request) {
    service.createCategory(request);
        return ApiResponse.message("Category created successfully");
    }

  @PutMapping("/cabinet-papers/categories/{id}")
  @PreAuthorize("hasAuthority('cabinet_paper_category:update')")
  public ApiResponse<Void> updateCategory(
      @PathVariable Integer id, @Valid @RequestBody UpdateCabinetPaperCategoryRequestDto request) {
    service.updateCategory(id, request);
        return ApiResponse.message("Category updated successfully");
    }

  @DeleteMapping("/cabinet-papers/categories/{id}")
  @PreAuthorize("hasAuthority('cabinet_paper_category:delete')")
  public ApiResponse<Void> deleteCategory(@PathVariable Integer id) {
        service.deleteCategory(id);
        return ApiResponse.message("Category deleted successfully");
    }
}
