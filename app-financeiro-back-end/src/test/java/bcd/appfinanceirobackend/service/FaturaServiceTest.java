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
import bcd.appfinanceirobackend.model.enums.TipoConta;
import bcd.appfinanceirobackend.model.enums.TipoTransacao;
import bcd.appfinanceirobackend.repository.CartaoCreditoRepository;
import bcd.appfinanceirobackend.repository.ContaRepository;
import bcd.appfinanceirobackend.repository.FaturaRepository;
import bcd.appfinanceirobackend.repository.TransacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FaturaService")
class FaturaServiceTest {

    @Mock
    private FaturaRepository faturaRepository;

    @Mock
    private ContaRepository contaRepository;

    @Mock
    private CartaoCreditoRepository cartaoCreditoRepository;

    @Mock
    private TransacaoRepository transacaoRepository;

    private FaturaService faturaService;

    private Usuario usuario;
    private Usuario outroUsuario;
    private Conta conta;

    @BeforeEach
    void setUp() {
        faturaService = new FaturaService(
                faturaRepository, contaRepository, cartaoCreditoRepository, transacaoRepository, new FaturaMapper());

        usuario = new Usuario();
        usuario.setId(UUID.randomUUID());

        outroUsuario = new Usuario();
        outroUsuario.setId(UUID.randomUUID());

        conta = new Conta();
        conta.setId(UUID.randomUUID());
        conta.setNome("Cartão Nubank");
        conta.setTipoConta(TipoConta.CARTAO_CREDITO);
        conta.setUsuario(usuario);
    }

    private CartaoCredito cartao(int diaFechamento, int diaVencimento) {
        CartaoCredito cartao = new CartaoCredito();
        cartao.setId(UUID.randomUUID());
        cartao.setConta(conta);
        cartao.setDia_fechamento(diaFechamento);
        cartao.setDia_vencimento(diaVencimento);
        return cartao;
    }

    private Fatura fatura(StatusFatura status) {
        Fatura fatura = new Fatura();
        fatura.setId(UUID.randomUUID());
        fatura.setConta(conta);
        fatura.setMesReferencia(YearMonth.of(2026, 7));
        fatura.setDataVencimento(LocalDate.of(2026, 7, 28));
        fatura.setValorTotal(new BigDecimal("500.00"));
        fatura.setStatus(status);
        return fatura;
    }

    private Transacao transacaoDaFatura(TipoTransacao tipo, String valor) {
        Transacao transacao = new Transacao();
        transacao.setId(UUID.randomUUID());
        transacao.setConta(conta);
        transacao.setData(LocalDate.of(2026, 7, 10));
        transacao.setTipo(tipo);
        transacao.setValor(new BigDecimal(valor));
        return transacao;
    }

    @Nested
    @DisplayName("gerarFatura")
    class GerarFatura {

        @Test
        @DisplayName("deve retornar fatura existente sem criar outra")
        void deveRetornarFaturaExistente() {
            Fatura existente = fatura(StatusFatura.ABERTA);
            when(contaRepository.findById(conta.getId())).thenReturn(Optional.of(conta));
            when(faturaRepository.findByContaIdAndMesReferencia(conta.getId(), YearMonth.of(2026, 7)))
                    .thenReturn(Optional.of(existente));

            Fatura resultado = faturaService.gerarFatura(conta.getId(), YearMonth.of(2026, 7), usuario);

            assertThat(resultado).isSameAs(existente);
            verify(faturaRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve negar geração de fatura em conta de outro usuário")
        void deveNegarGeracaoParaContaDeOutroUsuario() {
            when(contaRepository.findById(conta.getId())).thenReturn(Optional.of(conta));

            assertThatThrownBy(() -> faturaService.gerarFatura(conta.getId(), YearMonth.of(2026, 7), outroUsuario))
                    .isInstanceOf(ResponseStatusException.class)
                    .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                    .isEqualTo(HttpStatus.FORBIDDEN);
            verify(faturaRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve criar fatura aberta com vencimento no próprio mês quando vencimento é após o fechamento")
        void deveCriarFaturaComVencimentoNoProprioMes() {
            when(faturaRepository.findByContaIdAndMesReferencia(conta.getId(), YearMonth.of(2026, 7)))
                    .thenReturn(Optional.empty());
            when(contaRepository.findById(conta.getId())).thenReturn(Optional.of(conta));
            when(cartaoCreditoRepository.findByContaId(conta.getId())).thenReturn(Optional.of(cartao(20, 28)));
            when(faturaRepository.save(any(Fatura.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Fatura resultado = faturaService.gerarFatura(conta.getId(), YearMonth.of(2026, 7), usuario);

            assertThat(resultado.getStatus()).isEqualTo(StatusFatura.ABERTA);
            assertThat(resultado.getValorTotal()).isEqualByComparingTo("0");
            assertThat(resultado.getMesReferencia()).isEqualTo(YearMonth.of(2026, 7));
            assertThat(resultado.getDataVencimento()).isEqualTo(LocalDate.of(2026, 7, 28));
        }

        @Test
        @DisplayName("deve criar fatura com vencimento no mês seguinte quando vencimento é antes do fechamento")
        void deveCriarFaturaComVencimentoNoMesSeguinte() {
            when(faturaRepository.findByContaIdAndMesReferencia(conta.getId(), YearMonth.of(2026, 7)))
                    .thenReturn(Optional.empty());
            when(contaRepository.findById(conta.getId())).thenReturn(Optional.of(conta));
            when(cartaoCreditoRepository.findByContaId(conta.getId())).thenReturn(Optional.of(cartao(25, 5)));
            when(faturaRepository.save(any(Fatura.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Fatura resultado = faturaService.gerarFatura(conta.getId(), YearMonth.of(2026, 7), usuario);

            assertThat(resultado.getDataVencimento()).isEqualTo(LocalDate.of(2026, 8, 5));
        }

        @Test
        @DisplayName("deve ajustar o dia de vencimento ao tamanho do mês")
        void deveAjustarDiaDeVencimentoAoTamanhoDoMes() {
            when(faturaRepository.findByContaIdAndMesReferencia(conta.getId(), YearMonth.of(2026, 2)))
                    .thenReturn(Optional.empty());
            when(contaRepository.findById(conta.getId())).thenReturn(Optional.of(conta));
            when(cartaoCreditoRepository.findByContaId(conta.getId())).thenReturn(Optional.of(cartao(20, 31)));
            when(faturaRepository.save(any(Fatura.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Fatura resultado = faturaService.gerarFatura(conta.getId(), YearMonth.of(2026, 2), usuario);

            assertThat(resultado.getDataVencimento()).isEqualTo(LocalDate.of(2026, 2, 28));
        }

        @Test
        @DisplayName("deve falhar quando a conta não possui cartão de crédito")
        void deveFalharSemCartaoDeCredito() {
            when(faturaRepository.findByContaIdAndMesReferencia(conta.getId(), YearMonth.of(2026, 7)))
                    .thenReturn(Optional.empty());
            when(contaRepository.findById(conta.getId())).thenReturn(Optional.of(conta));
            when(cartaoCreditoRepository.findByContaId(conta.getId())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> faturaService.gerarFatura(conta.getId(), YearMonth.of(2026, 7), usuario))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(faturaRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("calcularTotal")
    class CalcularTotal {

        @Test
        @DisplayName("deve somar débitos, abater créditos e persistir o total")
        void deveSomarDebitosEAbaterCreditos() {
            Fatura faturaAberta = fatura(StatusFatura.ABERTA);
            when(faturaRepository.findById(faturaAberta.getId())).thenReturn(Optional.of(faturaAberta));
            when(transacaoRepository.findAllByFaturaId(faturaAberta.getId())).thenReturn(List.of(
                    transacaoDaFatura(TipoTransacao.DEBITO, "100.00"),
                    transacaoDaFatura(TipoTransacao.DEBITO, "50.00"),
                    transacaoDaFatura(TipoTransacao.CREDITO, "30.00")));

            BigDecimal total = faturaService.calcularTotal(faturaAberta.getId(), usuario);

            assertThat(total).isEqualByComparingTo("120.00");
            ArgumentCaptor<Fatura> captor = ArgumentCaptor.forClass(Fatura.class);
            verify(faturaRepository).save(captor.capture());
            assertThat(captor.getValue().getValorTotal()).isEqualByComparingTo("120.00");
        }

        @Test
        @DisplayName("deve falhar quando a fatura não existe")
        void deveFalharQuandoFaturaNaoExiste() {
            UUID faturaId = UUID.randomUUID();
            when(faturaRepository.findById(faturaId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> faturaService.calcularTotal(faturaId, usuario))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("deve negar recálculo de fatura de outro usuário")
        void deveNegarRecalculoParaFaturaDeOutroUsuario() {
            Fatura faturaAberta = fatura(StatusFatura.ABERTA);
            when(faturaRepository.findById(faturaAberta.getId())).thenReturn(Optional.of(faturaAberta));

            assertThatThrownBy(() -> faturaService.calcularTotal(faturaAberta.getId(), outroUsuario))
                    .isInstanceOf(ResponseStatusException.class)
                    .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                    .isEqualTo(HttpStatus.FORBIDDEN);
            verify(faturaRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("pagar")
    class Pagar {

        @Test
        @DisplayName("deve marcar fatura como paga")
        void deveMarcarFaturaComoPaga() {
            Fatura faturaAberta = fatura(StatusFatura.ABERTA);
            when(faturaRepository.findById(faturaAberta.getId())).thenReturn(Optional.of(faturaAberta));
            when(faturaRepository.save(any(Fatura.class))).thenAnswer(invocation -> invocation.getArgument(0));

            FaturaResumoDTO resultado = faturaService.pagar(faturaAberta.getId(), usuario);

            assertThat(resultado.getStatus()).isEqualTo(StatusFatura.PAGA);
            assertThat(resultado.getFaturaId()).isEqualTo(faturaAberta.getId());
        }

        @Test
        @DisplayName("deve rejeitar pagamento de fatura já paga")
        void deveRejeitarFaturaJaPaga() {
            Fatura faturaPaga = fatura(StatusFatura.PAGA);
            when(faturaRepository.findById(faturaPaga.getId())).thenReturn(Optional.of(faturaPaga));

            assertThatThrownBy(() -> faturaService.pagar(faturaPaga.getId(), usuario))
                    .isInstanceOf(ResponseStatusException.class)
                    .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                    .isEqualTo(HttpStatus.CONFLICT);
            verify(faturaRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve negar acesso a fatura de outro usuário")
        void deveNegarAcessoAFaturaDeOutroUsuario() {
            Fatura faturaAberta = fatura(StatusFatura.ABERTA);
            when(faturaRepository.findById(faturaAberta.getId())).thenReturn(Optional.of(faturaAberta));

            assertThatThrownBy(() -> faturaService.pagar(faturaAberta.getId(), outroUsuario))
                    .isInstanceOf(ResponseStatusException.class)
                    .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                    .isEqualTo(HttpStatus.FORBIDDEN);
        }
    }

    @Nested
    @DisplayName("buscarPorConta")
    class BuscarPorConta {

        @Test
        @DisplayName("deve listar faturas da conta como resumo")
        void deveListarFaturasDaConta() {
            Fatura faturaAberta = fatura(StatusFatura.ABERTA);
            when(contaRepository.findById(conta.getId())).thenReturn(Optional.of(conta));
            when(faturaRepository.findAllByContaIdOrderByMesReferenciaDesc(conta.getId()))
                    .thenReturn(List.of(faturaAberta));

            List<FaturaResumoDTO> resultado = faturaService.buscarPorConta(conta.getId(), usuario);

            assertThat(resultado).hasSize(1);
            assertThat(resultado.getFirst().getFaturaId()).isEqualTo(faturaAberta.getId());
            assertThat(resultado.getFirst().getContaId()).isEqualTo(conta.getId());
            assertThat(resultado.getFirst().getMesReferencia()).isEqualTo("2026-07");
        }

        @Test
        @DisplayName("deve falhar quando a conta não existe")
        void deveFalharQuandoContaNaoExiste() {
            UUID contaId = UUID.randomUUID();
            when(contaRepository.findById(contaId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> faturaService.buscarPorConta(contaId, usuario))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("deve negar acesso a conta de outro usuário")
        void deveNegarAcessoAContaDeOutroUsuario() {
            when(contaRepository.findById(conta.getId())).thenReturn(Optional.of(conta));

            assertThatThrownBy(() -> faturaService.buscarPorConta(conta.getId(), outroUsuario))
                    .isInstanceOf(ResponseStatusException.class)
                    .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                    .isEqualTo(HttpStatus.FORBIDDEN);
        }
    }

    @Nested
    @DisplayName("buscarPorId")
    class BuscarPorId {

        @Test
        @DisplayName("deve retornar resumo da fatura")
        void deveRetornarResumoDaFatura() {
            Fatura faturaAberta = fatura(StatusFatura.ABERTA);
            when(faturaRepository.findById(faturaAberta.getId())).thenReturn(Optional.of(faturaAberta));

            FaturaResumoDTO resultado = faturaService.buscarPorId(faturaAberta.getId(), usuario);

            assertThat(resultado.getFaturaId()).isEqualTo(faturaAberta.getId());
            assertThat(resultado.getValorTotal()).isEqualByComparingTo("500.00");
            assertThat(resultado.getContaNome()).isEqualTo("Cartão Nubank");
        }

        @Test
        @DisplayName("deve falhar quando a fatura não existe")
        void deveFalharQuandoFaturaNaoExiste() {
            UUID faturaId = UUID.randomUUID();
            when(faturaRepository.findById(faturaId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> faturaService.buscarPorId(faturaId, usuario))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
