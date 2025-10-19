package lk.gov.mohe.adminsystem.cabinetpaper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
public class CabinetPaperCategoryController {

    @Autowired
    private CabinetPaperCategoryService service;

    @GetMapping("/cabinet-paper-categories")
    public List<CabinetPaperCategory> getAllCategories() {
        return service.getAllCategories();
    }

    @GetMapping("cabinet-paper-categories/{id}")
    public ResponseEntity<CabinetPaperCategory> getCategoryById(@PathVariable Integer id) {
        return service.getCategoryById(id)
                .map(category -> ResponseEntity.ok().body(category))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/cabinet-paper-categories")
    public CabinetPaperCategory createCategory(@Valid @RequestBody CabinetPaperCategory category) {
        return service.createCategory(category);
    }

    @PutMapping("/cabinet-paper-categories/{id}")
    public ResponseEntity<CabinetPaperCategory> updateCategory(
            @PathVariable Integer id,
            @Valid @RequestBody CabinetPaperCategory categoryDetails) {
            CabinetPaperCategory updatedCategory = service.updateCategory(id, categoryDetails);
            return ResponseEntity.ok(updatedCategory);
    }

    @DeleteMapping("cabinet-paper-categories/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Integer id) {
            service.deleteCategory(id);
            return ResponseEntity.ok().build();

    }
}
