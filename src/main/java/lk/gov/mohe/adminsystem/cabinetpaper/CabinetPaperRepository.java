package lk.gov.mohe.adminsystem.cabinetpaper;

import lk.gov.mohe.adminsystem.cabinetpaper.category.CabinetPaperCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CabinetPaperRepository extends JpaRepository<CabinetPaper, Integer> {
  boolean existsByReferenceId(String referenceId);

  boolean existsByCategory(CabinetPaperCategory category);
}
