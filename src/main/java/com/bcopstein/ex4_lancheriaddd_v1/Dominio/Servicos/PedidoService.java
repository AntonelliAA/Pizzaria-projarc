package com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.PedidosRepository;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Pedido;

/**
 * Serviço de domínio "Pedidos".
 * Concentra o acesso aos dados de pedido; os casos de uso operam por aqui, não pelo repositório.
 */
@Service
public class PedidoService {
    private final PedidosRepository pedidosRepository;

    @Autowired
    public PedidoService(PedidosRepository pedidosRepository) {
        this.pedidosRepository = pedidosRepository;
    }

    public Pedido salva(Pedido pedido) {
        return pedidosRepository.salva(pedido);
    }

    public Optional<Pedido> recuperaPorId(Long id) {
        return pedidosRepository.recuperaPorId(id);
    }

    public void atualizaStatus(Long id, Pedido.Status novoStatus) {
        pedidosRepository.atualizaStatus(id, novoStatus);
    }

    public long contaPedidosClienteNosUltimosDias(String cpf, int dias) {
        return pedidosRepository.contaPedidosClienteNosUltimosDias(cpf, dias);
    }

    /** UC8 — Pedidos entregues num intervalo de datas. */
    public List<Pedido> recuperaEntreguesEntreDatas(LocalDateTime inicio, LocalDateTime fim) {
        return pedidosRepository.recuperaEntreguesEntreDatas(inicio, fim);
    }

    /** UC9 — Pedidos entregues de um cliente num intervalo de datas. */
    public List<Pedido> recuperaEntreguesDeClienteEntreDatas(String cpf, LocalDateTime inicio, LocalDateTime fim) {
        return pedidosRepository.recuperaEntreguesDeClienteEntreDatas(cpf, inicio, fim);
    }
}
