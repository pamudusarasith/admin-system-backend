package lk.gov.mohe.adminsystem.role;

import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public interface RoleRepository extends CrudRepository<Role, Integer> {
  Page<Role> findAll(Pageable pageable);

  @NotNull
  Optional<Role> findById(@NotNull Integer id);

  Boolean existsByNameIgnoreCase(String name);

  Optional<Role> findByName(String name);
}
