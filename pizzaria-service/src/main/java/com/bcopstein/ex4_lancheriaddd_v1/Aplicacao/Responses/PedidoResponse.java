package com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Responses;

import java.util.List;

public class PedidoResponse {

    private Long id;
    private String status;

    private double valor;
    private double impostos;
    private double desconto;
    private double valorCobrado;

    private List<String> itensIndisponiveis;

    public PedidoResponse() {}

    /** Construtor para pedido APROVADO com preço calculado. */
    public PedidoResponse(Long id, String status,
                          double valor, double impostos, double desconto, double valorCobrado) {
        this.id = id;
        this.status = status;
        this.valor = valor;
        this.impostos = impostos;
        this.desconto = desconto;
        this.valorCobrado = valorCobrado;
    }

    /** Construtor para pedido NEGADO por falta de estoque. */
    public PedidoResponse(Long id, String status, List<String> itensIndisponiveis) {
        this.id = id;
        this.status = status;
        this.itensIndisponiveis = itensIndisponiveis;
    }

    public Long getId() { return id; }
    public String getStatus() { return status; }
    public double getValor() { return valor; }
    public double getImpostos() { return impostos; }
    public double getDesconto() { return desconto; }
    public double getValorCobrado() { return valorCobrado; }
    public List<String> getItensIndisponiveis() { return itensIndisponiveis; }
}
