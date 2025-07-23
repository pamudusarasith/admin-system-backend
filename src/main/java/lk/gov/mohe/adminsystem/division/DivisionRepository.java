package lk.gov.mohe.adminsystem.division;

import lk.gov.mohe.adminsystem.role.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DivisionRepository extends JpaRepository<Division, Integer> {
    Optional<Division> findByName(String name);
}