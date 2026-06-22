package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Apresentacao;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Pedido;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos.PedidoService;

/**
 * Endpoint INTERNO de atualização de status de pedido.
 *
 * Consumido pelo microsserviço de entregas (via Eureka/REST) para registrar as
 * mudanças de estado do pedido (TRANSPORTE, ENTREGUE) no banco da pizzaria.
 * Não é roteado pelo gateway — uso service-to-service apenas.
 */
@RestController
@RequestMapping("/internal/pedidos")
public class InternalPedidoController {

    private final PedidoService pedidoService;

    public InternalPedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Void> atualizaStatus(@PathVariable Long id,
                                               @RequestParam String status) {
        Pedido.Status novoStatus;
        try {
            novoStatus = Pedido.Status.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
        pedidoService.atualizaStatus(id, novoStatus);
        return ResponseEntity.ok().build();
    }
}
