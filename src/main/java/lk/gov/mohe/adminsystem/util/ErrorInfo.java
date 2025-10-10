package lk.gov.mohe.adminsystem.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.lang.NonNull;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorInfo(String field, @NonNull String message) {}
