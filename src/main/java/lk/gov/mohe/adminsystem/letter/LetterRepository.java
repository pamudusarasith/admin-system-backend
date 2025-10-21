package lk.gov.mohe.adminsystem.letter;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface LetterRepository
    extends JpaRepository<Letter, Integer>, JpaSpecificationExecutor<Letter> {

  boolean existsLetterByReference(String reference);

  @Query("SELECT COUNT(l) FROM Letter l WHERE l.assignedDivision IS NULL")
  long countUnassignedLetters();

  @Query("SELECT l.status, COUNT(l) FROM Letter l GROUP BY l.status")
  List<Object[]> countByStatus();

  @Query("SELECT l.priority, COUNT(l) FROM Letter l GROUP BY l.priority")
  List<Object[]> countByPriority();

  @Query(
      "SELECT COALESCE(d.name, 'Unassigned'), COUNT(l) FROM Letter l LEFT JOIN l.assignedDivision"
          + " d GROUP BY d.name")
  List<Object[]> countByDivision();

  // Division scope methods
  @Query("SELECT COUNT(l) FROM Letter l WHERE l.assignedDivision.id = :divisionId")
  long countByAssignedDivisionId(Integer divisionId);

  @Query(
      "SELECT l.status, COUNT(l) FROM Letter l WHERE l.assignedDivision.id = :divisionId GROUP BY"
          + " l.status")
  List<Object[]> countByStatusAndDivisionId(Integer divisionId);

  @Query(
      "SELECT l.priority, COUNT(l) FROM Letter l WHERE l.assignedDivision.id = :divisionId GROUP"
          + " BY l.priority")
  List<Object[]> countByPriorityAndDivisionId(Integer divisionId);

  // Unassigned scope methods
  @Query(
      "SELECT l.status, COUNT(l) FROM Letter l WHERE l.assignedDivision IS NULL GROUP BY l.status")
  List<Object[]> countUnassignedByStatus();

  @Query(
      "SELECT l.priority, COUNT(l) FROM Letter l WHERE l.assignedDivision IS NULL GROUP BY"
          + " l.priority")
  List<Object[]> countUnassignedByPriority();

  // Own letters scope methods
  @Query("SELECT COUNT(l) FROM Letter l WHERE l.assignedUser.id = :userId")
  long countByAssignedUserId(Integer userId);

  @Query("SELECT COUNT(l) FROM Letter l WHERE l.assignedUser.id = :userId")
  long countActiveLettersByUserId(Integer userId);

  @Query(
      "SELECT l.status, COUNT(l) FROM Letter l WHERE l.assignedUser.id = :userId GROUP BY"
          + " l.status")
  List<Object[]> countByStatusAndUserId(Integer userId);

  @Query(
      "SELECT l.priority, COUNT(l) FROM Letter l WHERE l.assignedUser.id = :userId GROUP BY"
          + " l.priority")
  List<Object[]> countByPriorityAndUserId(Integer userId);
}
