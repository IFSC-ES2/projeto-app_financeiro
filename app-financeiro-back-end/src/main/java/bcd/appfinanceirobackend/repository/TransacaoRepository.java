package bcd.appfinanceirobackend.repository;

import bcd.appfinanceirobackend.model.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TransacaoRepository
        extends JpaRepository<Transacao, UUID>, JpaSpecificationExecutor<Transacao> {
}
