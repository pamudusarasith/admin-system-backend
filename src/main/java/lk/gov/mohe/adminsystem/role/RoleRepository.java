package lk.gov.mohe.adminsystem.role;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends CrudRepository<Role, Long> {
    Optional<Role> findByName(String name);
    //List<Role> findAll();
}
