package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Servicos;

import org.springframework.stereotype.Service;

import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Pedido;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos.IPagamentoService;

/**
 * Implementação fake do serviço de pagamento.
 * Sempre retorna true, indicando que o pagamento foi autorizado com sucesso.
 */
@Service
public class PagamentoServiceFake implements IPagamentoService {

    @Override
    public boolean processarPagamento(Pedido p) {
        return true;
    }
}
