package com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos;

/**
 * Serviço de cálculo de impostos.
 * Projetado para facilitar mudanças frequentes na fórmula de cálculo.
 * Regra atual: 10% sobre o somatório do custo dos itens.
 */
public interface IImpostosService {
    double calculaImposto(double valorBase);
}
