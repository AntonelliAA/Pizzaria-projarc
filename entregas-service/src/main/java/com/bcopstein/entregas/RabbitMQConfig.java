package com.bcopstein.entregas;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cada instância do serviço de entregas declara a fila que consome.
 * Assim a fila passa a existir assim que qualquer consumidor sobe, sem depender
 * de o serviço de pizzaria abrir a conexão (que é "lazy", no primeiro publish).
 * A declaração é idempotente: fila durável, mesmo nome em todas as instâncias.
 */
@Configuration
public class RabbitMQConfig {

    public static final String FILA_ENTREGAS = "pedidos.prontos";

    @Bean
    public Queue filaEntregas() {
        return new Queue(FILA_ENTREGAS, true);
    }
}
