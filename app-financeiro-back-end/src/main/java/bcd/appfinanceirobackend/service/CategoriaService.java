package bcd.appfinanceirobackend.service;

import bcd.appfinanceirobackend.dto.transacao.CategoriaTransacaoDTO;
import bcd.appfinanceirobackend.exception.ResourceNotFoundException;
import bcd.appfinanceirobackend.model.Categoria;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.repository.CategoriaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public List<CategoriaTransacaoDTO> listarParaUsuario(Usuario usuario) {
        return categoriaRepository.findByPadraoTrueOrUsuarioId(usuario.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public Categoria buscarCategoriaPermitida(UUID categoriaId, Usuario usuario) {
        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));
        boolean categoriaPadrao = categoria.isPadrao();
        boolean categoriaDoUsuario = categoria.getUsuario() != null
                && categoria.getUsuario().getId().equals(usuario.getId());

        if (!categoriaPadrao && !categoriaDoUsuario) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Categoria não pertence ao usuário autenticado");
        }
        return categoria;
    }

    private CategoriaTransacaoDTO toResponse(Categoria categoria) {
        CategoriaTransacaoDTO dto = new CategoriaTransacaoDTO();
        dto.setCategoriaId(categoria.getId());
        dto.setNome(categoria.getNome());
        dto.setIcone(categoria.getIcone());
        dto.setCor(categoria.getCor());
        dto.setPadrao(categoria.isPadrao());
        return dto;
    }
}