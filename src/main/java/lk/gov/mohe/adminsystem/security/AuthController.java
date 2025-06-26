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

    @Value("${jwt.lifetime}")
    private Long jwtLifetime;

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
            .expiresAt(now.plusSeconds(jwtLifetime))
            .subject(user.getUsername())
            .claim("scope", scope)
            .build();
        String jwt = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        TokenResponse tokenResponse = TokenResponse.builder()
            .accessToken(jwt)
            .tokenType("Bearer")
            .expiresIn(jwtLifetime)
            .scope(scope)
            .build();
        return ResponseEntity.ok(tokenResponse);
    }

    public record LoginRequest(String username, String password) {
    }

    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TokenResponse {
        @JsonProperty("access_token")
        private String accessToken;
        @JsonProperty("token_type")
        private String tokenType;
        @JsonProperty("expires_in")
        private Long expiresIn;
        @JsonProperty("refresh_token")
        private String refreshToken;
        private String scope;
        private String error;
        @JsonProperty("error_description")
        private String errorDescription;
    }
}
