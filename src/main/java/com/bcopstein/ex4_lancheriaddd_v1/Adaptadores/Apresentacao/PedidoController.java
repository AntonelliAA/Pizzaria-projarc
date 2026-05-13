package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Apresentacao;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.CriaPedidoUC;
import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Requests.PedidoRequest;
import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Responses.PedidoResponse;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Pedido;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/pedidos")
@Tag(name = "Pedidos", description = "Operações relacionadas a pedidos")
public class PedidoController {

    private final CriaPedidoUC criaPedidoUC;

    public PedidoController(CriaPedidoUC criaPedidoUC) {
        this.criaPedidoUC = criaPedidoUC;
    }

    /**
     * UC4 — Submete um pedido para aprovação.
     * Retorna 200 (APROVADO) com preço calculado ou 422 (NOVO) com itens indisponíveis.
     */
    @PostMapping
    @CrossOrigin("*")
    @Operation(summary = "Submeter pedido para aprovação",
               description = "Verifica estoque, calcula imposto e desconto. " +
                             "Retorna APROVADO com preço ou lista de itens sem estoque.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pedido aprovado com preço calculado",
                     content = @Content(schema = @Schema(implementation = PedidoResponse.class))),
        @ApiResponse(responseCode = "422", description = "Pedido negado por falta de ingredientes",
                     content = @Content(schema = @Schema(implementation = PedidoResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos")
    })
    public ResponseEntity<PedidoResponse> submeterPedido(@Valid @RequestBody PedidoRequest req) {
        Pedido p = criaPedidoUC.run(req);

        if (p.getStatus() == Pedido.Status.APROVADO) {
            PedidoResponse resp = new PedidoResponse(
                    p.getId(),
                    p.getStatus().name(),
                    p.getValor(),
                    p.getImpostos(),
                    p.getDesconto(),
                    p.getValorCobrado()
            );
            return ResponseEntity.ok(resp);
        }

        // Pedido negado por falta de estoque — não foi persistido
        List<String> indisponiveis = p.getItens().stream()
                .filter(item -> !item.getItem().isDisponivel())
                .map(item -> item.getItem().getDescricao())
                .collect(Collectors.toList());

        PedidoResponse resp = new PedidoResponse(p.getId(), p.getStatus().name(), indisponiveis);
        return ResponseEntity.unprocessableEntity().body(resp);
    }
}
