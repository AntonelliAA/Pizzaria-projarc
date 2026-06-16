package com.bcopstein.ex4_lancheriaddd_v1.Aplicacao;

import org.springframework.stereotype.Service;

import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Responses.StatusPedidoResponse;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Pedido;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos.PedidoService;

/**
 * UC5 — Solicitar status de pedido.
 *
 * Recebe o ID do pedido e retorna o status atual,
 * data/hora de criação, endereço de entrega e valor cobrado.
 */
@Service
public class ConsultaStatusPedidoUC {

    private final PedidoService pedidoService;

    public ConsultaStatusPedidoUC(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    public StatusPedidoResponse run(Long pedidoId) {
        Pedido p = pedidoService.recuperaPorId(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Pedido não encontrado: " + pedidoId));

        return new StatusPedidoResponse(
                p.getId(),
                p.getStatus().name(),
                p.getDataHoraCriacao(),
                p.getEnderecoEntrega(),
                p.getValorCobrado() > 0 ? p.getValorCobrado() : null
        );
    }
}
