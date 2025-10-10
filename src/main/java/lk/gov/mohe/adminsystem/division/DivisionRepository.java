package lk.gov.mohe.adminsystem.division;

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
}
