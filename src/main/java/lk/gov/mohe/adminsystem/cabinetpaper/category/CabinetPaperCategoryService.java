package lk.gov.mohe.adminsystem.cabinetpaper.category;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CabinetPaperCategoryService {

    private final CabinetPaperCategoryRepository repository;

    public Page<CabinetPaperCategory> getAllCategories(String query, Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);

        if (query == null || query.trim().isEmpty()) {
            return repository.findAll(pageable);
        } else {
            return repository.findByNameContainingIgnoreCase(query, pageable);
        }
    }

    public Optional<CabinetPaperCategory> getCategoryById(Integer id) {
        return repository.findById(id);
    }

    public CabinetPaperCategory createCategory(CabinetPaperCategory category) {
        if (repository.existsByName(category.getName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Category with name '" + category.getName() + "' already exists");
        }

        try {
            return repository.save(category);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to create category", e);
        }
    }

    public CabinetPaperCategory updateCategory(Integer id, CabinetPaperCategory categoryDetails) {
        CabinetPaperCategory category = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found with id: " + id));

        Optional<CabinetPaperCategory> existingCategory = repository.findByName(categoryDetails.getName());
        if (existingCategory.isPresent() && !existingCategory.get().getId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Category with name '" + categoryDetails.getName() + "' already exists");
        }

        try {
            category.setName(categoryDetails.getName());
            category.setDescription(categoryDetails.getDescription());
            return repository.save(category);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to update category", e);
        }
    }

    public void deleteCategory(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found with id: " + id);
        }

        try {
            repository.deleteById(id);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Failed to delete category", e);
        }
    }
}
