package bcd.appfinanceirobackend.service;

import bcd.appfinanceirobackend.dto.fatura.FaturaResumoDTO;
import bcd.appfinanceirobackend.exception.ResourceNotFoundException;
import bcd.appfinanceirobackend.model.CartaoCredito;
import bcd.appfinanceirobackend.model.Fatura;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.model.enums.StatusFatura;
import bcd.appfinanceirobackend.repository.FaturaRepository;
import bcd.appfinanceirobackend.repository.TransacaoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class FaturaService {
    private final FaturaRepository faturaRepository;
    private final TransacaoRepository transacaoRepository;
    private final CartaoCreditoService cartaoCreditoService;

    public FaturaService(
            FaturaRepository faturaRepository,
            TransacaoRepository transacaoRepository,
            CartaoCreditoService cartaoCreditoService) {
        this.cartaoCreditoService = cartaoCreditoService;
        this.faturaRepository = faturaRepository;
        this.transacaoRepository = transacaoRepository;
    }

    public Fatura gerarFatura(
            UUID contaId,
            YearMonth mesReferencia,
            Usuario usuarioAutenticado
    ) {
        CartaoCredito cartaoCredito =
                cartaoCreditoService.buscarPorConta(
                        contaId,
                        usuarioAutenticado
                );

        return faturaRepository
                .findByContaIdAndMesReferencia(contaId, mesReferencia)
                .orElseGet(() -> {

                    int diaVencimento = Math.min(
                            cartaoCredito.getDia_vencimento(),
                            mesReferencia.lengthOfMonth()
                    );

                    LocalDate dataVencimento =
                            mesReferencia.atDay(diaVencimento);

                    Fatura fatura = new Fatura();

                    fatura.setConta(cartaoCredito.getConta());
                    fatura.setMesReferencia(mesReferencia);
                    fatura.setDataVencimento(dataVencimento);
                    fatura.setValorTotal(BigDecimal.ZERO);
                    fatura.setStatus(StatusFatura.ABERTA);

                    return faturaRepository.save(fatura);
                });
    }

    public BigDecimal calcularTotal(UUID faturaId, Usuario usuario) {
        Fatura fatura = buscarFaturaDoUsuario(faturaId, usuario);
        BigDecimal total = transacaoRepository.somarValorPorFatura(fatura.getId());
        fatura.setValorTotal(total);
        faturaRepository.save(fatura);
        return total;
    }

    public Fatura buscarFaturaDoUsuario(UUID faturaId, Usuario usuario) {
        Fatura fatura = faturaRepository.findById(faturaId)
                .orElseThrow(() -> new ResourceNotFoundException("Fatura não encontrada"));

        if(!fatura.getConta().getUsuario().getId().equals(usuario.getId())){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Fatura não pertence ao usuário");
        }
        return fatura;
    }

    public List<FaturaResumoDTO> buscarPorConta(UUID contaId, Usuario usuario) {
        cartaoCreditoService.buscarPorConta(
                contaId,
                usuario
        );

        return faturaRepository
                .findAllByContaOrderByMesReferenciaDesc(contaId)
                .stream()
                .map(this::toFaturaResumoDTO)
                .toList();
    }

    public FaturaResumoDTO toFaturaResumoDTO(Fatura fatura){
        FaturaResumoDTO faturaResumoDTO = new FaturaResumoDTO();
        faturaResumoDTO.setFaturaId(fatura.getId());
        faturaResumoDTO.setNomeConta(fatura.getConta().getNome());
        faturaResumoDTO.setDataVencimento(fatura.getDataVencimento());
        faturaResumoDTO.setValorTotal(fatura.getValorTotal());
        faturaResumoDTO.setStatus(fatura.getStatus());
        return faturaResumoDTO;
    }

}
