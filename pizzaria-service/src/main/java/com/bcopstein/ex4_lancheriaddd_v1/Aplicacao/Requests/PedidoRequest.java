package com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Requests;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class PedidoRequest {

    @NotNull
    private String clienteCpf;

    @NotBlank(message = "Endereço de entrega é obrigatório")
    private String enderecoEntrega;

    @NotEmpty(message = "O pedido deve ter pelo menos um item")
    @Valid
    private List<ItemPedidoRequest> itens;

    public PedidoRequest() {}

    public PedidoRequest(String clienteCpf, String enderecoEntrega, List<ItemPedidoRequest> itens) {
        this.clienteCpf = clienteCpf;
        this.enderecoEntrega = enderecoEntrega;
        this.itens = itens;
    }

    public String getClienteCpf() { return clienteCpf; }
    public String getEnderecoEntrega() { return enderecoEntrega; }
    public List<ItemPedidoRequest> getItens() { return itens; }
}
