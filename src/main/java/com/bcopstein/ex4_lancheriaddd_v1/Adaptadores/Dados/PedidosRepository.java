package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Dados;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Pedido;

@Repository
public interface PedidosRepository extends JpaRepository<Pedido, Long>,
        com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.PedidosRepository {

    @Override
    default Pedido salva(Pedido p) {
        return save(p);
    }

    @Override
    default Optional<Pedido> recuperaPorId(Long id) {
        return findById(id);
    }

    /**
     * Conta pedidos entregues ao cliente no período de referência.
     * Utilizado pela política de desconto por fidelidade.
     */
    @Query("SELECT COUNT(p) FROM Pedido p WHERE p.cliente.cpf = :cpf " +
           "AND p.dataHoraCriacao >= :dataLimite")
    long contaPedidosClienteNoPeriodo(@Param("cpf") String cpf,
                                      @Param("dataLimite") LocalDateTime dataLimite);

    @Override
    default long contaPedidosClienteNosUltimosDias(String cpf, int dias) {
        return contaPedidosClienteNoPeriodo(cpf, LocalDateTime.now().minusDays(dias));
    }

    @Override
    default void atualizaStatus(Long id, Pedido.Status novoStatus) {
        findById(id).ifPresent(p -> {
            p.setStatus(novoStatus);
            save(p);
        });
    }
}
