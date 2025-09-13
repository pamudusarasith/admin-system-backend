package lk.gov.mohe.adminsystem.letter;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

public interface LetterRepository extends JpaRepository<Letter, Integer> {
    @NonNull
    Page<Letter> findAll(@NonNull Pageable pageable);

    boolean existsLetterByReference(String reference);
}