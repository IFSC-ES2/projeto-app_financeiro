package bcd.appfinanceirobackend.service;

import bcd.appfinanceirobackend.dto.extrato.ProjecaoMensalDTO;
import bcd.appfinanceirobackend.mapper.TransacaoMapper;
import bcd.appfinanceirobackend.model.Conta;
import bcd.appfinanceirobackend.model.Fatura;
import bcd.appfinanceirobackend.model.Transacao;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.model.enums.StatusFatura;
import bcd.appfinanceirobackend.model.enums.TipoPagamento;
import bcd.appfinanceirobackend.model.enums.TipoTransacao;
import bcd.appfinanceirobackend.repository.FaturaRepository;
import bcd.appfinanceirobackend.repository.TransacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExtratoFuturoService - projecao dos proximos meses")
class ExtratoFuturoServiceTest {

    @Mock
    private TransacaoRepository transacaoRepository;

    @Mock
    private FaturaRepository faturaRepository;

    private ExtratoFuturoService extratoFuturoService;
    private Usuario usuario;
    private Conta conta;
    private YearMonth mesAtual;

    @BeforeEach
    void setUp() {
        extratoFuturoService = new ExtratoFuturoService(
                transacaoRepository,
                faturaRepository,
                new TransacaoMapper()
        );

        usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setNome("Usuario Teste");
        usuario.setEmail("usuario@test.com");

        conta = new Conta();
        conta.setId(UUID.randomUUID());
        conta.setNome("Conta Principal");
        conta.setUsuario(usuario);

        mesAtual = YearMonth.now();
    }

    private Transacao transacao(
            String valor,
            LocalDate data,
            TipoTransacao tipo,
            TipoPagamento formaPagamento,
            Fatura fatura
    ) {
        Transacao t = new Transacao();
        t.setId(UUID.randomUUID());
        t.setConta(conta);
        t.setValor(new BigDecimal(valor));
        t.setData(data);
        t.setTipo(tipo);
        t.setFormaPagamento(formaPagamento);
        t.setFatura(fatura);
        t.setFutura(true);
        t.setCategorizada(false);
        return t;
    }

    private Fatura faturaAberta(LocalDate vencimento) {
        Conta contaCartao = new Conta();
        contaCartao.setId(UUID.randomUUID());
        contaCartao.setNome("Cartao Nubank");
        contaCartao.setUsuario(usuario);

        Fatura f = new Fatura();
        f.setId(UUID.randomUUID());
        f.setConta(contaCartao);
        f.setMesReferencia(YearMonth.from(vencimento));
        f.setDataVencimento(vencimento);
        f.setValorTotal(BigDecimal.ZERO);
        f.setStatus(StatusFatura.ABERTA);
        return f;
    }

    private void mockRepositorios(List<Transacao> transacoes, List<Fatura> faturas) {
        when(transacaoRepository.findAllByContaUsuarioIdAndFuturaTrueAndDataBetweenOrderByDataAsc(
                eq(usuario.getId()), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(transacoes);
        when(faturaRepository.findAllByContaUsuarioIdAndStatusAndDataVencimentoBetweenOrderByDataVencimentoAsc(
                eq(usuario.getId()), eq(StatusFatura.ABERTA), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(faturas);
    }

    @Test
    @DisplayName("calcula a projecao padrao dos proximos 3 meses")
    void deveCalcularProjecaoPadraoDeTresMeses() {
        mockRepositorios(List.of(), List.of());

        List<ProjecaoMensalDTO> projecoes = extratoFuturoService.calcularProjecao(usuario, null);

        assertThat(projecoes).hasSize(3);
        assertThat(projecoes.get(0).getMes()).isEqualTo(mesAtual);
        assertThat(projecoes.get(1).getMes()).isEqualTo(mesAtual.plusMonths(1));
        assertThat(projecoes.get(2).getMes()).isEqualTo(mesAtual.plusMonths(2));
    }

    @Test
    @DisplayName("usuario sem dados futuros retorna meses zerados e listas vazias")
    void deveRetornarZeradoSemDadosFuturos() {
        mockRepositorios(List.of(), List.of());

        List<ProjecaoMensalDTO> projecoes = extratoFuturoService.calcularProjecao(usuario, 3);

        assertThat(projecoes).hasSize(3);
        assertThat(projecoes).allSatisfy(projecao -> {
            assertThat(projecao.getTotalCreditos()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(projecao.getTotalDebitos()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(projecao.getSaldoPrevisto()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(projecao.getQuantidadeVencimentos()).isZero();
            assertThat(projecao.getTransacoes()).isEmpty();
            assertThat(projecao.getFaturas()).isEmpty();
        });
    }

    @Test
    @DisplayName("aloca boleto e transacao futura no mes correto da projecao")
    void deveAlocarTransacoesFuturasNoMesCorreto() {
        Transacao salarioFuturo = transacao(
                "1000.00", mesAtual.atDay(5), TipoTransacao.CREDITO, TipoPagamento.PIX, null);
        Transacao boletoMesSeguinte = transacao(
                "200.00", mesAtual.plusMonths(1).atDay(15), TipoTransacao.DEBITO, TipoPagamento.BOLETO, null);
        mockRepositorios(List.of(salarioFuturo, boletoMesSeguinte), List.of());

        List<ProjecaoMensalDTO> projecoes = extratoFuturoService.calcularProjecao(usuario, 3);

        ProjecaoMensalDTO atual = projecoes.get(0);
        assertThat(atual.getTotalCreditos()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(atual.getTotalDebitos()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(atual.getSaldoPrevisto()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(atual.getTransacoes()).hasSize(1);

        ProjecaoMensalDTO seguinte = projecoes.get(1);
        assertThat(seguinte.getTotalDebitos()).isEqualByComparingTo(new BigDecimal("200.00"));
        assertThat(seguinte.getSaldoPrevisto()).isEqualByComparingTo(new BigDecimal("-200.00"));
        assertThat(seguinte.getQuantidadeVencimentos()).isEqualTo(1);
        assertThat(seguinte.getTransacoes()).hasSize(1);
        assertThat(seguinte.getTransacoes().getFirst().getValor())
                .isEqualByComparingTo(new BigDecimal("200.00"));

        assertThat(projecoes.get(2).getTransacoes()).isEmpty();
    }

    @Test
    @DisplayName("inclui fatura aberta no mes de vencimento somando o total dela")
    void deveIncluirFaturaAbertaNoMesDeVencimento() {
        Fatura fatura = faturaAberta(mesAtual.plusMonths(1).atDay(10));
        mockRepositorios(List.of(), List.of(fatura));
        when(transacaoRepository.somarValorPorFatura(fatura.getId()))
                .thenReturn(new BigDecimal("350.00"));

        List<ProjecaoMensalDTO> projecoes = extratoFuturoService.calcularProjecao(usuario, 3);

        assertThat(projecoes.get(0).getFaturas()).isEmpty();

        ProjecaoMensalDTO mesDaFatura = projecoes.get(1);
        assertThat(mesDaFatura.getFaturas()).hasSize(1);
        assertThat(mesDaFatura.getFaturas().getFirst().getNomeConta()).isEqualTo("Cartao Nubank");
        assertThat(mesDaFatura.getFaturas().getFirst().getValorTotal())
                .isEqualByComparingTo(new BigDecimal("350.00"));
        assertThat(mesDaFatura.getTotalDebitos()).isEqualByComparingTo(new BigDecimal("350.00"));
        assertThat(mesDaFatura.getSaldoPrevisto()).isEqualByComparingTo(new BigDecimal("-350.00"));
        assertThat(mesDaFatura.getQuantidadeVencimentos()).isEqualTo(1);
    }

    @Test
    @DisplayName("nao soma duas vezes debito que ja esta dentro da fatura")
    void naoDeveSomarDuasVezesDebitoDaFatura() {
        Fatura fatura = faturaAberta(mesAtual.atDay(10));
        Transacao compraNoCartao = transacao(
                "120.00", mesAtual.atDay(6), TipoTransacao.DEBITO, TipoPagamento.CARTAO_CREDITO, fatura);
        mockRepositorios(List.of(compraNoCartao), List.of(fatura));
        when(transacaoRepository.somarValorPorFatura(fatura.getId()))
                .thenReturn(new BigDecimal("120.00"));

        ProjecaoMensalDTO projecao = extratoFuturoService.calcularProjecao(usuario, 1).getFirst();

        assertThat(projecao.getTotalDebitos()).isEqualByComparingTo(new BigDecimal("120.00"));
        assertThat(projecao.getQuantidadeVencimentos()).isEqualTo(1);
    }

    @Test
    @DisplayName("consulta os repositorios apenas com o id do usuario autenticado e o periodo projetado")
    void deveConsultarApenasDadosDoUsuarioAutenticado() {
        mockRepositorios(List.of(), List.of());

        extratoFuturoService.calcularProjecao(usuario, 3);

        ArgumentCaptor<LocalDate> inicioTransacoes = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> fimTransacoes = ArgumentCaptor.forClass(LocalDate.class);
        verify(transacaoRepository).findAllByContaUsuarioIdAndFuturaTrueAndDataBetweenOrderByDataAsc(
                eq(usuario.getId()), inicioTransacoes.capture(), fimTransacoes.capture());
        assertThat(inicioTransacoes.getValue()).isEqualTo(mesAtual.atDay(1));
        assertThat(fimTransacoes.getValue()).isEqualTo(mesAtual.plusMonths(2).atEndOfMonth());

        verify(faturaRepository).findAllByContaUsuarioIdAndStatusAndDataVencimentoBetweenOrderByDataVencimentoAsc(
                eq(usuario.getId()), eq(StatusFatura.ABERTA), any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    @DisplayName("rejeita quantidade de meses fora de 1 a 12")
    void deveRejeitarQuantidadeDeMesesInvalida() {
        assertThatThrownBy(() -> extratoFuturoService.calcularProjecao(usuario, 0))
                .isInstanceOfSatisfying(ResponseStatusException.class, ex ->
                        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));

        assertThatThrownBy(() -> extratoFuturoService.calcularProjecao(usuario, 13))
                .isInstanceOfSatisfying(ResponseStatusException.class, ex ->
                        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));

        verifyNoInteractions(transacaoRepository, faturaRepository);
    }
}
