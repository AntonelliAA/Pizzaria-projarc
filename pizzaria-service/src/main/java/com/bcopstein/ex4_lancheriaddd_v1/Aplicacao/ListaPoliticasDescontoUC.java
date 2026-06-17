package com.bcopstein.ex4_lancheriaddd_v1.Aplicacao;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Responses.ListaPoliticasDescontoResponse;
import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Responses.PoliticaDescontoResponse;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos.DescontosServiceSelector;

/**
 * UC3 (Adm) — Listar as políticas de desconto disponíveis.
 *
 * Acessa o serviço de domínio de descontos (DescontosServiceSelector), que
 * conhece as estratégias e a política corrente. O caso de uso não depende de
 * nenhuma implementação concreta nem da camada de Adaptadores.
 */
@Service
@Transactional(readOnly = true)
public class ListaPoliticasDescontoUC {

    private final DescontosServiceSelector descontosService;

    public ListaPoliticasDescontoUC(DescontosServiceSelector descontosService) {
        this.descontosService = descontosService;
    }

    public ListaPoliticasDescontoResponse run() {
        List<PoliticaDescontoResponse> politicas = descontosService.listarDisponiveis()
                .stream()
                .map(d -> new PoliticaDescontoResponse(d.getCodigo(), d.getDescricao(), d.getLei()))
                .toList();

        return new ListaPoliticasDescontoResponse(politicas, descontosService.getCodigo());
    }
}
