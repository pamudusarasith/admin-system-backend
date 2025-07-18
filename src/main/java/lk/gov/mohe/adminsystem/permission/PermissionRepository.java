package lk.gov.mohe.adminsystem.permission;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.List;

public interface PermissionRepository extends CrudRepository<Permission, Long> {
    @NonNull
    List<Permission> findAllByNameIsIn(Collection<@NotNull String> names);
}
