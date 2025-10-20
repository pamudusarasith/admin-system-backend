package lk.gov.mohe.adminsystem.cabinetpaper;

import lk.gov.mohe.adminsystem.cabinetpaper.category.CabinetPaperCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CabinetPaperRepository
    extends JpaRepository<CabinetPaper, Integer>, JpaSpecificationExecutor<CabinetPaper> {
  boolean existsByReferenceId(String referenceId);

  boolean existsByCategory(CabinetPaperCategory category);
}
