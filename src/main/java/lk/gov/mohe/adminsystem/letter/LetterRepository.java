package lk.gov.mohe.adminsystem.letter;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LetterRepository extends JpaRepository<Letter, Integer>,
    JpaSpecificationExecutor<Letter> {

    boolean existsLetterByReference(String reference);
}