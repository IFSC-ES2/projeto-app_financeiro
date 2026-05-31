package bcd.appfinanceirobackend.controller;

import bcd.appfinanceirobackend.dto.categoria.CategorizarTransacaoRequestDTO;
import bcd.appfinanceirobackend.dto.comum.PaginaDTO;
import bcd.appfinanceirobackend.dto.transacao.TransacaoRequestDTO;
import bcd.appfinanceirobackend.dto.transacao.TransacaoResponseDTO;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.model.enums.TipoTransacao;
import bcd.appfinanceirobackend.service.TransacaoService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
    public ResponseEntity<PaginaDTO<TransacaoResponseDTO>> listar(
            @AuthenticationPrincipal Usuario usuario,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) UUID categoriaId,
            @RequestParam(required = false) TipoTransacao tipo,
            @PageableDefault(size = 20, sort = "data", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(
                transacaoService.listarTransacoesPorUsuario(usuario, dataInicio, dataFim, categoriaId, tipo, pageable));
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
