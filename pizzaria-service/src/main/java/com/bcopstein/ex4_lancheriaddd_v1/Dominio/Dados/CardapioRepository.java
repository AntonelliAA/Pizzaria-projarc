package com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados;

import java.util.List;
import java.util.Optional;

import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.CabecalhoCardapio;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Cardapio;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Produto;

public interface CardapioRepository {
    List<CabecalhoCardapio> cardapiosDisponiveis();
    Cardapio recuperaPorId(long id);
    List<Produto> indicacoesDoChef();

    /**
     * Retorna todos os cardápios com seus dados completos (incluindo status de corrente).
     */
    List<Cardapio> recuperaTodos();

    /**
     * Retorna o cardápio corrente (marcado como corrente = true).
     */
    Optional<Cardapio> recuperaCorrente();

    /**
     * Salva um cardápio (para atualizar o status de corrente).
     */
    Cardapio salva(Cardapio cardapio);
}
