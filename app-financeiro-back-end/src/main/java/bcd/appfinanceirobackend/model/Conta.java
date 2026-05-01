package bcd.appfinanceirobackend.model;

import bcd.appfinanceirobackend.model.enums.TipoConta;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name="contas")
@Getter
@Setter
public class Conta {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name= "usuario_id", nullable = false)
    private Usuario usuario;

    @OneToOne(mappedBy = "conta")
    private CartaoCredito CartaoCredito;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = true)
    private String banco;

    @Column(nullable = false)
    private TipoConta tipoConta;

    @Column(nullable = true)
    private String descricao;

}
