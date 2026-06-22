package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Servicos;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Pedido;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos.IEntregaService;

/**
 * Implementação real que envia o pedido para a fila do RabbitMQ.
 */
@Service
@Primary
public class EntregaServiceRabbitMQ implements IEntregaService {

    private final RabbitTemplate rabbitTemplate;
    private static final String FILA_ENTREGAS = "pedidos.prontos";

    public EntregaServiceRabbitMQ(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void chegadaDePedido(Pedido p) {
        // Apenas enviamos o ID do pedido para não sobrecarregar a fila e evitar problemas de serialização
        rabbitTemplate.convertAndSend(FILA_ENTREGAS, p.getId());
        System.out.println("EntregaServiceRabbitMQ: Pedido " + p.getId() + " publicado na fila '" + FILA_ENTREGAS + "'.");
    }
}
