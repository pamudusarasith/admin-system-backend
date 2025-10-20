package lk.gov.mohe.adminsystem.cabinetpaper.decision;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CabinetDecisionRepository extends JpaRepository<CabinetDecision, Integer> {
  Optional<CabinetDecision> findByPaperId(Integer paperId);

  boolean existsByPaperId(Integer paperId);
}
