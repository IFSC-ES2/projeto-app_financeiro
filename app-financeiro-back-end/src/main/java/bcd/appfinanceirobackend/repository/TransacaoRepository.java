package bcd.appfinanceirobackend.repository;

import bcd.appfinanceirobackend.model.Conta;
import bcd.appfinanceirobackend.model.Transacao;
import bcd.appfinanceirobackend.model.enums.TipoTransacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransacaoRepository
        extends JpaRepository<Transacao, UUID>, JpaSpecificationExecutor<Transacao> {
    List<Transacao> findAllByContaUsuarioId(UUID usuarioId);
    boolean existsByContaId(UUID contaId);
    List<Transacao> findAllByContaUsuarioIdAndDataBetween(UUID usuarioId, LocalDate dataInicio, LocalDate dataFim);
    boolean existsByContaIdAndDataAndDescricaoAndValorAndTipo(
            UUID contaId,
            LocalDate data,
            String descricao,
            BigDecimal valor,
            TipoTransacao tipo
    );
}
