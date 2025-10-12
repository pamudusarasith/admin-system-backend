package lk.gov.mohe.adminsystem.permission;

import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface PermissionCategoryRepository extends CrudRepository<PermissionCategory, Integer> {

  @org.jetbrains.annotations.NotNull
  List<PermissionCategory> findAll();
}
