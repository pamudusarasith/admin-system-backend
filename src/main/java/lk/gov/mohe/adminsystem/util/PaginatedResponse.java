package lk.gov.mohe.adminsystem.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.Page;

import java.util.List;

public record PaginatedResponse<T>(
    @JsonProperty("data") List<T> data,
    @JsonProperty("pagination") PaginationDetails paginationDetails
) {
    public PaginatedResponse(Page<T> page) {
        this(
            page.getContent(),
            new PaginationDetails(page.getNumber(), page.getSize(), page.getTotalPages())
        );
    }

    public record PaginationDetails(int page, int itemsPerPage, int totalPages) {
    }
}