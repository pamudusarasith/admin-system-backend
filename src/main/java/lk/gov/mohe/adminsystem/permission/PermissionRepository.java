package lk.gov.mohe.adminsystem.permission;

import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.repository.CrudRepository;

public interface PermissionRepository extends CrudRepository<Permission, Long> {
  List<Permission> findAllByNameIsIn(Collection<String> names);

  @NotNull
  List<Permission> findAll();
}
