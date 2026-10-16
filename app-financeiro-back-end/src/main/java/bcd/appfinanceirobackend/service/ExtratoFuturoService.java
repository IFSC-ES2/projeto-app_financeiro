package bcd.appfinanceirobackend.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import bcd.appfinanceirobackend.dto.extrato.ProjecaoMensalDTO;
import bcd.appfinanceirobackend.dto.fatura.FaturaResumoDTO;
import bcd.appfinanceirobackend.dto.transacao.TransacaoResponseDTO;
import bcd.appfinanceirobackend.mapper.TransacaoMapper;
import bcd.appfinanceirobackend.model.Fatura;
import bcd.appfinanceirobackend.model.Transacao;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.model.enums.StatusFatura;
import bcd.appfinanceirobackend.model.enums.TipoPagamento;
import bcd.appfinanceirobackend.model.enums.TipoTransacao;
import bcd.appfinanceirobackend.repository.FaturaRepository;
import bcd.appfinanceirobackend.repository.TransacaoRepository;

@Service 
public class ExtratoFuturoService {
    private final TransacaoRepository transacaoRepository;
    private final FaturaRepository faturaRepository;
    private final TransacaoMapper transacaoMapper;

    public ExtratoFuturoService(
        TransacaoRepository transacaoRepository,
        FaturaRepository faturaRepository,
        TransacaoMapper transacaoMapper
    ){
        this.transacaoRepository =transacaoRepository;
        this.faturaRepository = faturaRepository;
        this.transacaoMapper = transacaoMapper;
    }

    private List<Transacao> listarTransacoesFuturas(
        Usuario usuario, 
        LocalDate inicio, 
        LocalDate fim
    ){
        return transacaoRepository.findAllByContaUsuarioIdAndFuturaTrueAndDataBetweenOrderByDataAsc(
            usuario.getId(), inicio, fim);
    }

   private List<Fatura> listarFaturasAbertas(
        Usuario usuario,
        LocalDate dataInicial,
        LocalDate dataFinal
        ) {
        return faturaRepository
                .findAllByContaUsuarioIdAndStatusAndDataVencimentoBetweenOrderByDataVencimentoAsc(
                        usuario.getId(),
                        StatusFatura.ABERTA,
                        dataInicial,
                        dataFinal
                );
        }
        
    private BigDecimal calcularTotalFatura(Fatura fatura) {
                return transacaoRepository
                        .somarValorPorFatura(fatura.getId());
    }

    private Map<YearMonth, List<Transacao>> agruparTransacoesPorMes(List<Transacao> transacoes) {
        return transacoes.stream()
                .collect(Collectors.groupingBy(
                        transacao -> YearMonth.from(transacao.getData())
                ));
    }

    private Map<YearMonth, List<Fatura>> agruparFaturasPorMes(
        List<Fatura> faturas
    ) {
        return faturas.stream()
                .collect(Collectors.groupingBy(
                        fatura -> YearMonth.from(
                                fatura.getDataVencimento()
                        )
                ));
    }

    private BigDecimal calcularTotalCreditos(List<Transacao> transacoes) {
    return transacoes.stream()
            .filter(transacao ->
                    transacao.getTipo() == TipoTransacao.CREDITO
            )
            .map(Transacao::getValor)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calcularTotalDebitos(
        List<Transacao> transacoes,
        List<Fatura> faturas
        ) {
                BigDecimal totalTransacoes = transacoes.stream()
                        .filter(transacao ->
                                transacao.getTipo() == TipoTransacao.DEBITO
                        )
                        .filter(transacao ->
                                transacao.getFatura() == null
                        )
                        .map(Transacao::getValor)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal totalFaturas = faturas.stream()
                        .map(this::calcularTotalFatura)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                return totalTransacoes.add(totalFaturas);
        }

        private FaturaResumoDTO toFaturaResumoDTO(Fatura fatura) {

                FaturaResumoDTO dto = new FaturaResumoDTO();

                dto.setFaturaId(fatura.getId());
                dto.setNomeConta(fatura.getConta().getNome());
                dto.setMesReferencia(fatura.getMesReferencia());
                dto.setDataVencimento(fatura.getDataVencimento());

                dto.setValorTotal(
                        calcularTotalFatura(fatura)
                );

                dto.setStatus(fatura.getStatus());

                return dto;
        }

    private BigDecimal calcularSaldoPrevisto(
            BigDecimal totalCreditos,
            BigDecimal totalDebitos
    ) {
        return totalCreditos.subtract(totalDebitos);
    }

        private int calcularQuantidadeVencimentos(
                List<Transacao> transacoes,
                List<Fatura> faturas
        ) {
                long quantidadeBoletos = transacoes.stream()
                        .filter(transacao ->
                                transacao.getTipo() == TipoTransacao.DEBITO
                        )
                        .filter(transacao ->
                                transacao.getFormaPagamento() == TipoPagamento.BOLETO
                        )
                        .count();

                return (int) quantidadeBoletos + faturas.size();
        }

    private ProjecaoMensalDTO montarProjecaoMensal(
        YearMonth mes,
        List<Transacao> transacoes,
        List<Fatura> faturas
    ) {

        BigDecimal totalCreditos =
                calcularTotalCreditos(transacoes);

        BigDecimal totalDebitos =
                calcularTotalDebitos(transacoes, faturas);

        BigDecimal saldoPrevisto =
                calcularSaldoPrevisto(
                        totalCreditos,
                        totalDebitos
                );

        int quantidadeVencimentos =
                calcularQuantidadeVencimentos(
                        transacoes,
                        faturas
                );

        List<TransacaoResponseDTO> transacoesDTO =
                transacoes.stream()
                        .map(transacaoMapper::toResponse)
                        .toList();

        List<FaturaResumoDTO> faturasDTO =
                faturas.stream()
                        .map(this::toFaturaResumoDTO)
                        .toList();

        ProjecaoMensalDTO dto =
                new ProjecaoMensalDTO();

        dto.setMes(mes);
        dto.setTotalCreditos(totalCreditos);
        dto.setTotalDebitos(totalDebitos);
        dto.setSaldoPrevisto(saldoPrevisto);
        dto.setQuantidadeVencimentos(
                quantidadeVencimentos
        );
        dto.setTransacoes(transacoesDTO);
        dto.setFaturas(faturasDTO);

        return dto;
    }

    public List<ProjecaoMensalDTO> calcularProjecao(
            Usuario usuario,
            Integer meses
    ) {
        if (meses == null) {
            meses = 3;
        }
        if (meses <= 0 || meses > 12) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Quantidade de meses deve estar entre 1 e 12"
                );
        }
        YearMonth mesInicial =
                YearMonth.now();
        YearMonth mesFinal =
                mesInicial.plusMonths(meses - 1);
        LocalDate dataInicial =
                mesInicial.atDay(1);
        LocalDate dataFinal =
                mesFinal.atEndOfMonth();
        List<Transacao> transacoesFuturas =
                listarTransacoesFuturas(
                        usuario,
                        dataInicial,
                        dataFinal
                );
        List<Fatura> faturasAbertas =
                listarFaturasAbertas(
                        usuario,
                        dataInicial,
                        dataFinal
                );
        Map<YearMonth, List<Transacao>>
                transacoesPorMes =
                agruparTransacoesPorMes(
                        transacoesFuturas
                );
        Map<YearMonth, List<Fatura>>
                faturasPorMes =
                agruparFaturasPorMes(
                        faturasAbertas
                );
        List<ProjecaoMensalDTO> projecoes =
                new ArrayList<>();
        for (int i = 0; i < meses; i++) {
            YearMonth mesAtual =
                    mesInicial.plusMonths(i);
            List<Transacao> transacoesDoMes =
                    transacoesPorMes.getOrDefault(
                            mesAtual,
                            List.of()
                    );
            List<Fatura> faturasDoMes =
                    faturasPorMes.getOrDefault(
                            mesAtual,
                            List.of()
                    );
            ProjecaoMensalDTO projecao =
                    montarProjecaoMensal(
                            mesAtual,
                            transacoesDoMes,
                            faturasDoMes
                    );
            projecoes.add(projecao);
        }
        return projecoes;
    }
}
