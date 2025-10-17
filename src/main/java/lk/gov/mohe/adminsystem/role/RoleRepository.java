package lk.gov.mohe.adminsystem.role;

import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

public interface RoleRepository
    extends CrudRepository<Role, Integer>, JpaSpecificationExecutor<Role> {
  @NotNull
  Optional<Role> findById(@NotNull Integer id);

  Boolean existsByNameIgnoreCase(String name);

  Optional<Role> findByName(String name);
}
