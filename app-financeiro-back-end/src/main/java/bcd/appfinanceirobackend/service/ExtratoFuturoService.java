package bcd.appfinanceirobackend.service;

import bcd.appfinanceirobackend.dto.extrato.ProjecaoMensalDTO;
import bcd.appfinanceirobackend.mapper.FaturaMapper;
import bcd.appfinanceirobackend.mapper.TransacaoMapper;
import bcd.appfinanceirobackend.model.Fatura;
import bcd.appfinanceirobackend.model.Transacao;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.model.enums.StatusFatura;
import bcd.appfinanceirobackend.model.enums.TipoTransacao;
import bcd.appfinanceirobackend.repository.FaturaRepository;
import bcd.appfinanceirobackend.repository.TransacaoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ExtratoFuturoService {

    public static final int MESES_PADRAO = 3;
    private static final int MESES_MAXIMO = 12;

    private final TransacaoRepository transacaoRepository;
    private final FaturaRepository faturaRepository;
    private final TransacaoMapper transacaoMapper;
    private final FaturaMapper faturaMapper;

    public ExtratoFuturoService(
            TransacaoRepository transacaoRepository,
            FaturaRepository faturaRepository,
            TransacaoMapper transacaoMapper,
            FaturaMapper faturaMapper) {
        this.transacaoRepository = transacaoRepository;
        this.faturaRepository = faturaRepository;
        this.transacaoMapper = transacaoMapper;
        this.faturaMapper = faturaMapper;
    }

    public List<ProjecaoMensalDTO> calcularProjecao(Usuario usuario, int meses) {
        return calcularProjecao(usuario, meses, LocalDate.now());
    }

    public List<ProjecaoMensalDTO> calcularProjecao(Usuario usuario, int meses, LocalDate hoje) {
        if (meses < 1 || meses > MESES_MAXIMO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Quantidade de meses deve estar entre 1 e " + MESES_MAXIMO);
        }

        YearMonth mesInicial = YearMonth.from(hoje);
        YearMonth mesFinal = mesInicial.plusMonths(meses - 1);

        List<Transacao> transacoesFuturas = listarTransacoesFuturas(usuario, hoje, mesFinal.atEndOfMonth());
        List<Fatura> faturasEmAberto =
                faturaRepository.findAllByContaUsuarioIdAndStatusNot(usuario.getId(), StatusFatura.PAGA);

        // O saldo previsto é acumulado: parte do saldo real até hoje e propaga mês a mês
        BigDecimal saldoCorrente = calcularSaldoAtual(usuario, hoje);

        List<ProjecaoMensalDTO> projecao = new ArrayList<>();
        for (YearMonth mes = mesInicial; !mes.isAfter(mesFinal); mes = mes.plusMonths(1)) {
            ProjecaoMensalDTO projecaoMensal = montarProjecaoDoMes(mes, transacoesFuturas, faturasEmAberto, saldoCorrente);
            saldoCorrente = projecaoMensal.getSaldoPrevisto();
            projecao.add(projecaoMensal);
        }
        return projecao;
    }

    public List<Transacao> listarTransacoesFuturas(Usuario usuario, LocalDate hoje, LocalDate dataFim) {
        return transacaoRepository.findAllByContaUsuarioIdAndDataBetween(
                usuario.getId(), hoje.plusDays(1), dataFim);
    }

    private ProjecaoMensalDTO montarProjecaoDoMes(
            YearMonth mes,
            List<Transacao> transacoesFuturas,
            List<Fatura> faturasEmAberto,
            BigDecimal saldoAnterior) {

        List<Transacao> transacoesDoMes = transacoesFuturas.stream()
                .filter(transacao -> YearMonth.from(transacao.getData()).equals(mes))
                .sorted(Comparator.comparing(Transacao::getData))
                .toList();

        List<Fatura> faturasDoMes = faturasEmAberto.stream()
                .filter(fatura -> fatura.getDataVencimento() != null
                        && YearMonth.from(fatura.getDataVencimento()).equals(mes))
                .sorted(Comparator.comparing(Fatura::getDataVencimento))
                .toList();

        BigDecimal totalCreditos = somarPorTipo(transacoesDoMes, TipoTransacao.CREDITO);

        // Débitos já vinculados a uma fatura entram pelo valor da fatura (não pelo lançamento),
        // para não contar duas vezes a mesma despesa
        BigDecimal debitosAvulsos = transacoesDoMes.stream()
                .filter(transacao -> transacao.getTipo() == TipoTransacao.DEBITO && transacao.getFatura() == null)
                .map(Transacao::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalFaturas = faturasDoMes.stream()
                .map(Fatura::getValorTotal)
                .filter(valor -> valor != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDebitos = debitosAvulsos.add(totalFaturas);

        ProjecaoMensalDTO dto = new ProjecaoMensalDTO();
        dto.setAno(mes.getYear());
        dto.setMes(mes.getMonthValue());
        dto.setDataInicio(mes.atDay(1));
        dto.setDataFim(mes.atEndOfMonth());
        dto.setTotalCreditos(totalCreditos);
        dto.setTotalDebitos(totalDebitos);
        dto.setSaldoPrevisto(saldoAnterior.add(totalCreditos).subtract(totalDebitos));
        dto.setFaturas(faturasDoMes.stream().map(faturaMapper::toResumo).toList());
        dto.setTransacoes(transacoesDoMes.stream().map(transacaoMapper::toResponse).toList());
        return dto;
    }

    private BigDecimal calcularSaldoAtual(Usuario usuario, LocalDate hoje) {
        BigDecimal saldo = BigDecimal.ZERO;
        for (Transacao transacao : transacaoRepository.findAllByContaUsuarioId(usuario.getId())) {
            if (transacao.getData().isAfter(hoje)) {
                continue;
            }
            saldo = transacao.getTipo() == TipoTransacao.CREDITO
                    ? saldo.add(transacao.getValor())
                    : saldo.subtract(transacao.getValor());
        }
        return saldo;
    }

    private BigDecimal somarPorTipo(List<Transacao> transacoes, TipoTransacao tipo) {
        return transacoes.stream()
                .filter(transacao -> transacao.getTipo() == tipo)
                .map(Transacao::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
