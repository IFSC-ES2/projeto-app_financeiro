package bcd.appfinanceirobackend.controller;

import bcd.appfinanceirobackend.config.JwtAuthFilter;
import bcd.appfinanceirobackend.config.SecurityConfig;
import bcd.appfinanceirobackend.dto.transacao.TransacaoResponseDTO;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.model.enums.TipoPagamento;
import bcd.appfinanceirobackend.model.enums.TipoTransacao;
import bcd.appfinanceirobackend.repository.UsuarioRepository;
import bcd.appfinanceirobackend.security.JwtUtil;
import bcd.appfinanceirobackend.service.TransacaoService;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransacaoController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class})
@DisplayName("GET /transacoes")
class TransacaoListagemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransacaoService transacaoService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UsuarioRepository usuarioRepository;

    private Usuario usuarioAutenticado;
    private TransacaoResponseDTO transacaoResponse;

    @BeforeEach
    void setUp() {
        usuarioAutenticado = new Usuario();
        usuarioAutenticado.setId(UUID.randomUUID());
        usuarioAutenticado.setNome("João Silva");
        usuarioAutenticado.setEmail("joao@email.com");
        usuarioAutenticado.setSenha("hash");
        usuarioAutenticado.setCpf("12345678900");

        transacaoResponse = new TransacaoResponseDTO();
        transacaoResponse.setTransacaoId(UUID.randomUUID());
        transacaoResponse.setValor(new BigDecimal("200.00"));
        transacaoResponse.setData(LocalDate.of(2026, 5, 30));
        transacaoResponse.setDescricao("Supermercado");
        transacaoResponse.setTipoTransacao(TipoTransacao.DEBITO);
        transacaoResponse.setFormaPagamento(TipoPagamento.PIX);
        transacaoResponse.setContaId(UUID.randomUUID());
        transacaoResponse.setCategoriaId(UUID.randomUUID());
        transacaoResponse.setImportacaoId(UUID.randomUUID());
    }

    @Nested
    @DisplayName("Listagem")
    class Listagem {

        @Test
        @DisplayName("Retorna 200 com transações do usuário autenticado")
        void deveRetornar200ComTransacoesDoUsuario() throws Exception {
            when(transacaoService.listarTransacoesPorUsuario(any(Usuario.class)))
                    .thenReturn(List.of(transacaoResponse));

            mockMvc.perform(get("/transacoes")
                            .with(user(usuarioAutenticado)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].transacaoId")
                            .value(transacaoResponse.getTransacaoId().toString()))
                    .andExpect(jsonPath("$[0].valor").value(200.00))
                    .andExpect(jsonPath("$[0].data").value("2026-05-30"))
                    .andExpect(jsonPath("$[0].descricao").value("Supermercado"))
                    .andExpect(jsonPath("$[0].tipoTransacao").value("DEBITO"))
                    .andExpect(jsonPath("$[0].formaPagamento").value("PIX"))
                    .andExpect(jsonPath("$[0].contaId")
                            .value(transacaoResponse.getContaId().toString()))
                    .andExpect(jsonPath("$[0].categoriaId")
                            .value(transacaoResponse.getCategoriaId().toString()))
                    .andExpect(jsonPath("$[0].importacaoId")
                            .value(transacaoResponse.getImportacaoId().toString()));
        }

        @Test
        @DisplayName("Retorna 200 com lista vazia quando usuário não possui transações")
        void deveRetornarListaVazia() throws Exception {
            when(transacaoService.listarTransacoesPorUsuario(any(Usuario.class)))
                    .thenReturn(List.of());

            mockMvc.perform(get("/transacoes")
                            .with(user(usuarioAutenticado)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        @DisplayName("Delega listagem para o service usando o usuário autenticado")
        void deveDelegarParaServiceComUsuarioAutenticado() throws Exception {
            ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);

            when(transacaoService.listarTransacoesPorUsuario(any(Usuario.class)))
                    .thenReturn(List.of());

            mockMvc.perform(get("/transacoes")
                            .with(user(usuarioAutenticado)))
                    .andExpect(status().isOk());

            verify(transacaoService).listarTransacoesPorUsuario(captor.capture());
            assertThat(captor.getValue()).isEqualTo(usuarioAutenticado);
        }

        @Test
        @DisplayName("Retorna 401 ou 403 quando usuário não está autenticado")
        void deveRetornar401Ou403QuandoNaoAutenticado() throws Exception {
            mockMvc.perform(get("/transacoes"))
                    .andExpect(status().is4xxClientError());

            verify(transacaoService, never()).listarTransacoesPorUsuario(any());
        }
    }
}