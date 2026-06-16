package com.bcopstein.gateway.servicos;

public interface ITokenService {
    String gerar(String cpf);
    boolean valido(String token);
    String cpfDe(String token);
}
