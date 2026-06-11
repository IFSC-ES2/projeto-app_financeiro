package bcd.appfinanceirobackend.repository;

import bcd.appfinanceirobackend.model.Fatura;
import bcd.appfinanceirobackend.model.enums.StatusFatura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FaturaRepository extends JpaRepository<Fatura, UUID> {
    List<Fatura> findAllByContaIdOrderByMesReferenciaDesc(UUID contaId);
    List<Fatura> findAllByContaUsuarioIdAndStatusNot(UUID usuarioId, StatusFatura status);
    Optional<Fatura> findByContaIdAndMesReferencia(UUID contaId, YearMonth mesReferencia);
}
