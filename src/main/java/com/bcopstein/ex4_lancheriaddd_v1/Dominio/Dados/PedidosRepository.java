package com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Pedido;

public interface PedidosRepository {
    Pedido salva(Pedido p);
    Optional<Pedido> recuperaPorId(Long id);
    long contaPedidosClienteNosUltimosDias(String cpf, int dias);
    void atualizaStatus(Long id, Pedido.Status novoStatus);

    /** UC8 — Pedidos entregues num intervalo de datas. */
    List<Pedido> recuperaEntreguesEntreDatas(LocalDateTime inicio, LocalDateTime fim);

    /** UC9 — Pedidos entregues de um cliente num intervalo de datas. */
    List<Pedido> recuperaEntreguesDeClienteEntreDatas(String cpf, LocalDateTime inicio, LocalDateTime fim);
}

