package com.transport.auth_service.security;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class JwtTokenService {

    private final JwtEncoder jwtEncoder;

    private static final Logger log= LoggerFactory.getLogger(JwtTokenService.class);

    public JwtTokenService(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public String issue(Authentication authentication) {
        Instant now = Instant.now();

        var roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring("ROLE_".length()))
                .toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("transport-api")
                .subject(authentication.getName())
                .issuedAt(now)
                .expiresAt(now.plus(Duration.ofHours(1)))
                .claim("roles", roles)
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        String token= jwtEncoder
                .encode(JwtEncoderParameters.from(header,claims))
                .getTokenValue();
        log.atInfo()
                .addKeyValue("event", "token_issued")
                .addKeyValue("subject", authentication.getName())
                .log("Token JWT emitido");
        return token;
    }
}
