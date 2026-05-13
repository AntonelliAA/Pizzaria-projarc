package com.bcopstein.ex4_lancheriaddd_v1.Aplicacao;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Requests.ItemPedidoRequest;
import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Requests.PedidoRequest;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.ClientesRepository;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.PedidosRepository;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.ProdutosRepository;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Cliente;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.ItemPedido;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Pedido;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Produto;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos.IDescontosService;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos.IEstoqueService;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos.IImpostosService;

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

    private final ProdutosRepository produtosRepo;
    private final ClientesRepository clientesRepo;
    private final PedidosRepository pedidosRepo;
    private final IEstoqueService estoqueService;
    private final IImpostosService impostosService;
    private final IDescontosService descontosService;

    public CriaPedidoUC(ProdutosRepository produtosRepo,
                        ClientesRepository clientesRepo,
                        PedidosRepository pedidosRepo,
                        IEstoqueService estoqueService,
                        IImpostosService impostosService,
                        IDescontosService descontosService) {
        this.produtosRepo = produtosRepo;
        this.clientesRepo = clientesRepo;
        this.pedidosRepo = pedidosRepo;
        this.estoqueService = estoqueService;
        this.impostosService = impostosService;
        this.descontosService = descontosService;
    }

    public Pedido run(PedidoRequest req) {
        // 1. Recupera e valida o cliente
        Cliente cliente = clientesRepo.recuperaPorCpf(req.getClienteCpf())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Cliente não encontrado: " + req.getClienteCpf()));

        // 2. Monta os itens e valida produtos
        List<ItemPedido> itens = montaItens(req.getItens());

        // 3. Verifica disponibilidade no estoque
        List<String> indisponiveis = verificaEstoque(itens);

        if (!indisponiveis.isEmpty()) {
            // 3a. Itens sem estoque — marca produtos como indisponíveis no cardápio
            //     e retorna o pedido SEM persistir (evita lixo no banco)
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

        // 3b. Estoque OK — calcula custo e aprova o pedido
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
        return pedidosRepo.salva(pedidoAprovado);
    }

    // -------------------------------------------------------------------------
    // Helpers privados
    // -------------------------------------------------------------------------

    private List<ItemPedido> montaItens(List<ItemPedidoRequest> requests) {
        return requests.stream().map(ipr -> {
            Produto p = produtosRepo.recuperaProdutoPorid(ipr.getProdutoId());
            if (p == null) throw new IllegalArgumentException(
                    "Produto não encontrado: " + ipr.getProdutoId());
            return new ItemPedido(p, ipr.getQuantidade());
        }).collect(Collectors.toList());
    }

    private List<String> verificaEstoque(List<ItemPedido> itens) {
        List<String> indisponiveis = new ArrayList<>();
        for (ItemPedido item : itens) {
            if (!estoqueService.verificaDisponibilidade(item.getItem(), item.getQuantidade())) {
                indisponiveis.add(item.getItem().getDescricao());
            }
        }
        return indisponiveis;
    }

    private void marcarProdutosIndisponiveis(List<ItemPedido> itens, List<String> indisponiveis) {
        itens.stream()
                .filter(item -> indisponiveis.contains(item.getItem().getDescricao()))
                .forEach(item -> {
                    item.getItem().marcarComoIndisponivel();
                    produtosRepo.marcaComoIndisponivel(item.getItem().getId());
                });
    }

    private double calculaValorBruto(List<ItemPedido> itens) {
        return itens.stream()
                .mapToDouble(item -> (double) item.getItem().getPreco() * item.getQuantidade())
                .sum();
    }
}
