package bcd.appfinanceirobackend.model;

import bcd.appfinanceirobackend.model.enums.StatusFatura;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.UUID;

@Entity
public class Fatura {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "conta_id")
    private Conta conta;

    @Column
    private YearMonth mesReferencia;

    @Column
    private LocalDate dataVencimento;

    @Column
    private BigDecimal valorTotal;

    @Column
    private StatusFatura status;

}
