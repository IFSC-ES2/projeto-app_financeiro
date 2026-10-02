package bcd.appfinanceirobackend.controller;

import bcd.appfinanceirobackend.config.JwtAuthFilter;
import bcd.appfinanceirobackend.config.SecurityConfig;
import bcd.appfinanceirobackend.dto.resumo.GrupoCategoriaDTO;
import bcd.appfinanceirobackend.dto.resumo.ResumoMensalDTO;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.repository.UsuarioRepository;
import bcd.appfinanceirobackend.security.JwtUtil;
import bcd.appfinanceirobackend.service.ResumoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ResumoController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class})
@DisplayName("ResumoController - resumo mensal e categorias")
class ResumoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ResumoService resumoService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UsuarioRepository usuarioRepository;

    private Usuario usuarioAutenticado;
    private ResumoMensalDTO resumoMensal;
    private GrupoCategoriaDTO grupoAlimentacao;

    @BeforeEach
    void setUp() {
        usuarioAutenticado = new Usuario();
        usuarioAutenticado.setId(UUID.randomUUID());
        usuarioAutenticado.setNome("João Silva");
        usuarioAutenticado.setEmail("joao@email.com");
        usuarioAutenticado.setSenha("hash");
        usuarioAutenticado.setCpf("12345678900");

        UUID categoriaId = UUID.randomUUID();

        resumoMensal = new ResumoMensalDTO();
        resumoMensal.setAno(2026);
        resumoMensal.setMes(6);
        resumoMensal.setDataInicio(LocalDate.of(2026, 6, 1));
        resumoMensal.setDataFim(LocalDate.of(2026, 6, 30));
        resumoMensal.setTotalRecebido(new BigDecimal("2500.00"));
        resumoMensal.setTotalGasto(new BigDecimal("1000.00"));
        resumoMensal.setSaldo(new BigDecimal("1500.00"));
        resumoMensal.setCategoriaMaiorGastoId(categoriaId);
        resumoMensal.setCategoriaMaiorGastoNome("Alimentação");
        resumoMensal.setCategoriaMaiorGastoTotal(new BigDecimal("700.00"));
        resumoMensal.setVariacaoPercentualGastos(new BigDecimal("10.00"));
        resumoMensal.setPossuiTransacoes(true);

        grupoAlimentacao = new GrupoCategoriaDTO();
        grupoAlimentacao.setCategoriaID(categoriaId);
        grupoAlimentacao.setNome("Alimentação");
        grupoAlimentacao.setIcone("icone-alimentacao");
        grupoAlimentacao.setCor("#FFAA00");
        grupoAlimentacao.setTotal(new BigDecimal("700.00"));
        grupoAlimentacao.setQuantidade(3);
        grupoAlimentacao.setPercentual(new BigDecimal("70.00"));
    }

    @Nested
    @DisplayName("GET /resumo")
    class ObterResumoMensal {

        @Test
        @DisplayName("retorna 200 com o resumo mensal do usuário autenticado")
        void deveRetornar200ComResumoMensal() throws Exception {
            when(resumoService.gerarResumoMensal(eq(usuarioAutenticado), eq(2026), eq(6)))
                    .thenReturn(resumoMensal);

            mockMvc.perform(get("/resumo")
                            .param("ano", "2026")
                            .param("mes", "6")
                            .with(user(usuarioAutenticado)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.ano").value(2026))
                    .andExpect(jsonPath("$.mes").value(6))
                    .andExpect(jsonPath("$.totalRecebido").value(2500.00))
                    .andExpect(jsonPath("$.totalGasto").value(1000.00))
                    .andExpect(jsonPath("$.saldo").value(1500.00))
                    .andExpect(jsonPath("$.categoriaMaiorGastoNome").value("Alimentação"))
                    .andExpect(jsonPath("$.categoriaMaiorGastoTotal").value(700.00))
                    .andExpect(jsonPath("$.variacaoPercentualGastos").value(10.00))
                    .andExpect(jsonPath("$.possuiTransacoes").value(true));
        }

        @Test
        @DisplayName("repassa filtro de mês e ano ao service")
        void deveRepassarFiltroMesAno() throws Exception {
            when(resumoService.gerarResumoMensal(eq(usuarioAutenticado), eq(2025), eq(12)))
                    .thenReturn(resumoMensal);

            mockMvc.perform(get("/resumo")
                            .param("ano", "2025")
                            .param("mes", "12")
                            .with(user(usuarioAutenticado)))
                    .andExpect(status().isOk());

            ArgumentCaptor<Integer> anoCaptor = ArgumentCaptor.forClass(Integer.class);
            ArgumentCaptor<Integer> mesCaptor = ArgumentCaptor.forClass(Integer.class);
            verify(resumoService).gerarResumoMensal(
                    eq(usuarioAutenticado), anoCaptor.capture(), mesCaptor.capture());

            assertThat(anoCaptor.getValue()).isEqualTo(2025);
            assertThat(mesCaptor.getValue()).isEqualTo(12);
        }

        @Test
        @DisplayName("aceita requisição sem ano e mês (usa período padrão do service)")
        void deveAceitarSemAnoMes() throws Exception {
            when(resumoService.gerarResumoMensal(eq(usuarioAutenticado), isNull(), isNull()))
                    .thenReturn(resumoMensal);

            mockMvc.perform(get("/resumo")
                            .with(user(usuarioAutenticado)))
                    .andExpect(status().isOk());

            verify(resumoService).gerarResumoMensal(usuarioAutenticado, null, null);
        }

        @Test
        @DisplayName("bloqueia a requisição quando não há autenticação")
        void deveBloquearSemAutenticacao() throws Exception {
            mockMvc.perform(get("/resumo")
                            .param("ano", "2026")
                            .param("mes", "6"))
                    .andExpect(status().is4xxClientError());

            verify(resumoService, never()).gerarResumoMensal(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("GET /resumo/categorias")
    class ObterResumoPorCategorias {

        @Test
        @DisplayName("retorna 200 com gastos agrupados por categoria")
        void deveRetornar200ComGrupos() throws Exception {
            when(resumoService.agruparPorCategoria(eq(usuarioAutenticado), eq(2026), eq(6)))
                    .thenReturn(List.of(grupoAlimentacao));

            mockMvc.perform(get("/resumo/categorias")
                            .param("ano", "2026")
                            .param("mes", "6")
                            .with(user(usuarioAutenticado)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].nome").value("Alimentação"))
                    .andExpect(jsonPath("$[0].total").value(700.00))
                    .andExpect(jsonPath("$[0].quantidade").value(3))
                    .andExpect(jsonPath("$[0].percentual").value(70.00))
                    .andExpect(jsonPath("$[0].icone").value("icone-alimentacao"))
                    .andExpect(jsonPath("$[0].cor").value("#FFAA00"));
        }

        @Test
        @DisplayName("retorna lista vazia quando não há gastos no mês")
        void deveRetornarListaVazia() throws Exception {
            when(resumoService.agruparPorCategoria(eq(usuarioAutenticado), eq(2026), eq(6)))
                    .thenReturn(List.of());

            mockMvc.perform(get("/resumo/categorias")
                            .param("ano", "2026")
                            .param("mes", "6")
                            .with(user(usuarioAutenticado)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        @DisplayName("repassa filtro de mês e ano ao service")
        void deveRepassarFiltroMesAno() throws Exception {
            when(resumoService.agruparPorCategoria(eq(usuarioAutenticado), eq(2026), eq(1)))
                    .thenReturn(List.of());

            mockMvc.perform(get("/resumo/categorias")
                            .param("ano", "2026")
                            .param("mes", "1")
                            .with(user(usuarioAutenticado)))
                    .andExpect(status().isOk());

            verify(resumoService).agruparPorCategoria(usuarioAutenticado, 2026, 1);
        }

        @Test
        @DisplayName("bloqueia a requisição quando não há autenticação")
        void deveBloquearSemAutenticacao() throws Exception {
            mockMvc.perform(get("/resumo/categorias")
                            .param("ano", "2026")
                            .param("mes", "6"))
                    .andExpect(status().is4xxClientError());

            verify(resumoService, never()).agruparPorCategoria(any(), any(), any());
        }
    }
}
