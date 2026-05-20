package com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados;

import java.util.Optional;

import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Pedido;

public interface PedidosRepository {
    Pedido salva(Pedido p);
    Optional<Pedido> recuperaPorId(Long id);
    long contaPedidosClienteNosUltimosDias(String cpf, int dias);
    void atualizaStatus(Long id, Pedido.Status novoStatus);
}

