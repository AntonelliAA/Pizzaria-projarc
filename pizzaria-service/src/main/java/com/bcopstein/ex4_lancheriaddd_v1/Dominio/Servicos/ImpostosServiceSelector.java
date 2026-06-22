package com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * Seletor da estratégia de impostos.
 * O serviço ativo é definido pela variável de ambiente/propriedade 'imposto.politica'.
 */
@Service
@Primary
public class ImpostosServiceSelector implements IImpostosService {

    private final IImpostosService estrategiaAtiva;

    public ImpostosServiceSelector(
            ImpostoAliquotaDezPorcentoImpl imposto10,
            ImpostoAliquotaCincoPorcentoImpl imposto5,
            @Value("${imposto.politica:Lei10Porcento}") String politicaImposto) {

        List<IImpostosService> disponiveis = List.of(imposto10, imposto5);
        
        this.estrategiaAtiva = disponiveis.stream()
                .filter(i -> i.getLei().equalsIgnoreCase(politicaImposto))
                .findFirst()
                .orElse(imposto10); // fallback para 10%
    }

    @Override
    public double calculaImposto(double valorBase) {
        return estrategiaAtiva.calculaImposto(valorBase);
    }

    @Override
    public String getLei() {
        return estrategiaAtiva.getLei();
    }
}
