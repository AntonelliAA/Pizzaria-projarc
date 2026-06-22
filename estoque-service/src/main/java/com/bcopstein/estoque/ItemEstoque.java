package com.bcopstein.estoque;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "itens_estoque")
public class ItemEstoque {
    @Id
    private long ingredienteId;
    private String descricao;
    private int quantidade;

    public ItemEstoque() {}

    public ItemEstoque(long ingredienteId, String descricao, int quantidade) {
        this.ingredienteId = ingredienteId;
        this.descricao = descricao;
        this.quantidade = quantidade;
    }

    public long getIngredienteId() {
        return ingredienteId;
    }

    public String getDescricao() {
        return descricao;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void reduzirQuantidade(int qtd) {
        if (this.quantidade < qtd) {
            throw new IllegalArgumentException("Quantidade insuficiente no estoque para: " + descricao);
        }
        this.quantidade -= qtd;
    }
}
