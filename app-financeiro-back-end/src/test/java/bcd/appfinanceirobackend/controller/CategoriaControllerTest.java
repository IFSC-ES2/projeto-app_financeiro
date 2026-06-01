package bcd.appfinanceirobackend.controller;

import bcd.appfinanceirobackend.config.JwtAuthFilter;
import bcd.appfinanceirobackend.config.SecurityConfig;
import bcd.appfinanceirobackend.dto.transacao.CategoriaTransacaoDTO;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.repository.UsuarioRepository;
import bcd.appfinanceirobackend.security.JwtUtil;
import bcd.appfinanceirobackend.service.CategoriaService;
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

@WebMvcTest(CategoriaController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class})
@DisplayName("CategoriaController")
class CategoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoriaService categoriaService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UsuarioRepository usuarioRepository;

    private Usuario usuarioAutenticado;
    private CategoriaTransacaoDTO categoriaPadrao;
    private CategoriaTransacaoDTO categoriaUsuario;

    @BeforeEach
    void setUp() {
        usuarioAutenticado = new Usuario();
        usuarioAutenticado.setId(UUID.randomUUID());
        usuarioAutenticado.setNome("João Silva");
        usuarioAutenticado.setEmail("joao@email.com");
        usuarioAutenticado.setSenha("hash");
        usuarioAutenticado.setCpf("12345678900");

        categoriaPadrao = criarCategoriaDTO(
                "Alimentação",
                "icone-alimentacao",
                "#FFAA00",
                true
        );

        categoriaUsuario = criarCategoriaDTO(
                "Academia",
                "icone-academia",
                "#00AAFF",
                false
        );
    }

    @Nested
    @DisplayName("GET /categorias")
    class ListarCategorias {

        @Test
        @DisplayName("Retorna 200 com categorias")
        void deveRetornar200ComCategorias() throws Exception {
            when(categoriaService.listarParaUsuario(any(Usuario.class)))
                    .thenReturn(List.of(categoriaPadrao, categoriaUsuario));

            mockMvc.perform(get("/categorias")
                            .with(user(usuarioAutenticado)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].categoriaId").value(categoriaPadrao.getCategoriaId().toString()))
                    .andExpect(jsonPath("$[0].nome").value("Alimentação"))
                    .andExpect(jsonPath("$[0].icone").value("icone-alimentacao"))
                    .andExpect(jsonPath("$[0].cor").value("#FFAA00"))
                    .andExpect(jsonPath("$[0].padrao").value(true))
                    .andExpect(jsonPath("$[1].categoriaId").value(categoriaUsuario.getCategoriaId().toString()))
                    .andExpect(jsonPath("$[1].nome").value("Academia"))
                    .andExpect(jsonPath("$[1].icone").value("icone-academia"))
                    .andExpect(jsonPath("$[1].cor").value("#00AAFF"))
                    .andExpect(jsonPath("$[1].padrao").value(false));
        }

        @Test
        @DisplayName("Retorna categorias padrão e categorias do usuário")
        void deveRetornarCategoriasPadraoEDoUsuario() throws Exception {
            when(categoriaService.listarParaUsuario(any(Usuario.class)))
                    .thenReturn(List.of(categoriaPadrao, categoriaUsuario));

            mockMvc.perform(get("/categorias")
                            .with(user(usuarioAutenticado)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].padrao").value(true))
                    .andExpect(jsonPath("$[1].padrao").value(false));
        }

        @Test
        @DisplayName("Retorna 200 com lista vazia quando não existem categorias")
        void deveRetornarListaVazia() throws Exception {
            when(categoriaService.listarParaUsuario(any(Usuario.class)))
                    .thenReturn(List.of());

            mockMvc.perform(get("/categorias")
                            .with(user(usuarioAutenticado)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        @DisplayName("Delega listagem para o service usando o usuário autenticado")
        void deveDelegarParaServiceComUsuarioAutenticado() throws Exception {
            ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);

            when(categoriaService.listarParaUsuario(any(Usuario.class)))
                    .thenReturn(List.of());

            mockMvc.perform(get("/categorias")
                            .with(user(usuarioAutenticado)))
                    .andExpect(status().isOk());

            verify(categoriaService).listarParaUsuario(captor.capture());
            assertThat(captor.getValue()).isEqualTo(usuarioAutenticado);
        }

        @Test
        @DisplayName("Retorna 401 ou 403 quando usuário não está autenticado")
        void deveRetornar401Ou403QuandoNaoAutenticado() throws Exception {
            mockMvc.perform(get("/categorias"))
                    .andExpect(status().is4xxClientError());

            verify(categoriaService, never()).listarParaUsuario(any());
        }
    }

    private CategoriaTransacaoDTO criarCategoriaDTO(
            String nome,
            String icone,
            String cor,
            boolean padrao
    ) {
        CategoriaTransacaoDTO dto = new CategoriaTransacaoDTO();
        dto.setCategoriaId(UUID.randomUUID());
        dto.setNome(nome);
        dto.setIcone(icone);
        dto.setCor(cor);
        dto.setPadrao(padrao);
        return dto;
    }
}