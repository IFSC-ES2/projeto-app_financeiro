package bcd.appfinanceirobackend.model;

import bcd.appfinanceirobackend.model.enums.TipoConta;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name="contas")
public class Conta {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name= "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = true)
    private String banco;

    @Column(nullable = false)
    private TipoConta tipoConta;

    @Column(nullable = true)
    private String descricao;

}
