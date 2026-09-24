package bcd.appfinanceirobackend.controller;

import bcd.appfinanceirobackend.dto.fatura.FaturaResumoDTO;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.service.FaturaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class FaturaController {

    private final FaturaService faturaService;

    public FaturaController(FaturaService faturaService) {
        this.faturaService = faturaService;
    }

    @GetMapping("/contas/{contaId}/faturas")
    public ResponseEntity<List<FaturaResumoDTO>> listarPorConta(
            @PathVariable UUID contaId,
            @AuthenticationPrincipal Usuario usuario) {

        return ResponseEntity.ok(
                faturaService.buscarPorConta(contaId, usuario)
        );
    }

    @GetMapping("/faturas/{faturaId}")
    public ResponseEntity<FaturaResumoDTO> buscarPorId(
            @PathVariable UUID faturaId,
            @AuthenticationPrincipal Usuario usuario) {

        return ResponseEntity.ok(
                faturaService.buscarPorId(faturaId, usuario)
        );
    }

    @PatchMapping("/faturas/{faturaId}/pagar")
    public ResponseEntity<Void> marcarComoPaga(
            @PathVariable UUID faturaId,
            @AuthenticationPrincipal Usuario usuario) {

        faturaService.marcarComoPaga(faturaId, usuario);

        return ResponseEntity.noContent().build();
    }
}