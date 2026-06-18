package bcd.appfinanceirobackend.controller;

import bcd.appfinanceirobackend.dto.fatura.FaturaResumoDTO;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.service.FaturaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class FaturaController {

    private final FaturaService faturaService;

    public FaturaController(FaturaService faturaService) {
        this.faturaService = faturaService;
    }

    @GetMapping("/contas/{contaId}/faturas")
    public ResponseEntity<List<FaturaResumoDTO>> listarFaturasDaConta(
            @PathVariable UUID contaId,
            @AuthenticationPrincipal Usuario usuarioAutenticado) {
        return ResponseEntity.ok(faturaService.buscarPorConta(contaId, usuarioAutenticado));
    }

    @GetMapping("/faturas/{faturaId}")
    public ResponseEntity<FaturaResumoDTO> buscarFatura(
            @PathVariable UUID faturaId,
            @AuthenticationPrincipal Usuario usuarioAutenticado) {
        return ResponseEntity.ok(faturaService.buscarPorId(faturaId, usuarioAutenticado));
    }

    @PatchMapping("/faturas/{faturaId}/pagar")
    public ResponseEntity<FaturaResumoDTO> pagarFatura(
            @PathVariable UUID faturaId,
            @AuthenticationPrincipal Usuario usuarioAutenticado) {
        return ResponseEntity.ok(faturaService.pagar(faturaId, usuarioAutenticado));
    }
}
