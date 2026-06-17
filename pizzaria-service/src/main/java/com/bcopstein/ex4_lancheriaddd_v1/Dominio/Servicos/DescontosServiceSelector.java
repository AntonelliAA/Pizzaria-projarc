package com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Servicos.DescontoFidelidadeImpl;
import com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Servicos.DescontoPromocaoVeraoImpl;
import com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Servicos.DescontoSemDescontoImpl;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Cliente;

/**
 * Seletor de estratégia de desconto.
 * Escolhe qual implementação de IDescontosService usar baseado em variável de ambiente.
 *
 * Exemplo em application.yaml:
 *   desconto:
 *     politica: fidelidade  # ou "promocao_verao", "sem_desconto"
 *
 * Esta classe implementa o padrão Strategy (SRP + OCP):
 * - A escolha é trocável sem alterar o resto do sistema
 * - Cada estratégia está em sua própria classe
 */
@Service
@org.springframework.context.annotation.Primary
public class DescontosServiceSelector implements IDescontosService {

    private final IDescontosService estrategiaAtiva;

    public DescontosServiceSelector(
            DescontoFidelidadeImpl descontoFidelidade,
            DescontoPromocaoVeraoImpl descontoPromocaoVerao,
            DescontoSemDescontoImpl descontoSemDesconto,
            @Value("${desconto.politica:fidelidade}") String politicaAtiva) {

        this.estrategiaAtiva = selecionaEstrategia(
                descontoFidelidade, descontoPromocaoVerao, descontoSemDesconto, politicaAtiva);
    }

    private IDescontosService selecionaEstrategia(
            DescontoFidelidadeImpl fidelidade,
            DescontoPromocaoVeraoImpl promocao,
            DescontoSemDescontoImpl semDesconto,
            String politica) {

        return switch (politica.toLowerCase().strip()) {
            case "fidelidade" -> fidelidade;
            case "promocao_verao", "promocao-verao" -> promocao;
            case "sem_desconto", "sem-desconto" -> semDesconto;
            default -> {
                System.out.println("[DESCONTO] Política desconhecida: " + politica
                        + ". Usando fidelidade como padrão.");
                yield fidelidade;
            }
        };
    }

    @Override
    public double calculaDesconto(Cliente cliente, double valorBase) {
        return estrategiaAtiva.calculaDesconto(cliente, valorBase);
    }

    @Override
    public String getCodigo() {
        return estrategiaAtiva.getCodigo();
    }

    @Override
    public String getDescricao() {
        return estrategiaAtiva.getDescricao();
    }

    @Override
    public String getLei() {
        return estrategiaAtiva.getLei();
    }
}
