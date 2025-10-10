package lk.gov.mohe.adminsystem.security;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for CSRF token management.
 *
 * <p>Provides endpoints for the frontend to retrieve CSRF tokens required for making authenticated
 * requests.
 */
@RestController
public class CsrfController {

  /**
   * Get the current CSRF token.
   *
   * <p>This endpoint allows the frontend to retrieve the CSRF token that must be included in
   * subsequent requests to protected endpoints.
   *
   * <p>The token is automatically set as a cookie (XSRF-TOKEN) and can also be retrieved via this
   * endpoint for manual inclusion in requests.
   *
   * @param csrfToken the CSRF token automatically injected by Spring Security
   * @return ResponseEntity containing the CSRF token information
   */
  @GetMapping("/csrf-token")
  public ResponseEntity<Map<String, String>> getCsrfToken(CsrfToken csrfToken) {
    if (csrfToken == null) {
      return ResponseEntity.badRequest().build();
    }

    return ResponseEntity.ok(
        Map.of(
            "token", csrfToken.getToken(),
            "headerName", csrfToken.getHeaderName(),
            "parameterName", csrfToken.getParameterName()));
  }
}
