package lk.gov.mohe.adminsystem.cabinetpaper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class CabinetPaperCategoryService {

    @Autowired
    private CabinetPaperCategoryRepository repository;

    public List<CabinetPaperCategory> getAllCategories() {
        return repository.findAll();
    }

    public Optional<CabinetPaperCategory> getCategoryById(Integer id) {
        return repository.findById(id);
    }

    public CabinetPaperCategory createCategory(CabinetPaperCategory category) {
        return repository.save(category);
    }

    public CabinetPaperCategory updateCategory(Integer id, CabinetPaperCategory categoryDetails) {
        CabinetPaperCategory category = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        category.setName(categoryDetails.getName());
        return repository.save(category);
    }

    public void deleteCategory(Integer id) {
        repository.deleteById(id);
    }
}
