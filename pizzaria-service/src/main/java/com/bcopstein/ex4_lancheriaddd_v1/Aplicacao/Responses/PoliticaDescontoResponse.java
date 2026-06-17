package com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Responses;

/**
 * DTO para representar uma política de desconto disponível.
 * Usado em UC3 — Listar as políticas de desconto disponíveis.
 */
public class PoliticaDescontoResponse {
    private String codigo;
    private String descricao;
    private String lei;

    public PoliticaDescontoResponse(String codigo, String descricao, String lei) {
        this.codigo = codigo;
        this.descricao = descricao;
        this.lei = lei;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getLei() {
        return lei;
    }
}
