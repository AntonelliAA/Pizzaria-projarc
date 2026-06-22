package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Servicos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Ingrediente;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.ItemPedido;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos.IEstoqueService;

@Service
@Primary
public class EstoqueServiceRest implements IEstoqueService {

    private final RestTemplate restTemplate;
    private final String estoqueUrl = "http://estoque-service/estoque/verificar";

    public EstoqueServiceRest(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public List<String> verificaEDeduzEstoque(List<ItemPedido> itens) {
        // Calcula a quantidade total de cada ingrediente necessário
        Map<Long, Integer> totais = new HashMap<>();
        
        for (ItemPedido item : itens) {
            int qtdProduto = item.getQuantidade();
            List<Ingrediente> ingredientes = item.getItem().getReceita().getIngredientes();
            
            for (Ingrediente ingrediente : ingredientes) {
                totais.merge(ingrediente.getId(), qtdProduto, Integer::sum);
            }
        }

        // Monta o request
        List<Map<String, Object>> requestItens = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : totais.entrySet()) {
            Map<String, Object> reqItem = new HashMap<>();
            reqItem.put("ingredienteId", entry.getKey());
            reqItem.put("quantidade", entry.getValue());
            requestItens.add(reqItem);
        }
        
        Map<String, Object> body = new HashMap<>();
        body.put("itens", requestItens);

        // Faz a chamada REST
        try {
            VerificaEstoqueResponse response = restTemplate.postForObject(
                    estoqueUrl, body, VerificaEstoqueResponse.class);
            
            if (response != null && !response.isDisponivel()) {
                return response.getItensIndisponiveis();
            }
            return new ArrayList<>();
            
        } catch (Exception e) {
            System.err.println("Erro ao chamar estoque-service: " + e.getMessage());
            // Em caso de falha de comunicação, recusa tudo ou lança exception.
            // Vamos retornar uma string genérica para falhar o pedido.
            return List.of("SERVICO_ESTOQUE_INDISPONIVEL");
        }
    }

    // Classe auxiliar para mapear a resposta
    public static class VerificaEstoqueResponse {
        private boolean disponivel;
        private List<String> itensIndisponiveis;
        public boolean isDisponivel() { return disponivel; }
        public void setDisponivel(boolean disponivel) { this.disponivel = disponivel; }
        public List<String> getItensIndisponiveis() { return itensIndisponiveis; }
        public void setItensIndisponiveis(List<String> itensIndisponiveis) { this.itensIndisponiveis = itensIndisponiveis; }
    }
}
