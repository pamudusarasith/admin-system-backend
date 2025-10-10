package lk.gov.mohe.adminsystem.division;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DivisionRepository extends JpaRepository<Division, Integer>,
    JpaSpecificationExecutor<Division> {

    @NonNull
    Optional<Division> findById(@NonNull Integer id);

}