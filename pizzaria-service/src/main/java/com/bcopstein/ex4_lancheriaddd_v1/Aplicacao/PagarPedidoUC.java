package com.bcopstein.ex4_lancheriaddd_v1.Aplicacao;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Responses.StatusPedidoResponse;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Pedido;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos.ICozinhaService;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos.IPagamentoService;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos.PedidoService;

/**
 * UC7 — Pagar pedido.
 *
 * Regra de negócio: apenas pedidos com status APROVADO podem ser pagos.
 * Se o pagamento for efetuado com sucesso, o status é atualizado para PAGO
 * e o pedido é encaminhado para a cozinha.
 */
@Service
@Transactional
public class PagarPedidoUC {

    private final PedidoService pedidoService;
    private final IPagamentoService pagamentoService;
    private final ICozinhaService cozinhaService;

    public PagarPedidoUC(PedidoService pedidoService,
                         IPagamentoService pagamentoService,
                         ICozinhaService cozinhaService) {
        this.pedidoService = pedidoService;
        this.pagamentoService = pagamentoService;
        this.cozinhaService = cozinhaService;
    }

    public StatusPedidoResponse run(Long pedidoId) {
        Pedido p = pedidoService.recuperaPorId(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Pedido não encontrado: " + pedidoId));

        if (p.getStatus() != Pedido.Status.APROVADO) {
            throw new IllegalStateException(
                    "Pedido " + pedidoId + " não pode ser pago — status atual: " + p.getStatus());
        }

        boolean pago = pagamentoService.processarPagamento(p);
        if (!pago) {
            throw new IllegalStateException("Falha no processamento do pagamento para o pedido: " + pedidoId);
        }

        pedidoService.atualizaStatus(pedidoId, Pedido.Status.PAGO);

        // Encaminha para cozinha
        cozinhaService.chegadaDePedido(p);

        return new StatusPedidoResponse(
                p.getId(),
                Pedido.Status.PAGO.name(),
                p.getDataHoraCriacao(),
                p.getEnderecoEntrega(),
                p.getValorCobrado()
        );
    }
}
