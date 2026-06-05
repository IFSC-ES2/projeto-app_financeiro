package bcd.appfinanceirobackend.controller;

import bcd.appfinanceirobackend.dto.resumo.GrupoPagamentoDTO;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.service.ResumoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/resumo")
public class ResumoController {

    private final ResumoService resumoService;

    public ResumoController(ResumoService resumoService){
        this.resumoService = resumoService;
    }

    @GetMapping("/pagamentos")
    public ResponseEntity<List<GrupoPagamentoDTO>> resumoPagamentoAgrupados(
            @AuthenticationPrincipal Usuario usuarioAutenticado
            ) {
        return ResponseEntity.ok(resumoService.agruparFormaPagamento(usuarioAutenticado));

    }
}
