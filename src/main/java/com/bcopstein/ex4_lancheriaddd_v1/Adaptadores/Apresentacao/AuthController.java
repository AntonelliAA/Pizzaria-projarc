package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Apresentacao;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.AutenticarUC;
import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Requests.LoginRequest;
import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Responses.AuthResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "Autenticação de clientes")
public class AuthController {

    private final AutenticarUC autenticarUC;

    public AuthController(AutenticarUC autenticarUC) {
        this.autenticarUC = autenticarUC;
    }

    @PostMapping
    @CrossOrigin("*")
    @Operation(summary = "Autenticar", description = "Autentica usuário e retorna token de sessão")
    public ResponseEntity<AuthResponse> autenticar(@Valid @RequestBody LoginRequest req) {
        AuthResponse resp = autenticarUC.run(req);
        return ResponseEntity.ok(resp);
    }
}
