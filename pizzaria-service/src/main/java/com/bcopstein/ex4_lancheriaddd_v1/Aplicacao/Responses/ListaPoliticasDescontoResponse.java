package com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Responses;

import java.util.List;

/**
 * DTO wrapper para lista de políticas de desconto.
 * Usado em UC3 — Listar as políticas de desconto disponíveis.
 */
public class ListaPoliticasDescontoResponse {
    private List<PoliticaDescontoResponse> politicas;
    private String politicaCorrente;

    public ListaPoliticasDescontoResponse(List<PoliticaDescontoResponse> politicas, String politicaCorrente) {
        this.politicas = politicas;
        this.politicaCorrente = politicaCorrente;
    }

    public List<PoliticaDescontoResponse> getPoliticas() {
        return politicas;
    }

    public String getPoliticaCorrente() {
        return politicaCorrente;
    }
}
