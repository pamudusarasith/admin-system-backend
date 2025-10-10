package lk.gov.mohe.adminsystem.user;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

import java.util.List;

public interface UserRepository extends CrudRepository<User, Integer> {
    @NonNull
    List<User> findAll();

    User findByUsername(String username);

    @Query("SELECT u.role.id, COUNT(u) FROM User u WHERE u.role.id IN :roleIds" +
            " GROUP BY u.role.id")
    List<Object[]> countUsersByRoleIds(@Param("roleIds") List<Integer> roleIds);


}
