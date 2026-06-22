package com.bcopstein.ex4_lancheriaddd_v1.Aplicacao;

import org.springframework.stereotype.Service;

import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Requests.DefinePoliticaDescontoRequest;
import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Responses.PoliticaDescontoResponse;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos.DescontosServiceSelector;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos.IDescontosService;

/**
 * UC4 (Adm) — Definir a política de desconto corrente.
 * 
 * Permite ao administrador escolher qual estratégia de desconto será aplicada aos pedidos.
 */
@Service
public class DefinePoliticaDescontoUC {

    private final DescontosServiceSelector descontosService;

    public DefinePoliticaDescontoUC(DescontosServiceSelector descontosService) {
        this.descontosService = descontosService;
    }

    public PoliticaDescontoResponse run(DefinePoliticaDescontoRequest req) {
        IDescontosService novaPolitica = descontosService.defineCorrente(req.getCodigo());
        return new PoliticaDescontoResponse(
            novaPolitica.getCodigo(), 
            novaPolitica.getDescricao(), 
            novaPolitica.getLei()
        );
    }
}
