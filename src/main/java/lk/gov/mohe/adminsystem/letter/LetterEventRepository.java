package lk.gov.mohe.adminsystem.letter;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LetterEventRepository extends JpaRepository<LetterEvent, Integer> {
    List<LetterEvent> findByLetterId(Integer id);
}