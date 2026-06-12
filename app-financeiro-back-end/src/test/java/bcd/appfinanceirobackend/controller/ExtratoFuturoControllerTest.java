package bcd.appfinanceirobackend.controller;

import bcd.appfinanceirobackend.config.JwtAuthFilter;
import bcd.appfinanceirobackend.config.SecurityConfig;
import bcd.appfinanceirobackend.dto.extrato.ProjecaoMensalDTO;
import bcd.appfinanceirobackend.dto.fatura.FaturaResumoDTO;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.model.enums.StatusFatura;
import bcd.appfinanceirobackend.repository.UsuarioRepository;
import bcd.appfinanceirobackend.security.JwtUtil;
import bcd.appfinanceirobackend.service.ExtratoFuturoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExtratoFuturoController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class})
@DisplayName("ExtratoFuturoController")
class ExtratoFuturoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExtratoFuturoService extratoFuturoService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UsuarioRepository usuarioRepository;

    private Usuario usuarioAutenticado;

    @BeforeEach
    void setUp() {
        usuarioAutenticado = new Usuario();
        usuarioAutenticado.setId(UUID.randomUUID());
        usuarioAutenticado.setNome("João Silva");
        usuarioAutenticado.setEmail("joao@email.com");
        usuarioAutenticado.setSenha("hash");
        usuarioAutenticado.setCpf("12345678900");
    }

    private ProjecaoMensalDTO projecaoDeJulho() {
        FaturaResumoDTO fatura = new FaturaResumoDTO();
        fatura.setFaturaId(UUID.randomUUID());
        fatura.setContaId(UUID.randomUUID());
        fatura.setContaNome("Cartão Nubank");
        fatura.setMesReferencia("2026-06");
        fatura.setDataVencimento(LocalDate.of(2026, 7, 8));
        fatura.setValorTotal(new BigDecimal("980.00"));
        fatura.setStatus(StatusFatura.ABERTA);

        ProjecaoMensalDTO projecao = new ProjecaoMensalDTO();
        projecao.setAno(2026);
        projecao.setMes(7);
        projecao.setDataInicio(LocalDate.of(2026, 7, 1));
        projecao.setDataFim(LocalDate.of(2026, 7, 31));
        projecao.setSaldoPrevisto(new BigDecimal("-980.00"));
        projecao.setTotalDebitos(new BigDecimal("980.00"));
        projecao.setTotalCreditos(BigDecimal.ZERO);
        projecao.setFaturas(List.of(fatura));
        projecao.setTransacoes(List.of());
        return projecao;
    }

    @Test
    @DisplayName("deve usar a quantidade padrão de meses quando o parâmetro não é informado")
    void deveUsarQuantidadePadraoDeMeses() throws Exception {
        when(extratoFuturoService.calcularProjecao(any(Usuario.class), isNull()))
                .thenReturn(List.of(projecaoDeJulho()));

        mockMvc.perform(get("/extrato-futuro").with(user(usuarioAutenticado)))
                .andExpect(status().isOk());

        verify(extratoFuturoService).calcularProjecao(any(Usuario.class), isNull());
    }

    @Test
    @DisplayName("deve repassar a quantidade de meses informada")
    void deveRepassarQuantidadeDeMesesInformada() throws Exception {
        when(extratoFuturoService.calcularProjecao(any(Usuario.class), eq(6)))
                .thenReturn(List.of());

        mockMvc.perform(get("/extrato-futuro").param("meses", "6").with(user(usuarioAutenticado)))
                .andExpect(status().isOk());

        verify(extratoFuturoService).calcularProjecao(any(Usuario.class), eq(6));
    }

    @Test
    @DisplayName("deve serializar a projeção no formato consumido pelo frontend")
    void deveSerializarProjecaoNoFormatoEsperado() throws Exception {
        when(extratoFuturoService.calcularProjecao(any(Usuario.class), isNull()))
                .thenReturn(List.of(projecaoDeJulho()));

        mockMvc.perform(get("/extrato-futuro").with(user(usuarioAutenticado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ano").value(2026))
                .andExpect(jsonPath("$[0].mes").value(7))
                .andExpect(jsonPath("$[0].dataInicio").value("2026-07-01"))
                .andExpect(jsonPath("$[0].saldoPrevisto").value(-980.00))
                .andExpect(jsonPath("$[0].totalDebitos").value(980.00))
                .andExpect(jsonPath("$[0].totalCreditos").value(0))
                .andExpect(jsonPath("$[0].faturas[0].contaNome").value("Cartão Nubank"))
                .andExpect(jsonPath("$[0].faturas[0].mesReferencia").value("2026-06"))
                .andExpect(jsonPath("$[0].faturas[0].dataVencimento").value("2026-07-08"))
                .andExpect(jsonPath("$[0].faturas[0].valorTotal").value(980.00))
                .andExpect(jsonPath("$[0].faturas[0].status").value("ABERTA"))
                .andExpect(jsonPath("$[0].transacoes").isEmpty());
    }
}
