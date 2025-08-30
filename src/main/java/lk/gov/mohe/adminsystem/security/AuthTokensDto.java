package lk.gov.mohe.adminsystem.security;

import lombok.Builder;

@Builder
public record AuthTokensDto(
    AccessTokenDto accessTokenDto,
    RefreshTokenDto refreshTokenDto
) {
}
