package com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.ProdutosRepository;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Produto;

/**
 * Serviço de domínio de Produtos (parte do domínio de Cardápio).
 * Os casos de uso consultam/alteram produtos por aqui, não pelo repositório direto.
 */
@Service
public class ProdutoService {
    private final ProdutosRepository produtosRepository;

    @Autowired
    public ProdutoService(ProdutosRepository produtosRepository) {
        this.produtosRepository = produtosRepository;
    }

    public Produto recuperaProdutoPorId(long id) {
        return produtosRepository.recuperaProdutoPorid(id);
    }

    public List<Produto> recuperaProdutosCardapio(long cardapioId) {
        return produtosRepository.recuperaProdutosCardapio(cardapioId);
    }

    public void marcaComoIndisponivel(long id) {
        produtosRepository.marcaComoIndisponivel(id);
    }
}
