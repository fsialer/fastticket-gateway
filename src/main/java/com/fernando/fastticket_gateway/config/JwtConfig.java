package com.fernando.fastticket_gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;

import javax.crypto.spec.SecretKeySpec;

@Configuration
public class JwtConfig {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        SecretKeySpec secretKey = new SecretKeySpec(jwtSecret.getBytes(), "HmacSHA256");
        NimbusReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder.withSecretKey(secretKey).build();
        
        decoder.setJwtValidator(JwtValidators.createDefault());
        
        // Wrapper para debugging
        return jwt -> {
            return decoder.decode(jwt)
                .doOnSuccess(decodedJwt -> System.out.println("✅ JWT válido, exp: " + decodedJwt.getExpiresAt()))
                .doOnError(error -> System.out.println("❌ JWT inválido: " + error.getMessage()));
        };
    }
}