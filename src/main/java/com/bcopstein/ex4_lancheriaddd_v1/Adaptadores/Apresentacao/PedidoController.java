package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Apresentacao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.CancelaPedidoUC;
import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.ConsultaStatusPedidoUC;
import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.CriaPedidoUC;
import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.ListaPedidosEntreguesClienteUC;
import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.ListaPedidosEntreguesUC;
import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.PagarPedidoUC;
import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Requests.PedidoRequest;
import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Responses.PedidoEntregueResponse;
import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Responses.PedidoResponse;
import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Responses.StatusPedidoResponse;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Pedido;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
    private final ConsultaStatusPedidoUC consultaStatusPedidoUC;
    private final CancelaPedidoUC cancelaPedidoUC;
    private final PagarPedidoUC pagarPedidoUC;
    private final ListaPedidosEntreguesUC listaPedidosEntreguesUC;
    private final ListaPedidosEntreguesClienteUC listaPedidosEntreguesClienteUC;

    public PedidoController(CriaPedidoUC criaPedidoUC,
                            ConsultaStatusPedidoUC consultaStatusPedidoUC,
                            CancelaPedidoUC cancelaPedidoUC,
                            PagarPedidoUC pagarPedidoUC,
                            ListaPedidosEntreguesUC listaPedidosEntreguesUC,
                            ListaPedidosEntreguesClienteUC listaPedidosEntreguesClienteUC) {
        this.criaPedidoUC = criaPedidoUC;
        this.consultaStatusPedidoUC = consultaStatusPedidoUC;
        this.cancelaPedidoUC = cancelaPedidoUC;
        this.pagarPedidoUC = pagarPedidoUC;
        this.listaPedidosEntreguesUC = listaPedidosEntreguesUC;
        this.listaPedidosEntreguesClienteUC = listaPedidosEntreguesClienteUC;
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

        List<String> indisponiveis = p.getItens().stream()
                .filter(item -> !item.getItem().isDisponivel())
                .map(item -> item.getItem().getDescricao())
                .collect(Collectors.toList());

        PedidoResponse resp = new PedidoResponse(p.getId(), p.getStatus().name(), indisponiveis);
        return ResponseEntity.unprocessableEntity().body(resp);
    }

    /**
     * UC5 — Solicitar status de pedido.
     * Retorna o status atual do pedido a partir do seu ID.
     */
    @GetMapping("/{id}/status")
    @CrossOrigin("*")
    @Operation(summary = "Consultar status do pedido",
               description = "Retorna o status atual, data de criação, endereço de entrega e valor cobrado do pedido.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Status do pedido retornado com sucesso",
                     content = @Content(schema = @Schema(implementation = StatusPedidoResponse.class))),
        @ApiResponse(responseCode = "400", description = "Pedido não encontrado")
    })
    // TODO: proteger com autenticação (UC2)
    public ResponseEntity<StatusPedidoResponse> consultarStatus(
            @Parameter(description = "ID do pedido", example = "1")
            @PathVariable Long id) {
        StatusPedidoResponse resp = consultaStatusPedidoUC.run(id);
        return ResponseEntity.ok(resp);
    }

    /**
     * UC6 — Cancelar pedido aprovado.
     * Só é possível cancelar pedidos com status APROVADO (não pagos).
     */
    @PutMapping("/{id}/cancelar")
    @CrossOrigin("*")
    @Operation(summary = "Cancelar pedido",
               description = "Cancela um pedido aprovado. Pedidos pagos ou em outro status não podem ser cancelados.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pedido cancelado com sucesso",
                     content = @Content(schema = @Schema(implementation = StatusPedidoResponse.class))),
        @ApiResponse(responseCode = "400", description = "Pedido não encontrado"),
        @ApiResponse(responseCode = "422", description = "Pedido não pode ser cancelado (status inválido)")
    })
    // TODO: proteger com autenticação (UC2)
    public ResponseEntity<StatusPedidoResponse> cancelarPedido(
            @Parameter(description = "ID do pedido a cancelar", example = "1")
            @PathVariable Long id) {
        StatusPedidoResponse resp = cancelaPedidoUC.run(id);
        return ResponseEntity.ok(resp);
    }

    /**
     * UC7 — Pagar pedido.
     * Só é possível pagar pedidos com status APROVADO.
     */
    @PutMapping("/{id}/pagar")
    @CrossOrigin("*")
    @Operation(summary = "Pagar pedido",
               description = "Processa o pagamento de um pedido aprovado e o envia para a cozinha.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pedido pago com sucesso",
                     content = @Content(schema = @Schema(implementation = StatusPedidoResponse.class))),
        @ApiResponse(responseCode = "400", description = "Pedido não encontrado"),
        @ApiResponse(responseCode = "422", description = "Pedido não pode ser pago (status inválido)")
    })
    public ResponseEntity<StatusPedidoResponse> pagarPedido(
            @Parameter(description = "ID do pedido a pagar", example = "1")
            @PathVariable Long id) {
        StatusPedidoResponse resp = pagarPedidoUC.run(id);
        return ResponseEntity.ok(resp);
    }

    // ─── UC8 — Listar pedidos entregues entre duas datas ────────────────

    /**
     * UC8 — Listar pedidos entregues entre duas datas.
     * Retorna todos os pedidos com status ENTREGUE no intervalo informado.
     */
    @GetMapping("/entregues")
    @CrossOrigin("*")
    @Operation(summary = "Listar pedidos entregues entre datas",
               description = "Retorna todos os pedidos com status ENTREGUE cujas datas de criação " +
                             "estejam no intervalo [inicio, fim]. Formato: yyyy-MM-ddTHH:mm:ss")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de pedidos entregues retornada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Parâmetros de data inválidos")
    })
    public ResponseEntity<List<PedidoEntregueResponse>> listarEntregues(
            @Parameter(description = "Data/hora de início (ISO)", example = "2026-01-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @Parameter(description = "Data/hora de fim (ISO)", example = "2026-12-31T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {

        List<PedidoEntregueResponse> resp = listaPedidosEntreguesUC.run(inicio, fim);
        return ResponseEntity.ok(resp);
    }

    // ─── UC9 — Listar pedidos de um cliente entregues entre duas datas ──

    /**
     * UC9 — Listar pedidos de um cliente entregues entre duas datas.
     * Retorna os pedidos entregues do cliente identificado pelo CPF no intervalo informado.
     */
    @GetMapping("/entregues/{cpf}")
    @CrossOrigin("*")
    @Operation(summary = "Listar pedidos entregues de um cliente entre datas",
               description = "Retorna os pedidos com status ENTREGUE do cliente identificado pelo CPF, " +
                             "cujas datas de criação estejam no intervalo [inicio, fim]. Formato: yyyy-MM-ddTHH:mm:ss")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de pedidos entregues do cliente retornada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Cliente não encontrado ou parâmetros inválidos")
    })
    public ResponseEntity<List<PedidoEntregueResponse>> listarEntreguesCliente(
            @Parameter(description = "CPF do cliente", example = "9001")
            @PathVariable String cpf,
            @Parameter(description = "Data/hora de início (ISO)", example = "2026-01-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @Parameter(description = "Data/hora de fim (ISO)", example = "2026-12-31T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {

        List<PedidoEntregueResponse> resp = listaPedidosEntreguesClienteUC.run(cpf, inicio, fim);
        return ResponseEntity.ok(resp);
    }
}
