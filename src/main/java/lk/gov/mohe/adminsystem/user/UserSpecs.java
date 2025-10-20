package lk.gov.mohe.adminsystem.user;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import lk.gov.mohe.adminsystem.division.Division;
import lk.gov.mohe.adminsystem.permission.Permission;
import lk.gov.mohe.adminsystem.role.Role;
import org.springframework.data.jpa.domain.Specification;

public final class UserSpecs {
  private static final String ROLE = "role";
  private static final String DIVISION = "division";
  private static final String PERMISSIONS = "permissions";

  private UserSpecs() {}

  public static Specification<User> matchesQuery(String query) {
    String like = "%" + query.toLowerCase() + "%";
    return (root, criteriaQuery, cb) ->
        cb.or(
            cb.like(cb.lower(root.get("username")), like),
            cb.like(cb.lower(root.get("email")), like),
            cb.like(cb.lower(root.get("fullName")), like),
            cb.like(cb.lower(root.get("phoneNumber")), like));
  }

  public static Specification<User> hasRoleNameContaining(String roleName) {
    String like = "%" + roleName.toLowerCase() + "%";
    return (root, query, cb) -> {
      Join<User, Role> roleJoin = root.join(ROLE, JoinType.LEFT);
      return cb.like(cb.lower(roleJoin.get("name")), like);
    };
  }

  public static Specification<User> hasDivisionNameContaining(String divisionName) {
    String like = "%" + divisionName.toLowerCase() + "%";
    return (root, query, cb) -> {
      Join<User, Division> divisionJoin = root.join(DIVISION, JoinType.LEFT);
      return cb.like(cb.lower(divisionJoin.get("name")), like);
    };
  }

  public static Specification<User> hasDivisionId(Integer divisionId) {
    return (root, query, cb) -> cb.equal(root.get(DIVISION).get("id"), divisionId);
  }

  public static Specification<User> hasPermission(String permissionName) {
    return (root, query, cb) -> {
      Join<User, Role> roleJoin = root.join(ROLE, JoinType.LEFT);
      Join<Role, Permission> permissionsJoin = roleJoin.join(PERMISSIONS, JoinType.LEFT);
      return cb.equal(permissionsJoin.get("name"), permissionName);
    };
  }
}
