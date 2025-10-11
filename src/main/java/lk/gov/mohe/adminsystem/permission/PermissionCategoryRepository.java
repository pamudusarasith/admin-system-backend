package lk.gov.mohe.adminsystem.permission;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.List;

public interface PermissionCategoryRepository extends CrudRepository<PermissionCategory, Integer> {

    @org.jetbrains.annotations.NotNull List<PermissionCategory> findAll();
}