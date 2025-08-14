package lk.gov.mohe.adminsystem.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final RefreshTokenCookieConfig refreshTokenCookieConfig;

    @Value("${custom.jwt.refresh-token-validity-seconds}")
    private Long refreshTokenValiditySeconds;

    @PostMapping("/login")
    public ResponseEntity<AccessTokenDto> login(@Valid @RequestBody LoginRequestDto loginRequest,
                                                HttpServletResponse response) {
        AuthTokensDto authTokensDto = authService.login(loginRequest);

        setRefreshTokenCookie(response, authTokensDto.refreshTokenDto().refreshToken());

        return ResponseEntity.ok(authTokensDto.accessTokenDto());
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AccessTokenDto> refreshToken(HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {
        String refreshToken = getRefreshTokenFromCookie(request);

        RefreshTokenRequestDto refreshTokenRequest =
            new RefreshTokenRequestDto(refreshToken);
        AuthTokensDto authTokensDto = authService.refreshAccessToken(refreshTokenRequest);

        setRefreshTokenCookie(response, authTokensDto.refreshTokenDto().refreshToken());

        return ResponseEntity.ok(authTokensDto.accessTokenDto());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request,
                                       HttpServletResponse response) {
        try {
            String refreshToken = getRefreshTokenFromCookie(request);

            if (refreshToken != null) {
                authService.revokeRefreshToken(refreshToken);
            }

            // Clear the refresh token cookie
            clearRefreshTokenCookie(response);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error during logout", e);
            // Still clear the cookie even if revocation fails
            clearRefreshTokenCookie(response);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(Authentication authentication,
                                          HttpServletResponse response) {
        try {
            if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
                String username = jwt.getSubject();
                authService.revokeAllUserTokens(username);

                // Clear the refresh token cookie
                clearRefreshTokenCookie(response);

                return ResponseEntity.ok().build();
            }
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error during logout all", e);
            // Still clear the cookie even if revocation fails
            clearRefreshTokenCookie(response);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Sets a secure HTTP-only cookie with the refresh token
     */
    private void setRefreshTokenCookie(HttpServletResponse response,
                                       String refreshToken) {
        for (String path : refreshTokenCookieConfig.getPaths()) {
            if (path == null || path.isEmpty()) {
                log.warn("Invalid cookie path: {}", path);
                continue;
            }
            Cookie cookie = createRefreshTokenCookie(refreshToken, path);

            response.addCookie(cookie);

            log.debug("Set refresh token cookie with path: {} and maxAge: {}",
                path, refreshTokenValiditySeconds);
        }
    }

    /**
     * Retrieves the refresh token from the HTTP-only cookie
     */
    private String getRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            return Arrays.stream(request.getCookies())
                .filter(cookie -> refreshTokenCookieConfig.getName().equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
        }
        return null;
    }

    /**
     * Clears the refresh token cookie by setting it to expire immediately
     */
    private void clearRefreshTokenCookie(HttpServletResponse response) {
        for (String path : refreshTokenCookieConfig.getPaths()) {
            if (path == null || path.isEmpty()) {
                log.warn("Invalid cookie path for clearing: {}", path);
                continue;
            }
            Cookie cookie = createRefreshTokenCookie("", path);
            cookie.setMaxAge(0); // Expire immediately

            response.addCookie(cookie);

            log.debug("Cleared refresh token cookie for path: {}", path);
        }
    }

    private Cookie createRefreshTokenCookie(String refreshToken, String path) {
        Cookie cookie = new Cookie(refreshTokenCookieConfig.getName(), refreshToken);
        cookie.setHttpOnly(refreshTokenCookieConfig.isHttpOnly());
        cookie.setSecure(refreshTokenCookieConfig.isSecure());
        cookie.setPath(path);
        cookie.setMaxAge(refreshTokenValiditySeconds.intValue());
        cookie.setAttribute("SameSite", refreshTokenCookieConfig.getSameSite());

        if (refreshTokenCookieConfig.getDomain() != null) {
            cookie.setDomain(refreshTokenCookieConfig.getDomain());
        }

        return cookie;
    }
}
