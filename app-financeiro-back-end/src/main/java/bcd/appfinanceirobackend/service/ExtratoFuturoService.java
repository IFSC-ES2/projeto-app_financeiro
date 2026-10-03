package bcd.appfinanceirobackend.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import bcd.appfinanceirobackend.dto.fatura.FaturaResumoDTO;
import bcd.appfinanceirobackend.model.Fatura;
import bcd.appfinanceirobackend.model.Transacao;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.model.enums.StatusFatura;
import bcd.appfinanceirobackend.repository.FaturaRepository;
import bcd.appfinanceirobackend.repository.TransacaoRepository;

@Service 
public class ExtratoFuturoService {
    private final TransacaoRepository transacaoRepository;
    private final FaturaService faturaService;
    private final FaturaRepository faturaRepository;

    public ExtratoFuturoService(
        TransacaoRepository transacaoRepository,
        FaturaService faturaService,
        FaturaRepository faturaRepository
    ){
        this.transacaoRepository =transacaoRepository;
        this.faturaService = faturaService;
        this.faturaRepository = faturaRepository;
    }

    public List<Transacao> listarTransacoesFuturas(
        Usuario usuario, 
        LocalDate inicio, 
        LocalDate fim
    ){
        return transacaoRepository.findAllByContaUsuarioIdAndFuturaTrueAndDataBetweenOrderByDataAsc(
            usuario.getId(), inicio, fim);
    }

    public List<Fatura> listarFaturasAbertas(
        Usuario usuario,
        LocalDate dataInicial,
        LocalDate dataFinal
) {

    List<Fatura> faturas =
            faturaRepository
                    .findAllByContaUsuarioIdAndStatusAndDataVencimentoBetweenOrderByDataVencimentoAsc(
                            usuario.getId(),
                            StatusFatura.ABERTA,
                            dataInicial,
                            dataFinal
                    );

    for (Fatura fatura : faturas) {
        faturaService.calcularTotal(
                fatura.getId(),
                usuario
        );
    }

    return faturas;
}
}
