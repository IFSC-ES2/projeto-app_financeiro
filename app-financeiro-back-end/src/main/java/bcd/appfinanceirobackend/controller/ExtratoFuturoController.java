package bcd.appfinanceirobackend.controller;

import bcd.appfinanceirobackend.dto.extrato.ProjecaoMensalDTO;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.service.ExtratoFuturoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/extrato-futuro")
public class ExtratoFuturoController {

    private final ExtratoFuturoService extratoFuturoService;

    public ExtratoFuturoController(ExtratoFuturoService extratoFuturoService) {
        this.extratoFuturoService = extratoFuturoService;
    }

    @GetMapping
    public ResponseEntity<List<ProjecaoMensalDTO>> obterExtratoFuturo(
            @AuthenticationPrincipal Usuario usuarioAutenticado,
            @RequestParam(required = false, defaultValue = "" + ExtratoFuturoService.MESES_PADRAO) int meses) {
        return ResponseEntity.ok(extratoFuturoService.calcularProjecao(usuarioAutenticado, meses));
    }
}
