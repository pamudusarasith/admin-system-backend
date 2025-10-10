package lk.gov.mohe.adminsystem.security;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.*;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
@EnableWebSecurity(debug = true)
@RequiredArgsConstructor
public class SecurityConfig {
  @Value("${custom.jwt.private-key}")
  private RSAPrivateKey privateKey;

  @Value("${custom.jwt.public-key}")
  private RSAPublicKey publicKey;

  @Value("${custom.cors.allowed-origins}")
  private String[] allowedOrigins;

  @Value("${custom.cors.allowed-methods}")
  private String[] allowedMethods;

  @Value("${custom.cors.allowed-headers}")
  private String[] allowedHeaders;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(
            csrf ->
                csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                    .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler()))
        .cors(Customizer.withDefaults())
        .formLogin(AbstractHttpConfigurer::disable)
        .logout(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
        .exceptionHandling(
            exception ->
                exception
                    .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
                    .accessDeniedHandler(new BearerTokenAccessDeniedHandler()))
        .authorizeHttpRequests(
            authorize ->
                authorize
                    .requestMatchers("/login")
                    .permitAll()
                    .requestMatchers("/refresh-token")
                    .permitAll()
                    .requestMatchers("/csrf-token")
                    .permitAll()
                    .requestMatchers("/error")
                    .permitAll()
                    .anyRequest()
                    .authenticated());

    return http.build();
  }

  @Bean
  UrlBasedCorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(List.of(allowedOrigins));
    configuration.setAllowedMethods(List.of(allowedMethods));
    configuration.setAllowedHeaders(List.of(allowedHeaders));
    configuration.setAllowCredentials(true); // Required for cookies
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  JwtEncoder jwtEncoder() {
    JWK jwk = new RSAKey.Builder(this.publicKey).privateKey(privateKey).build();
    JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(jwk));
    return new NimbusJwtEncoder(jwkSource);
  }

  @Bean
  JwtDecoder jwtDecoder() {
    NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withPublicKey(this.publicKey).build();
    Map<String, Converter<Object, ?>> claimNameToConverter = new HashMap<>();
    claimNameToConverter.put(
        "userId", userId -> userId != null ? Integer.parseInt(userId.toString()) : null);
    claimNameToConverter.put(
        "divisionId",
        divisionId -> divisionId != null ? Integer.parseInt(divisionId.toString()) : null);
    MappedJwtClaimSetConverter converter =
        MappedJwtClaimSetConverter.withDefaults(claimNameToConverter);
    jwtDecoder.setClaimSetConverter(converter);
    return jwtDecoder;
  }

  @Bean
  JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter =
        new JwtGrantedAuthoritiesConverter();
    grantedAuthoritiesConverter.setAuthorityPrefix("");
    JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
    jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
    return jwtAuthenticationConverter;
  }
}

final class SpaCsrfTokenRequestHandler implements CsrfTokenRequestHandler {
  private final CsrfTokenRequestHandler plain = new CsrfTokenRequestAttributeHandler();
  private final CsrfTokenRequestHandler xor = new XorCsrfTokenRequestAttributeHandler();

  @Override
  public void handle(
      HttpServletRequest request, HttpServletResponse response, Supplier<CsrfToken> csrfToken) {
    /*
     * Always use XorCsrfTokenRequestAttributeHandler to provide BREACH protection of
     * the CsrfToken when it is rendered in the response body.
     */
    this.xor.handle(request, response, csrfToken);
    /*
     * Render the token value to a cookie by causing the deferred token to be loaded.
     */
    csrfToken.get();
  }

  @Override
  public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {
    String headerValue = request.getHeader(csrfToken.getHeaderName());
    /*
     * If the request contains a request header, use CsrfTokenRequestAttributeHandler
     * to resolve the CsrfToken. This applies when a single-page application includes
     * the header value automatically, which was obtained via a cookie containing the
     * raw CsrfToken.
     *
     * In all other cases (e.g. if the request contains a request parameter), use
     * XorCsrfTokenRequestAttributeHandler to resolve the CsrfToken. This applies
     * when a server-side rendered form includes the _csrf request parameter as a
     * hidden input.
     */
    return (StringUtils.hasText(headerValue) ? this.plain : this.xor)
        .resolveCsrfTokenValue(request, csrfToken);
  }
}
