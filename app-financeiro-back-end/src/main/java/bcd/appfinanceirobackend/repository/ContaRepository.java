package bcd.appfinanceirobackend.repository;

import bcd.appfinanceirobackend.model.Conta;
import bcd.appfinanceirobackend.model.enums.TipoConta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContaRepository extends JpaRepository<Conta, UUID> {
    List<Conta> findByUsuarioId(UUID usuarioId);

    Optional<Conta> findByUsuarioIdAndTipoContaAndNome(
            UUID usuarioId,
            TipoConta tipoConta,
            String nome
    );
}

