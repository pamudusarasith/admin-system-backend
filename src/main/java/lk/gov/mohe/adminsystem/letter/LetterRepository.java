package lk.gov.mohe.adminsystem.letter;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.util.Optional;

public interface LetterRepository extends JpaRepository<Letter, Integer> {
    @NonNull
    Page<Letter> findAll(@NonNull Pageable pageable);

    @NonNull
    Optional<Letter> findById(@NotNull Integer integer);

    boolean existsLetterByReference(String reference);
}