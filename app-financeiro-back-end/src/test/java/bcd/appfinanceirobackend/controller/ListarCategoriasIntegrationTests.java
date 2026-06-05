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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoriaController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class})
@DisplayName("GET /categorias - Integração")
class ListarCategoriasIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoriaService categoriaService;

    // Dependências do JwtAuthFilter
    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UsuarioRepository usuarioRepository;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setNome("João Silva");
        usuario.setEmail("joao@email.com");
        usuario.setSenha("hash");
        usuario.setCpf("12345678900");
    }

    private CategoriaTransacaoDTO dto(String nome, boolean padrao) {
        CategoriaTransacaoDTO c = new CategoriaTransacaoDTO();
        c.setCategoriaId(UUID.randomUUID());
        c.setNome(nome);
        c.setPadrao(padrao);
        return c;
    }

    @Test
    @DisplayName("Retorna 200 com as categorias padrão e personalizadas do usuário")
    void deveRetornar200ComCategorias() throws Exception {
        when(categoriaService.listarParaUsuario(any()))
                .thenReturn(List.of(dto("Alimentação", true), dto("Pets", false)));

        mockMvc.perform(get("/categorias").with(user(usuario)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nome").value("Alimentação"))
                .andExpect(jsonPath("$[0].padrao").value(true))
                .andExpect(jsonPath("$[1].nome").value("Pets"))
                .andExpect(jsonPath("$[1].padrao").value(false));
    }

    @Test
    @DisplayName("Retorna 200 com lista vazia quando o usuário não tem categorias")
    void deveRetornar200ComListaVazia() throws Exception {
        when(categoriaService.listarParaUsuario(any())).thenReturn(List.of());

        mockMvc.perform(get("/categorias").with(user(usuario)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Bloqueia o acesso sem autenticação")
    void deveBloquearSemAutenticacao() throws Exception {
        mockMvc.perform(get("/categorias"))
                .andExpect(status().is4xxClientError());
    }
}
