package bcd.appfinanceirobackend.controller;

import bcd.appfinanceirobackend.dto.categoria.CategorizarTransacaoRequestDTO;
import bcd.appfinanceirobackend.dto.transacao.TransacaoRequestDTO;
import bcd.appfinanceirobackend.dto.transacao.TransacaoResponseDTO;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.service.TransacaoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/transacoes")
public class TransacaoController {

    private final TransacaoService transacaoService;

    public TransacaoController( TransacaoService transacaoService) {
        this.transacaoService = transacaoService;
    }

    @PostMapping("/manual")
    public ResponseEntity<TransacaoResponseDTO> registrarTransacaoManual (
            @RequestBody TransacaoRequestDTO requestDTO,
            @AuthenticationPrincipal Usuario usuarioAutenticado) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transacaoService.registrarManual(
                requestDTO, usuarioAutenticado));
    }

    @GetMapping
    public ResponseEntity<List<TransacaoResponseDTO>> listar(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(transacaoService.listarTransacoesPorUsuario(usuario));
    }

    @PatchMapping("/{transacaoId}/categoria")
    public ResponseEntity<TransacaoResponseDTO> categorizarTransacaoManual (
            @PathVariable UUID transacaoId,
            @RequestBody CategorizarTransacaoRequestDTO request,
            @AuthenticationPrincipal Usuario usuarioAutenticado) {
        return ResponseEntity.status(HttpStatus.OK).body(transacaoService.categorizar(
                transacaoId, request.getCategoriaId(), usuarioAutenticado
        ));
    }
}
