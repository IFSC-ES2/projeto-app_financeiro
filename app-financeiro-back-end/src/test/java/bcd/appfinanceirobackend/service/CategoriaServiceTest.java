package bcd.appfinanceirobackend.service;

import bcd.appfinanceirobackend.dto.transacao.CategoriaTransacaoDTO;
import bcd.appfinanceirobackend.model.Categoria;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.repository.CategoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoriaService")
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaService categoriaService;

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

    @Nested
    @DisplayName("Listagem de categorias")
    class ListagemCategorias {

        @Test
        @DisplayName("Retorna categorias padrão e categorias do usuário autenticado")
        void deveRetornarCategoriasPadraoECategoriasDoUsuario() {
            Categoria categoriaPadrao = criarCategoriaPadrao(
                    "Alimentação",
                    "icone-alimentacao",
                    "#FFAA00"
            );
            Categoria categoriaUsuario = criarCategoriaDoUsuario(
                    "Academia",
                    "icone-academia",
                    "#00AAFF",
                    usuarioAutenticado
            );

            when(categoriaRepository.findByPadraoTrueOrUsuarioId(usuarioAutenticado.getId()))
                    .thenReturn(List.of(categoriaPadrao, categoriaUsuario));

            List<CategoriaTransacaoDTO> response = categoriaService.listarParaUsuario(usuarioAutenticado);

            assertThat(response).hasSize(2);

            assertThat(response.get(0).getCategoriaId()).isEqualTo(categoriaPadrao.getId());
            assertThat(response.get(0).getNome()).isEqualTo("Alimentação");
            assertThat(response.get(0).isPadrao()).isTrue();

            assertThat(response.get(1).getCategoriaId()).isEqualTo(categoriaUsuario.getId());
            assertThat(response.get(1).getNome()).isEqualTo("Academia");
            assertThat(response.get(1).isPadrao()).isFalse();
        }

        @Test
        @DisplayName("Busca categorias usando filtro de padrão ou usuário autenticado")
        void deveBuscarCategoriasComFiltroDeUsuarioOuPadrao() {
            when(categoriaRepository.findByPadraoTrueOrUsuarioId(usuarioAutenticado.getId()))
                    .thenReturn(List.of());

            categoriaService.listarParaUsuario(usuarioAutenticado);

            verify(categoriaRepository).findByPadraoTrueOrUsuarioId(usuarioAutenticado.getId());
        }

        @Test
        @DisplayName("Mapeia categoria para DTO")
        void deveMapearCategoriaParaDTO() {
            Categoria categoria = criarCategoriaPadrao(
                    "Saúde",
                    "icone-saude",
                    "#00FF00"
            );

            when(categoriaRepository.findByPadraoTrueOrUsuarioId(usuarioAutenticado.getId()))
                    .thenReturn(List.of(categoria));

            List<CategoriaTransacaoDTO> response = categoriaService.listarParaUsuario(usuarioAutenticado);

            assertThat(response).hasSize(1);
            assertThat(response.get(0).getCategoriaId()).isEqualTo(categoria.getId());
            assertThat(response.get(0).getNome()).isEqualTo(categoria.getNome());
            assertThat(response.get(0).getIcone()).isEqualTo(categoria.getIcone());
            assertThat(response.get(0).getCor()).isEqualTo(categoria.getCor());
            assertThat(response.get(0).isPadrao()).isEqualTo(categoria.isPadrao());
        }

        @Test
        @DisplayName("Retorna lista vazia quando não existem categorias")
        void deveRetornarListaVaziaQuandoNaoExistemCategorias() {
            when(categoriaRepository.findByPadraoTrueOrUsuarioId(usuarioAutenticado.getId()))
                    .thenReturn(List.of());

            List<CategoriaTransacaoDTO> response = categoriaService.listarParaUsuario(usuarioAutenticado);

            assertThat(response).isEmpty();
        }
    }

    private Categoria criarCategoriaPadrao(String nome, String icone, String cor) {
        Categoria categoria = new Categoria();
        categoria.setId(UUID.randomUUID());
        categoria.setNome(nome);
        categoria.setIcone(icone);
        categoria.setCor(cor);
        categoria.setPadrao(true);
        categoria.setUsuario(null);
        return categoria;
    }

    private Categoria criarCategoriaDoUsuario(String nome, String icone, String cor, Usuario usuario) {
        Categoria categoria = new Categoria();
        categoria.setId(UUID.randomUUID());
        categoria.setNome(nome);
        categoria.setIcone(icone);
        categoria.setCor(cor);
        categoria.setPadrao(false);
        categoria.setUsuario(usuario);
        return categoria;
    }
}