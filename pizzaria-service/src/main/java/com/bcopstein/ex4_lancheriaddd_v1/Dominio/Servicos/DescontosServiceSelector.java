package com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Cliente;

/**
 * Seletor da estratégia de desconto corrente (padrão Strategy — SRP + OCP).
 *
 * É o serviço de domínio de descontos: escolhe qual {@link IDescontosService}
 * está ativo e expõe a lista de estratégias disponíveis. A escolha é trocável
 * sem alterar o resto do sistema (adicionar uma nova estratégia é só criar mais
 * uma classe que implemente {@link IDescontosService}).
 *
 * A política inicial vem de {@code desconto.politica} (application.yaml);
 * valores: "fidelidade", "promocao_verao", "sem_desconto".
 */
@Service
@Primary
public class DescontosServiceSelector implements IDescontosService {

    private final List<IDescontosService> disponiveis;
    private IDescontosService estrategiaAtiva;

    public DescontosServiceSelector(
            DescontoFidelidadeImpl descontoFidelidade,
            DescontoPromocaoVeraoImpl descontoPromocaoVerao,
            DescontoSemDescontoImpl descontoSemDesconto,
            @Value("${desconto.politica:fidelidade}") String politicaInicial) {

        this.disponiveis = List.of(descontoFidelidade, descontoPromocaoVerao, descontoSemDesconto);
        this.estrategiaAtiva = selecionaPorCodigo(politicaInicial);
    }

    /** Estratégias de desconto disponíveis (para listagem — UC3). */
    public List<IDescontosService> listarDisponiveis() {
        return disponiveis;
    }

    /**
     * Troca a política corrente pelo código (base para o endpoint de admin — UC4).
     * @return a estratégia que passou a vigorar
     */
    public IDescontosService defineCorrente(String codigo) {
        this.estrategiaAtiva = selecionaPorCodigo(codigo);
        return estrategiaAtiva;
    }

    private IDescontosService selecionaPorCodigo(String codigo) {
        String alvo = codigo == null ? "" : codigo.toLowerCase().strip().replace('-', '_');
        return disponiveis.stream()
                .filter(d -> d.getCodigo().equals(alvo))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Política de desconto desconhecida: " + codigo));
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
