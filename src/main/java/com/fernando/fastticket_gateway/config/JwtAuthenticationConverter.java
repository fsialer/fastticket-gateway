package com.fernando.fastticket_gateway.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationConverter implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {
    @Override
    public Mono<AbstractAuthenticationToken> convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);

        JwtAuthenticationToken authenticationToken = new JwtAuthenticationToken(jwt, authorities);

        return Mono.just(authenticationToken);
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        // Extract roles from JWT claims
        Object rolesClaim = jwt.getClaim("roles");

        if (rolesClaim instanceof List<?> rolesList) {
            return rolesList.stream()
                    .filter(role -> role instanceof String)
                    .map(role -> new SimpleGrantedAuthority(role.toString()))
                    .collect(Collectors.toList());
        }

        // Fallback: try to extract from scope or other claims
        Object scopeClaim = jwt.getClaim("scope");
        if (scopeClaim instanceof String scopeString) {
            return Arrays.stream(scopeString.split(" "))
                    .map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope))
                    .collect(Collectors.toList());
        }

        return List.of();
    }
}
