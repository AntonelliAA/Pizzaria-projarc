package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Apresentacao;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Root", description = "Endpoint raiz da aplicação")
public class Controller {
    @GetMapping("")
    @CrossOrigin("*")
    @Operation(summary = "Mensagem de boas-vindas", description = "Retorna a mensagem de boas-vindas da Pizzaria ECA")
    public String welcomeMessage() {
        return "Bem Vindo a Pizzaria ECA";
    }
}
