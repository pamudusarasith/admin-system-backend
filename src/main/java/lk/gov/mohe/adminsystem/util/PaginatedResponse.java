package lk.gov.mohe.adminsystem.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.Page;

import java.util.List;

public class PaginatedResponse<T> {
    public final List<T> data;
    @JsonProperty("pagination")
    public final PaginationDetails paginationDetails;

    public PaginatedResponse(Page<T> page) {
        this.data = page.getContent();
        this.paginationDetails = new PaginationDetails(page.getNumber(), page.getSize(),
            page.getTotalPages());
    }

    public record PaginationDetails(int page, int itemsPerPage, int totalPages) {
    }
}