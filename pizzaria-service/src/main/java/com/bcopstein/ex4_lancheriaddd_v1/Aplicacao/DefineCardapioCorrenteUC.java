package com.bcopstein.ex4_lancheriaddd_v1.Aplicacao;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Requests.DefineCardapioCorrenteRequest;
import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Responses.CardapioListaResponse;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Cardapio;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos.CardapioService;

/**
 * UC2 (Adm) — Definir o cardápio corrente.
 * 
 * Permite ao administrador escolher qual cardápio é o ativo no momento.
 * Apenas um cardápio pode ser corrente por vez.
 */
@Service
@Transactional
public class DefineCardapioCorrenteUC {

    private final CardapioService cardapioService;

    public DefineCardapioCorrenteUC(CardapioService cardapioService) {
        this.cardapioService = cardapioService;
    }

    public CardapioListaResponse run(DefineCardapioCorrenteRequest req) {
        Cardapio cardapioAtualizado = cardapioService.defineCorrente(req.getCardapioId());
        return new CardapioListaResponse(
            cardapioAtualizado.getId(),
            cardapioAtualizado.getTitulo(),
            cardapioAtualizado.isCorrente()
        );
    }
}
