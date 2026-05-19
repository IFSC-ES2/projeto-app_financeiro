package bcd.appfinanceirobackend.controller;

import bcd.appfinanceirobackend.dto.importacao.ImportacaoResponseDTO;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.model.enums.StatusImportacao;
import bcd.appfinanceirobackend.service.ImportacaoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/importacoes")
public class ImportacaoController {

    private final ImportacaoService importacaoService;

    public ImportacaoController(ImportacaoService importacaoService) {
        this.importacaoService = importacaoService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportacaoResponseDTO> importar(
            @RequestParam("arquivo") MultipartFile arquivo,
            @RequestParam("contaId") UUID contaId,
            @AuthenticationPrincipal Usuario usuarioAutenticado) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(importacaoService.processar(arquivo, contaId, usuarioAutenticado));
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<StatusImportacao> status(
            @PathVariable UUID id,
            @AuthenticationPrincipal Usuario usuarioAutenticado) {

        return ResponseEntity.ok(importacaoService.buscarStatus(id, usuarioAutenticado));
    }
}