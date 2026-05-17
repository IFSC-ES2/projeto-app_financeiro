package bcd.appfinanceirobackend.controller;

import bcd.appfinanceirobackend.dto.conta.ContaRequestDTO;
import bcd.appfinanceirobackend.dto.conta.ContaResponseDTO;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.service.ContaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/contas")
public class ContaController {

    private final ContaService contaService;

    public ContaController (ContaService contaService) { this.contaService = contaService;}

    @GetMapping
    public ResponseEntity<List<ContaResponseDTO>> listarContas(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(contaService.listarPorUsuario(usuario));
    }

    @PostMapping("/registrar")
    public ResponseEntity<ContaResponseDTO> registrarConta (@RequestBody ContaRequestDTO dto,
                                                            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contaService.registrar(dto, usuario));
    }
}