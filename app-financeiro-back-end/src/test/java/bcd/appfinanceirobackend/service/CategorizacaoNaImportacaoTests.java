package bcd.appfinanceirobackend.service;

import bcd.appfinanceirobackend.model.Categoria;
import bcd.appfinanceirobackend.model.Conta;
import bcd.appfinanceirobackend.model.Importacao;
import bcd.appfinanceirobackend.model.Transacao;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.parser.ParserExtrato;
import bcd.appfinanceirobackend.parser.ResultadoParser;
import bcd.appfinanceirobackend.repository.ContaRepository;
import bcd.appfinanceirobackend.repository.ImportacaoRepository;
import bcd.appfinanceirobackend.repository.TransacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ImportacaoService - categorização automática de transações importadas")
class CategorizacaoNaImportacaoTests {

    @Mock
    private ContaRepository contaRepository;

    @Mock
    private ImportacaoRepository importacaoRepository;

    @Mock
    private TransacaoRepository transacaoRepository;

    @Mock
    private SugestaoCategoriaService sugestaoCategoriaService;

    @Mock
    private ParserExtrato parser;

    private ImportacaoService importacaoService;

    private Usuario usuario;
    private Conta conta;

    @BeforeEach
    void setUp() {
        importacaoService = new ImportacaoService(
                List.of(parser),
                importacaoRepository,
                transacaoRepository,
                contaRepository,
                sugestaoCategoriaService);

        usuario = new Usuario();
        usuario.setId(UUID.randomUUID());

        conta = new Conta();
        conta.setId(UUID.randomUUID());
        conta.setUsuario(usuario);
    }

    private MockMultipartFile csv(String conteudo) {
        return new MockMultipartFile(
                "arquivo", "extrato.csv", "text/csv", conteudo.getBytes(StandardCharsets.UTF_8));
    }

    private ResultadoParser umaTransacao(String descricao) {
        Transacao t = new Transacao();
        t.setValor(BigDecimal.TEN);
        t.setData(LocalDate.of(2024, 1, 15));
        t.setDescricao(descricao);
        t.setConta(conta);

        ResultadoParser resultado = new ResultadoParser();
        resultado.setTotalLinhas(1);
        resultado.setLinhasInvalidas(0);
        resultado.setTransacoes(List.of(t));
        return resultado;
    }

    @Test
    @DisplayName("Transação com categoria sugerida fica com categoria_id preenchido e categorizada = true")
    void deveCategorizarAutomaticamenteQuandoHaSugestao() {
        Categoria alimentacao = new Categoria();
        alimentacao.setId(UUID.randomUUID());
        alimentacao.setNome("Alimentação");
        alimentacao.setPadrao(true);

        when(contaRepository.findById(conta.getId())).thenReturn(Optional.of(conta));
        when(parser.aceita(any())).thenReturn(true);
        when(parser.parsear(any(), eq(conta))).thenReturn(umaTransacao("Mercado Central"));
        when(importacaoRepository.save(any(Importacao.class))).thenAnswer(inv -> inv.getArgument(0));
        when(sugestaoCategoriaService.sugerirCategoria("Mercado Central")).thenReturn(alimentacao);

        importacaoService.processar(csv("linha"), conta.getId(), usuario);

        ArgumentCaptor<Transacao> captor = ArgumentCaptor.forClass(Transacao.class);
        verify(transacaoRepository).save(captor.capture());
        assertThat(captor.getValue().getCategoria()).isEqualTo(alimentacao);
        assertThat(captor.getValue().getCategorizada()).isTrue();
    }

    @Test
    @DisplayName("Transação sem categoria sugerida permanece não categorizada")
    void deveManterNaoCategorizadaQuandoNaoHaSugestao() {
        when(contaRepository.findById(conta.getId())).thenReturn(Optional.of(conta));
        when(parser.aceita(any())).thenReturn(true);
        when(parser.parsear(any(), eq(conta))).thenReturn(umaTransacao("Transferência avulsa"));
        when(importacaoRepository.save(any(Importacao.class))).thenAnswer(inv -> inv.getArgument(0));
        when(sugestaoCategoriaService.sugerirCategoria("Transferência avulsa")).thenReturn(null);

        importacaoService.processar(csv("linha"), conta.getId(), usuario);

        ArgumentCaptor<Transacao> captor = ArgumentCaptor.forClass(Transacao.class);
        verify(transacaoRepository).save(captor.capture());
        assertThat(captor.getValue().getCategoria()).isNull();
        assertThat(captor.getValue().getCategorizada()).isFalse();
    }
}
