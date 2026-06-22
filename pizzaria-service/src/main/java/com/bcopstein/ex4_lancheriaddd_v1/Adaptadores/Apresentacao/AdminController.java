package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Apresentacao;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.DefineCardapioCorrenteUC;
import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.ListaCardapiosUC;
import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.ListaPoliticasDescontoUC;
import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Requests.DefineCardapioCorrenteRequest;
import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Requests.DefinePoliticaDescontoRequest;
import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Responses.CardapioListaResponse;
import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Responses.ListaPoliticasDescontoResponse;
import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Responses.PoliticaDescontoResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Controller de administração da pizzaria.
 * Endpoints para gerenciar cardápios e políticas de desconto.
 * 
 * UC1: GET /admin/cardapios — Listar cardápios disponíveis
 * UC2: PUT /admin/cardapios/definir-corrente — Definir cardápio corrente
 * UC3: GET /admin/descontos/politicas — Listar políticas de desconto disponíveis
 */
@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*")
@Tag(name = "Administração", description = "Operações de administração da pizzaria")
public class AdminController {

    private final ListaCardapiosUC listaCardapiosUC;
    private final DefineCardapioCorrenteUC defineCardapioCorrenteUC;
    private final ListaPoliticasDescontoUC listaPoliticasDescontoUC;
    private final com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.DefinePoliticaDescontoUC definePoliticaDescontoUC;

    public AdminController(
            ListaCardapiosUC listaCardapiosUC,
            DefineCardapioCorrenteUC defineCardapioCorrenteUC,
            ListaPoliticasDescontoUC listaPoliticasDescontoUC,
            com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.DefinePoliticaDescontoUC definePoliticaDescontoUC) {
        this.listaCardapiosUC = listaCardapiosUC;
        this.defineCardapioCorrenteUC = defineCardapioCorrenteUC;
        this.listaPoliticasDescontoUC = listaPoliticasDescontoUC;
        this.definePoliticaDescontoUC = definePoliticaDescontoUC;
    }

    /**
     * UC1 — Listar cardápios disponíveis.
     * Retorna todos os cardápios cadastrados com indicação de qual é o corrente.
     */
    @GetMapping("/cardapios")
    @Operation(summary = "UC1 — Listar cardápios disponíveis",
               description = "Retorna a lista de todos os cardápios cadastrados, indicando qual é o corrente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cardápios retornados com sucesso",
                     content = @Content(mediaType = "application/json",
                                        schema = @Schema(implementation = CardapioListaResponse.class))),
        @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    public ResponseEntity<List<CardapioListaResponse>> listaCardapios() {
        List<CardapioListaResponse> cardapios = listaCardapiosUC.run();
        return ResponseEntity.ok(cardapios);
    }

    /**
     * UC2 — Definir o cardápio corrente.
     * Permite ao admin escolher qual cardápio é ativo no momento.
     */
    @PutMapping("/cardapios/definir-corrente")
    @Operation(summary = "UC2 — Definir o cardápio corrente",
               description = "Define qual cardápio será o ativo. Apenas um cardápio pode ser corrente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cardápio definido como corrente",
                     content = @Content(mediaType = "application/json",
                                        schema = @Schema(implementation = CardapioListaResponse.class))),
        @ApiResponse(responseCode = "400", description = "Cardápio ID inválido"),
        @ApiResponse(responseCode = "404", description = "Cardápio não encontrado"),
        @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    public ResponseEntity<CardapioListaResponse> defineCardapioCorrente(
            @Valid @RequestBody DefineCardapioCorrenteRequest req) {
        CardapioListaResponse resultado = defineCardapioCorrenteUC.run(req);
        return ResponseEntity.ok(resultado);
    }

    /**
     * UC3 — Listar as políticas de desconto disponíveis.
     * Retorna todas as estratégias de desconto e qual está ativa.
     */
    @GetMapping("/descontos/politicas")
    @Operation(summary = "UC3 — Listar as políticas de desconto disponíveis",
               description = "Retorna todas as estratégias de desconto cadastradas e qual é a corrente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Políticas de desconto retornadas",
                     content = @Content(mediaType = "application/json",
                                        schema = @Schema(implementation = ListaPoliticasDescontoResponse.class))),
        @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    public ResponseEntity<ListaPoliticasDescontoResponse> listaPoliticasDesconto() {
        ListaPoliticasDescontoResponse resultado = listaPoliticasDescontoUC.run();
        return ResponseEntity.ok(resultado);
    }

    /**
     * UC4 — Definir a política de desconto corrente.
     * Permite ao admin escolher qual política de desconto é ativa no momento.
     */
    @PutMapping("/descontos/definir-corrente")
    @Operation(summary = "UC4 — Definir a política de desconto corrente",
               description = "Define qual política de desconto será ativa a partir de agora")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Política de desconto definida como corrente",
                     content = @Content(mediaType = "application/json",
                                        schema = @Schema(implementation = PoliticaDescontoResponse.class))),
        @ApiResponse(responseCode = "400", description = "Código inválido ou desconhecido"),
        @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    public ResponseEntity<PoliticaDescontoResponse> definePoliticaDesconto(
            @Valid @RequestBody DefinePoliticaDescontoRequest req) {
        try {
            PoliticaDescontoResponse resultado = definePoliticaDescontoUC.run(req);
            return ResponseEntity.ok(resultado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
