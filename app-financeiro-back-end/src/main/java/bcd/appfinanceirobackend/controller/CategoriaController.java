package bcd.appfinanceirobackend.controller;

import bcd.appfinanceirobackend.dto.categoria.CategoriaRequestDTO;
import bcd.appfinanceirobackend.dto.transacao.CategoriaTransacaoDTO;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.service.CategoriaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public ResponseEntity<List<CategoriaTransacaoDTO>> listar(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(categoriaService.listarParaUsuario(usuario));
    }

    @PostMapping
    public ResponseEntity<CategoriaTransacaoDTO> criar(
            @Valid @RequestBody CategoriaRequestDTO dto,
            @AuthenticationPrincipal Usuario usuario
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaService.criar(dto, usuario));
    }
}
