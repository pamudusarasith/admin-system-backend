package lk.gov.mohe.adminsystem.cabinetpaper;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CabinetPaperCategoryRepository
    extends JpaRepository<CabinetPaperCategory, Integer> {

  boolean existsByName(String name);

  Optional<CabinetPaperCategory> findByName(String name);

  Page<CabinetPaperCategory> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
