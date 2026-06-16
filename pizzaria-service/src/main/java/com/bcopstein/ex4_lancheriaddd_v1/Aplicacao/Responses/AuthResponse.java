package com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Responses;

public class AuthResponse {
    private String token;
    private String cpf;
    private String email;

    public AuthResponse() {}

    public AuthResponse(String token, String cpf, String email) {
        this.token = token;
        this.cpf = cpf;
        this.email = email;
    }

    public String getToken() { return token; }
    public String getCpf() { return cpf; }
    public String getEmail() { return email; }
}
