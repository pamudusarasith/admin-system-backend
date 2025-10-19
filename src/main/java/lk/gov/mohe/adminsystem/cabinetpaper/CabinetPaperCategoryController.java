package lk.gov.mohe.adminsystem.cabinetpaper;

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

    @GetMapping("/cabinet-paper-categories")
    public ApiResponse<List<CabinetPaperCategory>> getAllCategories(
            @RequestParam(defaultValue = "") String query,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<CabinetPaperCategory> categories = service.getAllCategories(query, page, pageSize);
        return ApiResponse.paged(categories);
    }

    @PostMapping("/cabinet-paper-categories")
    public ApiResponse<Void> createCategory(@Valid @RequestBody CabinetPaperCategory category) {
        service.createCategory(category);
        return ApiResponse.message("Category created successfully");
    }

    @PutMapping("/cabinet-paper-categories/{id}")
    public ApiResponse<Void> updateCategory(
            @PathVariable Integer id,
            @Valid @RequestBody CabinetPaperCategory categoryDetails) {
        service.updateCategory(id, categoryDetails);
        return ApiResponse.message("Category updated successfully");
    }

    @DeleteMapping("/cabinet-paper-categories/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable Integer id) {
        service.deleteCategory(id);
        return ApiResponse.message("Category deleted successfully");
    }
}
