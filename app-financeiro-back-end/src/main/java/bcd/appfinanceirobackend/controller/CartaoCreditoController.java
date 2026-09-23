package bcd.appfinanceirobackend.controller;

import bcd.appfinanceirobackend.dto.conta.CartaoCreditoRequestDTO;
import bcd.appfinanceirobackend.dto.conta.CartaoCreditoResponseDTO;
import bcd.appfinanceirobackend.model.CartaoCredito;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.service.CartaoCreditoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/contas/{contaId}/cartao")
public class CartaoCreditoController {

    private final CartaoCreditoService cartaoCreditoService;

    public CartaoCreditoController(CartaoCreditoService cartaoCreditoService) {
        this.cartaoCreditoService = cartaoCreditoService;
    }

    @PostMapping
    public ResponseEntity<CartaoCreditoResponseDTO> associar(
            @PathVariable UUID contaId,
            @RequestBody CartaoCreditoRequestDTO dto,
            @AuthenticationPrincipal Usuario usuario) {
        CartaoCredito cartaoCredito = cartaoCreditoService.associar(contaId, dto, usuario);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(cartaoCreditoService.toResponse(cartaoCredito));
    }

    @GetMapping
    public ResponseEntity<CartaoCreditoResponseDTO> buscarPorConta(
            @PathVariable UUID contaId,
            @AuthenticationPrincipal Usuario usuario) {
        CartaoCredito cartaoCredito = cartaoCreditoService.buscarPorConta(contaId, usuario);
        return ResponseEntity.ok(cartaoCreditoService.toResponse(cartaoCredito));
    }
}
