package bcd.appfinanceirobackend.repository;

import bcd.appfinanceirobackend.model.RegraCategorizacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RegraCategorizacaoRepository extends JpaRepository<RegraCategorizacao, UUID> {

    /** Regras globais mais as aprendidas pelo usuário; a categoria vem junto para evitar N+1. */
    @Query("""
            SELECT r FROM RegraCategorizacao r JOIN FETCH r.categoria
            WHERE r.usuario IS NULL OR r.usuario.id = :usuarioId
            """)
    List<RegraCategorizacao> findGlobaisEDoUsuario(@Param("usuarioId") UUID usuarioId);

    Optional<RegraCategorizacao> findByUsuarioIdAndTermo(UUID usuarioId, String termo);
}
