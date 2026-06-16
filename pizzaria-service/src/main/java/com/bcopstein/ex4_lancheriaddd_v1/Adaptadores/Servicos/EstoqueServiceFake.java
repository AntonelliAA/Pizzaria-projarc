package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Servicos;

import org.springframework.stereotype.Service;

import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Produto;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos.IEstoqueService;

/**
 * Implementação fake do serviço de estoque.
 * Responde sempre que o estoque é suficiente, conforme simplificação prevista no enunciado.
 * Troca futura pela implementação real via injeção de dependência (basta registrar outro @Service
 * com @Primary ou usar qualificador).
 */
@Service
public class EstoqueServiceFake implements IEstoqueService {

    @Override
    public boolean verificaDisponibilidade(Produto produto, int quantidade) {
        // Fake: estoque sempre disponível
        return true;
    }
}
