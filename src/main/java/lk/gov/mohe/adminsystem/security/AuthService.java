package lk.gov.mohe.adminsystem.security;

import lk.gov.mohe.adminsystem.permission.Permission;
import lk.gov.mohe.adminsystem.user.User;
import lk.gov.mohe.adminsystem.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtEncoder jwtEncoder;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    @Value("${custom.jwt.access-token-validity-seconds}")
    private Long accessTokenValiditySeconds;

    @Transactional
    public AuthTokensDto login(LoginRequestDto loginRequest) {
        User user = userRepository.findByUsername(loginRequest.username());
        if (user == null || !passwordEncoder.matches(loginRequest.password(),
            user.getPassword())) {
            return AuthTokensDto.builder()
                .error("invalid_grant")
                .errorDescription("Invalid username or password")
                .build();
        }

        if (!user.getIsActive()) {
            return AuthTokensDto.builder()
                .error("account_disabled")
                .errorDescription("User account is disabled")
                .build();
        }

        // Generate access token
        AccessTokenDto accessToken = generateAccessToken(user);

        // Generate refresh token
        RefreshTokenDto refreshToken = refreshTokenService.createRefreshToken(user);

        return AuthTokensDto.builder()
            .accessTokenDto(accessToken)
            .refreshTokenDto(refreshToken)
            .build();
    }

    @Transactional
    public AuthTokensDto refreshAccessToken(RefreshTokenRequestDto refreshTokenRequest) throws Exception {
        String tokenValue = refreshTokenRequest.refreshToken();

        // Validate the refresh token
        Optional<RefreshToken> refreshTokenOpt =
            refreshTokenService.validateRefreshToken(tokenValue);

        if (refreshTokenOpt.isEmpty()) {
            log.warn("Attempted to use invalid refresh token");
            throw new Exception("Invalid refresh token");
        }

        RefreshToken refreshToken = refreshTokenOpt.get();
        User user = refreshToken.getUser();

        // Additional security: verify the token type claim
        try {
            // Note: We don't need to inject JwtDecoder here since validation is done in RefreshTokenService
            // But we could add additional verification if needed
        } catch (Exception e) {
            log.warn("Error validating refresh token JWT structure: {}", e.getMessage());
            throw new Exception("Invalid refresh token format");
        }

        // Check if user is still active
        if (!user.getIsActive()) {
            log.warn("Attempted to refresh token for inactive user: {}", user.getUsername());
            refreshTokenService.revokeAllUserTokens(user);
            throw new Exception("User account is disabled");
        }

        // Rotate the refresh token (security best practice)
        RefreshTokenDto newRefreshToken =
            refreshTokenService.rotateRefreshToken(refreshToken);

        // Generate new access token
        AccessTokenDto accessToken = generateAccessToken(user);

        log.debug("Successfully refreshed token for user: {}", user.getUsername());

        return AuthTokensDto.builder()
            .accessTokenDto(accessToken)
            .refreshTokenDto(newRefreshToken)
            .build();
    }

    public void revokeRefreshToken(String tokenValue) {
        refreshTokenService.revokeRefreshTokenByJwt(tokenValue);
    }

    public void revokeAllUserTokens(String username) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            refreshTokenService.revokeAllUserTokens(user);
            log.debug("Revoked all tokens for user: {}", username);
        }
    }

    private AccessTokenDto generateAccessToken(User user) {
        Instant now = Instant.now();
        String scope = user.getRole()
            .getPermissions().stream()
            .map(Permission::getName).collect(Collectors.joining(" "));

        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuedAt(now)
            .expiresAt(now.plusSeconds(accessTokenValiditySeconds))
            .subject(user.getUsername())
            .claim("scope", scope)
            .claim("user_id", user.getId())
            .claim("full_name", user.getFullName())
            .build();

        String accessToken =
            jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        return AccessTokenDto.builder()
            .accessToken(accessToken)
            .tokenType("Bearer")
            .expiresIn(accessTokenValiditySeconds)
            .scope(scope)
            .build();
    }
}
