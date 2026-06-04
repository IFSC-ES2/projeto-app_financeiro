package bcd.appfinanceirobackend.controller;

import bcd.appfinanceirobackend.dto.conta.ContaRequestDTO;
import bcd.appfinanceirobackend.dto.conta.ContaResponseDTO;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.service.ContaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletarConta(@AuthenticationPrincipal Usuario usuario,
                                             @PathVariable UUID id){
        contaService.removerConta(usuario, id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("");
    }

}