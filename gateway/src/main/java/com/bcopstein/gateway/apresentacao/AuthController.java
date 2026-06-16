package com.bcopstein.gateway.apresentacao;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.bcopstein.gateway.aplicacao.LoginRequest;
import com.bcopstein.gateway.aplicacao.LoginUC;
import com.bcopstein.gateway.aplicacao.TokenResponse;
import com.bcopstein.gateway.servicos.CredenciaisInvalidasException;

import reactor.core.publisher.Mono;

@RestController
public class AuthController {

    private final LoginUC loginUC;

    public AuthController(LoginUC loginUC) {
        this.loginUC = loginUC;
    }

    @PostMapping("/auth")
    public Mono<ResponseEntity<TokenResponse>> auth(@RequestBody LoginRequest req) {
        return loginUC.run(req.email(), req.senha())
                .map(ResponseEntity::ok)
                .onErrorResume(CredenciaisInvalidasException.class,
                        e -> Mono.just(ResponseEntity.status(401).build()));
    }
}
