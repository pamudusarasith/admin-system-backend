package lk.gov.mohe.adminsystem.role;

import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends CrudRepository<Role, Integer> {
    @NonNull
    Optional<Role> findById(@NonNull Integer id);

    Optional<Role> findByName(String name);

    @NonNull
    List<Role> findAll();
}
