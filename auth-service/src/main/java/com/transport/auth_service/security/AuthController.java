package com.transport.auth_service.security;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@SecurityRequirement(name = "basicAuth")
@RequestMapping("/api/auth")

public class AuthController {

    private final JwtTokenService jwtTokenService;

    public AuthController(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @PostMapping("/token")
    public TokenResponse token(Authentication authentication) {
        return new TokenResponse(jwtTokenService.issue(authentication));
    }

    public record TokenResponse(String accessToken) {}


}
