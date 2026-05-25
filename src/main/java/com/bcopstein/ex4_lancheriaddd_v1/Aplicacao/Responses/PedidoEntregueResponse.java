package com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Responses;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Pedido;

/**
 * Response para UC8 e UC9 — dados resumidos de pedidos entregues.
 */
public class PedidoEntregueResponse {

    private Long id;
    private String clienteCpf;
    private String clienteNome;
    private String dataHoraCriacao;
    private String enderecoEntrega;
    private double valorCobrado;
    private List<ItemResumo> itens;

    public PedidoEntregueResponse() {}

    public PedidoEntregueResponse(Pedido pedido) {
        this.id = pedido.getId();
        this.clienteCpf = pedido.getCliente().getCpf();
        this.clienteNome = pedido.getCliente().getNome();
        this.dataHoraCriacao = pedido.getDataHoraCriacao() != null
                ? pedido.getDataHoraCriacao().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                : null;
        this.enderecoEntrega = pedido.getEnderecoEntrega();
        this.valorCobrado = pedido.getValorCobrado();
        this.itens = pedido.getItens() != null
                ? pedido.getItens().stream()
                    .map(ip -> new ItemResumo(
                            ip.getItem().getDescricao(),
                            ip.getQuantidade(),
                            ip.getItem().getPreco()))
                    .collect(Collectors.toList())
                : List.of();
    }

    // ─── Getters ────────────────────────────────────────────────────────

    public Long getId() { return id; }
    public String getClienteCpf() { return clienteCpf; }
    public String getClienteNome() { return clienteNome; }
    public String getDataHoraCriacao() { return dataHoraCriacao; }
    public String getEnderecoEntrega() { return enderecoEntrega; }
    public double getValorCobrado() { return valorCobrado; }
    public List<ItemResumo> getItens() { return itens; }

    // ─── DTO interno para itens ─────────────────────────────────────────

    public static class ItemResumo {
        private String descricao;
        private int quantidade;
        private int precoUnitario;

        public ItemResumo() {}

        public ItemResumo(String descricao, int quantidade, int precoUnitario) {
            this.descricao = descricao;
            this.quantidade = quantidade;
            this.precoUnitario = precoUnitario;
        }

        public String getDescricao() { return descricao; }
        public int getQuantidade() { return quantidade; }
        public int getPrecoUnitario() { return precoUnitario; }
    }
}
