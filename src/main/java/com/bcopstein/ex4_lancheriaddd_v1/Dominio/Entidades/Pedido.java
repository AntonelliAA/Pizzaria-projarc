package com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "pedidos")
public class Pedido {

    public enum Status {
        NOVO,
        APROVADO,
        PAGO,
        AGUARDANDO,
        PREPARACAO,
        PRONTO,
        TRANSPORTE,
        ENTREGUE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "cliente_cpf", nullable = false)
    private Cliente cliente;

    private LocalDateTime dataHoraCriacao;

    private String enderecoEntrega;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedido> itens;

    @Enumerated(EnumType.STRING)
    private Status status;

    private double valor;
    private double impostos;
    private double desconto;
    private double valorCobrado;

    public Pedido() {}

    /** Construtor para criação de novos pedidos (sem id — gerado pelo banco). */
    public Pedido(Cliente cliente, LocalDateTime dataHoraCriacao, String enderecoEntrega,
                  List<ItemPedido> itens, Status status,
                  double valor, double impostos, double desconto, double valorCobrado) {
        this.cliente = cliente;
        this.dataHoraCriacao = dataHoraCriacao;
        this.enderecoEntrega = enderecoEntrega;
        this.itens = itens;
        this.status = status;
        this.valor = valor;
        this.impostos = impostos;
        this.desconto = desconto;
        this.valorCobrado = valorCobrado;
        // Seta a referência inversa para que o JPA preencha pedido_id no INSERT
        if (itens != null) {
            itens.forEach(item -> item.setPedido(this));
        }
    }

    public long getId() { return id; }
    public Cliente getCliente() { return cliente; }
    public LocalDateTime getDataHoraCriacao() { return dataHoraCriacao; }
    public String getEnderecoEntrega() { return enderecoEntrega; }
    public List<ItemPedido> getItens() { return itens; }
    public Status getStatus() { return status; }
    public double getValor() { return valor; }
    public double getImpostos() { return impostos; }
    public double getDesconto() { return desconto; }
    public double getValorCobrado() { return valorCobrado; }

    public void setStatus(Status status) { this.status = status; }
    public void setValor(double valor) { this.valor = valor; }
    public void setImpostos(double impostos) { this.impostos = impostos; }
    public void setDesconto(double desconto) { this.desconto = desconto; }
    public void setValorCobrado(double valorCobrado) { this.valorCobrado = valorCobrado; }
}
