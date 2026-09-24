package bcd.appfinanceirobackend.service;

import bcd.appfinanceirobackend.dto.transacao.CategoriaTransacaoDTO;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.repository.CategoriaRepository;
import bcd.appfinanceirobackend.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@DisplayName("Categorias padrão (seed Flyway) - Integração com banco real")
class CategoriaSeedIntegrationTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("app_financeiro_test")
            .withUsername("postgres")
            .withPassword("1234");

    private static final List<String> CATEGORIAS_PADRAO = List.of(
            "Alimentação", "Transporte", "Saúde", "Lazer", "Habitação", "Serviços", "Manutenção");

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("As 7 categorias padrão estão disponíveis após subir o projeto")
    void categoriasPadraoDisponiveisAposSubir() {
        for (String nome : CATEGORIAS_PADRAO) {
            assertThat(categoriaRepository.findByNomeAndPadraoTrue(nome))
                    .as("categoria padrão '%s' deve existir após o seed do Flyway", nome)
                    .isPresent()
                    .get()
                    .satisfies(categoria -> assertThat(categoria.isPadrao()).isTrue());
        }
    }

    @Test
    @DisplayName("listarParaUsuario expõe as categorias padrão para qualquer usuário")
    void listarRetornaCategoriasPadrao() {
        // Usuário transiente: a consulta usa apenas o id, não precisa existir no banco.
        Usuario usuario = new Usuario();
        usuario.setId(UUID.randomUUID());

        List<CategoriaTransacaoDTO> categorias = categoriaService.listarParaUsuario(usuario);

        assertThat(categorias).extracting(CategoriaTransacaoDTO::getNome).containsAll(CATEGORIAS_PADRAO);
        assertThat(categorias).filteredOn(CategoriaTransacaoDTO::isPadrao).hasSize(7);
    }

    @Test
    @DisplayName("GET /categorias retorna apenas as categorias personalizadas do usuário autenticado")
    void categoriaPersonalizadaVisivelApenasParaDono() throws Exception {
        Usuario usuarioA = salvarUsuario("Usuário A", "usuario-a@test.com", "98765432100");
        Usuario usuarioB = salvarUsuario("Usuário B", "usuario-b@test.com", "98765432101");

        criarCategoria("Investimentos", usuarioA);
        criarCategoria("Viagens", usuarioB);

        mockMvc.perform(get("/categorias")
                        .with(user(usuarioA))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.nome == 'Investimentos')]").exists())
                .andExpect(jsonPath("$[?(@.nome == 'Viagens')]").doesNotExist())
                .andExpect(jsonPath("$[?(@.padrao == true)]").isNotEmpty());
    }

    private Usuario salvarUsuario(String nome, String email, String cpf) {
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenha("hash");
        usuario.setCpf(cpf);
        usuario.setCreatedAt(LocalDateTime.now());
        return usuarioRepository.save(usuario);
    }

    private void criarCategoria(String nome, Usuario usuario) throws Exception {
        mockMvc.perform(post("/categorias")
                        .with(user(usuario))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"%s\"}".formatted(nome)))
                .andExpect(status().isCreated());
    }
}
