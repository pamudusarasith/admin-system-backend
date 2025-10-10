package lk.gov.mohe.adminsystem.security;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lk.gov.mohe.adminsystem.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

  // properties
  private final RefreshTokenRepository refreshTokenRepository;
  private final JwtEncoder jwtEncoder;
  private final JwtDecoder jwtDecoder;

  @Value("${custom.jwt.max-tokens-per-user}")
  private int maxTokensPerUser; // Limit concurrent sessions, configurable via

  @Value("${custom.jwt.refresh-token-validity-seconds}")
  private Long refreshTokenValiditySeconds;

  /**
   * Creates a new refresh token for the given user. Implements token rotation by revoking old
   * tokens if limit is exceeded.
   */
  @Transactional
  public RefreshTokenDto createRefreshToken(User user) {
    if (user == null) {
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Cannot create refresh token for null user");
    }

    // Clean up expired tokens first
    cleanupExpiredTokens();

    // Check token limit and revoke oldest if necessary
    enforceTokenLimit(user);

    // Generate JWT refresh token with JTI
    String jti = UUID.randomUUID().toString();
    String refreshTokenJwt = generateRefreshTokenJwt(user, jti);

    RefreshToken refreshToken =
        RefreshToken.builder()
            .jti(jti)
            .user(user)
            .expiresAt(Instant.now().plusSeconds(refreshTokenValiditySeconds))
            .revoked(false)
            .build();

    refreshTokenRepository.save(refreshToken);
    log.debug("Created new refresh token for user: {}", user.getUsername());

    return new RefreshTokenDto(refreshTokenJwt);
  }

  /**
   * Validates a refresh token and returns the associated user if valid. This method decodes the JWT
   * and checks the JTI against stored values.
   */
  @Transactional(readOnly = true)
  public Optional<RefreshToken> validateRefreshToken(String tokenValue) {
    if (tokenValue == null || tokenValue.trim().isEmpty()) {
      log.debug("Attempted to validate null or empty refresh token");
      return Optional.empty();
    }

    try {
      // Decode the JWT refresh token
      Jwt jwt = jwtDecoder.decode(tokenValue);
      String jti = jwt.getClaim("jti");

      if (jti == null) {
        log.warn("Refresh token missing JTI claim");
        return Optional.empty();
      }

      // Check if the token exists and is valid in the database
      Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByJti(jti);

      if (refreshTokenOpt.isEmpty()) {
        log.warn("Refresh token with JTI {} not found in database", jti);
        return Optional.empty();
      }

      RefreshToken refreshToken = refreshTokenOpt.get();

      if (!refreshToken.isValid()) {
        log.warn("Refresh token with JTI {} is not valid (revoked or expired)", jti);
        return Optional.empty();
      }

      return Optional.of(refreshToken);
    } catch (JwtException e) {
      log.warn("Invalid refresh token JWT: {}", e.getMessage());
      return Optional.empty();
    }
  }

  /**
   * Rotates a refresh token - creates a new one and marks the old one as replaced. This implements
   * the refresh token rotation security pattern.
   */
  @Transactional
  public RefreshTokenDto rotateRefreshToken(RefreshToken oldToken) {
    if (oldToken == null) {
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Cannot rotate null refresh token");
    }

    if (oldToken.getUser() == null) {
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Cannot rotate token with null user");
    }

    // Create new token
    RefreshTokenDto newTokenDto = createRefreshToken(oldToken.getUser());

    // Find the new token by extracting its JTI
    try {
      Jwt jwt = jwtDecoder.decode(newTokenDto.refreshToken());
      String newJti = jwt.getClaim("jti");

      Optional<RefreshToken> newTokenOpt = refreshTokenRepository.findByJti(newJti);
      if (newTokenOpt.isEmpty()) {
        log.error(
            "Failed to find newly created refresh token for user: {}",
            oldToken.getUser().getUsername());
        throw new ResponseStatusException(
            HttpStatus.INTERNAL_SERVER_ERROR, "Internal error during token rotation");
      }

      RefreshToken newToken = newTokenOpt.get();

      // Mark old token as replaced
      oldToken.setReplacedByToken(newToken);
      oldToken.setRevoked(true);
      refreshTokenRepository.save(oldToken);

      log.debug("Rotated refresh token for user: {}", oldToken.getUser().getUsername());

      return newTokenDto;
    } catch (JwtException e) {
      log.error("Failed to decode newly created refresh token", e);
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Internal error during token processing", e);
    }
  }

  /**
   * Revokes a refresh token and its entire token family (all tokens derived from rotation). This is
   * used when token reuse is detected for security.
   */
  @Transactional
  public void revokeTokenFamily(RefreshToken token) {
    // Find the root token (the one that started the rotation chain)
    RefreshToken rootToken = findRootToken(token);

    // Revoke all tokens in the family
    revokeTokenChain(rootToken);

    log.warn(
        "Revoked token family for user: {} due to security violation",
        token.getUser().getUsername());
  }

  /** Revokes a refresh token by its JWT value */
  @Transactional
  public void revokeRefreshTokenByJwt(String tokenValue) {
    if (tokenValue == null || tokenValue.trim().isEmpty()) {
      log.debug("Attempted to revoke null or empty refresh token");
      return;
    }

    Optional<RefreshToken> refreshTokenOpt = validateRefreshToken(tokenValue);
    if (refreshTokenOpt.isPresent()) {
      RefreshToken refreshToken = refreshTokenOpt.get();
      revokeTokenFamily(refreshToken);
      log.debug("Revoked refresh token for user: {}", refreshToken.getUser().getUsername());
    } else {
      log.warn("Attempted to revoke invalid or non-existent refresh token");
    }
  }

  /** Revokes all refresh tokens for a user. */
  @Transactional
  public void revokeAllUserTokens(User user) {
    if (user == null) {
      log.debug("Attempted to revoke tokens for null user");
      return;
    }

    refreshTokenRepository.revokeAllUserTokens(user);
    log.debug("Revoked all tokens for user: {}", user.getUsername());
  }

  /** Cleanup method to remove expired tokens. */
  @Transactional
  public void cleanupExpiredTokens() {
    refreshTokenRepository.deleteExpiredTokens(Instant.now());
  }

  /** Generates a JWT refresh token with the given user and JTI */
  private String generateRefreshTokenJwt(User user, String jti) {
    Instant now = Instant.now();

    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .issuer("admin-system")
            .subject(user.getUsername())
            .id(jti)
            .issuedAt(now)
            .expiresAt(now.plusSeconds(refreshTokenValiditySeconds))
            .claim("token_type", "refresh")
            .claim("user_id", user.getId())
            .build();

    return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
  }

  private void enforceTokenLimit(User user) {
    long activeTokenCount = refreshTokenRepository.countValidTokensByUser(user, Instant.now());

    if (activeTokenCount >= maxTokensPerUser) {
      // Revoke oldest tokens to make room for new one
      var validTokens = refreshTokenRepository.findValidTokensByUser(user, Instant.now());
      validTokens.stream()
          .sorted((t1, t2) -> t1.getCreatedAt().compareTo(t2.getCreatedAt()))
          .limit(activeTokenCount - maxTokensPerUser + 1)
          .forEach(
              token -> {
                token.setRevoked(true);
                refreshTokenRepository.save(token);
              });
    }
  }

  private RefreshToken findRootToken(RefreshToken token) {
    // This is a simplified implementation
    // In a full implementation, you might need to traverse backwards through the
    // chain
    return token;
  }

  private void revokeTokenChain(RefreshToken rootToken) {
    // Recursively revoke this token and any tokens that replaced it
    rootToken.setRevoked(true);
    refreshTokenRepository.save(rootToken);

    // Revoke all tokens in the replacement chain
    RefreshToken replacedToken = rootToken.getReplacedByToken();
    while (replacedToken != null) {
      replacedToken.setRevoked(true);
      refreshTokenRepository.save(replacedToken);
      replacedToken = replacedToken.getReplacedByToken();
    }
  }
}
