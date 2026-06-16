package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Servicos;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.PedidosRepository;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Pedido;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos.ICozinhaService;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos.IEntregaService;

/**
 * Implementação simulada (assíncrona) do setor da cozinha.
 * Atualiza o status do pedido no banco de dados e avança para entrega.
 * Adaptador de serviço externo — trocável pela implementação real via {@link ICozinhaService}.
 */
@Service
public class CozinhaService implements ICozinhaService {

    private final PedidosRepository pedidosRepo;
    private final IEntregaService entregaService;
    private final ScheduledExecutorService scheduler;

    public CozinhaService(PedidosRepository pedidosRepo, IEntregaService entregaService) {
        this.pedidosRepo = pedidosRepo;
        this.entregaService = entregaService;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void chegadaDePedido(Pedido p) {
        System.out.println("Cozinha: Pedido " + p.getId() + " recebido na cozinha.");
        // Agenda transição para AGUARDANDO após 2 segundos
        scheduler.schedule(() -> colocaEmAguardando(p.getId()), 2, TimeUnit.SECONDS);
    }

    private void colocaEmAguardando(Long id) {
        pedidosRepo.atualizaStatus(id, Pedido.Status.AGUARDANDO);
        System.out.println("Cozinha: Pedido " + id + " aguardando preparo (AGUARDANDO)");

        // Agenda transição para PREPARACAO após 2 segundos
        scheduler.schedule(() -> colocaEmPreparacao(id), 2, TimeUnit.SECONDS);
    }

    private void colocaEmPreparacao(Long id) {
        pedidosRepo.atualizaStatus(id, Pedido.Status.PREPARACAO);
        System.out.println("Cozinha: Pedido " + id + " começou a ser preparado (PREPARACAO)");

        // Agenda transição para PRONTO após 2 segundos
        scheduler.schedule(() -> pedidoPronto(id), 2, TimeUnit.SECONDS);
    }

    private void pedidoPronto(Long id) {
        pedidosRepo.atualizaStatus(id, Pedido.Status.PRONTO);
        System.out.println("Cozinha: Pedido " + id + " finalizado (PRONTO)");

        // Envia para o setor de entregas
        pedidosRepo.recuperaPorId(id).ifPresent(entregaService::chegadaDePedido);
    }
}
