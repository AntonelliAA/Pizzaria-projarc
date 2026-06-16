package com.bcopstein.ex4_lancheriaddd_v1.Aplicacao;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Responses.StatusPedidoResponse;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Pedido;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos.PedidoService;

/**
 * UC6 — Cancelar pedido.
 *
 * Regra de negócio: apenas pedidos com status APROVADO podem ser cancelados.
 * Pedidos pagos ou em qualquer outro estado não podem ser cancelados.
 */
@Service
@Transactional
public class CancelaPedidoUC {

    private final PedidoService pedidoService;

    public CancelaPedidoUC(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    public StatusPedidoResponse run(Long pedidoId) {
        Pedido p = pedidoService.recuperaPorId(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Pedido não encontrado: " + pedidoId));

        if (p.getStatus() != Pedido.Status.APROVADO) {
            throw new IllegalStateException(
                    "Pedido " + pedidoId + " não pode ser cancelado — status atual: " + p.getStatus());
        }

        pedidoService.atualizaStatus(pedidoId, Pedido.Status.CANCELADO);

        return new StatusPedidoResponse(
                p.getId(),
                Pedido.Status.CANCELADO.name(),
                p.getDataHoraCriacao(),
                p.getEnderecoEntrega(),
                null
        );
    }
}
