package bcd.appfinanceirobackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Regra "termo -> categoria" usada na sugestão automática de categoria.
 * Sem usuário é uma regra global; com usuário, foi aprendida com a categorização manual dele.
 */
@Entity
@Table(name = "regra_categorizacao")
@Getter
@Setter
public class RegraCategorizacao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 120)
    private String termo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();
}
