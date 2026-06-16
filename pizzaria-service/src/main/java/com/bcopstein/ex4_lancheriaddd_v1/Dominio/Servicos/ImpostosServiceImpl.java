package com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos;

import org.springframework.stereotype.Service;

/**
 * Implementação do serviço de impostos.
 * Regra atual: 10% sobre o somatório do custo dos itens.
 * Para alterar a fórmula basta modificar esta classe — o restante do sistema não é impactado.
 */
@Service
public class ImpostosServiceImpl implements IImpostosService {

    private static final double ALIQUOTA = 0.10;

    @Override
    public double calculaImposto(double valorBase) {
        return valorBase * ALIQUOTA;
    }
}
