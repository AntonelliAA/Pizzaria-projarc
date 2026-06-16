package com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos;

import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Produto;

/**
 * Serviço de estoque — verifica se há ingredientes suficientes
 * para preparar uma quantidade de um produto.
 * Permite troca futura por implementação real via injeção de dependência.
 */
public interface IEstoqueService {
    boolean verificaDisponibilidade(Produto produto, int quantidade);
}
