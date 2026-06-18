package bcd.appfinanceirobackend.service;

import bcd.appfinanceirobackend.dto.extrato.ProjecaoMensalDTO;
import bcd.appfinanceirobackend.mapper.FaturaMapper;
import bcd.appfinanceirobackend.mapper.TransacaoMapper;
import bcd.appfinanceirobackend.model.Conta;
import bcd.appfinanceirobackend.model.Fatura;
import bcd.appfinanceirobackend.model.Transacao;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.model.enums.StatusFatura;
import bcd.appfinanceirobackend.model.enums.TipoConta;
import bcd.appfinanceirobackend.model.enums.TipoPagamento;
import bcd.appfinanceirobackend.model.enums.TipoTransacao;
import bcd.appfinanceirobackend.repository.FaturaRepository;
import bcd.appfinanceirobackend.repository.TransacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExtratoFuturoService")
class ExtratoFuturoServiceTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 6, 15);

    @Mock
    private TransacaoRepository transacaoRepository;

    @Mock
    private FaturaRepository faturaRepository;

    private ExtratoFuturoService extratoFuturoService;

    private Usuario usuario;
    private Conta conta;

    @BeforeEach
    void setUp() {
        extratoFuturoService = new ExtratoFuturoService(
                transacaoRepository, faturaRepository, new TransacaoMapper(), new FaturaMapper());

        usuario = new Usuario();
        usuario.setId(UUID.randomUUID());

        conta = new Conta();
        conta.setId(UUID.randomUUID());
        conta.setNome("Conta Corrente");
        conta.setTipoConta(TipoConta.CORRENTE);
        conta.setUsuario(usuario);
    }

    private void prepararCenario(List<Transacao> historico, List<Transacao> futuras, List<Fatura> faturasEmAberto) {
        LocalDate fimPeriodo = YearMonth.from(HOJE).plusMonths(2).atEndOfMonth();
        when(transacaoRepository.findAllByContaUsuarioIdAndDataBetween(usuario.getId(), HOJE.plusDays(1), fimPeriodo))
                .thenReturn(futuras);
        when(transacaoRepository.findAllByContaUsuarioIdAndDataLessThanEqual(usuario.getId(), HOJE))
                .thenReturn(historico);
        when(faturaRepository.findAllByContaUsuarioIdAndStatusNot(usuario.getId(), StatusFatura.PAGA))
                .thenReturn(faturasEmAberto);
    }

    private Transacao transacao(LocalDate data, TipoTransacao tipo, String valor, TipoPagamento formaPagamento) {
        Transacao transacao = new Transacao();
        transacao.setId(UUID.randomUUID());
        transacao.setConta(conta);
        transacao.setData(data);
        transacao.setTipo(tipo);
        transacao.setValor(new BigDecimal(valor));
        transacao.setFormaPagamento(formaPagamento);
        transacao.setCategorizada(false);
        transacao.setFutura(data.isAfter(HOJE));
        return transacao;
    }

    private Fatura fatura(LocalDate dataVencimento, String valorTotal, StatusFatura status) {
        Fatura fatura = new Fatura();
        fatura.setId(UUID.randomUUID());
        fatura.setConta(conta);
        fatura.setMesReferencia(YearMonth.from(dataVencimento).minusMonths(1));
        fatura.setDataVencimento(dataVencimento);
        fatura.setValorTotal(new BigDecimal(valorTotal));
        fatura.setStatus(status);
        return fatura;
    }

    @Nested
    @DisplayName("calcularProjecao")
    class CalcularProjecao {

        @Test
        @DisplayName("deve projetar parcelas futuras agrupadas por mês")
        void deveProjetarParcelasFuturasPorMes() {
            Transacao parcelaJulho = transacao(
                    LocalDate.of(2026, 7, 10), TipoTransacao.DEBITO, "300.00", TipoPagamento.CARTAO_CREDITO);
            Transacao parcelaAgosto = transacao(
                    LocalDate.of(2026, 8, 10), TipoTransacao.DEBITO, "300.00", TipoPagamento.CARTAO_CREDITO);
            prepararCenario(List.of(), List.of(parcelaJulho, parcelaAgosto), List.of());

            List<ProjecaoMensalDTO> projecao = extratoFuturoService.calcularProjecao(usuario, 3, HOJE);

            assertThat(projecao).hasSize(3);
            assertThat(projecao.get(0).getMes()).isEqualTo(6);
            assertThat(projecao.get(0).getAno()).isEqualTo(2026);
            assertThat(projecao.get(0).getTransacoes()).isEmpty();
            assertThat(projecao.get(1).getMes()).isEqualTo(7);
            assertThat(projecao.get(1).getTransacoes()).hasSize(1);
            assertThat(projecao.get(1).getTransacoes().getFirst().getTransacaoId()).isEqualTo(parcelaJulho.getId());
            assertThat(projecao.get(1).getTotalDebitos()).isEqualByComparingTo("300.00");
            assertThat(projecao.get(2).getMes()).isEqualTo(8);
            assertThat(projecao.get(2).getTransacoes()).hasSize(1);
            assertThat(projecao.get(2).getTotalDebitos()).isEqualByComparingTo("300.00");
        }

        @Test
        @DisplayName("deve listar boletos futuros no mês correspondente")
        void deveListarBoletosNoMesCorrespondente() {
            Transacao boleto = transacao(
                    LocalDate.of(2026, 7, 5), TipoTransacao.DEBITO, "150.00", TipoPagamento.BOLETO);
            prepararCenario(List.of(), List.of(boleto), List.of());

            List<ProjecaoMensalDTO> projecao = extratoFuturoService.calcularProjecao(usuario, 3, HOJE);

            assertThat(projecao.get(1).getTransacoes()).hasSize(1);
            assertThat(projecao.get(1).getTransacoes().getFirst().getFormaPagamento())
                    .isEqualTo(TipoPagamento.BOLETO);
            assertThat(projecao.get(1).getTotalDebitos()).isEqualByComparingTo("150.00");
        }

        @Test
        @DisplayName("deve incluir faturas em aberto no mês do vencimento")
        void deveIncluirFaturasAbertasComVencimentoNoMes() {
            Fatura faturaJulho = fatura(LocalDate.of(2026, 7, 10), "500.00", StatusFatura.ABERTA);
            prepararCenario(List.of(), List.of(), List.of(faturaJulho));

            List<ProjecaoMensalDTO> projecao = extratoFuturoService.calcularProjecao(usuario, 3, HOJE);

            assertThat(projecao.get(0).getFaturas()).isEmpty();
            assertThat(projecao.get(1).getFaturas()).hasSize(1);
            assertThat(projecao.get(1).getFaturas().getFirst().getFaturaId()).isEqualTo(faturaJulho.getId());
            assertThat(projecao.get(1).getFaturas().getFirst().getDataVencimento())
                    .isEqualTo(LocalDate.of(2026, 7, 10));
            assertThat(projecao.get(1).getTotalDebitos()).isEqualByComparingTo("500.00");
            assertThat(projecao.get(2).getFaturas()).isEmpty();
        }

        @Test
        @DisplayName("não deve contar duas vezes débito já vinculado a fatura")
        void naoDeveContarDuasVezesDebitoVinculadoAFatura() {
            Fatura faturaJulho = fatura(LocalDate.of(2026, 7, 10), "500.00", StatusFatura.ABERTA);
            Transacao parcelaVinculada = transacao(
                    LocalDate.of(2026, 7, 10), TipoTransacao.DEBITO, "500.00", TipoPagamento.CARTAO_CREDITO);
            parcelaVinculada.setFatura(faturaJulho);
            prepararCenario(List.of(), List.of(parcelaVinculada), List.of(faturaJulho));

            List<ProjecaoMensalDTO> projecao = extratoFuturoService.calcularProjecao(usuario, 3, HOJE);

            assertThat(projecao.get(1).getTotalDebitos()).isEqualByComparingTo("500.00");
            assertThat(projecao.get(1).getTransacoes()).hasSize(1);
            assertThat(projecao.get(1).getFaturas()).hasSize(1);
        }

        @Test
        @DisplayName("deve acumular saldo previsto a partir do saldo atual")
        void deveAcumularSaldoPrevistoAPartirDoSaldoAtual() {
            Transacao salarioPassado = transacao(
                    LocalDate.of(2026, 6, 1), TipoTransacao.CREDITO, "1000.00", TipoPagamento.PIX);
            Transacao gastoPassado = transacao(
                    LocalDate.of(2026, 6, 10), TipoTransacao.DEBITO, "200.00", TipoPagamento.PIX);
            Transacao salarioFuturo = transacao(
                    LocalDate.of(2026, 7, 1), TipoTransacao.CREDITO, "500.00", TipoPagamento.PIX);
            Transacao boletoFuturo = transacao(
                    LocalDate.of(2026, 8, 5), TipoTransacao.DEBITO, "300.00", TipoPagamento.BOLETO);
            prepararCenario(
                    List.of(salarioPassado, gastoPassado),
                    List.of(salarioFuturo, boletoFuturo),
                    List.of());

            List<ProjecaoMensalDTO> projecao = extratoFuturoService.calcularProjecao(usuario, 3, HOJE);

            assertThat(projecao.get(0).getSaldoPrevisto()).isEqualByComparingTo("800.00");
            assertThat(projecao.get(1).getSaldoPrevisto()).isEqualByComparingTo("1300.00");
            assertThat(projecao.get(1).getTotalCreditos()).isEqualByComparingTo("500.00");
            assertThat(projecao.get(2).getSaldoPrevisto()).isEqualByComparingTo("1000.00");
        }

        @Test
        @DisplayName("não deve descontar do saldo débito passado vinculado a fatura em aberto")
        void naoDeveDescontarDoSaldoDebitoVinculadoAFaturaEmAberto() {
            Fatura faturaJulho = fatura(LocalDate.of(2026, 7, 10), "500.00", StatusFatura.ABERTA);
            Transacao salarioPassado = transacao(
                    LocalDate.of(2026, 6, 1), TipoTransacao.CREDITO, "1000.00", TipoPagamento.PIX);
            Transacao compraPassada = transacao(
                    LocalDate.of(2026, 6, 5), TipoTransacao.DEBITO, "500.00", TipoPagamento.CARTAO_CREDITO);
            compraPassada.setFatura(faturaJulho);
            prepararCenario(List.of(salarioPassado, compraPassada), List.of(), List.of(faturaJulho));

            List<ProjecaoMensalDTO> projecao = extratoFuturoService.calcularProjecao(usuario, 3, HOJE);

            // A compra só sai do caixa no vencimento da fatura, em julho
            assertThat(projecao.get(0).getSaldoPrevisto()).isEqualByComparingTo("1000.00");
            assertThat(projecao.get(1).getSaldoPrevisto()).isEqualByComparingTo("500.00");
        }

        @Test
        @DisplayName("deve descontar do saldo débito passado vinculado a fatura já paga")
        void deveDescontarDoSaldoDebitoVinculadoAFaturaPaga() {
            Fatura faturaPaga = fatura(LocalDate.of(2026, 5, 10), "500.00", StatusFatura.PAGA);
            Transacao compraPassada = transacao(
                    LocalDate.of(2026, 5, 2), TipoTransacao.DEBITO, "500.00", TipoPagamento.CARTAO_CREDITO);
            compraPassada.setFatura(faturaPaga);
            prepararCenario(List.of(compraPassada), List.of(), List.of());

            List<ProjecaoMensalDTO> projecao = extratoFuturoService.calcularProjecao(usuario, 3, HOJE);

            assertThat(projecao.get(0).getSaldoPrevisto()).isEqualByComparingTo("-500.00");
        }

        @Test
        @DisplayName("não deve somar crédito futuro vinculado a fatura nos totais")
        void naoDeveSomarCreditoVinculadoAFatura() {
            Fatura faturaJulho = fatura(LocalDate.of(2026, 7, 10), "470.00", StatusFatura.ABERTA);
            Transacao estornoFuturo = transacao(
                    LocalDate.of(2026, 7, 12), TipoTransacao.CREDITO, "30.00", TipoPagamento.CARTAO_CREDITO);
            estornoFuturo.setFatura(faturaJulho);
            prepararCenario(List.of(), List.of(estornoFuturo), List.of(faturaJulho));

            List<ProjecaoMensalDTO> projecao = extratoFuturoService.calcularProjecao(usuario, 3, HOJE);

            // O estorno já abate o valorTotal da fatura; somar de novo contaria duas vezes
            assertThat(projecao.get(1).getTotalCreditos()).isEqualByComparingTo("0");
            assertThat(projecao.get(1).getTotalDebitos()).isEqualByComparingTo("470.00");
            assertThat(projecao.get(1).getTransacoes()).hasSize(1);
        }

        @Test
        @DisplayName("deve incluir fatura atrasada não paga no primeiro mês projetado")
        void deveIncluirFaturaAtrasadaNoPrimeiroMes() {
            Fatura faturaAtrasada = fatura(LocalDate.of(2026, 5, 10), "200.00", StatusFatura.FECHADA);
            prepararCenario(List.of(), List.of(), List.of(faturaAtrasada));

            List<ProjecaoMensalDTO> projecao = extratoFuturoService.calcularProjecao(usuario, 3, HOJE);

            assertThat(projecao.get(0).getFaturas()).hasSize(1);
            assertThat(projecao.get(0).getTotalDebitos()).isEqualByComparingTo("200.00");
            assertThat(projecao.get(1).getFaturas()).isEmpty();
            assertThat(projecao.get(2).getFaturas()).isEmpty();
        }

        @Test
        @DisplayName("deve projetar 3 meses por padrão quando a quantidade não é informada")
        void deveProjetarTresMesesPorPadrao() {
            when(transacaoRepository.findAllByContaUsuarioIdAndDataBetween(
                    eq(usuario.getId()), any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of());
            when(transacaoRepository.findAllByContaUsuarioIdAndDataLessThanEqual(
                    eq(usuario.getId()), any(LocalDate.class))).thenReturn(List.of());
            when(faturaRepository.findAllByContaUsuarioIdAndStatusNot(usuario.getId(), StatusFatura.PAGA))
                    .thenReturn(List.of());

            List<ProjecaoMensalDTO> projecao = extratoFuturoService.calcularProjecao(usuario, (Integer) null);

            assertThat(projecao).hasSize(3);
        }

        @Test
        @DisplayName("deve retornar meses vazios quando não há transações futuras")
        void deveRetornarMesesVaziosQuandoNaoHaTransacoesFuturas() {
            prepararCenario(List.of(), List.of(), List.of());

            List<ProjecaoMensalDTO> projecao = extratoFuturoService.calcularProjecao(usuario, 3, HOJE);

            assertThat(projecao).hasSize(3);
            for (ProjecaoMensalDTO mes : projecao) {
                assertThat(mes.getTransacoes()).isEmpty();
                assertThat(mes.getFaturas()).isEmpty();
                assertThat(mes.getTotalDebitos()).isEqualByComparingTo("0");
                assertThat(mes.getTotalCreditos()).isEqualByComparingTo("0");
                assertThat(mes.getSaldoPrevisto()).isEqualByComparingTo("0");
            }
        }

        @Test
        @DisplayName("deve preencher período de cada mês projetado")
        void devePreencherPeriodoDeCadaMes() {
            prepararCenario(List.of(), List.of(), List.of());

            List<ProjecaoMensalDTO> projecao = extratoFuturoService.calcularProjecao(usuario, 3, HOJE);

            assertThat(projecao.get(0).getDataInicio()).isEqualTo(LocalDate.of(2026, 6, 1));
            assertThat(projecao.get(0).getDataFim()).isEqualTo(LocalDate.of(2026, 6, 30));
            assertThat(projecao.get(2).getDataInicio()).isEqualTo(LocalDate.of(2026, 8, 1));
            assertThat(projecao.get(2).getDataFim()).isEqualTo(LocalDate.of(2026, 8, 31));
        }

        @Test
        @DisplayName("deve rejeitar quantidade de meses fora do intervalo permitido")
        void deveRejeitarQuantidadeDeMesesInvalida() {
            assertThatThrownBy(() -> extratoFuturoService.calcularProjecao(usuario, 0, HOJE))
                    .isInstanceOf(ResponseStatusException.class)
                    .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                    .isEqualTo(HttpStatus.BAD_REQUEST);
            assertThatThrownBy(() -> extratoFuturoService.calcularProjecao(usuario, 13, HOJE))
                    .isInstanceOf(ResponseStatusException.class)
                    .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                    .isEqualTo(HttpStatus.BAD_REQUEST);
            verifyNoInteractions(transacaoRepository, faturaRepository);
        }
    }

    @Nested
    @DisplayName("listarTransacoesFuturas")
    class ListarTransacoesFuturas {

        @Test
        @DisplayName("deve buscar transações a partir do dia seguinte")
        void deveBuscarTransacoesAPartirDoDiaSeguinte() {
            LocalDate fimPeriodo = LocalDate.of(2026, 8, 31);
            Transacao futura = transacao(
                    LocalDate.of(2026, 7, 1), TipoTransacao.DEBITO, "100.00", TipoPagamento.BOLETO);
            when(transacaoRepository.findAllByContaUsuarioIdAndDataBetween(
                    usuario.getId(), HOJE.plusDays(1), fimPeriodo)).thenReturn(List.of(futura));

            List<Transacao> resultado = extratoFuturoService.listarTransacoesFuturas(usuario, HOJE, fimPeriodo);

            assertThat(resultado).containsExactly(futura);
        }
    }
}
