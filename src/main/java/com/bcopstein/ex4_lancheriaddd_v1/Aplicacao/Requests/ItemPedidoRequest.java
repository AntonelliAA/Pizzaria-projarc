package com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Requests;

import jakarta.validation.constraints.Min;

public class ItemPedidoRequest {
    @Min(1)
    private long produtoId;

    @Min(1)
    private int quantidade;

    public ItemPedidoRequest() {}

    public ItemPedidoRequest(long produtoId, int quantidade) {
        this.produtoId = produtoId;
        this.quantidade = quantidade;
    }

    public long getProdutoId() { return produtoId; }
    public int getQuantidade() { return quantidade; }
}
