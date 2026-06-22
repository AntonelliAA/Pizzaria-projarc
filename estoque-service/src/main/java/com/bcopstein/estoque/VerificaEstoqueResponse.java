package com.bcopstein.estoque;

import java.util.List;

public class VerificaEstoqueResponse {
    private boolean disponivel;
    private List<String> itensIndisponiveis;

    public VerificaEstoqueResponse() {}

    public VerificaEstoqueResponse(boolean disponivel, List<String> itensIndisponiveis) {
        this.disponivel = disponivel;
        this.itensIndisponiveis = itensIndisponiveis;
    }

    public boolean isDisponivel() { return disponivel; }
    public void setDisponivel(boolean disponivel) { this.disponivel = disponivel; }
    public List<String> getItensIndisponiveis() { return itensIndisponiveis; }
    public void setItensIndisponiveis(List<String> itensIndisponiveis) { this.itensIndisponiveis = itensIndisponiveis; }
}
