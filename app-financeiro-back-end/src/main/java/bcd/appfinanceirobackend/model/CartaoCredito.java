package bcd.appfinanceirobackend.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
public class CartaoCredito {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "conta_id")
    private Conta conta;

    @Column(nullable = true)
    private BigDecimal limite;

    @Column(nullable = false)
    private int dia_fechamento;

    @Column(nullable = false)
    private int dia_vencimento;




}
