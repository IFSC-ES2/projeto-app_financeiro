package bcd.appfinanceirobackend.service;

import bcd.appfinanceirobackend.model.Categoria;
import bcd.appfinanceirobackend.model.RegraCategorizacao;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.repository.RegraCategorizacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SugestaoCategoriaService")
class SugestaoCategoriaServiceTest {

    @Mock
    private RegraCategorizacaoRepository regraRepository;

    private SugestaoCategoriaService service;

    private final UUID usuarioId = UUID.randomUUID();

    private final Categoria alimentacao = categoria("Alimentação");
    private final Categoria transporte = categoria("Transporte");
    private final Categoria saude = categoria("Saúde");
    private final Categoria assinaturas = categoria("Assinaturas");
    private final Categoria lazer = categoria("Lazer");

    private final List<RegraCategorizacao> regras = new ArrayList<>();

    @BeforeEach
    void setUp() {
        service = new SugestaoCategoriaService(regraRepository);

        regras.addAll(List.of(
                global("uber", transporte),
                global("99", transporte),
                global("autoposto", transporte),
                global("supermercado", alimentacao),
                global("99food", alimentacao),
                global("ifood", alimentacao),
                global("ifood club", assinaturas),
                global("+ natural", alimentacao),
                global("farmacia", saude),
                global("raia", saude),
                global("bar", lazer)
        ));
    }

    private Function<String, Categoria> sugestor() {
        when(regraRepository.findGlobaisEDoUsuario(usuarioId)).thenReturn(regras);
        return service.sugestorPara(usuarioId);
    }

    @Nested
    @DisplayName("sugestorPara()")
    class Sugestao {

        @ParameterizedTest(name = "\"{0}\" -> {1}")
        @CsvSource(delimiter = '|', value = {
                "Pagamento Uber centro | Transporte",
                "Corrida de Uber | Transporte",
                "Compra na Farmácia São João | Saúde",
                "Debito visa electron brasil - 26/11 dl*99 99 | Transporte",
                "Debito visa electron brasil - 25/11 99food *tacho deliv | Alimentação",
                "Debito visa electron brasil - 24/11 ifd*ifood club | Assinaturas",
                "Debito visa electron brasil - 20/11 supermercados imper | Alimentação",
                "Debito visa electron brasil - 19/11 autopostocentral | Transporte",
                "Debito visa electron brasil - 18/11 raia440 | Saúde",
                "Debito visa electron brasil - 26/11 + natural | Alimentação",
                "Happy hour no bar do Zé | Lazer"
        })
        @DisplayName("usa a regra que casa com a descrição")
        void sugereCategoria(String descricao, String categoriaEsperada) {
            assertThat(sugestor().apply(descricao).getNome()).isEqualTo(categoriaEsperada);
        }

        @ParameterizedTest(name = "\"{0}\" não casa")
        @CsvSource(delimiter = '|', value = {
                "Viagem para Uberlandia",
                "Barbearia do centro",
                "Transferência diversa 1234",
                "Pix enviado - Fulano de Tal"
        })
        @DisplayName("termo curto só vale como palavra inteira, e descrição sem regra fica sem sugestão")
        void naoSugereSemRegra(String descricao) {
            assertThat(sugestor().apply(descricao)).isNull();
        }

        @Test
        @DisplayName("descrição nula ou vazia não tem sugestão")
        void descricaoVazia() {
            Function<String, Categoria> sugestor = sugestor();

            assertThat(sugestor.apply(null)).isNull();
            assertThat(sugestor.apply("   ")).isNull();
        }

        @Test
        @DisplayName("regra aprendida do usuário tem prioridade sobre a global, mesmo sendo mais curta")
        void regraDoUsuarioTemPrioridade() {
            Usuario usuario = new Usuario();
            usuario.setId(usuarioId);
            RegraCategorizacao doUsuario = global("uber", lazer);
            doUsuario.setUsuario(usuario);
            regras.add(doUsuario);

            assertThat(sugestor().apply("Corrida de Uber").getNome()).isEqualTo("Lazer");
        }

        @Test
        @DisplayName("consulta as regras uma vez só, não a cada descrição")
        void carregaRegrasUmaVez() {
            Function<String, Categoria> sugestor = sugestor();

            sugestor.apply("Corrida de Uber");
            sugestor.apply("Farmácia");

            verify(regraRepository).findGlobaisEDoUsuario(usuarioId);
        }
    }

    @Nested
    @DisplayName("aprenderCategorizacao()")
    class Aprender {

        private final Usuario usuario = new Usuario();

        @BeforeEach
        void setUp() {
            usuario.setId(usuarioId);
        }

        @Test
        @DisplayName("cria a regra do usuário com o estabelecimento da descrição")
        void criaRegraDoUsuario() {
            when(regraRepository.findByUsuarioIdAndTermo(usuarioId, "jose luis cella de")).thenReturn(Optional.empty());

            service.aprenderCategorizacao("Debito visa electron brasil - 26/11 José Luis Cella de", alimentacao, usuario);

            ArgumentCaptor<RegraCategorizacao> captor = ArgumentCaptor.forClass(RegraCategorizacao.class);
            verify(regraRepository).save(captor.capture());
            assertThat(captor.getValue().getTermo()).isEqualTo("jose luis cella de");
            assertThat(captor.getValue().getCategoria()).isEqualTo(alimentacao);
            assertThat(captor.getValue().getUsuario()).isEqualTo(usuario);
        }

        @Test
        @DisplayName("recategorizar o mesmo estabelecimento atualiza a regra existente")
        void atualizaRegraExistente() {
            RegraCategorizacao existente = global("+ natural", lazer);
            existente.setUsuario(usuario);
            when(regraRepository.findByUsuarioIdAndTermo(usuarioId, "+ natural")).thenReturn(Optional.of(existente));

            service.aprenderCategorizacao("Debito visa electron brasil - 26/11 + natural", alimentacao, usuario);

            verify(regraRepository).save(existente);
            assertThat(existente.getCategoria()).isEqualTo(alimentacao);
        }

        @Test
        @DisplayName("descrição sem estabelecimento útil não gera regra")
        void naoAprendeDescricaoGenerica() {
            service.aprenderCategorizacao("Transação importada", alimentacao, usuario);
            service.aprenderCategorizacao("ab", alimentacao, usuario);
            service.aprenderCategorizacao(null, alimentacao, usuario);

            verify(regraRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("extrairTermo()")
    class ExtrairTermo {

        @ParameterizedTest(name = "\"{0}\" -> \"{1}\"")
        @CsvSource(delimiter = '|', value = {
                "Debito visa electron brasil - 26/11 + natural | + natural",
                "Pix enviado - Maria Exemplo | maria exemplo",
                "Almoço no Restaurante   Sabor | almoco no restaurante sabor"
        })
        @DisplayName("usa o último trecho da descrição, sem a data, normalizado")
        void extraiEstabelecimento(String descricao, String termo) {
            assertThat(SugestaoCategoriaService.extrairTermo(descricao)).isEqualTo(termo);
        }
    }

    private static Categoria categoria(String nome) {
        Categoria categoria = new Categoria();
        categoria.setId(UUID.randomUUID());
        categoria.setNome(nome);
        categoria.setPadrao(true);
        return categoria;
    }

    private static RegraCategorizacao global(String termo, Categoria categoria) {
        RegraCategorizacao regra = new RegraCategorizacao();
        regra.setTermo(termo);
        regra.setCategoria(categoria);
        return regra;
    }
}
