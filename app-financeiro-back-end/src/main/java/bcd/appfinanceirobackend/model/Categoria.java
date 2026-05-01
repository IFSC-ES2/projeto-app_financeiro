package bcd.appfinanceirobackend.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = true)
    private Usuario usuario;

    @Column(nullable = false)
    private String nome;

    @Column
    private String icone;

    @Column
    private String cor;

    @Column(nullable = false)
    private boolean padrao = false;

}
