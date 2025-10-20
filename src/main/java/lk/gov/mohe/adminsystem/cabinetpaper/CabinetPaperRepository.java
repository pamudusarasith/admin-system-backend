package lk.gov.mohe.adminsystem.cabinetpaper;

import java.util.List;
import lk.gov.mohe.adminsystem.cabinetpaper.category.CabinetPaperCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface CabinetPaperRepository
    extends JpaRepository<CabinetPaper, Integer>, JpaSpecificationExecutor<CabinetPaper> {
  boolean existsByReferenceId(String referenceId);

  boolean existsByCategory(CabinetPaperCategory category);

  @Query("SELECT cp.status, COUNT(cp) FROM CabinetPaper cp GROUP BY cp.status")
  List<Object[]> countByStatus();

  @Query(
      "SELECT COALESCE(c.name, 'Uncategorized'), COUNT(cp) FROM CabinetPaper cp LEFT JOIN"
          + " cp.category c GROUP BY c.name")
  List<Object[]> countByCategory();

  @Query("SELECT COUNT(cp) FROM CabinetPaper cp WHERE cp.submittedByUser.id = :userId")
  long countBySubmittedByUserId(Integer userId);
}
