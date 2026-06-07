package bcd.appfinanceirobackend.service;

import bcd.appfinanceirobackend.model.Categoria;
import bcd.appfinanceirobackend.repository.CategoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SugestaoCategoriaService")
class SugestaoCategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    private SugestaoCategoriaService sugestaoCategoriaService;

    @BeforeEach
    void setUp() {
        sugestaoCategoriaService = new SugestaoCategoriaService(categoriaRepository);
    }

    @Nested
    @DisplayName("sugerirCategoria()")
    class SugerirCategoria {

        @Test
        @DisplayName("Sugere categoria a partir de palavra-chave da descrição")
        void deveSugerirPorPalavraChave() {
            Categoria transporte = new Categoria();
            transporte.setId(UUID.randomUUID());
            transporte.setNome("Transporte");
            transporte.setPadrao(true);

            when(categoriaRepository.findByNomeAndPadraoTrue("Transporte"))
                    .thenReturn(Optional.of(transporte));

            Categoria sugerida = sugestaoCategoriaService.sugerirCategoria("Pagamento Uber centro");

            assertThat(sugerida).isSameAs(transporte);
        }

        @Test
        @DisplayName("Ignora acentuação ao casar a palavra-chave")
        void deveIgnorarAcentuacao() {
            Categoria saude = new Categoria();
            saude.setId(UUID.randomUUID());
            saude.setNome("Saúde");
            saude.setPadrao(true);

            when(categoriaRepository.findByNomeAndPadraoTrue("Saúde"))
                    .thenReturn(Optional.of(saude));

            Categoria sugerida = sugestaoCategoriaService.sugerirCategoria("Compra na Farmácia São João");

            assertThat(sugerida).isSameAs(saude);
        }

        @Test
        @DisplayName("Retorna null quando a descrição é nula, sem consultar o repositório")
        void deveRetornarNullParaDescricaoNula() {
            Categoria sugerida = sugestaoCategoriaService.sugerirCategoria(null);

            assertThat(sugerida).isNull();
            verifyNoInteractions(categoriaRepository);
        }

        @Test
        @DisplayName("Retorna null quando a descrição está em branco")
        void deveRetornarNullParaDescricaoEmBranco() {
            Categoria sugerida = sugestaoCategoriaService.sugerirCategoria("   ");

            assertThat(sugerida).isNull();
            verifyNoInteractions(categoriaRepository);
        }

        @Test
        @DisplayName("Retorna null quando nenhuma palavra-chave casa")
        void deveRetornarNullQuandoSemCorrespondencia() {
            Categoria sugerida = sugestaoCategoriaService.sugerirCategoria("Transferência diversa 1234");

            assertThat(sugerida).isNull();
            verifyNoInteractions(categoriaRepository);
        }

        @Test
        @DisplayName("Respeita limite de palavra: 'Uberlandia' não casa com 'uber'")
        void naoDeveCasarPalavraParcial() {
            Categoria sugerida = sugestaoCategoriaService.sugerirCategoria("Viagem para Uberlandia");

            assertThat(sugerida).isNull();
            verifyNoInteractions(categoriaRepository);
        }

        @Test
        @DisplayName("Retorna null quando a palavra-chave casa, mas a categoria padrão não existe")
        void deveRetornarNullQuandoCategoriaPadraoNaoExiste() {
            when(categoriaRepository.findByNomeAndPadraoTrue("Transporte"))
                    .thenReturn(Optional.empty());

            Categoria sugerida = sugestaoCategoriaService.sugerirCategoria("Corrida de Uber");

            assertThat(sugerida).isNull();
        }
    }
}