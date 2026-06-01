package bcd.appfinanceirobackend.controller;

import bcd.appfinanceirobackend.config.JwtAuthFilter;
import bcd.appfinanceirobackend.config.SecurityConfig;
import bcd.appfinanceirobackend.dto.conta.ContaRequestDTO;
import bcd.appfinanceirobackend.dto.conta.ContaResponseDTO;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.model.enums.TipoConta;
import bcd.appfinanceirobackend.repository.UsuarioRepository;
import bcd.appfinanceirobackend.security.JwtUtil;
import bcd.appfinanceirobackend.service.ContaService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ContaController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class})
@DisplayName("ContaController")
class ContaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContaService contaService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UsuarioRepository usuarioRepository;

    private final ObjectMapper mapper = new ObjectMapper();

    private Usuario usuarioAutenticado;
    private ContaRequestDTO requestValido;
    private ContaResponseDTO responseValido;

    @BeforeEach
    void setUp() {
        usuarioAutenticado = new Usuario();
        usuarioAutenticado.setId(UUID.randomUUID());
        usuarioAutenticado.setNome("João Silva");
        usuarioAutenticado.setEmail("joao@email.com");
        usuarioAutenticado.setSenha("hash");
        usuarioAutenticado.setCpf("12345678900");

        requestValido = new ContaRequestDTO();
        requestValido.setNome("Conta Nubank");
        requestValido.setTipoConta(TipoConta.CORRENTE);
        requestValido.setBanco("Nubank");
        requestValido.setDescricao("Conta principal");

        responseValido = new ContaResponseDTO();
        responseValido.setContaId(UUID.randomUUID());
        responseValido.setNome(requestValido.getNome());
        responseValido.setTipoConta(requestValido.getTipoConta());
        responseValido.setBanco(requestValido.getBanco());
        responseValido.setDescricao(requestValido.getDescricao());
    }

    @Nested
    @DisplayName("GET /contas")
    class ListarContas {

        @Test
        @DisplayName("Retorna 200 com contas do usuário autenticado")
        void deveRetornar200ComContasDoUsuario() throws Exception {
            when(contaService.listarPorUsuario(any(Usuario.class)))
                    .thenReturn(List.of(responseValido));

            mockMvc.perform(get("/contas")
                            .with(user(usuarioAutenticado)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].contaId").value(responseValido.getContaId().toString()))
                    .andExpect(jsonPath("$[0].nome").value("Conta Nubank"))
                    .andExpect(jsonPath("$[0].tipoConta").value("CORRENTE"))
                    .andExpect(jsonPath("$[0].banco").value("Nubank"))
                    .andExpect(jsonPath("$[0].descricao").value("Conta principal"));
        }

        @Test
        @DisplayName("Retorna 200 com lista vazia quando usuário não possui contas")
        void deveRetornarListaVazia() throws Exception {
            when(contaService.listarPorUsuario(any(Usuario.class)))
                    .thenReturn(List.of());

            mockMvc.perform(get("/contas")
                            .with(user(usuarioAutenticado)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        @DisplayName("Delega listagem para o service usando o usuário autenticado")
        void deveDelegarParaServiceComUsuarioAutenticado() throws Exception {
            ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);

            when(contaService.listarPorUsuario(any(Usuario.class)))
                    .thenReturn(List.of());

            mockMvc.perform(get("/contas")
                            .with(user(usuarioAutenticado)))
                    .andExpect(status().isOk());

            verify(contaService).listarPorUsuario(captor.capture());
            assertThat(captor.getValue()).isEqualTo(usuarioAutenticado);
        }

        @Test
        @DisplayName("Retorna 401 ou 403 quando usuário não está autenticado")
        void deveRetornar401Ou403QuandoNaoAutenticado() throws Exception {
            mockMvc.perform(get("/contas"))
                    .andExpect(status().is4xxClientError());

            verify(contaService, never()).listarPorUsuario(any());
        }
    }

    @Nested
    @DisplayName("POST /contas/registrar")
    class RegistrarConta {

        @Test
        @DisplayName("Retorna 201 Created com a conta criada")
        void deveRetornar201ComContaCriada() throws Exception {
            when(contaService.registrar(any(ContaRequestDTO.class), any(Usuario.class)))
                    .thenReturn(responseValido);

            mockMvc.perform(post("/contas/registrar")
                            .with(user(usuarioAutenticado))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(requestValido)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.contaId").value(responseValido.getContaId().toString()))
                    .andExpect(jsonPath("$.nome").value("Conta Nubank"))
                    .andExpect(jsonPath("$.tipoConta").value("CORRENTE"))
                    .andExpect(jsonPath("$.banco").value("Nubank"))
                    .andExpect(jsonPath("$.descricao").value("Conta principal"));
        }

        @Test
        @DisplayName("Delega registro para o service usando request e usuário autenticado")
        void deveDelegarParaServiceComRequestEUsuarioAutenticado() throws Exception {
            ArgumentCaptor<ContaRequestDTO> dtoCaptor = ArgumentCaptor.forClass(ContaRequestDTO.class);
            ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);

            when(contaService.registrar(any(ContaRequestDTO.class), any(Usuario.class)))
                    .thenReturn(responseValido);

            mockMvc.perform(post("/contas/registrar")
                            .with(user(usuarioAutenticado))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(requestValido)))
                    .andExpect(status().isCreated());

            verify(contaService).registrar(dtoCaptor.capture(), usuarioCaptor.capture());

            assertThat(dtoCaptor.getValue().getNome()).isEqualTo(requestValido.getNome());
            assertThat(dtoCaptor.getValue().getTipoConta()).isEqualTo(requestValido.getTipoConta());
            assertThat(dtoCaptor.getValue().getBanco()).isEqualTo(requestValido.getBanco());
            assertThat(dtoCaptor.getValue().getDescricao()).isEqualTo(requestValido.getDescricao());
            assertThat(usuarioCaptor.getValue()).isEqualTo(usuarioAutenticado);
        }

        @Test
        @DisplayName("Retorna 400 quando nome é inválido")
        void deveRetornar400QuandoNomeInvalido() throws Exception {
            requestValido.setNome(null);

            when(contaService.registrar(any(ContaRequestDTO.class), any(Usuario.class)))
                    .thenThrow(new IllegalArgumentException(
                            "Campos obrigatórios de uma conta não informados (nome e TipoConta)"));

            mockMvc.perform(post("/contas/registrar")
                            .with(user(usuarioAutenticado))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(requestValido)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Campos obrigatórios")));
        }

        @Test
        @DisplayName("Retorna 400 quando tipoConta é nulo")
        void deveRetornar400QuandoTipoContaNulo() throws Exception {
            requestValido.setTipoConta(null);

            when(contaService.registrar(any(ContaRequestDTO.class), any(Usuario.class)))
                    .thenThrow(new IllegalArgumentException(
                            "Campos obrigatórios de uma conta não informados (nome e TipoConta)"));

            mockMvc.perform(post("/contas/registrar")
                            .with(user(usuarioAutenticado))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(requestValido)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Campos obrigatórios")));
        }

        @Test
        @DisplayName("Retorna 400 quando o body está malformado")
        void deveRetornar400QuandoBodyMalformado() throws Exception {
            mockMvc.perform(post("/contas/registrar")
                            .with(user(usuarioAutenticado))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{ json inválido }"))
                    .andExpect(status().isBadRequest());

            verify(contaService, never()).registrar(any(), any());
        }

        @Test
        @DisplayName("Retorna 415 quando Content-Type não é JSON")
        void deveRetornar415QuandoContentTypeInvalido() throws Exception {
            mockMvc.perform(post("/contas/registrar")
                            .with(user(usuarioAutenticado))
                            .contentType(MediaType.TEXT_PLAIN)
                            .content("texto plano"))
                    .andExpect(status().isUnsupportedMediaType());

            verify(contaService, never()).registrar(any(), any());
        }

        @Test
        @DisplayName("Retorna 401 ou 403 quando usuário não está autenticado")
        void deveRetornar401Ou403QuandoNaoAutenticado() throws Exception {
            mockMvc.perform(post("/contas/registrar")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(requestValido)))
                    .andExpect(status().is4xxClientError());

            verify(contaService, never()).registrar(any(), any());
        }
    }
}