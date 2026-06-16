package com.bcopstein.gateway.aplicacao;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;

import com.bcopstein.gateway.servicos.CredenciaisInvalidasException;
import com.bcopstein.gateway.servicos.ICredenciaisService;
import com.bcopstein.gateway.servicos.ITokenService;
import com.bcopstein.gateway.servicos.Identidade;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class LoginUCTest {

    @Test
    void emiteTokenQuandoCredenciaisValidas() {
        ICredenciaisService cred = mock(ICredenciaisService.class);
        ITokenService tokens = mock(ITokenService.class);
        when(cred.validar("hug@email.com", "senha"))
                .thenReturn(Mono.just(new Identidade("9001", "hug@email.com")));
        when(tokens.gerar("9001")).thenReturn("jwt-fake");

        LoginUC uc = new LoginUC(cred, tokens);

        StepVerifier.create(uc.run("hug@email.com", "senha"))
                .expectNextMatches(r -> r.token().equals("jwt-fake")
                        && r.cpf().equals("9001")
                        && r.email().equals("hug@email.com"))
                .verifyComplete();
    }

    @Test
    void propagaErroQuandoCredenciaisInvalidas() {
        ICredenciaisService cred = mock(ICredenciaisService.class);
        ITokenService tokens = mock(ITokenService.class);
        when(cred.validar(anyString(), anyString()))
                .thenReturn(Mono.error(new CredenciaisInvalidasException()));

        LoginUC uc = new LoginUC(cred, tokens);

        StepVerifier.create(uc.run("x@x.com", "errada"))
                .expectError(CredenciaisInvalidasException.class)
                .verify();
    }
}
