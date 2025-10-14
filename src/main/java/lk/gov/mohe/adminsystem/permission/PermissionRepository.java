package lk.gov.mohe.adminsystem.permission;

import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.NonNull;

public interface PermissionRepository extends CrudRepository<Permission, Long> {
  @NonNull
  List<Permission> findAllByNameIsIn(Collection<@NotNull String> names);
}
