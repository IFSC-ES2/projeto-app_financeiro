package bcd.appfinanceirobackend.service;

import bcd.appfinanceirobackend.dto.fatura.FaturaResumoDTO;
import bcd.appfinanceirobackend.exception.ResourceNotFoundException;
import bcd.appfinanceirobackend.mapper.FaturaMapper;
import bcd.appfinanceirobackend.model.CartaoCredito;
import bcd.appfinanceirobackend.model.Conta;
import bcd.appfinanceirobackend.model.Fatura;
import bcd.appfinanceirobackend.model.Transacao;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.model.enums.StatusFatura;
import bcd.appfinanceirobackend.model.enums.TipoTransacao;
import bcd.appfinanceirobackend.repository.CartaoCreditoRepository;
import bcd.appfinanceirobackend.repository.ContaRepository;
import bcd.appfinanceirobackend.repository.FaturaRepository;
import bcd.appfinanceirobackend.repository.TransacaoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
public class FaturaService {

    private final FaturaRepository faturaRepository;
    private final ContaRepository contaRepository;
    private final CartaoCreditoRepository cartaoCreditoRepository;
    private final TransacaoRepository transacaoRepository;
    private final FaturaMapper faturaMapper;

    public FaturaService(
            FaturaRepository faturaRepository,
            ContaRepository contaRepository,
            CartaoCreditoRepository cartaoCreditoRepository,
            TransacaoRepository transacaoRepository,
            FaturaMapper faturaMapper) {
        this.faturaRepository = faturaRepository;
        this.contaRepository = contaRepository;
        this.cartaoCreditoRepository = cartaoCreditoRepository;
        this.transacaoRepository = transacaoRepository;
        this.faturaMapper = faturaMapper;
    }

    public Fatura gerarFatura(UUID contaId, YearMonth mesReferencia) {
        return faturaRepository.findByContaIdAndMesReferencia(contaId, mesReferencia)
                .orElseGet(() -> criarFatura(contaId, mesReferencia));
    }

    public List<FaturaResumoDTO> buscarPorConta(UUID contaId, Usuario usuarioAutenticado) {
        Conta conta = contaRepository.findById(contaId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada"));
        validarAcesso(conta, usuarioAutenticado);

        return faturaRepository.findAllByContaIdOrderByMesReferenciaDesc(conta.getId()).stream()
                .map(faturaMapper::toResumo)
                .toList();
    }

    public FaturaResumoDTO buscarPorId(UUID faturaId, Usuario usuarioAutenticado) {
        return faturaMapper.toResumo(buscarFaturaDoUsuario(faturaId, usuarioAutenticado));
    }

    public BigDecimal calcularTotal(UUID faturaId) {
        Fatura fatura = faturaRepository.findById(faturaId)
                .orElseThrow(() -> new ResourceNotFoundException("Fatura não encontrada"));

        // Débitos somam ao total da fatura; créditos (estornos) abatem
        BigDecimal total = BigDecimal.ZERO;
        for (Transacao transacao : transacaoRepository.findAllByFaturaId(fatura.getId())) {
            if (transacao.getTipo() == TipoTransacao.DEBITO) {
                total = total.add(transacao.getValor());
            } else {
                total = total.subtract(transacao.getValor());
            }
        }

        fatura.setValorTotal(total);
        faturaRepository.save(fatura);
        return total;
    }

    public FaturaResumoDTO pagar(UUID faturaId, Usuario usuarioAutenticado) {
        Fatura fatura = buscarFaturaDoUsuario(faturaId, usuarioAutenticado);

        if (fatura.getStatus() == StatusFatura.PAGA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Fatura já está paga");
        }

        fatura.setStatus(StatusFatura.PAGA);
        return faturaMapper.toResumo(faturaRepository.save(fatura));
    }

    private Fatura criarFatura(UUID contaId, YearMonth mesReferencia) {
        Conta conta = contaRepository.findById(contaId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada"));

        CartaoCredito cartao = cartaoCreditoRepository.findByContaId(contaId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não possui cartão de crédito associado"));

        Fatura fatura = new Fatura();
        fatura.setConta(conta);
        fatura.setMesReferencia(mesReferencia);
        fatura.setDataVencimento(calcularVencimento(cartao, mesReferencia));
        fatura.setValorTotal(BigDecimal.ZERO);
        fatura.setStatus(StatusFatura.ABERTA);
        return faturaRepository.save(fatura);
    }

    // A fatura fecha no mês de referência; se o dia de vencimento é depois do fechamento,
    // vence no próprio mês, senão vence no mês seguinte.
    private LocalDate calcularVencimento(CartaoCredito cartao, YearMonth mesReferencia) {
        YearMonth mesVencimento = cartao.getDia_vencimento() > cartao.getDia_fechamento()
                ? mesReferencia
                : mesReferencia.plusMonths(1);
        int dia = Math.min(cartao.getDia_vencimento(), mesVencimento.lengthOfMonth());
        return mesVencimento.atDay(dia);
    }

    private Fatura buscarFaturaDoUsuario(UUID faturaId, Usuario usuarioAutenticado) {
        Fatura fatura = faturaRepository.findById(faturaId)
                .orElseThrow(() -> new ResourceNotFoundException("Fatura não encontrada"));
        validarAcesso(fatura.getConta(), usuarioAutenticado);
        return fatura;
    }

    private void validarAcesso(Conta conta, Usuario usuarioAutenticado) {
        if (conta == null || !conta.getUsuario().getId().equals(usuarioAutenticado.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado a esta conta");
        }
    }
}
