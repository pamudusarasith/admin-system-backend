package lk.gov.mohe.adminsystem.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/** Service to handle scheduled cleanup tasks for refresh tokens. */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenCleanupService {

  private final RefreshTokenService refreshTokenService;

  /** Cleanup expired refresh tokens every hour. This runs at minute 0 of every hour. */
  @Scheduled(cron = "0 0 * * * *")
  public void cleanupExpiredTokens() {
    try {
      log.debug("Starting cleanup of expired refresh tokens");
      refreshTokenService.cleanupExpiredTokens();
      log.debug("Completed cleanup of expired refresh tokens");
    } catch (Exception e) {
      log.error("Error during refresh token cleanup", e);
    }
  }
}
