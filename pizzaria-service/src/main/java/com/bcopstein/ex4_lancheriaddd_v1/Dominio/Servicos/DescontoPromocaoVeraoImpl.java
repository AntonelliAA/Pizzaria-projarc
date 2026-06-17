package com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos;

import org.springframework.stereotype.Service;

import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Cliente;

/**
 * Estratégia de desconto promocional — verão.
 * Regra: 10% desconto para todos, em promoção sazonal.
 * Lei: "Promoção Verão 2026 - 002"
 */
@Service("descontoPromocaoVerao")
public class DescontoPromocaoVeraoImpl implements IDescontosService {

    private static final double TAXA_DESCONTO = 0.10;

    @Override
    public double calculaDesconto(Cliente cliente, double valorBase) {
        return valorBase * TAXA_DESCONTO;
    }

    @Override
    public String getCodigo() {
        return "promocao_verao";
    }

    @Override
    public String getDescricao() {
        return "Desconto de 10% para todos - Promoção Verão";
    }

    @Override
    public String getLei() {
        return "Promoção Verão 2026 - 002";
    }
}
