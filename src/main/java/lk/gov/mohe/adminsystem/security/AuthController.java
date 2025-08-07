package lk.gov.mohe.adminsystem.security;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lk.gov.mohe.adminsystem.permission.Permission;
import lk.gov.mohe.adminsystem.user.User;
import lk.gov.mohe.adminsystem.user.UserRepository;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final JwtEncoder jwtEncoder;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${custom.jwt.access-token-validity-seconds}")
    private Long accessTokenValiditySeconds;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest loginRequest) {
        User user = userRepository.findByUsername(loginRequest.username());
        if (user == null || !passwordEncoder.matches(loginRequest.password(),
            user.getPassword())) {
            return ResponseEntity.badRequest()
                .body(TokenResponse.builder()
                    .error("invalid_grant")
                    .errorDescription("Invalid username or password")
                    .build());
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
            .build();
        String jwt = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        TokenResponse tokenResponse = TokenResponse.builder()
            .accessToken(jwt)
            .tokenType("Bearer")
            .expiresIn(accessTokenValiditySeconds)
            .scope(scope)
            .build();
        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<TokenResponse> refreshToken(String refreshToken) {
        // Implement refresh token logic here
        return ResponseEntity.status(501).body(TokenResponse.builder()
            .error("not_implemented")
            .errorDescription("Refresh token functionality is not implemented yet")
            .build());
    }

    public record LoginRequest(String username, String password) {
    }

    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record TokenResponse(
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
}
