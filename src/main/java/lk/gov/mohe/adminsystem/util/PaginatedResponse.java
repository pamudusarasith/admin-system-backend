package lk.gov.mohe.adminsystem.util;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PaginatedResponse {
    public final Object data;
    @JsonProperty("pagination")
    public final PaginationDetails paginationDetails;

    public PaginatedResponse(Object data, int page, int totalPages) {
        this.data = data;
        this.paginationDetails = new PaginationDetails(page, totalPages);
    }

    public record PaginationDetails(int page, int totalPages) {
    }
}