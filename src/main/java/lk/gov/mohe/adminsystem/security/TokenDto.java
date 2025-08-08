package lk.gov.mohe.adminsystem.security;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TokenDto(
    @JsonProperty("access_token")
    String accessToken,
    @JsonProperty("token_type")
    String tokenType,
    @JsonProperty("expires_in")
    Long expiresIn,
    @JsonProperty("refresh_token")
    String refreshToken,
    String scope,
    String error,
    @JsonProperty("error_description")
    String errorDescription
) {
}
