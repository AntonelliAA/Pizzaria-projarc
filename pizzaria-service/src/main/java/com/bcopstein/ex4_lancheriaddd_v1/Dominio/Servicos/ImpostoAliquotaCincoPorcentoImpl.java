package com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos;

import org.springframework.stereotype.Service;

/**
 * Implementação do serviço de impostos (5%).
 */
@Service
public class ImpostoAliquotaCincoPorcentoImpl implements IImpostosService {

    private static final double ALIQUOTA = 0.05;
    public static final String LEI = "Lei5Porcento";

    @Override
    public double calculaImposto(double valorBase) {
        return valorBase * ALIQUOTA;
    }

    @Override
    public String getLei() {
        return LEI;
    }
}
