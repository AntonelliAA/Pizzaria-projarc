package com.bcopstein.gateway.aplicacao;

import org.springframework.stereotype.Service;

import com.bcopstein.gateway.servicos.ICredenciaisService;
import com.bcopstein.gateway.servicos.ITokenService;

import reactor.core.publisher.Mono;

@Service
public class LoginUC {

    private final ICredenciaisService credenciais;
    private final ITokenService tokens;

    public LoginUC(ICredenciaisService credenciais, ITokenService tokens) {
        this.credenciais = credenciais;
        this.tokens = tokens;
    }

    public Mono<TokenResponse> run(String email, String senha) {
        return credenciais.validar(email, senha)
                .map(id -> new TokenResponse(tokens.gerar(id.cpf()), id.cpf(), id.email()));
    }
}
