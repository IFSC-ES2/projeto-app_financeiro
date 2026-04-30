package bcd.appfinanceirobackend.model;

import bcd.appfinanceirobackend.model.enums.FormatoArquivo;
import bcd.appfinanceirobackend.model.enums.StatusImportacao;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
public class Importacao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @OneToMany(mappedBy = "importacao")
    private List<Transacao> transacoes;

    @Column(nullable = false)
    private String nome_arquivo;

    @Column(nullable = false)
    private FormatoArquivo formatoArquivo;

    @Column(nullable = false)
    private StatusImportacao statusImportacao;

    @Column(nullable = false)
    private LocalDateTime importado_em;

    @Column
    private int total_linhas;

    @Column
    private int sucessos;

    @Column
    private int falhas;
}
