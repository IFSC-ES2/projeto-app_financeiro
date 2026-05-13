package bcd.appfinanceirobackend.controller;

import bcd.appfinanceirobackend.dto.transacao.TransacaoRequestDTO;
import bcd.appfinanceirobackend.dto.transacao.TransacaoResponseDTO;
import bcd.appfinanceirobackend.service.TransacaoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transacoes")
public class TransacaoController {

    private final TransacaoService transacaoService;

    public TransacaoController( TransacaoService transacaoService) {
        this.transacaoService = transacaoService;
    }

    @PostMapping("/manual")
    public ResponseEntity<TransacaoResponseDTO> registrarTransacaoManual (TransacaoRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transacaoService.registrarManual(requestDTO));
    }
}
