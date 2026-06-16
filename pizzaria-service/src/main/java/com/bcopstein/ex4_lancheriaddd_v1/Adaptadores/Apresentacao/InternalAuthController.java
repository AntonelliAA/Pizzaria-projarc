package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Apresentacao;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.AutenticarUC;
import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Requests.LoginRequest;
import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Responses.ValidacaoCredenciaisResponse;

import jakarta.validation.Valid;

/**
 * Endpoint INTERNO: valida credenciais para o gateway (que emite o JWT).
 * Não é destinado ao cliente final — o gateway é quem consome.
 */
@RestController
@RequestMapping("/internal/auth")
public class InternalAuthController {

    private final AutenticarUC autenticarUC;

    public InternalAuthController(AutenticarUC autenticarUC) {
        this.autenticarUC = autenticarUC;
    }

    @PostMapping("/validar")
    public ResponseEntity<ValidacaoCredenciaisResponse> validar(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(autenticarUC.run(req));
    }
}
