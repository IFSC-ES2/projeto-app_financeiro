package bcd.appfinanceirobackend.service;

import bcd.appfinanceirobackend.dto.resumo.GrupoCategoriaDTO;
import bcd.appfinanceirobackend.dto.resumo.ResumoMensalDTO;
import bcd.appfinanceirobackend.model.Categoria;
import bcd.appfinanceirobackend.model.Transacao;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.model.enums.TipoTransacao;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ResumoService - resumo mensal e gastos por categoria")
class ResumoMensalServiceTest {

    @Mock
    private TransacaoRepository transacaoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private ResumoService resumoService;

    private Usuario usuario;
    private UUID categoriaAlimentacaoId;
    private UUID categoriaTransporteId;
    private Categoria categoriaAlimentacao;
    private Categoria categoriaTransporte;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setNome("João Silva");
        usuario.setEmail("joao@email.com");

        categoriaAlimentacaoId = UUID.randomUUID();
        categoriaTransporteId = UUID.randomUUID();

        categoriaAlimentacao = new Categoria();
        categoriaAlimentacao.setId(categoriaAlimentacaoId);
        categoriaAlimentacao.setNome("Alimentação");
        categoriaAlimentacao.setIcone("icone-alimentacao");
        categoriaAlimentacao.setCor("#FFAA00");

        categoriaTransporte = new Categoria();
        categoriaTransporte.setId(categoriaTransporteId);
        categoriaTransporte.setNome("Transporte");
        categoriaTransporte.setIcone("icone-transporte");
        categoriaTransporte.setCor("#00AAFF");
    }

    private Transacao transacao(TipoTransacao tipo, String valor, LocalDate data, Categoria categoria) {
        Transacao transacao = new Transacao();
        transacao.setTipo(tipo);
        transacao.setValor(new BigDecimal(valor));
        transacao.setData(data);
        transacao.setCategoria(categoria);
        return transacao;
    }

    @Nested
    @DisplayName("gerarResumoMensal()")
    class GerarResumoMensal {

        @Test
        @DisplayName("calcula total recebido, total gasto e saldo do mês")
        void deveCalcularTotaisDoMes() {
            LocalDate inicio = LocalDate.of(2026, 6, 1);
            LocalDate fim = LocalDate.of(2026, 6, 30);
            LocalDate inicioAnterior = LocalDate.of(2026, 5, 1);
            LocalDate fimAnterior = LocalDate.of(2026, 5, 31);

            when(transacaoRepository.findAllByContaUsuarioIdAndDataBetween(
                    eq(usuario.getId()), eq(inicio), eq(fim)))
                    .thenReturn(List.of(
                            transacao(TipoTransacao.CREDITO, "2500.00", LocalDate.of(2026, 6, 5), null),
                            transacao(TipoTransacao.DEBITO, "800.00", LocalDate.of(2026, 6, 10), categoriaAlimentacao),
                            transacao(TipoTransacao.DEBITO, "200.00", LocalDate.of(2026, 6, 12), categoriaTransporte)
                    ));
            when(transacaoRepository.findAllByContaUsuarioIdAndDataBetween(
                    eq(usuario.getId()), eq(inicioAnterior), eq(fimAnterior)))
                    .thenReturn(List.of());
            when(categoriaRepository.findById(categoriaAlimentacaoId))
                    .thenReturn(Optional.of(categoriaAlimentacao));
            when(categoriaRepository.findById(categoriaTransporteId))
                    .thenReturn(Optional.of(categoriaTransporte));

            ResumoMensalDTO resumo = resumoService.gerarResumoMensal(usuario, 2026, 6);

            assertThat(resumo.getTotalRecebido()).isEqualByComparingTo(new BigDecimal("2500.00"));
            assertThat(resumo.getTotalGasto()).isEqualByComparingTo(new BigDecimal("1000.00"));
            assertThat(resumo.getSaldo()).isEqualByComparingTo(new BigDecimal("1500.00"));
            assertThat(resumo.getAno()).isEqualTo(2026);
            assertThat(resumo.getMes()).isEqualTo(6);
            assertThat(resumo.getDataInicio()).isEqualTo(inicio);
            assertThat(resumo.getDataFim()).isEqualTo(fim);
            assertThat(resumo.getPossuiTransacoes()).isTrue();
        }

        @Test
        @DisplayName("identifica a categoria com maior gasto no mês")
        void deveIdentificarCategoriaComMaiorGasto() {
            LocalDate inicio = LocalDate.of(2026, 6, 1);
            LocalDate fim = LocalDate.of(2026, 6, 30);
            LocalDate inicioAnterior = LocalDate.of(2026, 5, 1);
            LocalDate fimAnterior = LocalDate.of(2026, 5, 31);

            when(transacaoRepository.findAllByContaUsuarioIdAndDataBetween(
                    eq(usuario.getId()), eq(inicio), eq(fim)))
                    .thenReturn(List.of(
                            transacao(TipoTransacao.DEBITO, "300.00", LocalDate.of(2026, 6, 2), categoriaAlimentacao),
                            transacao(TipoTransacao.DEBITO, "100.00", LocalDate.of(2026, 6, 3), categoriaTransporte)
                    ));
            when(transacaoRepository.findAllByContaUsuarioIdAndDataBetween(
                    eq(usuario.getId()), eq(inicioAnterior), eq(fimAnterior)))
                    .thenReturn(List.of());
            when(categoriaRepository.findById(categoriaAlimentacaoId))
                    .thenReturn(Optional.of(categoriaAlimentacao));
            when(categoriaRepository.findById(categoriaTransporteId))
                    .thenReturn(Optional.of(categoriaTransporte));

            ResumoMensalDTO resumo = resumoService.gerarResumoMensal(usuario, 2026, 6);

            assertThat(resumo.getCategoriaMaiorGastoId()).isEqualTo(categoriaAlimentacaoId);
            assertThat(resumo.getCategoriaMaiorGastoNome()).isEqualTo("Alimentação");
            assertThat(resumo.getCategoriaMaiorGastoTotal()).isEqualByComparingTo(new BigDecimal("300.00"));
        }

        @Test
        @DisplayName("calcula variação percentual de gastos em relação ao mês anterior")
        void deveCalcularVariacaoPercentualGastos() {
            LocalDate inicio = LocalDate.of(2026, 6, 1);
            LocalDate fim = LocalDate.of(2026, 6, 30);
            LocalDate inicioAnterior = LocalDate.of(2026, 5, 1);
            LocalDate fimAnterior = LocalDate.of(2026, 5, 31);

            when(transacaoRepository.findAllByContaUsuarioIdAndDataBetween(
                    eq(usuario.getId()), eq(inicio), eq(fim)))
                    .thenReturn(List.of(
                            transacao(TipoTransacao.DEBITO, "150.00", LocalDate.of(2026, 6, 8), categoriaAlimentacao)
                    ));
            when(transacaoRepository.findAllByContaUsuarioIdAndDataBetween(
                    eq(usuario.getId()), eq(inicioAnterior), eq(fimAnterior)))
                    .thenReturn(List.of(
                            transacao(TipoTransacao.DEBITO, "100.00", LocalDate.of(2026, 5, 8), categoriaAlimentacao)
                    ));
            when(categoriaRepository.findById(categoriaAlimentacaoId))
                    .thenReturn(Optional.of(categoriaAlimentacao));

            ResumoMensalDTO resumo = resumoService.gerarResumoMensal(usuario, 2026, 6);

            // (150 - 100) / 100 * 100 = 50%
            assertThat(resumo.getVariacaoPercentualGastos()).isEqualByComparingTo(new BigDecimal("50.00"));
        }

        @Test
        @DisplayName("retorna 100% de variação quando o mês anterior não teve gastos e o atual teve")
        void deveRetornarCemQuandoMesAnteriorZerado() {
            LocalDate inicio = LocalDate.of(2026, 6, 1);
            LocalDate fim = LocalDate.of(2026, 6, 30);
            LocalDate inicioAnterior = LocalDate.of(2026, 5, 1);
            LocalDate fimAnterior = LocalDate.of(2026, 5, 31);

            when(transacaoRepository.findAllByContaUsuarioIdAndDataBetween(
                    eq(usuario.getId()), eq(inicio), eq(fim)))
                    .thenReturn(List.of(
                            transacao(TipoTransacao.DEBITO, "80.00", LocalDate.of(2026, 6, 1), categoriaAlimentacao)
                    ));
            when(transacaoRepository.findAllByContaUsuarioIdAndDataBetween(
                    eq(usuario.getId()), eq(inicioAnterior), eq(fimAnterior)))
                    .thenReturn(List.of());
            when(categoriaRepository.findById(categoriaAlimentacaoId))
                    .thenReturn(Optional.of(categoriaAlimentacao));

            ResumoMensalDTO resumo = resumoService.gerarResumoMensal(usuario, 2026, 6);

            assertThat(resumo.getVariacaoPercentualGastos()).isEqualByComparingTo(new BigDecimal("100"));
        }

        @Test
        @DisplayName("mês sem transações retorna valores zerados e sem categoria maior")
        void deveRetornarZeradoSemTransacoes() {
            LocalDate inicio = LocalDate.of(2026, 6, 1);
            LocalDate fim = LocalDate.of(2026, 6, 30);
            LocalDate inicioAnterior = LocalDate.of(2026, 5, 1);
            LocalDate fimAnterior = LocalDate.of(2026, 5, 31);

            when(transacaoRepository.findAllByContaUsuarioIdAndDataBetween(
                    eq(usuario.getId()), eq(inicio), eq(fim)))
                    .thenReturn(List.of());
            when(transacaoRepository.findAllByContaUsuarioIdAndDataBetween(
                    eq(usuario.getId()), eq(inicioAnterior), eq(fimAnterior)))
                    .thenReturn(List.of());

            ResumoMensalDTO resumo = resumoService.gerarResumoMensal(usuario, 2026, 6);

            assertThat(resumo.getTotalRecebido()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(resumo.getTotalGasto()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(resumo.getSaldo()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(resumo.getCategoriaMaiorGastoTotal()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(resumo.getCategoriaMaiorGastoId()).isNull();
            assertThat(resumo.getCategoriaMaiorGastoNome()).isNull();
            assertThat(resumo.getPossuiTransacoes()).isFalse();
            assertThat(resumo.getVariacaoPercentualGastos()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("considera apenas o período filtrado por mês e ano")
        void deveFiltrarPorMesEAno() {
            LocalDate inicio = LocalDate.of(2026, 3, 1);
            LocalDate fim = YearMonth.of(2026, 3).atEndOfMonth();
            LocalDate inicioAnterior = LocalDate.of(2026, 2, 1);
            LocalDate fimAnterior = YearMonth.of(2026, 2).atEndOfMonth();

            when(transacaoRepository.findAllByContaUsuarioIdAndDataBetween(
                    eq(usuario.getId()), eq(inicio), eq(fim)))
                    .thenReturn(List.of(
                            transacao(TipoTransacao.CREDITO, "500.00", LocalDate.of(2026, 3, 10), null)
                    ));
            when(transacaoRepository.findAllByContaUsuarioIdAndDataBetween(
                    eq(usuario.getId()), eq(inicioAnterior), eq(fimAnterior)))
                    .thenReturn(List.of());

            ResumoMensalDTO resumo = resumoService.gerarResumoMensal(usuario, 2026, 3);

            assertThat(resumo.getAno()).isEqualTo(2026);
            assertThat(resumo.getMes()).isEqualTo(3);
            assertThat(resumo.getDataInicio()).isEqualTo(inicio);
            assertThat(resumo.getDataFim()).isEqualTo(fim);
            assertThat(resumo.getTotalRecebido()).isEqualByComparingTo(new BigDecimal("500.00"));

            verify(transacaoRepository).findAllByContaUsuarioIdAndDataBetween(
                    usuario.getId(), inicio, fim);
        }

        @Test
        @DisplayName("consulta o repositório apenas com o ID do usuário autenticado")
        void deveConsultarApenasUsuarioAutenticado() {
            LocalDate inicio = LocalDate.of(2026, 6, 1);
            LocalDate fim = LocalDate.of(2026, 6, 30);
            LocalDate inicioAnterior = LocalDate.of(2026, 5, 1);
            LocalDate fimAnterior = LocalDate.of(2026, 5, 31);

            when(transacaoRepository.findAllByContaUsuarioIdAndDataBetween(
                    eq(usuario.getId()), eq(inicio), eq(fim)))
                    .thenReturn(List.of());
            when(transacaoRepository.findAllByContaUsuarioIdAndDataBetween(
                    eq(usuario.getId()), eq(inicioAnterior), eq(fimAnterior)))
                    .thenReturn(List.of());

            resumoService.gerarResumoMensal(usuario, 2026, 6);

            verify(transacaoRepository).findAllByContaUsuarioIdAndDataBetween(
                    usuario.getId(), inicio, fim);
            verify(transacaoRepository).findAllByContaUsuarioIdAndDataBetween(
                    usuario.getId(), inicioAnterior, fimAnterior);
        }

        @Test
        @DisplayName("lança AccessDenied quando o usuário é nulo")
        void deveLancarQuandoUsuarioNulo() {
            assertThatThrownBy(() -> resumoService.gerarResumoMensal(null, 2026, 6))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("Usuário autenticado não encontrado");

            verifyNoInteractions(transacaoRepository);
        }

        @Test
        @DisplayName("lança IllegalArgumentException quando apenas o mês é informado")
        void deveLancarQuandoApenasMesInformado() {
            assertThatThrownBy(() -> resumoService.gerarResumoMensal(usuario, null, 6))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("mês");
        }

        @Test
        @DisplayName("lança IllegalArgumentException quando o mês é inválido")
        void deveLancarQuandoMesInvalido() {
            assertThatThrownBy(() -> resumoService.gerarResumoMensal(usuario, 2026, 13))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Mês informado inválido");
        }
    }

    @Nested
    @DisplayName("agruparPorCategoria()")
    class AgruparPorCategoria {

        @Test
        @DisplayName("agrupa gastos por categoria com total, quantidade e percentual")
        void deveAgruparGastosPorCategoria() {
            LocalDate inicio = LocalDate.of(2026, 6, 1);
            LocalDate fim = LocalDate.of(2026, 6, 30);

            when(transacaoRepository.findAllByContaUsuarioIdAndDataBetween(
                    eq(usuario.getId()), eq(inicio), eq(fim)))
                    .thenReturn(List.of(
                            transacao(TipoTransacao.DEBITO, "70.00", LocalDate.of(2026, 6, 1), categoriaAlimentacao),
                            transacao(TipoTransacao.DEBITO, "30.00", LocalDate.of(2026, 6, 2), categoriaTransporte),
                            transacao(TipoTransacao.CREDITO, "1000.00", LocalDate.of(2026, 6, 3), null)
                    ));
            when(categoriaRepository.findById(categoriaAlimentacaoId))
                    .thenReturn(Optional.of(categoriaAlimentacao));
            when(categoriaRepository.findById(categoriaTransporteId))
                    .thenReturn(Optional.of(categoriaTransporte));

            List<GrupoCategoriaDTO> grupos = resumoService.agruparPorCategoria(usuario, 2026, 6);

            assertThat(grupos).hasSize(2);
            assertThat(grupos.getFirst().getNome()).isEqualTo("Alimentação");
            assertThat(grupos.getFirst().getTotal()).isEqualByComparingTo(new BigDecimal("70.00"));
            assertThat(grupos.getFirst().getQuantidade()).isEqualTo(1);
            assertThat(grupos.getFirst().getPercentual()).isEqualByComparingTo(new BigDecimal("70.00"));

            assertThat(grupos.get(1).getNome()).isEqualTo("Transporte");
            assertThat(grupos.get(1).getTotal()).isEqualByComparingTo(new BigDecimal("30.00"));
            assertThat(grupos.get(1).getPercentual()).isEqualByComparingTo(new BigDecimal("30.00"));
        }

        @Test
        @DisplayName("retorna lista vazia quando não há gastos no mês")
        void deveRetornarListaVaziaSemGastos() {
            LocalDate inicio = LocalDate.of(2026, 6, 1);
            LocalDate fim = LocalDate.of(2026, 6, 30);

            when(transacaoRepository.findAllByContaUsuarioIdAndDataBetween(
                    eq(usuario.getId()), eq(inicio), eq(fim)))
                    .thenReturn(List.of(
                            transacao(TipoTransacao.CREDITO, "500.00", LocalDate.of(2026, 6, 1), null)
                    ));

            List<GrupoCategoriaDTO> grupos = resumoService.agruparPorCategoria(usuario, 2026, 6);

            assertThat(grupos).isEmpty();
        }

        @Test
        @DisplayName("agrupa gastos sem categoria como \"Sem categoria\"")
        void deveAgruparSemCategoria() {
            LocalDate inicio = LocalDate.of(2026, 6, 1);
            LocalDate fim = LocalDate.of(2026, 6, 30);

            when(transacaoRepository.findAllByContaUsuarioIdAndDataBetween(
                    eq(usuario.getId()), eq(inicio), eq(fim)))
                    .thenReturn(List.of(
                            transacao(TipoTransacao.DEBITO, "40.00", LocalDate.of(2026, 6, 1), null)
                    ));

            List<GrupoCategoriaDTO> grupos = resumoService.agruparPorCategoria(usuario, 2026, 6);

            assertThat(grupos).hasSize(1);
            assertThat(grupos.getFirst().getNome()).isEqualTo("Sem categoria");
            assertThat(grupos.getFirst().getCategoriaID()).isNull();
            assertThat(grupos.getFirst().getTotal()).isEqualByComparingTo(new BigDecimal("40.00"));
        }

        @Test
        @DisplayName("consulta apenas o usuário autenticado no filtro de período")
        void deveIsolarDadosDoUsuarioAutenticado() {
            LocalDate inicio = LocalDate.of(2026, 4, 1);
            LocalDate fim = YearMonth.of(2026, 4).atEndOfMonth();

            when(transacaoRepository.findAllByContaUsuarioIdAndDataBetween(
                    eq(usuario.getId()), eq(inicio), eq(fim)))
                    .thenReturn(List.of());

            resumoService.agruparPorCategoria(usuario, 2026, 4);

            verify(transacaoRepository).findAllByContaUsuarioIdAndDataBetween(
                    usuario.getId(), inicio, fim);
        }

        @Test
        @DisplayName("lança AccessDenied quando o usuário não possui id")
        void deveLancarQuandoUsuarioSemId() {
            Usuario semId = new Usuario();

            assertThatThrownBy(() -> resumoService.agruparPorCategoria(semId, 2026, 6))
                    .isInstanceOf(AccessDeniedException.class);

            verifyNoInteractions(transacaoRepository);
        }
    }
}
