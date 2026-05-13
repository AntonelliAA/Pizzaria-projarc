package com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos;

import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Cliente;

/**
 * Serviço de cálculo de descontos e fidelidade.
 * Projetado para facilitar mudanças frequentes na política de desconto.
 * Regra atual: 7% por item para clientes com mais de 3 pedidos nos últimos 20 dias.
 */
public interface IDescontosService {
    double calculaDesconto(Cliente cliente, double valorBase);
}
