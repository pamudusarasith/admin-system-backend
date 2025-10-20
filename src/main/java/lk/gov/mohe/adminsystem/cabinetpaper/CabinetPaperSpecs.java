package lk.gov.mohe.adminsystem.cabinetpaper;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import java.time.Instant;
import lk.gov.mohe.adminsystem.cabinetpaper.category.CabinetPaperCategory;
import lk.gov.mohe.adminsystem.user.User;
import org.springframework.data.jpa.domain.Specification;

public final class CabinetPaperSpecs {
  private static final String CATEGORY = "category";
  private static final String SUBMITTED_BY_USER = "submittedByUser";
  private static final String CREATED_AT = "createdAt";
  private static final String UPDATED_AT = "updatedAt";

  private CabinetPaperSpecs() {}

  public static Specification<CabinetPaper> hasStatus(CabinetPaperStatusEnum status) {
    return (root, query, cb) -> cb.equal(root.get("status"), status);
  }

  public static Specification<CabinetPaper> hasCategoryNameContaining(String term) {
    String like = "%" + term.toLowerCase() + "%";
    return (root, query, cb) -> {
      Join<CabinetPaper, CabinetPaperCategory> categoryJoin = root.join(CATEGORY, JoinType.LEFT);
      return cb.like(cb.lower(categoryJoin.get("name")), like);
    };
  }

  public static Specification<CabinetPaper> hasSubmittedByUserContaining(String term) {
    String like = "%" + term.toLowerCase() + "%";
    return (root, query, cb) -> {
      Join<CabinetPaper, User> userJoin = root.join(SUBMITTED_BY_USER, JoinType.LEFT);
      return cb.or(
          cb.like(cb.lower(userJoin.get("fullName")), like),
          cb.like(cb.lower(userJoin.get("username")), like),
          cb.like(cb.lower(userJoin.get("email")), like));
    };
  }

  public static Specification<CabinetPaper> hasCreatedAtOnOrAfter(Instant date) {
    return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get(CREATED_AT), date);
  }

  public static Specification<CabinetPaper> hasCreatedAtOnOrBefore(Instant date) {
    return (root, query, cb) -> cb.lessThanOrEqualTo(root.get(CREATED_AT), date);
  }

  public static Specification<CabinetPaper> hasUpdatedAtOnOrAfter(Instant date) {
    return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get(UPDATED_AT), date);
  }

  public static Specification<CabinetPaper> hasUpdatedAtOnOrBefore(Instant date) {
    return (root, query, cb) -> cb.lessThanOrEqualTo(root.get(UPDATED_AT), date);
  }

  public static Specification<CabinetPaper> matchesQuery(String queryText) {
    String like = "%" + queryText.toLowerCase() + "%";
    return (root, query, cb) ->
        cb.or(
            cb.like(cb.lower(root.get("referenceId")), like),
            cb.like(cb.lower(root.get("subject")), like),
            cb.like(cb.lower(root.get("summary")), like));
  }
}
