package com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Responses;

/**
 * DTO para representar um cardápio na listagem.
 * Usado em UC1 — Listar cardápios disponíveis.
 */
public class CardapioListaResponse {
    private long id;
    private String titulo;
    private boolean corrente;

    public CardapioListaResponse(long id, String titulo, boolean corrente) {
        this.id = id;
        this.titulo = titulo;
        this.corrente = corrente;
    }

    public long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public boolean isCorrente() {
        return corrente;
    }
}
