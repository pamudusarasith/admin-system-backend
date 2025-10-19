package lk.gov.mohe.adminsystem.cabinetpaper;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CabinetPaperCategoryRepository
    extends JpaRepository<CabinetPaperCategory, Integer> {

    boolean existsByName(String name);
    Optional<CabinetPaperCategory> findByName(String name);
}
