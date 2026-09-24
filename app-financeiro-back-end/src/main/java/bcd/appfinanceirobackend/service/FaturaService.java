package bcd.appfinanceirobackend.service;

import bcd.appfinanceirobackend.model.CartaoCredito;
import bcd.appfinanceirobackend.model.Fatura;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.model.enums.StatusFatura;
import bcd.appfinanceirobackend.repository.FaturaRepository;
import bcd.appfinanceirobackend.repository.TransacaoRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
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

}
