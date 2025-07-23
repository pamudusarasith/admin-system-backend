package lk.gov.mohe.adminsystem.user;

import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.NonNull;

import java.util.List;

public interface UserRepository extends CrudRepository<User, Integer> {
    @NonNull
    List<User> findAll();

    User findByUsername(String username);

    boolean existsByUsername(String username);
}
