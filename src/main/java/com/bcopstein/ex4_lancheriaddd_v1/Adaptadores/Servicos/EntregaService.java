package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Servicos;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.PedidosRepository;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Pedido;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos.IEntregaService;

/**
 * Implementação simulada (assíncrona) do setor de entregas.
 * Atualiza o status do pedido no banco de dados.
 * Adaptador de serviço externo — trocável pela implementação real via {@link IEntregaService}.
 */
@Service
public class EntregaService implements IEntregaService {

    private final PedidosRepository pedidosRepo;
    private final ScheduledExecutorService scheduler;

    public EntregaService(PedidosRepository pedidosRepo) {
        this.pedidosRepo = pedidosRepo;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void chegadaDePedido(Pedido p) {
        System.out.println("Entrega: Pedido " + p.getId() + " recebido na fila de entregas.");
        // Agenda transição para TRANSPORTE após 2 segundos
        scheduler.schedule(() -> saiParaEntrega(p.getId()), 2, TimeUnit.SECONDS);
    }

    private void saiParaEntrega(Long id) {
        pedidosRepo.atualizaStatus(id, Pedido.Status.TRANSPORTE);
        System.out.println("Entrega: Pedido " + id + " saiu para entrega (TRANSPORTE)");

        // Agenda transição para ENTREGUE após 2 segundos
        scheduler.schedule(() -> pedidoEntregue(id), 2, TimeUnit.SECONDS);
    }

    private void pedidoEntregue(Long id) {
        pedidosRepo.atualizaStatus(id, Pedido.Status.ENTREGUE);
        System.out.println("Entrega: Pedido " + id + " entregue com sucesso (ENTREGUE)");
    }
}
