package com.transport.auth_service.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtTokenServiceTest {

    @Mock
    private JwtEncoder jwtEncoder;

    @InjectMocks
    private JwtTokenService jwtTokenService;

    @Test
    void issuesOneHourAdminToken() {
        var authentication = new UsernamePasswordAuthenticationToken(
                "admin", "ignored", List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("SCOPE_READ")));
        Instant now = Instant.now();
        Jwt signedJwt = new Jwt("signed-token", now, now.plus(Duration.ofHours(1)),
                Map.of("alg", "HS256"), Map.of("sub", "admin"));
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(signedJwt);

        String token = jwtTokenService.issue(authentication);

        assertEquals("signed-token", token);
        ArgumentCaptor<JwtEncoderParameters> parameters =
                ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(jwtEncoder).encode(parameters.capture());
        Map<String, Object> claims = parameters.getValue().getClaims().getClaims();
        assertEquals("transport-api", claims.get("iss"));
        assertEquals("admin", claims.get("sub"));
        assertEquals(List.of("ADMIN"), claims.get("roles"));
        assertEquals(Duration.ofHours(1),
                Duration.between((Instant) claims.get("iat"), (Instant) claims.get("exp")));
    }
}
