package lk.gov.mohe.adminsystem.division;

import jakarta.validation.constraints.NotBlank;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
public interface DivisionRepository
    extends JpaRepository<Division, Integer>, JpaSpecificationExecutor<Division> {

  @NonNull
  Optional<Division> findById(@NonNull Integer id);

  Optional<Division> findByName(@NotBlank(message = "Division must not be blank") String division);

  Boolean existsByName(String name);
}
