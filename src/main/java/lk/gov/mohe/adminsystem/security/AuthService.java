package lk.gov.mohe.adminsystem.security;

import lk.gov.mohe.adminsystem.permission.Permission;
import lk.gov.mohe.adminsystem.user.User;
import lk.gov.mohe.adminsystem.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
        if (loginRequest == null || loginRequest.username() == null || loginRequest.password() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                "Invalid username or password");
        }

        User user = userRepository.findByUsername(loginRequest.username());
        if (user == null || !passwordEncoder.matches(loginRequest.password(),
            user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                "Invalid username or password");
        }

        if (!user.getIsActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "User account is disabled");
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
    public AuthTokensDto refreshAccessToken(RefreshTokenRequestDto refreshTokenRequest) {
        if (refreshTokenRequest == null || refreshTokenRequest.refreshToken() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Refresh token is required");
        }

        String tokenValue = refreshTokenRequest.refreshToken();

        // Validate the refresh token
        Optional<RefreshToken> refreshTokenOpt =
            refreshTokenService.validateRefreshToken(tokenValue);

        if (refreshTokenOpt.isEmpty()) {
            log.warn("Attempted to use invalid refresh token");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                "Invalid refresh token");
        }

        RefreshToken refreshToken = refreshTokenOpt.get();
        User user = refreshToken.getUser();

        // Check if user is still active
        if (!user.getIsActive()) {
            log.warn("Attempted to refresh token for inactive user: {}",
                user.getUsername());
            refreshTokenService.revokeAllUserTokens(user);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "User account is disabled");
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
        if (tokenValue == null || tokenValue.trim().isEmpty()) {
            log.debug("Attempted to revoke null or empty token value");
            return; // Silently ignore null/empty tokens
        }
        refreshTokenService.revokeRefreshTokenByJwt(tokenValue);
    }

    public void revokeAllUserTokens(String username) {
        if (username == null || username.trim().isEmpty()) {
            log.debug("Attempted to revoke tokens for null or empty username");
            return; // Silently ignore null/empty usernames
        }

        User user = userRepository.findByUsername(username);
        if (user != null) {
            refreshTokenService.revokeAllUserTokens(user);
            log.debug("Revoked all tokens for user: {}", username);
        } else {
            log.debug("User not found for token revocation: {}", username);
        }
    }

    private AccessTokenDto generateAccessToken(User user) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Cannot generate token for null user");
        }

        if (user.getRole() == null || user.getRole().getPermissions() == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                "User has invalid role or permissions");
        }

        Instant now = Instant.now();
        String scope = user.getRole()
            .getPermissions().stream()
            .map(Permission::getName).collect(Collectors.joining(" "));

        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuedAt(now)
            .expiresAt(now.plusSeconds(accessTokenValiditySeconds))
            .subject(user.getUsername())
            .claim("scope", scope)
            .claim("userId", user.getId())
            .claim("divisionId", user.getDivision().getId())
            .claim("fullName", user.getFullName())
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
