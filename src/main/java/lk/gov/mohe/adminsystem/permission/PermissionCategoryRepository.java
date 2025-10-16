package lk.gov.mohe.adminsystem.permission;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.repository.CrudRepository;

public interface PermissionCategoryRepository extends CrudRepository<PermissionCategory, Integer> {
  @NotNull
  List<PermissionCategory> findAll();
}
