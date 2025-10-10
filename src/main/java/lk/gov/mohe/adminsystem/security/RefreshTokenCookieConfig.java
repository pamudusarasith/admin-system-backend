package lk.gov.mohe.adminsystem.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "custom.security.refresh-token-cookie")
public class RefreshTokenCookieConfig {

  /** Name of the refresh token cookie */
  private String name;

  /** Paths for the refresh token cookie */
  private String[] paths;

  /** Whether the cookie should be HTTP-only (recommended: true) */
  private boolean httpOnly;

  /** Whether the cookie should be secure (HTTPS only) Should be true in production */
  private boolean secure;

  /** SameSite attribute for the cookie Options: Strict, Lax, None */
  private String sameSite;

  /** Domain for the cookie (optional) */
  private String domain;
}
