package com.transport.assignment_service.config;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@Configuration
public class FeignAuthConfig {

    @Bean
    RequestInterceptor bearerTokenRelay() {
        return template -> {
            Authentication authentication =

                    SecurityContextHolder.getContext().getAuthentication();

            if (authentication instanceof JwtAuthenticationToken
                    jwt) {
                template.header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + jwt.getToken().getTokenValue());
            }
        };
    }
}