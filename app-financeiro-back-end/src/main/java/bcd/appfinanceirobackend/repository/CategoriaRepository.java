package bcd.appfinanceirobackend.repository;

import bcd.appfinanceirobackend.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, UUID> {
    List<Categoria> findByPadraoTrueOrUsuarioId(UUID usuarioId);
}