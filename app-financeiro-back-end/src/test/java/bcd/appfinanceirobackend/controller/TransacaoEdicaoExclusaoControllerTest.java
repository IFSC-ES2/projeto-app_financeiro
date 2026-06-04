package bcd.appfinanceirobackend.controller;

import bcd.appfinanceirobackend.config.JwtAuthFilter;
import bcd.appfinanceirobackend.config.SecurityConfig;
import bcd.appfinanceirobackend.dto.transacao.TransacaoRequestDTO;
import bcd.appfinanceirobackend.dto.transacao.TransacaoResponseDTO;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.model.enums.TipoPagamento;
import bcd.appfinanceirobackend.model.enums.TipoTransacao;
import bcd.appfinanceirobackend.repository.UsuarioRepository;
import bcd.appfinanceirobackend.security.JwtUtil;
import bcd.appfinanceirobackend.service.TransacaoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransacaoController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class})
@DisplayName("TransacaoController - editar e excluir")
class TransacaoEdicaoExclusaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransacaoService transacaoService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UsuarioRepository usuarioRepository;

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private Usuario usuarioAutenticado;
    private UUID transacaoId;
    private TransacaoRequestDTO requestValido;
    private TransacaoResponseDTO responseValido;

    @BeforeEach
    void setUp() {
        usuarioAutenticado = new Usuario();
        usuarioAutenticado.setId(UUID.randomUUID());
        usuarioAutenticado.setNome("João Silva");
        usuarioAutenticado.setEmail("joao@email.com");
        usuarioAutenticado.setSenha("hash");
        usuarioAutenticado.setCpf("12345678900");

        transacaoId = UUID.randomUUID();

        requestValido = new TransacaoRequestDTO();
        requestValido.setValor(new BigDecimal("250.00"));
        requestValido.setData(LocalDate.of(2026, 6, 1));
        requestValido.setDescricao("Transação atualizada");
        requestValido.setTipoTransacao(TipoTransacao.DEBITO);
        requestValido.setFormaPagamento(TipoPagamento.PIX);
        requestValido.setContaId(UUID.randomUUID());
        requestValido.setCategoriaId(UUID.randomUUID());

        responseValido = new TransacaoResponseDTO();
        responseValido.setTransacaoId(transacaoId);
        responseValido.setValor(requestValido.getValor());
        responseValido.setData(requestValido.getData());
        responseValido.setDescricao(requestValido.getDescricao());
        responseValido.setTipoTransacao(requestValido.getTipoTransacao());
        responseValido.setFormaPagamento(requestValido.getFormaPagamento());
        responseValido.setContaId(requestValido.getContaId());
        responseValido.setCategoriaId(requestValido.getCategoriaId());
        responseValido.setCategorizada(true);
    }

    @Nested
    @DisplayName("PUT /transacoes/{transacaoId}")
    class EditarTransacao {

        @Test
        @DisplayName("Retorna 200 com transação editada")
        void deveRetornar200ComTransacaoEditada() throws Exception {
            when(transacaoService.editar(eq(transacaoId), any(TransacaoRequestDTO.class), any(Usuario.class)))
                    .thenReturn(responseValido);

            mockMvc.perform(put("/transacoes/{transacaoId}", transacaoId)
                            .with(user(usuarioAutenticado))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(requestValido)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.transacaoId").value(transacaoId.toString()))
                    .andExpect(jsonPath("$.valor").value(250.00))
                    .andExpect(jsonPath("$.data").value("2026-06-01"))
                    .andExpect(jsonPath("$.descricao").value("Transação atualizada"))
                    .andExpect(jsonPath("$.tipoTransacao").value("DEBITO"))
                    .andExpect(jsonPath("$.formaPagamento").value("PIX"))
                    .andExpect(jsonPath("$.contaId").value(requestValido.getContaId().toString()))
                    .andExpect(jsonPath("$.categoriaId").value(requestValido.getCategoriaId().toString()))
                    .andExpect(jsonPath("$.categorizada").value(true));
        }

        @Test
        @DisplayName("Delega edição para o service usando id, request e usuário autenticado")
        void deveDelegarParaServiceComIdRequestEUsuario() throws Exception {
            ArgumentCaptor<TransacaoRequestDTO> dtoCaptor = ArgumentCaptor.forClass(TransacaoRequestDTO.class);
            ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);

            when(transacaoService.editar(eq(transacaoId), any(TransacaoRequestDTO.class), any(Usuario.class)))
                    .thenReturn(responseValido);

            mockMvc.perform(put("/transacoes/{transacaoId}", transacaoId)
                            .with(user(usuarioAutenticado))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(requestValido)))
                    .andExpect(status().isOk());

            verify(transacaoService).editar(eq(transacaoId), dtoCaptor.capture(), usuarioCaptor.capture());
            assertThat(dtoCaptor.getValue().getValor()).isEqualByComparingTo(requestValido.getValor());
            assertThat(dtoCaptor.getValue().getContaId()).isEqualTo(requestValido.getContaId());
            assertThat(usuarioCaptor.getValue()).isEqualTo(usuarioAutenticado);
        }

        @Test
        @DisplayName("Retorna 401 ou 403 quando usuário não está autenticado")
        void deveRetornar401Ou403QuandoNaoAutenticado() throws Exception {
            mockMvc.perform(put("/transacoes/{transacaoId}", transacaoId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(requestValido)))
                    .andExpect(status().is4xxClientError());

            verify(transacaoService, never()).editar(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("DELETE /transacoes/{transacaoId}")
    class ExcluirTransacao {

        @Test
        @DisplayName("Retorna 204 ao excluir transação")
        void deveRetornar204AoExcluirTransacao() throws Exception {
            doNothing().when(transacaoService).excluir(eq(transacaoId), any(Usuario.class));

            mockMvc.perform(delete("/transacoes/{transacaoId}", transacaoId)
                            .with(user(usuarioAutenticado)))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Delega exclusão para o service usando id e usuário autenticado")
        void deveDelegarParaServiceComIdEUsuario() throws Exception {
            ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);

            mockMvc.perform(delete("/transacoes/{transacaoId}", transacaoId)
                            .with(user(usuarioAutenticado)))
                    .andExpect(status().isNoContent());

            verify(transacaoService).excluir(eq(transacaoId), usuarioCaptor.capture());
            assertThat(usuarioCaptor.getValue()).isEqualTo(usuarioAutenticado);
        }

        @Test
        @DisplayName("Retorna 401 ou 403 quando usuário não está autenticado")
        void deveRetornar401Ou403QuandoNaoAutenticado() throws Exception {
            mockMvc.perform(delete("/transacoes/{transacaoId}", transacaoId))
                    .andExpect(status().is4xxClientError());

            verify(transacaoService, never()).excluir(any(), any());
        }
    }
}