package bcd.appfinanceirobackend.service;

import bcd.appfinanceirobackend.dto.transacao.CategoriaTransacaoDTO;
import bcd.appfinanceirobackend.model.Categoria;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.repository.CategoriaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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