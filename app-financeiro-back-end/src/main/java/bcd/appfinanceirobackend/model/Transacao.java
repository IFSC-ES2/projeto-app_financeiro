package bcd.appfinanceirobackend.model;

import bcd.appfinanceirobackend.model.enums.TipoTransacao;
import bcd.appfinanceirobackend.model.enums.TipoPagamento;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "transacoes")
public class Transacao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    // Obrigatória — toda transação pertence a uma conta
    @ManyToOne
    @JoinColumn(name = "conta_id", nullable = false)
    private Conta conta;

    // Nullable — preenchida após categorização
    @ManyToOne
    @JoinColumn(name = "categoria_id", nullable = true)
    private Categoria categoria;

    // Nullable — null para transações adicionadas manualmente
    @ManyToOne
    @JoinColumn(name = "importacao_id", nullable = true)
    private Importacao importacao;

    // Nullable — null para transações que não são de cartão de crédito
    @ManyToOne
    @JoinColumn(name = "fatura_id", nullable = true)
    private Fatura fatura;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false)
    private LocalDate data;

    @Column(nullable = true)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoTransacao tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento", nullable = true)
    private TipoPagamento formaPagamento;

    // false = aguardando categorização, true = categoria atribuída
    @Column(nullable = false)
    private Boolean categorizada = false;

    // false = transação real, true = transação projetada (parcela futura, boleto a vencer)
    @Column(nullable = false)
    private Boolean futura = false;

    public Transacao() {}

}
