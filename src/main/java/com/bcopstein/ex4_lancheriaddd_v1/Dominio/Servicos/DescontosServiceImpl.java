package com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos;

import org.springframework.stereotype.Service;

import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.PedidosRepository;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Cliente;

/**
 * Implementação do serviço de descontos e fidelidade.
 * Regra atual: 7% por item para clientes com mais de 3 pedidos nos últimos 20 dias.
 * Para alterar a política basta modificar esta classe — o restante do sistema não é impactado.
 */
@Service
public class DescontosServiceImpl implements IDescontosService {

    private static final double TAXA_DESCONTO = 0.07;
    private static final int LIMITE_PEDIDOS = 3;
    private static final int JANELA_DIAS = 20;

    private final PedidosRepository pedidosRepository;

    public DescontosServiceImpl(PedidosRepository pedidosRepository) {
        this.pedidosRepository = pedidosRepository;
    }

    @Override
    public double calculaDesconto(Cliente cliente, double valorBase) {
        long pedidosRecentes = pedidosRepository.contaPedidosClienteNosUltimosDias(
                cliente.getCpf(), JANELA_DIAS);

        if (pedidosRecentes > LIMITE_PEDIDOS) {
            return valorBase * TAXA_DESCONTO;
        }
        return 0.0;
    }
}
