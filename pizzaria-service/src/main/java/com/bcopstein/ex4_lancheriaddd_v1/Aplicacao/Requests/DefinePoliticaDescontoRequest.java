package com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Requests;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para requisição de definição da política de desconto corrente.
 * Usado em UC4 — Definir a política de desconto corrente.
 */
public class DefinePoliticaDescontoRequest {
    
    @NotBlank(message = "O código da política de desconto é obrigatório")
    private String codigo;

    public DefinePoliticaDescontoRequest() {}

    public DefinePoliticaDescontoRequest(String codigo) {
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }
}
