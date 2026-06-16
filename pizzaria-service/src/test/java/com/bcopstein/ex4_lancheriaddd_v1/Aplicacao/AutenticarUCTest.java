package com.bcopstein.ex4_lancheriaddd_v1.Aplicacao;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Requests.LoginRequest;
import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Responses.ValidacaoCredenciaisResponse;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Cliente;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos.ClienteService;

class AutenticarUCTest {

    @Test
    void devolveIdentidadeQuandoCredenciaisValidas() {
        ClienteService clienteService = mock(ClienteService.class);
        String hash = new BCryptPasswordEncoder().encode("senha");
        Cliente c = new Cliente("9001", "Huguinho", "999", "Rua A", "hug@email.com", hash);
        when(clienteService.recuperaPorEmail("hug@email.com")).thenReturn(Optional.of(c));

        AutenticarUC uc = new AutenticarUC(clienteService);
        ValidacaoCredenciaisResponse resp = uc.run(new LoginRequest("hug@email.com", "senha"));

        assertEquals("9001", resp.cpf());
        assertEquals("hug@email.com", resp.email());
    }

    @Test
    void lancaQuandoSenhaErrada() {
        ClienteService clienteService = mock(ClienteService.class);
        String hash = new BCryptPasswordEncoder().encode("senha");
        Cliente c = new Cliente("9001", "Huguinho", "999", "Rua A", "hug@email.com", hash);
        when(clienteService.recuperaPorEmail("hug@email.com")).thenReturn(Optional.of(c));

        AutenticarUC uc = new AutenticarUC(clienteService);
        assertThrows(IllegalArgumentException.class,
                () -> uc.run(new LoginRequest("hug@email.com", "errada")));
    }
}
