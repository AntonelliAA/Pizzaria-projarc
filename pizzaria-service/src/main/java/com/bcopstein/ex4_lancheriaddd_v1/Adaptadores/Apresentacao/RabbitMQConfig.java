package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Apresentacao;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String FILA_ENTREGAS = "pedidos.prontos";

    @Bean
    public Queue filaEntregas() {
        // Cria a fila se não existir, com durabilidade verdadeira
        return new Queue(FILA_ENTREGAS, true);
    }
}
