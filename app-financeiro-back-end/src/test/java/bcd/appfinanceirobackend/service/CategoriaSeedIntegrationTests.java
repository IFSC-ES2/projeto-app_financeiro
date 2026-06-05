package bcd.appfinanceirobackend.service;

import bcd.appfinanceirobackend.dto.transacao.CategoriaTransacaoDTO;
import bcd.appfinanceirobackend.model.Categoria;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.repository.CategoriaRepository;
import bcd.appfinanceirobackend.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
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
    @DisplayName("Categoria personalizada aparece apenas para o usuário dono")
    void categoriaPersonalizadaVisivelApenasParaDono() {
        Usuario dono = new Usuario();
        dono.setNome("Dono das Categorias");
        dono.setEmail("dono-categorias@test.com");
        dono.setSenha("hash");
        dono.setCpf("98765432100");
        dono.setCreatedAt(LocalDateTime.now());
        dono = usuarioRepository.save(dono);

        Usuario outro = new Usuario();
        outro.setId(UUID.randomUUID());

        Categoria personalizada = new Categoria();
        personalizada.setNome("Investimentos");
        personalizada.setPadrao(false);
        personalizada.setUsuario(dono);
        categoriaRepository.save(personalizada);

        List<String> nomesDono = categoriaService.listarParaUsuario(dono).stream()
                .map(CategoriaTransacaoDTO::getNome).toList();
        List<String> nomesOutro = categoriaService.listarParaUsuario(outro).stream()
                .map(CategoriaTransacaoDTO::getNome).toList();

        assertThat(nomesDono).contains("Investimentos").containsAll(CATEGORIAS_PADRAO);
        assertThat(nomesOutro).doesNotContain("Investimentos").containsAll(CATEGORIAS_PADRAO);
    }
}
