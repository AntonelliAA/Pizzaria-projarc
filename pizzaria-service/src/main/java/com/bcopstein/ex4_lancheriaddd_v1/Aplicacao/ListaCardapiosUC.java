package com.bcopstein.ex4_lancheriaddd_v1.Aplicacao;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Responses.CardapioListaResponse;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Cardapio;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos.CardapioService;

/**
 * UC1 (Adm) — Listar cardápios disponíveis.
 * 
 * Retorna a lista de todos os cardápios cadastrados, indicando qual é o corrente.
 */
@Service
@Transactional(readOnly = true)
public class ListaCardapiosUC {

    private final CardapioService cardapioService;

    public ListaCardapiosUC(CardapioService cardapioService) {
        this.cardapioService = cardapioService;
    }

    public List<CardapioListaResponse> run() {
        return cardapioService.recuperaTodos()
            .stream()
            .map(this::toResponse)
            .toList();
    }

    private CardapioListaResponse toResponse(Cardapio cardapio) {
        return new CardapioListaResponse(
            cardapio.getId(),
            cardapio.getTitulo(),
            cardapio.isCorrente()
        );
    }
}
