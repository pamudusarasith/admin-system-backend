package lk.gov.mohe.adminsystem.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import org.springframework.data.domain.Page;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<D>(
    String message, D data, Pagination pagination, List<ErrorInfo> errors) {
  public static <T> ApiResponse<T> of(T data) {
    return new ApiResponse<>(null, data, null, null);
  }

  public static ApiResponse<Void> message(String message) {
    return new ApiResponse<>(message, null, null, null);
  }

  public static ApiResponse<Void> error(String message, List<ErrorInfo> errors) {
    return new ApiResponse<>(message, null, null, errors);
  }

  public static <T> ApiResponse<List<T>> paged(Page<T> page) {
    return new ApiResponse<>(
        null,
        page.getContent(),
        new Pagination(page.getNumber(), page.getSize(), page.getTotalPages()),
        null);
  }

  public record Pagination(int page, int pageSize, int totalPages) {}
}
