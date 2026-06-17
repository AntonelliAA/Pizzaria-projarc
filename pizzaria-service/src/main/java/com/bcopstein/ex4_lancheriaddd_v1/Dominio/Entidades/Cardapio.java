package com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "cardapios")
public class Cardapio {
    @Id
    private long id;
    private String titulo;
    private boolean corrente = false;

    @ManyToMany
    @JoinTable(
        name = "cardapio_produto",
        joinColumns = @JoinColumn(name = "cardapio_id"),
        inverseJoinColumns = @JoinColumn(name = "produto_id")
    )
    private List<Produto> produtos;

    public Cardapio() {}

    public Cardapio(CabecalhoCardapio cabecalhoCardapio, List<Produto> produtos) {
        this.id = cabecalhoCardapio.id();
        this.titulo = cabecalhoCardapio.titulo();
        this.produtos = produtos;
        this.corrente = false;
    }

    public long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public boolean isCorrente() {
        return corrente;
    }

    public void setCorrente(boolean corrente) {
        this.corrente = corrente;
    }

    public CabecalhoCardapio getCabecalhoCardapio(){
        return new CabecalhoCardapio(id, titulo);
    }

    public List<Produto> getProdutos() { return produtos; }
    public void setProdutos(List<Produto> produtos){this.produtos = produtos;}
}
