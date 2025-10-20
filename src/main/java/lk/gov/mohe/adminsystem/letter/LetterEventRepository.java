package lk.gov.mohe.adminsystem.letter;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LetterEventRepository extends JpaRepository<LetterEvent, Integer> {
  List<LetterEvent> findByLetterIdOrderByCreatedAtDesc(Integer id);

  long countByUserId(Integer userId);
}
