package lk.gov.mohe.adminsystem.util;

import org.springframework.data.jpa.domain.Specification;

public final class SpecificationsUtil {
  private SpecificationsUtil() {}

  public static <T> Specification<T> andSpec(Specification<T> base, Specification<T> addition) {
    if (addition == null) {
      return base;
    }
    return (base == null) ? addition : base.and(addition);
  }

  public static <T> Specification<T> orSpec(Specification<T> base, Specification<T> addition) {
    if (addition == null) {
      return base;
    }
    return (base == null) ? addition : base.or(addition);
  }
}
