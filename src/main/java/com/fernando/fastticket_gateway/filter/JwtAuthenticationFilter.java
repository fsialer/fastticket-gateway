package com.fernando.fastticket_gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        log.info("🔍 JwtAuthenticationFilter INICIADO para: {}", path);
        
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        log.info("🔑 Authorization header presente: {}", authHeader != null);
        
        return ReactiveSecurityContextHolder.getContext()
                .cast(org.springframework.security.core.context.SecurityContext.class)
                .map(SecurityContext::getAuthentication)
                .cast(JwtAuthenticationToken.class)
                .map(jwtAuth -> {
                    Jwt jwt = jwtAuth.getToken();
                    String username = sanitizeHeader(jwt.getSubject());
                    Object rolesObj = jwt.getClaim("roles");
                    String roles = sanitizeHeader(rolesObj != null ? rolesObj.toString() : "[]");
                    
                    log.info("✅ JWT procesado - Usuario: {}, Roles: {}", username, roles);
                    
                    return exchange.getRequest().mutate()
                            .header("X-User-Id", username)
                            .header("X-User-Roles", roles)
                            .build();
                })
                .defaultIfEmpty(exchange.getRequest())
                .doOnNext(request -> log.info("📤 Headers agregados al request"))
                .flatMap(request -> chain.filter(exchange.mutate().request(request).build()))
                .onErrorResume(throwable -> {
                    log.warn("⚠️ Error en JWT filter: {}", throwable.getMessage());
                    return chain.filter(exchange);
                });
    }

    private String sanitizeHeader(String value) {
        if (value == null) return "";
        return value.replaceAll("[\r\n\t\f]", "")
                .substring(0, Math.min(value.length(), 255));
    }
}