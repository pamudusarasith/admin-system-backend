package lk.gov.mohe.adminsystem.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response<T> {
    private String message;
    private Object data;
    private Pagination pagination;
    private List<ErrorInfo> errors;

    public Response(String message, T data, List<ErrorInfo> errors) {
        this.message = message;
        if (data instanceof Page<?> page) {
            this.data = page.getContent();
            this.pagination = new Pagination(page.getNumber(), page.getSize(),
                page.getTotalPages());
        } else {
            this.data = data;
        }
        this.errors = errors;
    }

    public Response(String message) {
        this(message, null, null);
    }

    public Response(T data) {
        this(null, data, null);
    }

    public Response(List<ErrorInfo> errors) {
        this(null, null, errors);
    }
}

record Pagination(
    int page,
    int size,
    int totalPages
) {
}

