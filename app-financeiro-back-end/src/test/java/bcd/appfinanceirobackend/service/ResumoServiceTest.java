package bcd.appfinanceirobackend.service;

import bcd.appfinanceirobackend.dto.resumo.GrupoPagamentoDTO;
import bcd.appfinanceirobackend.model.Transacao;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.model.enums.TipoPagamento;
import bcd.appfinanceirobackend.repository.CategoriaRepository;
import bcd.appfinanceirobackend.repository.TransacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ResumoService - agruparFormaPagamento")
class ResumoServiceTest {

    private static final int ANO = 2026;
    private static final int MES = 4;

    @Mock
    private TransacaoRepository transacaoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private ResumoService resumoService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setNome("João Silva");
        usuario.setEmail("joao@email.com");
    }

    private Transacao transacao(
            TipoPagamento formaPagamento,
            String valor
    ) {
        Transacao transacao =
                new Transacao();

        transacao.setFormaPagamento(
                formaPagamento
        );

        transacao.setValor(
                new BigDecimal(valor)
        );

        return transacao;
    }

    private GrupoPagamentoDTO grupoPor(
            List<GrupoPagamentoDTO> grupos,
            TipoPagamento forma
    ) {
        return grupos.stream()
                .filter(
                        grupo ->
                                grupo.getFormaPagamento() ==
                                        forma
                )
                .findFirst()
                .orElseThrow();
    }

    private void mockTransacoesDoPeriodo(
            List<Transacao> transacoes
    ) {
        when(
                transacaoRepository
                        .findAllByContaUsuarioIdAndDataBetween(
                                usuario.getId(),
                                LocalDate.of(
                                        ANO,
                                        MES,
                                        1
                                ),
                                YearMonth
                                        .of(
                                                ANO,
                                                MES
                                        )
                                        .atEndOfMonth()
                        )
        ).thenReturn(
                transacoes
        );
    }

    private List<GrupoPagamentoDTO> agruparPeriodoPadrao() {
        return resumoService.agruparFormaPagamento(
                usuario,
                ANO,
                MES
        );
    }

    @Nested
    @DisplayName("Usuário inválido")
    class UsuarioInvalido {

        @Test
        @DisplayName("Lança AccessDenied quando o usuário é nulo")
        void deveLancarQuandoUsuarioNulo() {
            assertThatThrownBy(
                    () ->
                            resumoService
                                    .agruparFormaPagamento(
                                            null,
                                            ANO,
                                            MES
                                    )
            )
                    .isInstanceOf(
                            AccessDeniedException.class
                    )
                    .hasMessageContaining(
                            "Usuário autenticado não encontrado"
                    );

            verifyNoInteractions(
                    transacaoRepository
            );
        }

        @Test
        @DisplayName("Lança AccessDenied quando o usuário não possui id")
        void deveLancarQuandoUsuarioSemId() {
            Usuario semId =
                    new Usuario();

            assertThatThrownBy(
                    () ->
                            resumoService
                                    .agruparFormaPagamento(
                                            semId,
                                            ANO,
                                            MES
                                    )
            )
                    .isInstanceOf(
                            AccessDeniedException.class
                    );

            verifyNoInteractions(
                    transacaoRepository
            );
        }
    }

    @Nested
    @DisplayName("Período mensal")
    class PeriodoMensal {

        @Test
        @DisplayName("Consulta somente as transações do período informado")
        void deveConsultarSomentePeriodoInformado() {
            mockTransacoesDoPeriodo(
                    List.of(
                            transacao(
                                    TipoPagamento.PIX,
                                    "100.00"
                            )
                    )
            );

            List<GrupoPagamentoDTO> grupos =
                    agruparPeriodoPadrao();

            assertThat(grupos)
                    .hasSize(1);

            verify(
                    transacaoRepository
            ).findAllByContaUsuarioIdAndDataBetween(
                    usuario.getId(),
                    LocalDate.of(
                            ANO,
                            MES,
                            1
                    ),
                    YearMonth
                            .of(
                                    ANO,
                                    MES
                            )
                            .atEndOfMonth()
            );
        }

        @Test
        @DisplayName("Utiliza o mês atual quando ano e mês não são informados")
        void deveUtilizarMesAtualSemPeriodo() {
            YearMonth atual =
                    YearMonth.now();

            when(
                    transacaoRepository
                            .findAllByContaUsuarioIdAndDataBetween(
                                    usuario.getId(),
                                    atual.atDay(1),
                                    atual.atEndOfMonth()
                            )
            ).thenReturn(
                    List.of(
                            transacao(
                                    TipoPagamento.PIX,
                                    "100.00"
                            )
                    )
            );

            List<GrupoPagamentoDTO> grupos =
                    resumoService
                            .agruparFormaPagamento(
                                    usuario,
                                    null,
                                    null
                            );

            assertThat(grupos)
                    .hasSize(1);

            verify(
                    transacaoRepository
            ).findAllByContaUsuarioIdAndDataBetween(
                    usuario.getId(),
                    atual.atDay(1),
                    atual.atEndOfMonth()
            );
        }

        @Test
        @DisplayName("Rejeita quando apenas o ano é informado")
        void deveRejeitarApenasAno() {
            assertThatThrownBy(
                    () ->
                            resumoService
                                    .agruparFormaPagamento(
                                            usuario,
                                            ANO,
                                            null
                                    )
            )
                    .isInstanceOf(
                            IllegalArgumentException.class
                    );
        }

        @Test
        @DisplayName("Rejeita quando apenas o mês é informado")
        void deveRejeitarApenasMes() {
            assertThatThrownBy(
                    () ->
                            resumoService
                                    .agruparFormaPagamento(
                                            usuario,
                                            null,
                                            MES
                                    )
            )
                    .isInstanceOf(
                            IllegalArgumentException.class
                    );
        }
    }

    @Nested
    @DisplayName("Sem dados relevantes")
    class SemDados {

        @Test
        @DisplayName("Retorna lista vazia quando não existem transações no mês")
        void deveRetornarVazioSemTransacoes() {
            mockTransacoesDoPeriodo(
                    List.of()
            );

            assertThat(
                    agruparPeriodoPadrao()
            ).isEmpty();
        }

        @Test
        @DisplayName("Retorna lista vazia quando o total geral do mês é zero")
        void deveRetornarVazioQuandoTotalGeralZero() {
            mockTransacoesDoPeriodo(
                    List.of(
                            transacao(
                                    TipoPagamento.PIX,
                                    "0"
                            )
                    )
            );

            assertThat(
                    agruparPeriodoPadrao()
            ).isEmpty();
        }
    }

    @Nested
    @DisplayName("Agrupamento por forma de pagamento")
    class Agrupamento {

        @Test
        @DisplayName("Calcula total, quantidade e percentual somente com as transações do período")
        void deveCalcularTotaisPorGrupo() {
            mockTransacoesDoPeriodo(
                    List.of(
                            transacao(
                                    TipoPagamento.PIX,
                                    "100.00"
                            ),
                            transacao(
                                    TipoPagamento.PIX,
                                    "50.00"
                            ),
                            transacao(
                                    TipoPagamento.DINHEIRO,
                                    "50.00"
                            )
                    )
            );

            List<GrupoPagamentoDTO> grupos =
                    agruparPeriodoPadrao();

            assertThat(grupos)
                    .hasSize(2);

            GrupoPagamentoDTO pix =
                    grupoPor(
                            grupos,
                            TipoPagamento.PIX
                    );

            assertThat(
                    pix.getQuantidade()
            ).isEqualTo(2);

            assertThat(
                    pix.getTotal()
            ).isEqualByComparingTo(
                    new BigDecimal(
                            "150.00"
                    )
            );

            assertThat(
                    pix.getPercentual()
            ).isEqualByComparingTo(
                    new BigDecimal(
                            "75.00"
                    )
            );

            assertThat(
                    pix.getRotulo()
            ).isEqualTo(
                    "Pix"
            );

            GrupoPagamentoDTO dinheiro =
                    grupoPor(
                            grupos,
                            TipoPagamento.DINHEIRO
                    );

            assertThat(
                    dinheiro.getQuantidade()
            ).isEqualTo(1);

            assertThat(
                    dinheiro.getTotal()
            ).isEqualByComparingTo(
                    new BigDecimal(
                            "50.00"
                    )
            );

            assertThat(
                    dinheiro.getPercentual()
            ).isEqualByComparingTo(
                    new BigDecimal(
                            "25.00"
                    )
            );

            assertThat(
                    dinheiro.getRotulo()
            ).isEqualTo(
                    "Dinheiro"
            );
        }

        @Test
        @DisplayName("Soma dos percentuais dos grupos fecha em 100")
        void deveFecharPercentualEmCem() {
            mockTransacoesDoPeriodo(
                    List.of(
                            transacao(
                                    TipoPagamento.PIX,
                                    "100.00"
                            ),
                            transacao(
                                    TipoPagamento.DINHEIRO,
                                    "50.00"
                            ),
                            transacao(
                                    TipoPagamento.BOLETO,
                                    "50.00"
                            )
                    )
            );

            List<GrupoPagamentoDTO> grupos =
                    agruparPeriodoPadrao();

            BigDecimal somaPercentual =
                    grupos.stream()
                            .map(
                                    GrupoPagamentoDTO::getPercentual
                            )
                            .reduce(
                                    BigDecimal.ZERO,
                                    BigDecimal::add
                            );

            assertThat(
                    somaPercentual
            ).isEqualByComparingTo(
                    new BigDecimal(
                            "100.00"
                    )
            );
        }

        @Test
        @DisplayName("Agrupa transações sem forma de pagamento como Não informado")
        void deveAgruparSemFormaComoNaoInformado() {
            mockTransacoesDoPeriodo(
                    List.of(
                            transacao(
                                    null,
                                    "30.00"
                            ),
                            transacao(
                                    TipoPagamento.PIX,
                                    "70.00"
                            )
                    )
            );

            List<GrupoPagamentoDTO> grupos =
                    agruparPeriodoPadrao();

            GrupoPagamentoDTO naoInformado =
                    grupos.stream()
                            .filter(
                                    grupo ->
                                            grupo.getFormaPagamento() ==
                                                    null
                            )
                            .findFirst()
                            .orElseThrow();

            assertThat(
                    naoInformado.getRotulo()
            ).isEqualTo(
                    "Não informado"
            );

            assertThat(
                    naoInformado.getQuantidade()
            ).isEqualTo(1);

            assertThat(
                    naoInformado.getTotal()
            ).isEqualByComparingTo(
                    new BigDecimal(
                            "30.00"
                    )
            );

            assertThat(
                    naoInformado.getPercentual()
            ).isEqualByComparingTo(
                    new BigDecimal(
                            "30.00"
                    )
            );
        }

        @Test
        @DisplayName("Mapeia o rótulo de cada forma de pagamento")
        void deveMapearRotulosDeCadaForma() {
            mockTransacoesDoPeriodo(
                    List.of(
                            transacao(
                                    TipoPagamento.PIX,
                                    "10.00"
                            ),
                            transacao(
                                    TipoPagamento.CARTAO_DEBITO,
                                    "10.00"
                            ),
                            transacao(
                                    TipoPagamento.CARTAO_CREDITO,
                                    "10.00"
                            ),
                            transacao(
                                    TipoPagamento.DINHEIRO,
                                    "10.00"
                            ),
                            transacao(
                                    TipoPagamento.BOLETO,
                                    "10.00"
                            ),
                            transacao(
                                    TipoPagamento.TED_DOC,
                                    "10.00"
                            )
                    )
            );

            List<GrupoPagamentoDTO> grupos =
                    agruparPeriodoPadrao();

            assertThat(grupos)
                    .extracting(
                            GrupoPagamentoDTO::getRotulo
                    )
                    .containsExactlyInAnyOrder(
                            "Pix",
                            "Cartão de débito",
                            "Cartão de crédito",
                            "Dinheiro",
                            "Boleto",
                            "TED/DOC"
                    );
        }
    }
}