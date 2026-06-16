package com.bcopstein.gateway.servicos;

import reactor.core.publisher.Mono;

public interface ICredenciaisService {
    Mono<Identidade> validar(String email, String senha);
}
