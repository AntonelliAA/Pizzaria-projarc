package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Servicos;

import org.springframework.stereotype.Service;

import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.PedidosRepository;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Cliente;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos.IDescontosService;

/**
 * Estratégia de desconto por fidelidade.
 * Regra: 7% para clientes com mais de 3 pedidos nos últimos 20 dias.
 * Lei: "Lei da Fidelidade - 001"
 */
@Service("descontoFidelidade")
public class DescontoFidelidadeImpl implements IDescontosService {

    private static final double TAXA_DESCONTO = 0.07;
    private static final int LIMITE_PEDIDOS = 3;
    private static final int JANELA_DIAS = 20;

    private final PedidosRepository pedidosRepository;

    public DescontoFidelidadeImpl(PedidosRepository pedidosRepository) {
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

    @Override
    public String getCodigo() {
        return "fidelidade";
    }

    @Override
    public String getDescricao() {
        return "Desconto de 7% para clientes com mais de 3 pedidos nos últimos 20 dias";
    }

    @Override
    public String getLei() {
        return "Lei da Fidelidade - 001";
    }
}
