package com.bcopstein.ex4_lancheriaddd_v1.Aplicacao;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Requests.ItemPedidoRequest;
import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Requests.PedidoRequest;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Cliente;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.ItemPedido;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Pedido;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Produto;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos.ClienteService;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos.IDescontosService;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos.IEstoqueService;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos.IImpostosService;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos.PedidoService;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos.ProdutoService;

/**
 * UC4 — Submeter pedido para aprovação.
 *
 * Fluxo:
 *  1. Valida cliente e produtos.
 *  2. Verifica disponibilidade no estoque para cada item.
 *  3a. Sem estoque → salva pedido como NOVO, marca produtos indisponíveis no cardápio,
 *      retorna listagem dos itens que não puderam ser atendidos.
 *  3b. Estoque OK → calcula valor bruto, desconto (fidelidade) e imposto,
 *      salva pedido como APROVADO e retorna o preço detalhado.
 */
@Service
@Transactional
public class CriaPedidoUC {

    private final ProdutoService produtoService;
    private final ClienteService clienteService;
    private final PedidoService pedidoService;
    private final IEstoqueService estoqueService;
    private final IImpostosService impostosService;
    private final IDescontosService descontosService;

    public CriaPedidoUC(ProdutoService produtoService,
                        ClienteService clienteService,
                        PedidoService pedidoService,
                        IEstoqueService estoqueService,
                        IImpostosService impostosService,
                        IDescontosService descontosService) {
        this.produtoService = produtoService;
        this.clienteService = clienteService;
        this.pedidoService = pedidoService;
        this.estoqueService = estoqueService;
        this.impostosService = impostosService;
        this.descontosService = descontosService;
    }

    public Pedido run(PedidoRequest req) {
        Cliente cliente = clienteService.recuperaPorCpf(req.getClienteCpf())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Cliente não encontrado: " + req.getClienteCpf()));

        List<ItemPedido> itens = montaItens(req.getItens());
        List<String> indisponiveis = verificaEstoque(itens);

        if (!indisponiveis.isEmpty()) {
            marcarProdutosIndisponiveis(itens, indisponiveis);

            return new Pedido(
                    cliente,
                    LocalDateTime.now(),
                    req.getEnderecoEntrega(),
                    itens,
                    Pedido.Status.NOVO,
                    0.0, 0.0, 0.0, 0.0
            );
        }

        double valor = calculaValorBruto(itens);
        double desconto = descontosService.calculaDesconto(cliente, valor);
        double impostos = impostosService.calculaImposto(valor);
        double valorCobrado = valor - desconto + impostos;

        Pedido pedidoAprovado = new Pedido(
                cliente,
                LocalDateTime.now(),
                req.getEnderecoEntrega(),
                itens,
                Pedido.Status.APROVADO,
                valor,
                impostos,
                desconto,
                valorCobrado
        );
        return pedidoService.salva(pedidoAprovado);
    }

    private List<ItemPedido> montaItens(List<ItemPedidoRequest> requests) {
        return requests.stream().map(ipr -> {
            Produto p = produtoService.recuperaProdutoPorId(ipr.getProdutoId());
            if (p == null) throw new IllegalArgumentException(
                    "Produto não encontrado: " + ipr.getProdutoId());
            return new ItemPedido(p, ipr.getQuantidade());
        }).collect(Collectors.toList());
    }

    private List<String> verificaEstoque(List<ItemPedido> itens) {
        return estoqueService.verificaEDeduzEstoque(itens);
    }

    private void marcarProdutosIndisponiveis(List<ItemPedido> itens, List<String> indisponiveis) {
        itens.stream()
                .filter(item -> indisponiveis.contains(item.getItem().getDescricao()))
                .forEach(item -> {
                    item.getItem().marcarComoIndisponivel();
                    produtoService.marcaComoIndisponivel(item.getItem().getId());
                });
    }

    private double calculaValorBruto(List<ItemPedido> itens) {
        return itens.stream()
                .mapToDouble(item -> (double) item.getItem().getPreco() * item.getQuantidade())
                .sum();
    }
}
