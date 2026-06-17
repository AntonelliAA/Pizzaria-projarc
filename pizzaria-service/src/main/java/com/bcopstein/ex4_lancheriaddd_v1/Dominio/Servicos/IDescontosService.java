package com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos;

import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Cliente;

/**
 * Serviço de cálculo de descontos e fidelidade.
 * Projetado para facilitar mudanças frequentes na política de desconto.
 * Implementações: DescontoFidelidadeImpl, DescontoPromocaoVeraoImpl, DescontoSemDescontoImpl
 */
public interface IDescontosService {
    /**
     * Calcula o desconto a ser aplicado.
     * @param cliente Cliente para o qual calcular o desconto
     * @param valorBase Valor base para cálculo
     * @return Valor de desconto a ser subtraído
     */
    double calculaDesconto(Cliente cliente, double valorBase);

    /**
     * Retorna o código identificador da estratégia.
     * Exemplo: "fidelidade", "promocao_verao", "sem_desconto"
     */
    String getCodigo();

    /**
     * Retorna a descrição legível da estratégia.
     * Exemplo: "Desconto de 7% para clientes com mais de 3 pedidos..."
     */
    String getDescricao();

    /**
     * Retorna o número/identificação da lei ou política.
     * Exemplo: "Lei da Fidelidade - 001"
     */
    String getLei();
}
