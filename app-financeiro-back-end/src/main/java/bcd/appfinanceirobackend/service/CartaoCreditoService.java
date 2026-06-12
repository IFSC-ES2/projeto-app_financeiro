package bcd.appfinanceirobackend.service;

import bcd.appfinanceirobackend.dto.conta.CartaoCreditoRequestDTO;
import bcd.appfinanceirobackend.exception.ResourceNotFoundException;
import bcd.appfinanceirobackend.model.CartaoCredito;
import bcd.appfinanceirobackend.model.Conta;
import bcd.appfinanceirobackend.model.Usuario;
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

    public CartaoCreditoService(
            CartaoCreditoRepository cartaoCreditoRepository,
            ContaRepository contaRepository) {
        this.cartaoCreditoRepository = cartaoCreditoRepository;
        this.contaRepository = contaRepository;
    }

    public CartaoCredito associar(UUID contaId, CartaoCreditoRequestDTO requestDTO, Usuario usuarioAutenticado) {
        Conta conta = buscarContaDoUsuario(contaId, usuarioAutenticado);

        if (cartaoCreditoRepository.findByContaId(conta.getId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Conta já possui cartão de crédito associado");
        }

        validarCampos(requestDTO);

        CartaoCredito cartaoCredito = new CartaoCredito();
        cartaoCredito.setConta(conta);
        cartaoCredito.setLimite(requestDTO.getLimite());
        cartaoCredito.setDia_fechamento(requestDTO.getDiaFechamento());
        cartaoCredito.setDia_vencimento(requestDTO.getDiaVencimento());

        return cartaoCreditoRepository.save(cartaoCredito);
    }

    public CartaoCredito buscarPorConta(UUID contaId, Usuario usuarioAutenticado) {
        Conta conta = buscarContaDoUsuario(contaId, usuarioAutenticado);

        return cartaoCreditoRepository.findByContaId(conta.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Conta não possui cartão de crédito associado"));
    }

    private Conta buscarContaDoUsuario(UUID contaId, Usuario usuarioAutenticado) {
        Conta conta = contaRepository.findById(contaId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada"));

        if (!conta.getUsuario().getId().equals(usuarioAutenticado.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado a esta conta");
        }

        return conta;
    }

    private void validarCampos(CartaoCreditoRequestDTO requestDTO) {
        if (requestDTO.getDiaFechamento() == null || requestDTO.getDiaVencimento() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Dia de fechamento e dia de vencimento são obrigatórios");
        }
        if (foraDoIntervaloDeDias(requestDTO.getDiaFechamento()) || foraDoIntervaloDeDias(requestDTO.getDiaVencimento())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Dias de fechamento e vencimento devem estar entre 1 e 31");
        }
        if (requestDTO.getLimite() != null && requestDTO.getLimite().compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Limite não pode ser negativo");
        }
    }

    private boolean foraDoIntervaloDeDias(int dia) {
        return dia < 1 || dia > 31;
    }
}
