package lk.gov.mohe.adminsystem.letter;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import java.time.LocalDate;
import lk.gov.mohe.adminsystem.division.Division;
import lk.gov.mohe.adminsystem.user.User;
import org.springframework.data.jpa.domain.Specification;

public final class LetterSpecs {
  private static final String JSONB_EXTRACT = "jsonb_extract_path_text";
  private static final String SENDER_DETAILS = "senderDetails";
  private static final String RECEIVER_DETAILS = "receiverDetails";
  private static final String ASSIGNED_DIVISION = "assignedDivision";
  private static final String ASSIGNED_USER = "assignedUser";
  private static final String SENT_DATE = "sentDate";
  private static final String RECEIVED_DATE = "receivedDate";

  private LetterSpecs() {}

  public static Specification<Letter> hasNoAssignment() {
    return (root, query, cb) ->
        cb.and(cb.isNull(root.get(ASSIGNED_DIVISION)), cb.isNull(root.get(ASSIGNED_USER)));
  }

  public static Specification<Letter> belongsToDivision(Integer divisionId) {
    return (root, query, cb) -> cb.equal(root.get(ASSIGNED_DIVISION).get("id"), divisionId);
  }

  public static Specification<Letter> assignedToUser(Integer userId) {
    return (root, query, cb) -> cb.equal(root.get(ASSIGNED_USER).get("id"), userId);
  }

  public static Specification<Letter> hasStatus(StatusEnum status) {
    return (root, query, cb) -> cb.equal(root.get("status"), status);
  }

  public static Specification<Letter> hasPriority(PriorityEnum priority) {
    return (root, query, cb) -> cb.equal(root.get("priority"), priority);
  }

  public static Specification<Letter> hasModeOfArrival(ModeOfArrivalEnum modeOfArrival) {
    return (root, query, cb) -> cb.equal(root.get("modeOfArrival"), modeOfArrival);
  }

  public static Specification<Letter> hasSentDate(LocalDate date) {
    return (root, query, cb) -> cb.equal(root.get(SENT_DATE), date);
  }

  public static Specification<Letter> hasSentDateOnOrAfter(LocalDate date) {
    return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get(SENT_DATE), date);
  }

  public static Specification<Letter> hasSentDateOnOrBefore(LocalDate date) {
    return (root, query, cb) -> cb.lessThanOrEqualTo(root.get(SENT_DATE), date);
  }

  public static Specification<Letter> hasReceivedDate(LocalDate date) {
    return (root, query, cb) -> cb.equal(root.get(RECEIVED_DATE), date);
  }

  public static Specification<Letter> hasReceivedDateOnOrAfter(LocalDate date) {
    return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get(RECEIVED_DATE), date);
  }

  public static Specification<Letter> hasReceivedDateOnOrBefore(LocalDate date) {
    return (root, query, cb) -> cb.lessThanOrEqualTo(root.get(RECEIVED_DATE), date);
  }

  public static Specification<Letter> hasAssignedDivisionNameContaining(String term) {
    String like = "%" + term.toLowerCase() + "%";
    return (root, query, cb) -> {
      Join<Letter, Division> divisionJoin = root.join(ASSIGNED_DIVISION, JoinType.LEFT);
      return cb.like(cb.lower(divisionJoin.get("name")), like);
    };
  }

  public static Specification<Letter> hasAssignedUserContaining(String term) {
    String like = "%" + term.toLowerCase() + "%";
    return (root, query, cb) -> {
      Join<Letter, User> userJoin = root.join(ASSIGNED_USER, JoinType.LEFT);
      return cb.or(
          cb.like(cb.lower(userJoin.get("fullName")), like),
          cb.like(cb.lower(userJoin.get("username")), like),
          cb.like(cb.lower(userJoin.get("email")), like),
          cb.like(cb.lower(userJoin.get("phoneNumber")), like));
    };
  }

  public static Specification<Letter> hasSenderContaining(String term) {
    String like = "%" + term.toLowerCase() + "%";
    return (root, query, cb) ->
        cb.or(
            cb.like(cb.lower(jsonField(root, cb, SENDER_DETAILS, "name")), like),
            cb.like(cb.lower(jsonField(root, cb, SENDER_DETAILS, "address")), like),
            cb.like(cb.lower(jsonField(root, cb, SENDER_DETAILS, "email")), like),
            cb.like(cb.lower(jsonField(root, cb, SENDER_DETAILS, "phone_number")), like));
  }

  public static Specification<Letter> hasReceiverContaining(String term) {
    String like = "%" + term.toLowerCase() + "%";
    return (root, query, cb) ->
        cb.or(
            cb.like(cb.lower(jsonField(root, cb, RECEIVER_DETAILS, "name")), like),
            cb.like(cb.lower(jsonField(root, cb, RECEIVER_DETAILS, "designation")), like),
            cb.like(cb.lower(jsonField(root, cb, RECEIVER_DETAILS, "division_name")), like));
  }

  public static Specification<Letter> matchesQuery(String queryText) {
    String like = "%" + queryText.toLowerCase() + "%";
    return (root, query, cb) ->
        cb.or(
            cb.like(cb.lower(root.get("reference")), like),
            cb.like(cb.lower(root.get("subject")), like),
            cb.like(cb.lower(root.get("content")), like));
  }

  private static Expression<String> jsonField(
      Root<Letter> root, CriteriaBuilder cb, String column, String key) {
    return cb.function(JSONB_EXTRACT, String.class, root.get(column), cb.literal(key));
  }
}
