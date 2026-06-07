package bcd.appfinanceirobackend.controller;

import bcd.appfinanceirobackend.config.JwtAuthFilter;
import bcd.appfinanceirobackend.config.SecurityConfig;
import bcd.appfinanceirobackend.dto.transacao.TransacaoRequestDTO;
import bcd.appfinanceirobackend.dto.transacao.TransacaoResponseDTO;
import bcd.appfinanceirobackend.exception.ResourceNotFoundException;
import bcd.appfinanceirobackend.model.Conta;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.model.enums.TipoConta;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransacaoController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class})
@DisplayName("POST /transacoes/manual - Integração")
class RegistrarManualIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransacaoService transacaoService;

    // Dependências do JwtAuthFilter: JwtUtil e UsuarioRepository
    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UsuarioRepository usuarioRepository;

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private Usuario usuarioAutenticado;
    private Conta conta;
    private TransacaoRequestDTO dtoValido;
    private TransacaoResponseDTO responseValido;
    private UUID contaId;

    @BeforeEach
    void setUp() {
        contaId = UUID.randomUUID();

        usuarioAutenticado = new Usuario();
        usuarioAutenticado.setId(UUID.randomUUID());
        usuarioAutenticado.setNome("João Silva");
        usuarioAutenticado.setEmail("joao@email.com");
        usuarioAutenticado.setSenha("hash");
        usuarioAutenticado.setCpf("12345678900");

        conta = new Conta();
        conta.setId(contaId);
        conta.setNome("Conta Corrente");
        conta.setTipoConta(TipoConta.CORRENTE);
        conta.setUsuario(usuarioAutenticado);

        dtoValido = new TransacaoRequestDTO();
        dtoValido.setValor(new BigDecimal("200.00"));
        dtoValido.setData(LocalDate.of(2025, 5, 10));
        dtoValido.setDescricao("Supermercado");
        dtoValido.setTipoTransacao(TipoTransacao.DEBITO);
        dtoValido.setFormaPagamento(TipoPagamento.PIX);
        dtoValido.setContaId(contaId);

        responseValido = new TransacaoResponseDTO();
        responseValido.setTransacaoId(UUID.randomUUID());
        responseValido.setValor(dtoValido.getValor());
        responseValido.setData(dtoValido.getData());
        responseValido.setDescricao(dtoValido.getDescricao());
        responseValido.setTipoTransacao(dtoValido.getTipoTransacao());
        responseValido.setFormaPagamento(dtoValido.getFormaPagamento());
        responseValido.setContaId(contaId);
    }
    

    // ─────────────────────────── SUCESSO ─────────────────────────────────────

    @Nested
    @DisplayName("Registro bem-sucedido")
    class RegistroBemSucedido {

        @Test
        @DisplayName("Retorna 201 Created com o body da transação registrada")
        void deveRetornar201ComBodyCorreto() throws Exception {
            when(transacaoService.registrarManual(any(), any())).thenReturn(responseValido);

            mockMvc.perform(post("/transacoes/manual")
                            .with(user(usuarioAutenticado))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dtoValido)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.valor").value(200.00))
                    .andExpect(jsonPath("$.descricao").value("Supermercado"))
                    .andExpect(jsonPath("$.tipoTransacao").value("DEBITO"))
                    .andExpect(jsonPath("$.formaPagamento").value("PIX"))
                    .andExpect(jsonPath("$.contaId").value(contaId.toString()));
        }

        @Test
        @DisplayName("Retorna 201 com categoriaId nulo quando transação não tem categoria")
        void deveRetornar201ComCategoriaIdNulo() throws Exception {
            responseValido.setCategoriaId(null);
            when(transacaoService.registrarManual(any(), any())).thenReturn(responseValido);

            mockMvc.perform(post("/transacoes/manual")
                            .with(user(usuarioAutenticado))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dtoValido)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.categoriaId").doesNotExist());
        }

        @Test
        @DisplayName("Retorna 201 quando descrição é omitidos")
        void deveRetornar201SemCamposOpcionais() throws Exception {
            dtoValido.setDescricao(null);
            responseValido.setDescricao(null);

            when(transacaoService.registrarManual(any(), any())).thenReturn(responseValido);

            mockMvc.perform(post("/transacoes/manual")
                            .with(user(usuarioAutenticado))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dtoValido)))
                    .andExpect(status().isCreated());
        }
    }

    // ─────────────────────────── ERROS DO CLIENTE ────────────────────────────

    @Nested
    @DisplayName("Erros do cliente (4xx)")
    class ErrosDoCliente {

        @Test
        @DisplayName("Retorna 400 quando o service lança IllegalArgumentException por campo obrigatório ausente")
        void deveRetornar400QuandoCampoObrigatorioAusente() throws Exception {
            when(transacaoService.registrarManual(any(), any()))
                    .thenThrow(new IllegalArgumentException(
                            "Campos obrigatórios não informados(valor, data, contaId, tipoTransacao)"));

            dtoValido.setValor(null);

            mockMvc.perform(post("/transacoes/manual")
                            .with(user(usuarioAutenticado))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dtoValido)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("Campos obrigatórios")));
        }

        @Test
        @DisplayName("Retorna 404 quando a conta informada não existe")
        void deveRetornar404QuandoContaNaoEncontrada() throws Exception {
            when(transacaoService.registrarManual(any(), any()))
                    .thenThrow(new ResourceNotFoundException("Conta não encontrada"));

            mockMvc.perform(post("/transacoes/manual")
                            .with(user(usuarioAutenticado))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dtoValido)))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string("Conta não encontrada"));
        }

        @Test
        @DisplayName("Retorna 403 quando a conta pertence a outro usuário")
        void deveRetornar403QuandoContaNaoPertenceAoUsuario() throws Exception {
            when(transacaoService.registrarManual(any(), any()))
                    .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado a esta conta"));

            mockMvc.perform(post("/transacoes/manual")
                            .with(user(usuarioAutenticado))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dtoValido)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Retorna 415 quando Content-Type não é JSON")
        void deveRetornar415QuandoContentTypeInvalido() throws Exception {
            mockMvc.perform(post("/transacoes/manual")
                            .with(user(usuarioAutenticado))
                            .contentType(MediaType.TEXT_PLAIN)
                            .content("texto plano"))
                    .andExpect(status().isUnsupportedMediaType());
        }

        @Test
        @DisplayName("Retorna 400 quando o body da requisição está malformado")
        void deveRetornar400QuandoBodyMalformado() throws Exception {
            mockMvc.perform(post("/transacoes/manual")
                            .with(user(usuarioAutenticado))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{ json inválido }"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Retorna 400 quando valor é zero")
        void deveRetornar400QuandoValorZero() throws Exception {
            dtoValido.setValor(BigDecimal.ZERO);

            when(transacaoService.registrarManual(any(), any()))
                    .thenThrow(new IllegalArgumentException("O valor informado deve ser maior que zero"));

            mockMvc.perform(post("/transacoes/manual")
                            .with(user(usuarioAutenticado))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dtoValido)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString(
                            "O valor informado deve ser maior que zero"
                    )));
        }

        @Test
        @DisplayName("Retorna 400 quando valor é negativo")
        void deveRetornar400QuandoValorNegativo() throws Exception {
            dtoValido.setValor(new BigDecimal("-50.00"));

            when(transacaoService.registrarManual(any(), any()))
                    .thenThrow(new IllegalArgumentException("O valor informado deve ser maior que zero"));

            mockMvc.perform(post("/transacoes/manual")
                            .with(user(usuarioAutenticado))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dtoValido)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString(
                            "O valor informado deve ser maior que zero"
                    )));
        }

        @Test
        @DisplayName("Retorna 401 ou 403 quando usuário não está autenticado")
        void deveRetornar401Ou403QuandoNaoAutenticado() throws Exception {
            mockMvc.perform(post("/transacoes/manual")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dtoValido)))
                    .andExpect(status().is4xxClientError());

            verify(transacaoService, never()).registrarManual(any(), any());
        }

        @Test
        @DisplayName("Deve retornar 400 quando forma de pagamento não é informada")
        void deveRetornar400QuandoFormaPagamentoNaoInformada() throws Exception {
            TransacaoRequestDTO request = new TransacaoRequestDTO();
            request.setContaId(contaId);
            request.setValor(BigDecimal.valueOf(100));
            request.setData(LocalDate.now());
            request.setTipoTransacao(TipoTransacao.DEBITO);
            request.setFormaPagamento(null);

            when(transacaoService.registrarManual(any(TransacaoRequestDTO.class), any(Usuario.class)))
                    .thenThrow(new IllegalArgumentException("Campos obrigatórios não informados"));

            mockMvc.perform(post("/transacoes/manual")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }
}