package com.bcopstein.ex4_lancheriaddd_v1.Aplicacao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Responses.PedidoEntregueResponse;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.ClientesRepository;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.PedidosRepository;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Pedido;

/**
 * UC9 — Listar pedidos de um cliente entregues entre duas datas.
 *
 * Retorna todos os pedidos com status ENTREGUE do cliente identificado
 * pelo CPF, cuja data de criação esteja no intervalo [inicio, fim].
 */
@Service
public class ListaPedidosEntreguesClienteUC {

    private final PedidosRepository pedidosRepo;
    private final ClientesRepository clientesRepo;

    public ListaPedidosEntreguesClienteUC(PedidosRepository pedidosRepo,
                                           ClientesRepository clientesRepo) {
        this.pedidosRepo = pedidosRepo;
        this.clientesRepo = clientesRepo;
    }

    public List<PedidoEntregueResponse> run(String cpf, LocalDateTime inicio, LocalDateTime fim) {
        if (cpf == null || cpf.isBlank()) {
            throw new IllegalArgumentException("CPF do cliente é obrigatório");
        }
        if (inicio == null || fim == null) {
            throw new IllegalArgumentException("Datas de início e fim são obrigatórias");
        }
        if (inicio.isAfter(fim)) {
            throw new IllegalArgumentException("Data de início não pode ser posterior à data fim");
        }

        // Valida que o cliente existe
        clientesRepo.recuperaPorCpf(cpf)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Cliente não encontrado: " + cpf));

        List<Pedido> pedidos = pedidosRepo.recuperaEntreguesDeClienteEntreDatas(cpf, inicio, fim);

        return pedidos.stream()
                .map(PedidoEntregueResponse::new)
                .collect(Collectors.toList());
    }
}
