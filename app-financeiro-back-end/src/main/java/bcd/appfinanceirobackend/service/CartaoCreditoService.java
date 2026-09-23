package bcd.appfinanceirobackend.service;

import bcd.appfinanceirobackend.dto.conta.CartaoCreditoRequestDTO;
import bcd.appfinanceirobackend.dto.conta.CartaoCreditoResponseDTO;
import bcd.appfinanceirobackend.exception.ResourceNotFoundException;
import bcd.appfinanceirobackend.model.CartaoCredito;
import bcd.appfinanceirobackend.model.Conta;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.model.enums.TipoConta;
import bcd.appfinanceirobackend.repository.CartaoCreditoRepository;
import bcd.appfinanceirobackend.repository.ContaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class CartaoCreditoService {

    private final CartaoCreditoRepository cartaoCreditoRepository;
    private final ContaRepository contaRepository;

    public CartaoCreditoService(CartaoCreditoRepository cartaoCreditoRepository,
                                ContaRepository contaRepository) {
        this.cartaoCreditoRepository = cartaoCreditoRepository;
        this.contaRepository = contaRepository;
    }

    public CartaoCredito associar(UUID contaId,
                                  CartaoCreditoRequestDTO dto,
                                  Usuario usuarioAutenticado) {
        Conta conta = buscarContaDoUsuario(contaId, usuarioAutenticado);
        validarTipoConta(conta);

        if (cartaoCreditoRepository.findByContaId(contaId).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Conta já possui dados de cartão de crédito associados"
            );
        }

        validarCampos(dto);

        CartaoCredito cartaoCredito = new CartaoCredito();
        cartaoCredito.setConta(conta);
        cartaoCredito.setLimite(dto.getLimite());
        cartaoCredito.setDia_fechamento(dto.getDiaFechamento());
        cartaoCredito.setDia_vencimento(dto.getDiaVencimento());

        return cartaoCreditoRepository.save(cartaoCredito);
    }

    public CartaoCredito buscarPorConta(UUID contaId, Usuario usuarioAutenticado) {
        Conta conta = buscarContaDoUsuario(contaId, usuarioAutenticado);
        validarTipoConta(conta);

        return cartaoCreditoRepository.findByContaId(contaId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Conta não possui dados de cartão de crédito associados"
                ));
    }

    public CartaoCreditoResponseDTO toResponse(CartaoCredito cartaoCredito) {
        CartaoCreditoResponseDTO response = new CartaoCreditoResponseDTO();
        response.setCartaoId(cartaoCredito.getId());
        response.setContaId(cartaoCredito.getConta().getId());
        response.setLimite(cartaoCredito.getLimite());
        response.setDiaFechamento(cartaoCredito.getDia_fechamento());
        response.setDiaVencimento(cartaoCredito.getDia_vencimento());
        return response;
    }

    private Conta buscarContaDoUsuario(UUID contaId, Usuario usuarioAutenticado) {
        Conta conta = contaRepository.findById(contaId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada"));

        if (!conta.getUsuario().getId().equals(usuarioAutenticado.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado a esta conta");
        }

        return conta;
    }

    private void validarTipoConta(Conta conta) {
        if (conta.getTipoConta() != TipoConta.CARTAO_CREDITO) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A conta informada não é do tipo CARTAO_CREDITO"
            );
        }
    }

    private void validarCampos(CartaoCreditoRequestDTO dto) {
        if (dto == null || dto.getDiaFechamento() == null || dto.getDiaVencimento() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Dia de fechamento e dia de vencimento são obrigatórios"
            );
        }

        if (diaInvalido(dto.getDiaFechamento()) || diaInvalido(dto.getDiaVencimento())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Dias de fechamento e vencimento devem estar entre 1 e 31"
            );
        }

        if (dto.getLimite() != null && dto.getLimite().compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Limite não pode ser negativo");
        }
    }

    private boolean diaInvalido(int dia) {
        return dia < 1 || dia > 31;
    }
}
