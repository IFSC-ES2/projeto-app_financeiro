package bcd.appfinanceirobackend.repository;

import bcd.appfinanceirobackend.model.CartaoCredito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartaoCreditoRepository extends JpaRepository<CartaoCredito, UUID> {
    Optional<CartaoCredito> findByContaId(UUID contaId);
}
