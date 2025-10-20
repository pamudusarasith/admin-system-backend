package lk.gov.mohe.adminsystem.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(AuthControllerTest.TestConfig.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthService authService;

    @Autowired
    private RefreshTokenCookieConfig refreshTokenCookieConfig;

    @Autowired
    private ObjectMapper objectMapper;

    static class TestConfig {
        @Bean
        public AuthService authService() {
            return Mockito.mock(AuthService.class);
        }
        @Bean
        public RefreshTokenCookieConfig refreshTokenCookieConfig() {
            RefreshTokenCookieConfig config = new RefreshTokenCookieConfig();
            config.setName("refreshToken");
            config.setPaths(new String[]{"/"});
            config.setHttpOnly(true);
            config.setSecure(false);
            config.setSameSite("Strict");
            return config;
        }
    }

    @Test
    void testLogin_Success() throws Exception {
        LoginRequestDto loginRequest = new LoginRequestDto("user", "pass");
        AccessTokenDto accessTokenDto = AccessTokenDto.builder()
                .accessToken("access-token")
                .tokenType("Bearer")
                .expiresIn(3600L)
                .scope("read write")
                .build();
        RefreshTokenDto refreshTokenDto = new RefreshTokenDto("refresh-token");
        AuthTokensDto authTokensDto = AuthTokensDto.builder()
                .accessTokenDto(accessTokenDto)
                .refreshTokenDto(refreshTokenDto)
                .build();

        when(authService.login(any(LoginRequestDto.class))).thenReturn(authTokensDto);

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("access-token"))
                .andExpect(jsonPath("$.token_type").value("Bearer"))
                .andExpect(jsonPath("$.expires_in").value(3600))
                .andExpect(jsonPath("$.scope").value("read write"));
    }

    @Test
    void testRefreshToken_NoCookie_Unauthorized() throws Exception {
        mockMvc.perform(post("/refresh-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testLogout_ClearsCookie() throws Exception {
        Cookie cookie = new Cookie("refreshToken", "refresh-token");
        mockMvc.perform(post("/logout").cookie(cookie))
                .andExpect(status().isOk());
    }
}