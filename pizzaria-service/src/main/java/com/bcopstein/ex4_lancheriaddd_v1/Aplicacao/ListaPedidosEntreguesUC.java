package com.bcopstein.ex4_lancheriaddd_v1.Aplicacao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Responses.PedidoEntregueResponse;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Pedido;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos.PedidoService;

/**
 * UC8 — Listar pedidos entregues entre duas datas.
 *
 * Retorna todos os pedidos com status ENTREGUE cuja data de criação
 * esteja no intervalo [inicio, fim].
 */
@Service
public class ListaPedidosEntreguesUC {

    private final PedidoService pedidoService;

    public ListaPedidosEntreguesUC(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    public List<PedidoEntregueResponse> run(LocalDateTime inicio, LocalDateTime fim) {
        if (inicio == null || fim == null) {
            throw new IllegalArgumentException("Datas de início e fim são obrigatórias");
        }
        if (inicio.isAfter(fim)) {
            throw new IllegalArgumentException("Data de início não pode ser posterior à data fim");
        }

        List<Pedido> pedidos = pedidoService.recuperaEntreguesEntreDatas(inicio, fim);

        return pedidos.stream()
                .map(PedidoEntregueResponse::new)
                .collect(Collectors.toList());
    }
}
