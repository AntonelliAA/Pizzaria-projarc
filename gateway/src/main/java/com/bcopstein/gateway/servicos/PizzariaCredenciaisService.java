package com.bcopstein.gateway.servicos;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Service
public class PizzariaCredenciaisService implements ICredenciaisService {

    private final WebClient webClient;

    public PizzariaCredenciaisService(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("lb://pizzaria-service").build();
    }

    @Override
    public Mono<Identidade> validar(String email, String senha) {
        return webClient.post()
                .uri("/internal/auth/validar")
                .bodyValue(Map.of("email", email, "senha", senha))
                .retrieve()
                .onStatus(status -> status.value() == 401 || status.value() == 400,
                          resp -> Mono.error(new CredenciaisInvalidasException()))
                .bodyToMono(Identidade.class);
    }
}
