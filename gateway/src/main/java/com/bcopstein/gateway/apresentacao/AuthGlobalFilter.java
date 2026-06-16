package com.bcopstein.gateway.apresentacao;

import java.nio.charset.StandardCharsets;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.bcopstein.gateway.servicos.ITokenService;

import reactor.core.publisher.Mono;

@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final ITokenService tokens;

    public AuthGlobalFilter(ITokenService tokens) {
        this.tokens = tokens;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest req = exchange.getRequest();

        if (RotasPublicas.ehPublica(req.getMethod(), req.getPath().value())) {
            return chain.filter(exchange);
        }

        String header = req.getHeaders().getFirst("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return naoAutorizado(exchange, "Token ausente.");
        }

        String token = header.substring("Bearer ".length()).trim();
        if (!tokens.valido(token)) {
            return naoAutorizado(exchange, "Token inválido ou expirado.");
        }

        ServerHttpRequest mutado = req.mutate()
                .header("X-Cliente-Cpf", tokens.cpfDe(token))
                .build();
        return chain.filter(exchange.mutate().request(mutado).build());
    }

    private Mono<Void> naoAutorizado(ServerWebExchange exchange, String msg) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        DataBuffer buffer = exchange.getResponse().bufferFactory()
                .wrap(("{\"erro\":\"" + msg + "\"}").getBytes(StandardCharsets.UTF_8));
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -1; // antes do roteamento
    }
}
