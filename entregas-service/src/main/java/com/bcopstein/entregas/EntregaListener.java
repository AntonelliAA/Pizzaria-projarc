package com.bcopstein.entregas;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class EntregaListener {

    private final RestTemplate restTemplate;
    private static final String PIZZARIA_URL = "http://pizzaria-service/internal/pedidos/{id}/status?status={status}";

    public EntregaListener(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @RabbitListener(queues = "pedidos.prontos")
    public void receberPedidoParaEntrega(Long pedidoId) {
        System.out.println("[Entregador] Pegou pedido " + pedidoId + " da fila. Iniciando transporte...");
        
        // Atualiza status para TRANSPORTE
        atualizarStatusPizzaria(pedidoId, "TRANSPORTE");

        // Simula tempo de entrega (5 segundos)
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Atualiza status para ENTREGUE
        System.out.println("[Entregador] Pedido " + pedidoId + " entregue com sucesso no destino!");
        atualizarStatusPizzaria(pedidoId, "ENTREGUE");
    }

    private void atualizarStatusPizzaria(Long pedidoId, String status) {
        try {
            restTemplate.put(PIZZARIA_URL, null, pedidoId, status);
        } catch (Exception e) {
            System.err.println("Erro ao atualizar status do pedido " + pedidoId + " para " + status + ": " + e.getMessage());
        }
    }
}
