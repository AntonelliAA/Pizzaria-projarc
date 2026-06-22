package com.bcopstein.estoque;

import java.util.List;

public class VerificaEstoqueRequest {
    private List<ItemRequest> itens;

    public VerificaEstoqueRequest() {}

    public VerificaEstoqueRequest(List<ItemRequest> itens) {
        this.itens = itens;
    }

    public List<ItemRequest> getItens() {
        return itens;
    }

    public void setItens(List<ItemRequest> itens) {
        this.itens = itens;
    }

    public static class ItemRequest {
        private long ingredienteId;
        private int quantidade;

        public ItemRequest() {}

        public ItemRequest(long ingredienteId, int quantidade) {
            this.ingredienteId = ingredienteId;
            this.quantidade = quantidade;
        }

        public long getIngredienteId() { return ingredienteId; }
        public void setIngredienteId(long ingredienteId) { this.ingredienteId = ingredienteId; }
        public int getQuantidade() { return quantidade; }
        public void setQuantidade(int quantidade) { this.quantidade = quantidade; }
    }
}
