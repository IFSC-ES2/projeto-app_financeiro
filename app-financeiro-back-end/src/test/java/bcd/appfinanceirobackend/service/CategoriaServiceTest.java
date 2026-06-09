package bcd.appfinanceirobackend.service;

import bcd.appfinanceirobackend.dto.transacao.CategoriaTransacaoDTO;
import bcd.appfinanceirobackend.model.Categoria;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.repository.CategoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
@DisplayName("CategoriaService - listarParaUsuario")
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setNome("João Silva");
        usuario.setEmail("joao@email.com");
    }

    private Categoria categoria(String nome, String icone, String cor, boolean padrao, Usuario dono) {
        Categoria c = new Categoria();
        c.setId(UUID.randomUUID());
        c.setNome(nome);
        c.setIcone(icone);
        c.setCor(cor);
        c.setPadrao(padrao);
        c.setUsuario(dono);
        return c;
    }

    @Test
    @DisplayName("Retorna as categorias padrão do sistema")
    void deveRetornarCategoriasPadrao() {
        Categoria alimentacao = categoria("Alimentação", "🍔", "#FF6B6B", true, null);
        Categoria transporte = categoria("Transporte", "🚗", "#4ECDC4", true, null);
        when(categoriaRepository.findByPadraoTrueOrUsuarioId(usuario.getId()))
                .thenReturn(List.of(alimentacao, transporte));

        List<CategoriaTransacaoDTO> resultado = categoriaService.listarParaUsuario(usuario);

        assertThat(resultado).hasSize(2);
        assertThat(resultado).extracting(CategoriaTransacaoDTO::getNome)
                .containsExactly("Alimentação", "Transporte");
        assertThat(resultado).allMatch(CategoriaTransacaoDTO::isPadrao);
    }

    @Test
    @DisplayName("Retorna também as categorias personalizadas do usuário autenticado")
    void deveRetornarCategoriasPersonalizadasDoUsuario() {
        Categoria padrao = categoria("Saúde", "💊", "#45B7D1", true, null);
        Categoria personalizada = categoria("Pets", "🐶", "#000000", false, usuario);
        when(categoriaRepository.findByPadraoTrueOrUsuarioId(usuario.getId()))
                .thenReturn(List.of(padrao, personalizada));

        List<CategoriaTransacaoDTO> resultado = categoriaService.listarParaUsuario(usuario);

        assertThat(resultado).extracting(CategoriaTransacaoDTO::getNome).contains("Pets");

        CategoriaTransacaoDTO pets = resultado.stream()
                .filter(c -> "Pets".equals(c.getNome()))
                .findFirst()
                .orElseThrow();
        assertThat(pets.isPadrao()).isFalse();
    }

    @Test
    @DisplayName("Mapeia corretamente todos os campos da categoria para o DTO")
    void deveMapearTodosOsCamposParaDTO() {
        Categoria categoria = categoria("Lazer", "🎬", "#96CEB4", true, null);
        when(categoriaRepository.findByPadraoTrueOrUsuarioId(usuario.getId()))
                .thenReturn(List.of(categoria));

        CategoriaTransacaoDTO dto = categoriaService.listarParaUsuario(usuario).getFirst();

        assertThat(dto.getCategoriaId()).isEqualTo(categoria.getId());
        assertThat(dto.getNome()).isEqualTo("Lazer");
        assertThat(dto.getIcone()).isEqualTo("🎬");
        assertThat(dto.getCor()).isEqualTo("#96CEB4");
        assertThat(dto.isPadrao()).isTrue();
    }

    @Test
    @DisplayName("Consulta o repositório usando o id do usuário autenticado")
    void deveConsultarRepositorioComIdDoUsuario() {
        when(categoriaRepository.findByPadraoTrueOrUsuarioId(usuario.getId())).thenReturn(List.of());

        categoriaService.listarParaUsuario(usuario);

        verify(categoriaRepository).findByPadraoTrueOrUsuarioId(usuario.getId());
    }

    @Test
    @DisplayName("Retorna lista vazia quando não há categorias")
    void deveRetornarListaVaziaQuandoNaoHaCategorias() {
        when(categoriaRepository.findByPadraoTrueOrUsuarioId(usuario.getId())).thenReturn(List.of());

        assertThat(categoriaService.listarParaUsuario(usuario)).isEmpty();
    }
}
