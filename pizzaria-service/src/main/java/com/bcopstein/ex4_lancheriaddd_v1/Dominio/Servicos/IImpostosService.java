package com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos;

/**
 * Serviço de cálculo de impostos.
 * Projetado para facilitar mudanças frequentes na fórmula de cálculo.
 */
public interface IImpostosService {
    double calculaImposto(double valorBase);
    String getLei();
}
