package com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos;

/**
 * Serviço de Autenticação — emissão e validação de tokens de sessão.
 * A implementação concreta (mecanismo de armazenamento) fica na camada de Adaptadores,
 * permitindo a troca futura (ex.: JWT, banco, Redis) sem afetar o domínio nem os casos de uso.
 */
public interface IAuthTokenService {
    String createToken(String cpf);
    boolean validate(String token);
    String cpfFor(String token);
}
