package com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos;

import org.springframework.stereotype.Service;

import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Cliente;

/**
 * Estratégia de nenhum desconto.
 * Regra: 0% — sem desconto algum.
 * Lei: "Sem Desconto - 000"
 */
@Service("descontoSemDesconto")
public class DescontoSemDescontoImpl implements IDescontosService {

    @Override
    public double calculaDesconto(Cliente cliente, double valorBase) {
        return 0.0;
    }

    @Override
    public String getCodigo() {
        return "sem_desconto";
    }

    @Override
    public String getDescricao() {
        return "Nenhum desconto";
    }

    @Override
    public String getLei() {
        return "Sem Desconto - 000";
    }
}
