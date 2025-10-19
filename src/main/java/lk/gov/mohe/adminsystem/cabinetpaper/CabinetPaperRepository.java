package lk.gov.mohe.adminsystem.cabinetpaper;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CabinetPaperRepository extends JpaRepository<CabinetPaper, Integer> {
  boolean existsByReferenceId(String referenceId);
}
