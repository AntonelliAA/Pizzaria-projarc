package com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Requests;

/**
 * DTO para requisição de definição do cardápio corrente.
 * Usado em UC2 — Definir o cardápio corrente.
 */
public class DefineCardapioCorrenteRequest {
    private long cardapioId;

    public DefineCardapioCorrenteRequest() {}

    public DefineCardapioCorrenteRequest(long cardapioId) {
        this.cardapioId = cardapioId;
    }

    public long getCardapioId() {
        return cardapioId;
    }

    public void setCardapioId(long cardapioId) {
        this.cardapioId = cardapioId;
    }
}
