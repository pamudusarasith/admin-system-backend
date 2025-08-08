package lk.gov.mohe.adminsystem.security;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<TokenDto> login(@Valid @RequestBody LoginRequestDto loginRequest) {
        TokenDto tokenDto = authService.createNewAccessToken(loginRequest);
        if (tokenDto.error() != null) {
            return ResponseEntity.badRequest().body(tokenDto);
        }
        return ResponseEntity.ok(tokenDto);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<TokenDto> refreshToken(String refreshToken) {
        // Implement refresh token logic here
        return ResponseEntity.status(501).body(TokenDto.builder()
            .error("not_implemented")
            .errorDescription("Refresh token functionality is not implemented yet")
            .build());
    }
}
