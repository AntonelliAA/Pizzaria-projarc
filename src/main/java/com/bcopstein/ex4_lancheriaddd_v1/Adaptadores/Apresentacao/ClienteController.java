package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Apresentacao;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.RegistrarClienteUC;
import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Requests.ClienteRequest;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Cliente;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/clientes")
@Tag(name = "Clientes", description = "Operações relacionadas a clientes")
public class ClienteController {

    private final RegistrarClienteUC registrarClienteUC;

    public ClienteController(RegistrarClienteUC registrarClienteUC) {
        this.registrarClienteUC = registrarClienteUC;
    }

    @PostMapping
    @CrossOrigin("*")
    @Operation(summary = "Registrar cliente", description = "Registra um novo cliente no sistema para permitir login futuro")
    public ResponseEntity<Cliente> registrar(@Valid @RequestBody ClienteRequest req) {
        Cliente c = registrarClienteUC.run(req);
        return ResponseEntity.status(201).body(c);
    }
}
