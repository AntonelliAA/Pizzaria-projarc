package com.bcopstein.gateway.servicos;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class JwtTokenServiceTest {

    private final JwtTokenService service =
            new JwtTokenService("uma-chave-secreta-bem-grande-para-hs256-1234", 480);

    @Test
    void gerarEValidarTokenComCpf() {
        String token = service.gerar("9001");
        assertTrue(service.valido(token));
        assertEquals("9001", service.cpfDe(token));
    }

    @Test
    void tokenAdulteradoEhInvalido() {
        String token = service.gerar("9001");
        String adulterado = token.substring(0, token.length() - 2) + "xx";
        assertFalse(service.valido(adulterado));
    }

    @Test
    void tokenDeOutraChaveEhInvalido() {
        JwtTokenService outro =
                new JwtTokenService("uma-chave-secreta-completamente-diferente-9999", 480);
        String token = outro.gerar("9001");
        assertFalse(service.valido(token));
    }
}
