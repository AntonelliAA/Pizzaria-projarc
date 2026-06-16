package com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Responses;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Response genérica para consultas de status de pedido (UC5)
 * e confirmação de cancelamento (UC6).
 */
public class StatusPedidoResponse {

    private Long id;
    private String status;
    private String dataHoraCriacao;
    private String enderecoEntrega;
    private Double valorCobrado;

    public StatusPedidoResponse() {}

    public StatusPedidoResponse(Long id, String status, LocalDateTime dataHoraCriacao,
                                String enderecoEntrega, Double valorCobrado) {
        this.id = id;
        this.status = status;
        this.dataHoraCriacao = dataHoraCriacao != null
                ? dataHoraCriacao.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                : null;
        this.enderecoEntrega = enderecoEntrega;
        this.valorCobrado = valorCobrado;
    }

    public Long getId() { return id; }
    public String getStatus() { return status; }
    public String getDataHoraCriacao() { return dataHoraCriacao; }
    public String getEnderecoEntrega() { return enderecoEntrega; }
    public Double getValorCobrado() { return valorCobrado; }
}
