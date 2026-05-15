package bcd.appfinanceirobackend.repository;

import bcd.appfinanceirobackend.model.Importacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ImportacaoRepository extends JpaRepository<Importacao, UUID> {
}
