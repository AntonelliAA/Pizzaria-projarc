package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Dados;

import java.time.LocalDateTime;
import java.util.List;
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

    // ─── UC8 — Pedidos entregues entre datas ────────────────────────────

    @Query("SELECT p FROM Pedido p WHERE p.status = 'ENTREGUE' " +
           "AND p.dataHoraCriacao BETWEEN :inicio AND :fim " +
           "ORDER BY p.dataHoraCriacao DESC")
    List<Pedido> findEntreguesEntreDatas(@Param("inicio") LocalDateTime inicio,
                                         @Param("fim") LocalDateTime fim);

    @Override
    default List<Pedido> recuperaEntreguesEntreDatas(LocalDateTime inicio, LocalDateTime fim) {
        return findEntreguesEntreDatas(inicio, fim);
    }

    // ─── UC9 — Pedidos entregues de um cliente entre datas ──────────────

    @Query("SELECT p FROM Pedido p WHERE p.status = 'ENTREGUE' " +
           "AND p.cliente.cpf = :cpf " +
           "AND p.dataHoraCriacao BETWEEN :inicio AND :fim " +
           "ORDER BY p.dataHoraCriacao DESC")
    List<Pedido> findEntreguesDeClienteEntreDatas(@Param("cpf") String cpf,
                                                   @Param("inicio") LocalDateTime inicio,
                                                   @Param("fim") LocalDateTime fim);

    @Override
    default List<Pedido> recuperaEntreguesDeClienteEntreDatas(String cpf,
                                                               LocalDateTime inicio,
                                                               LocalDateTime fim) {
        return findEntreguesDeClienteEntreDatas(cpf, inicio, fim);
    }
}
