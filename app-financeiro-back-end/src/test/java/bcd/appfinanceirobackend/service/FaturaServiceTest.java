package bcd.appfinanceirobackend.service;

import bcd.appfinanceirobackend.dto.fatura.FaturaResumoDTO;
import bcd.appfinanceirobackend.exception.ResourceNotFoundException;
import bcd.appfinanceirobackend.model.CartaoCredito;
import bcd.appfinanceirobackend.model.Conta;
import bcd.appfinanceirobackend.model.Fatura;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.model.enums.StatusFatura;
import bcd.appfinanceirobackend.model.enums.TipoConta;
import bcd.appfinanceirobackend.repository.FaturaRepository;
import bcd.appfinanceirobackend.repository.TransacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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
    private TransacaoRepository transacaoRepository;

    @Mock
    private CartaoCreditoService cartaoCreditoService;

    @InjectMocks
    private FaturaService faturaService;

    private Usuario dono;
    private Usuario outroUsuario;
    private Conta contaCartao;
    private CartaoCredito cartao;

    @BeforeEach
    void setUp() {
        dono = usuario("dono@test.com");
        outroUsuario = usuario("outro@test.com");

        contaCartao = new Conta();
        contaCartao.setId(UUID.randomUUID());
        contaCartao.setNome("Cartao Nubank");
        contaCartao.setTipoConta(TipoConta.CARTAO_CREDITO);
        contaCartao.setUsuario(dono);

        cartao = new CartaoCredito();
        cartao.setId(UUID.randomUUID());
        cartao.setConta(contaCartao);
        cartao.setDia_fechamento(3);
        cartao.setDia_vencimento(10);
    }

    private Usuario usuario(String email) {
        Usuario u = new Usuario();
        u.setId(UUID.randomUUID());
        u.setNome("Usuario Teste");
        u.setEmail(email);
        return u;
    }

    private Fatura fatura(YearMonth mes, LocalDate vencimento, StatusFatura status) {
        Fatura f = new Fatura();
        f.setId(UUID.randomUUID());
        f.setConta(contaCartao);
        f.setMesReferencia(mes);
        f.setDataVencimento(vencimento);
        f.setValorTotal(BigDecimal.ZERO);
        f.setStatus(status);
        return f;
    }

    @Nested
    @DisplayName("gerarFatura()")
    class GerarFatura {

        @Test
        @DisplayName("gera fatura ABERTA com vencimento no dia do cartao")
        void deveGerarFaturaAberta() {
            YearMonth mes = YearMonth.of(2026, 8);
            when(cartaoCreditoService.buscarPorConta(contaCartao.getId(), dono)).thenReturn(cartao);
            when(faturaRepository.findByContaIdAndMesReferencia(contaCartao.getId(), mes))
                    .thenReturn(Optional.empty());
            when(faturaRepository.save(any(Fatura.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Fatura gerada = faturaService.gerarFatura(contaCartao.getId(), mes, dono);

            assertThat(gerada.getConta()).isEqualTo(contaCartao);
            assertThat(gerada.getMesReferencia()).isEqualTo(mes);
            assertThat(gerada.getDataVencimento()).isEqualTo(LocalDate.of(2026, 8, 10));
            assertThat(gerada.getValorTotal()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(gerada.getStatus()).isEqualTo(StatusFatura.ABERTA);
        }

        @Test
        @DisplayName("ajusta o vencimento para o ultimo dia quando o mes e mais curto")
        void deveAjustarVencimentoParaUltimoDiaDoMes() {
            cartao.setDia_vencimento(31);
            YearMonth fevereiro = YearMonth.of(2026, 2);
            when(cartaoCreditoService.buscarPorConta(contaCartao.getId(), dono)).thenReturn(cartao);
            when(faturaRepository.findByContaIdAndMesReferencia(contaCartao.getId(), fevereiro))
                    .thenReturn(Optional.empty());
            when(faturaRepository.save(any(Fatura.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Fatura gerada = faturaService.gerarFatura(contaCartao.getId(), fevereiro, dono);

            assertThat(gerada.getDataVencimento()).isEqualTo(LocalDate.of(2026, 2, 28));
        }

        @Test
        @DisplayName("retorna a fatura existente sem criar outra para o mesmo mes")
        void deveRetornarFaturaExistente() {
            YearMonth mes = YearMonth.of(2026, 8);
            Fatura existente = fatura(mes, LocalDate.of(2026, 8, 10), StatusFatura.ABERTA);
            when(cartaoCreditoService.buscarPorConta(contaCartao.getId(), dono)).thenReturn(cartao);
            when(faturaRepository.findByContaIdAndMesReferencia(contaCartao.getId(), mes))
                    .thenReturn(Optional.of(existente));

            Fatura resultado = faturaService.gerarFatura(contaCartao.getId(), mes, dono);

            assertThat(resultado).isEqualTo(existente);
            verify(faturaRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("buscarPorConta() e buscarPorId()")
    class Buscar {

        @Test
        @DisplayName("lista faturas da conta recalculando o valor total")
        void deveListarFaturasDaContaComTotalAtualizado() {
            Fatura agosto = fatura(YearMonth.of(2026, 8), LocalDate.of(2026, 8, 10), StatusFatura.ABERTA);
            Fatura julho = fatura(YearMonth.of(2026, 7), LocalDate.of(2026, 7, 10), StatusFatura.PAGA);
            when(cartaoCreditoService.buscarPorConta(contaCartao.getId(), dono)).thenReturn(cartao);
            when(faturaRepository.findAllByContaIdOrderByMesReferenciaDesc(contaCartao.getId()))
                    .thenReturn(List.of(agosto, julho));
            when(transacaoRepository.somarValorPorFatura(agosto.getId()))
                    .thenReturn(new BigDecimal("250.00"));
            when(transacaoRepository.somarValorPorFatura(julho.getId()))
                    .thenReturn(new BigDecimal("100.00"));

            List<FaturaResumoDTO> resultado = faturaService.buscarPorConta(contaCartao.getId(), dono);

            assertThat(resultado).hasSize(2);
            assertThat(resultado.getFirst().getFaturaId()).isEqualTo(agosto.getId());
            assertThat(resultado.getFirst().getNomeConta()).isEqualTo("Cartao Nubank");
            assertThat(resultado.getFirst().getValorTotal()).isEqualByComparingTo(new BigDecimal("250.00"));
            assertThat(resultado.get(1).getValorTotal()).isEqualByComparingTo(new BigDecimal("100.00"));
        }

        @Test
        @DisplayName("busca fatura por id do usuario dono")
        void deveBuscarFaturaPorId() {
            Fatura fatura = fatura(YearMonth.of(2026, 8), LocalDate.of(2026, 8, 10), StatusFatura.ABERTA);
            when(faturaRepository.findById(fatura.getId())).thenReturn(Optional.of(fatura));
            when(transacaoRepository.somarValorPorFatura(fatura.getId()))
                    .thenReturn(new BigDecimal("180.00"));

            FaturaResumoDTO dto = faturaService.buscarPorId(fatura.getId(), dono);

            assertThat(dto.getFaturaId()).isEqualTo(fatura.getId());
            assertThat(dto.getMesReferencia()).isEqualTo(YearMonth.of(2026, 8));
            assertThat(dto.getDataVencimento()).isEqualTo(LocalDate.of(2026, 8, 10));
            assertThat(dto.getValorTotal()).isEqualByComparingTo(new BigDecimal("180.00"));
            assertThat(dto.getStatus()).isEqualTo(StatusFatura.ABERTA);
        }

        @Test
        @DisplayName("impede acesso a fatura de outro usuario")
        void deveImpedirFaturaDeOutroUsuario() {
            Fatura fatura = fatura(YearMonth.of(2026, 8), LocalDate.of(2026, 8, 10), StatusFatura.ABERTA);
            when(faturaRepository.findById(fatura.getId())).thenReturn(Optional.of(fatura));

            assertThatThrownBy(() -> faturaService.buscarPorId(fatura.getId(), outroUsuario))
                    .isInstanceOfSatisfying(ResponseStatusException.class, ex ->
                            assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN));

            assertThatThrownBy(() -> faturaService.marcarComoPaga(fatura.getId(), outroUsuario))
                    .isInstanceOfSatisfying(ResponseStatusException.class, ex ->
                            assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN));

            assertThat(fatura.getStatus()).isEqualTo(StatusFatura.ABERTA);
            verify(faturaRepository, never()).save(any());
        }

        @Test
        @DisplayName("lanca ResourceNotFound quando a fatura nao existe")
        void deveLancarQuandoFaturaNaoExiste() {
            UUID inexistente = UUID.randomUUID();
            when(faturaRepository.findById(inexistente)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> faturaService.buscarPorId(inexistente, dono))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Fatura não encontrada");
        }
    }

    @Nested
    @DisplayName("marcarComoPaga()")
    class MarcarComoPaga {

        @Test
        @DisplayName("marca a fatura como PAGA e persiste")
        void deveMarcarFaturaComoPaga() {
            Fatura fatura = fatura(YearMonth.of(2026, 8), LocalDate.of(2026, 8, 10), StatusFatura.ABERTA);
            when(faturaRepository.findById(fatura.getId())).thenReturn(Optional.of(fatura));

            faturaService.marcarComoPaga(fatura.getId(), dono);

            assertThat(fatura.getStatus()).isEqualTo(StatusFatura.PAGA);
            verify(faturaRepository).save(fatura);
        }
    }
}
