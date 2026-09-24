package bcd.appfinanceirobackend.repository;

import bcd.appfinanceirobackend.model.Fatura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FaturaRepository extends JpaRepository<Fatura, UUID> {
    List<Fatura> findByContaOrderByMesReferenciaDesc(UUID contaId);
    Optional<Fatura> findByContaIdAndMesReferencia(UUID contaId, YearMonth mesReferencia);
}
