package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Servicos;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.ItemPedido;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos.IEstoqueService;

/**
 * Implementação fake do serviço de estoque.
 * Responde sempre que o estoque é suficiente.
 */
@Service
public class EstoqueServiceFake implements IEstoqueService {

    @Override
    public List<String> verificaEDeduzEstoque(List<ItemPedido> itens) {
        // Fake: estoque sempre disponível
        return new ArrayList<>();
    }
}
