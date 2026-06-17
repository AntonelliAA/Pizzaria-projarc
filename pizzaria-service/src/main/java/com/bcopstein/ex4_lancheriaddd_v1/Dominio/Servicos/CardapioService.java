package com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.CardapioRepository;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.CabecalhoCardapio;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Cardapio;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Produto;

@Service
public class CardapioService {
    private CardapioRepository cardapioRepository;

    @Autowired
    public CardapioService(CardapioRepository cardapioRepository){
        this.cardapioRepository = cardapioRepository;
    }

    public Cardapio recuperaCardapio(long Id){
        return cardapioRepository.recuperaPorId(Id);
    }

    public List<CabecalhoCardapio> recuperaListaDeCardapios(){
        return cardapioRepository.cardapiosDisponiveis();
    }

    public List<Produto> recuperaSugestoesDoChef(){
        return cardapioRepository.indicacoesDoChef();
    }

    /**
     * UC1 — Retorna todos os cardápios com seus dados completos.
     */
    public List<Cardapio> recuperaTodos() {
        return cardapioRepository.recuperaTodos();
    }

    /**
     * UC2 — Define qual cardápio é o corrente.
     * Desativa todos os outros.
     */
    public Cardapio defineCorrente(long cardapioId) {
        // Busca o cardápio a ativar
        Cardapio cardapioAtivar = cardapioRepository.recuperaPorId(cardapioId);
        if (cardapioAtivar == null) {
            throw new IllegalArgumentException("Cardápio não encontrado: " + cardapioId);
        }

        // Desativa todos os outros cardápios
        List<Cardapio> todosCardapios = cardapioRepository.recuperaTodos();
        for (Cardapio c : todosCardapios) {
            if (c.getId() != cardapioId && c.isCorrente()) {
                c.setCorrente(false);
                cardapioRepository.salva(c);
            }
        }

        // Ativa o selecionado
        cardapioAtivar.setCorrente(true);
        return cardapioRepository.salva(cardapioAtivar);
    }

    /**
     * UC5 — Retorna o cardápio corrente.
     */
    public Cardapio recuperaCorrente() {
        Optional<Cardapio> oc = cardapioRepository.recuperaCorrente();
        return oc.orElseThrow(() -> new IllegalArgumentException("Nenhum cardápio definido como corrente"));
    }
}
