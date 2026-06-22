package com.bcopstein.estoque;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/estoque")
public class EstoqueController {

    private final ItemEstoqueRepository repository;

    public EstoqueController(ItemEstoqueRepository repository) {
        this.repository = repository;
    }

    @PostMapping("/verificar")
    @Transactional
    public ResponseEntity<VerificaEstoqueResponse> verificar(@RequestBody VerificaEstoqueRequest req) {
        List<String> indisponiveis = new ArrayList<>();
        
        // Verifica cada item solicitado
        for (VerificaEstoqueRequest.ItemRequest itemReq : req.getItens()) {
            Optional<ItemEstoque> opt = repository.findById(itemReq.getIngredienteId());
            if (opt.isEmpty()) {
                indisponiveis.add("Item desconhecido ID: " + itemReq.getIngredienteId());
                continue;
            }
            ItemEstoque item = opt.get();
            if (item.getQuantidade() < itemReq.getQuantidade()) {
                indisponiveis.add(item.getDescricao());
            }
        }

        if (!indisponiveis.isEmpty()) {
            return ResponseEntity.ok(new VerificaEstoqueResponse(false, indisponiveis));
        }

        // Se chegou aqui, todos estão disponíveis. Deduz do estoque (se essa for a regra).
        // Como o enunciado exige que o BD seja afetado de verdade agora, vamos dar baixa.
        for (VerificaEstoqueRequest.ItemRequest itemReq : req.getItens()) {
            ItemEstoque item = repository.findById(itemReq.getIngredienteId()).get();
            item.reduzirQuantidade(itemReq.getQuantidade());
            repository.save(item);
        }

        return ResponseEntity.ok(new VerificaEstoqueResponse(true, new ArrayList<>()));
    }
}
