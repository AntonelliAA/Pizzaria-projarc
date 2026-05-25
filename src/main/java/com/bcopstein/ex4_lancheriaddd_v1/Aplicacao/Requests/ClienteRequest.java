package com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class ClienteRequest {

    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    @NotBlank(message = "CPF é obrigatório")
    private String cpf;

    @NotBlank(message = "Celular é obrigatório")
    private String celular;

    @NotBlank(message = "Endereço é obrigatório")
    private String endereco;

    @Email(message = "E-mail inválido")
    @NotBlank(message = "E-mail é obrigatório")
    private String email;

    @NotBlank(message = "Senha é obrigatória")
    private String senha;

    public ClienteRequest() {}

    public ClienteRequest(String nome, String cpf, String celular, String endereco, String email, String senha) {
        this.nome = nome;
        this.cpf = cpf;
        this.celular = celular;
        this.endereco = endereco;
        this.email = email;
        this.senha = senha;
    }

    public String getNome() { return nome; }
    public String getCpf() { return cpf; }
    public String getCelular() { return celular; }
    public String getEndereco() { return endereco; }
    public String getEmail() { return email; }
    public String getSenha() { return senha; }
}
