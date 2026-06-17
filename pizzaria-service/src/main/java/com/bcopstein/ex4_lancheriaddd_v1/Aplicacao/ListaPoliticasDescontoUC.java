package com.bcopstein.ex4_lancheriaddd_v1.Aplicacao;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Responses.ListaPoliticasDescontoResponse;
import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Responses.PoliticaDescontoResponse;
import com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Servicos.DescontoFidelidadeImpl;
import com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Servicos.DescontoPromocaoVeraoImpl;
import com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Servicos.DescontoSemDescontoImpl;

/**
 * UC3 (Adm) — Listar as políticas de desconto disponíveis.
 * 
 * Retorna a lista de todas as estratégias de desconto cadastradas no sistema,
 * indicando qual é a política corrente (de acordo com application.yaml).
 */
@Service
@Transactional(readOnly = true)
public class ListaPoliticasDescontoUC {

    private final DescontoFidelidadeImpl descontoFidelidade;
    private final DescontoPromocaoVeraoImpl descontoPromocao;
    private final DescontoSemDescontoImpl descontoSemDesconto;

    @Value("${desconto.politica:fidelidade}")
    private String politicaCorrente;

    public ListaPoliticasDescontoUC(
            DescontoFidelidadeImpl descontoFidelidade,
            DescontoPromocaoVeraoImpl descontoPromocao,
            DescontoSemDescontoImpl descontoSemDesconto) {
        this.descontoFidelidade = descontoFidelidade;
        this.descontoPromocao = descontoPromocao;
        this.descontoSemDesconto = descontoSemDesconto;
    }

    public ListaPoliticasDescontoResponse run() {
        List<PoliticaDescontoResponse> politicas = List.of(
            new PoliticaDescontoResponse(
                descontoFidelidade.getCodigo(),
                descontoFidelidade.getDescricao(),
                descontoFidelidade.getLei()
            ),
            new PoliticaDescontoResponse(
                descontoPromocao.getCodigo(),
                descontoPromocao.getDescricao(),
                descontoPromocao.getLei()
            ),
            new PoliticaDescontoResponse(
                descontoSemDesconto.getCodigo(),
                descontoSemDesconto.getDescricao(),
                descontoSemDesconto.getLei()
            )
        );

        return new ListaPoliticasDescontoResponse(politicas, politicaCorrente);
    }
}
