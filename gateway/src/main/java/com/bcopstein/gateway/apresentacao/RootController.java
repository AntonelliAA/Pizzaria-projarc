package com.bcopstein.gateway.apresentacao;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

/**
 * Mensagem de boas-vindas na raiz do gateway (porta de entrada do sistema).
 */
@RestController
public class RootController {

    @GetMapping("/")
    public Mono<String> welcome() {
        return Mono.just("Bem Vindo a Pizzaria ECA");
    }
}
