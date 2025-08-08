package lk.gov.mohe.adminsystem.security;

import lk.gov.mohe.adminsystem.permission.Permission;
import lk.gov.mohe.adminsystem.user.User;
import lk.gov.mohe.adminsystem.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtEncoder jwtEncoder;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${custom.jwt.access-token-validity-seconds}")
    private Long accessTokenValiditySeconds;

    public TokenDto createNewAccessToken(LoginRequestDto loginRequest) {
        User user = userRepository.findByUsername(loginRequest.username());
        if (user == null || !passwordEncoder.matches(loginRequest.password(),
            user.getPassword())) {
            return TokenDto.builder()
                .error("invalid_grant")
                .errorDescription("Invalid username or password")
                .build();
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
        return TokenDto.builder()
            .accessToken(jwt)
            .tokenType("Bearer")
            .expiresIn(accessTokenValiditySeconds)
            .scope(scope)
            .build();
    }
}
