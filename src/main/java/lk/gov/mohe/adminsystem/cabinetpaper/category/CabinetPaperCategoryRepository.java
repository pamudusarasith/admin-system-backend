package lk.gov.mohe.adminsystem.cabinetpaper.category;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CabinetPaperCategoryRepository
    extends JpaRepository<CabinetPaperCategory, Integer> {

  boolean existsByName(String name);

  boolean existsByNameAndIdNot(String name, Integer id);

  Optional<CabinetPaperCategory> findByName(String name);

  Page<CabinetPaperCategory> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
