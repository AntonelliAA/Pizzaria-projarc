package com.bcopstein.gateway.apresentacao;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

class RotasPublicasTest {

    @Test
    void cadastroEloginSaoPublicos() {
        assertTrue(RotasPublicas.ehPublica(HttpMethod.POST, "/clientes"));
        assertTrue(RotasPublicas.ehPublica(HttpMethod.POST, "/auth"));
        assertTrue(RotasPublicas.ehPublica(HttpMethod.GET, "/docs"));
    }

    @Test
    void rotasDePedidoSaoProtegidas() {
        assertFalse(RotasPublicas.ehPublica(HttpMethod.POST, "/pedidos"));
        assertFalse(RotasPublicas.ehPublica(HttpMethod.GET, "/pedidos/1/status"));
        assertFalse(RotasPublicas.ehPublica(HttpMethod.GET, "/cardapio/1"));
    }

    @Test
    void listagemDeEntreguesEhPublica() {
        assertTrue(RotasPublicas.ehPublica(HttpMethod.GET, "/pedidos/entregues"));
        assertTrue(RotasPublicas.ehPublica(HttpMethod.GET, "/pedidos/entregues/9001"));
    }
}
