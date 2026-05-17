package bcd.appfinanceirobackend.repository;

import bcd.appfinanceirobackend.model.Conta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ContaRepository extends JpaRepository<Conta, UUID> {
    List<Conta> findByUsuarioId(UUID usuarioId);
}