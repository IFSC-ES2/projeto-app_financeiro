package bcd.appfinanceirobackend.parser;

import bcd.appfinanceirobackend.model.Conta;
import bcd.appfinanceirobackend.model.Transacao;
import bcd.appfinanceirobackend.model.enums.TipoTransacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("ParserXML")
class ParserXMLTest {

    private static final String FIXTURES = "extratos/xml/";

    private ParserXML parser;
    private Conta conta;

    @BeforeEach
    void setUp() {
        parser = new ParserXML();
        conta = new Conta();
    }

    private MockMultipartFile fromFixture(String nomeFixture) throws IOException {
        return fromFixtureAs(nomeFixture, nomeFixture, "application/xml");
    }

    private MockMultipartFile fromFixtureAs(String nomeFixture, String nomeArquivo, String contentType) throws IOException {
        ClassPathResource resource = new ClassPathResource(FIXTURES + nomeFixture);
        byte[] bytes = resource.getInputStream().readAllBytes();
        return new MockMultipartFile("arquivo", nomeArquivo, contentType, bytes);
    }

    private MockMultipartFile fromString(String nomeArquivo, String conteudo) {
        return new MockMultipartFile(
                "arquivo",
                nomeArquivo,
                "application/xml",
                conteudo.getBytes(StandardCharsets.UTF_8)
        );
    }

    @Nested
    @DisplayName("aceita()")
    class Aceita {

        @Test
        @DisplayName("retorna true para XML genérico")
        void retornaTrue_paraXmlGenerico() throws IOException {
            MockMultipartFile arquivo = fromFixture("extrato-valido-transacao.xml");

            assertTrue(parser.aceita(arquivo));
        }

        @Test
        @DisplayName("retorna true para extensão .XML maiúscula")
        void retornaTrue_paraExtensaoXmlMaiuscula() throws IOException {
            MockMultipartFile arquivo = fromFixtureAs(
                    "extrato-valido-transacao.xml",
                    "EXTRATO.XML",
                    "application/xml"
            );

            assertTrue(parser.aceita(arquivo));
        }

        @Test
        @DisplayName("retorna false para XML com nfeProc")
        void retornaFalse_paraXmlComNfeProc() throws IOException {
            MockMultipartFile arquivo = fromFixture("xml-com-nfeproc.xml");

            assertFalse(parser.aceita(arquivo));
        }

        @Test
        @DisplayName("retorna false para XML com NFe")
        void retornaFalse_paraXmlComNFe() throws IOException {
            MockMultipartFile arquivo = fromFixture("xml-com-nfe.xml");

            assertFalse(parser.aceita(arquivo));
        }

        @Test
        @DisplayName("retorna false para extensão diferente de .xml")
        void retornaFalse_paraExtensaoNaoXml() {
            MockMultipartFile arquivo = fromString("extrato.txt", "<extrato/>");

            assertFalse(parser.aceita(arquivo));
        }

        @Test
        @DisplayName("retorna false quando nome do arquivo é null")
        void retornaFalse_paraNomeNulo() {
            MockMultipartFile arquivo = new MockMultipartFile(
                    "arquivo",
                    null,
                    "application/xml",
                    "<extrato/>".getBytes(StandardCharsets.UTF_8)
            );

            assertFalse(parser.aceita(arquivo));
        }

        @Test
        @DisplayName("retorna false quando não consegue ler o arquivo")
        void retornaFalse_quandoInputStreamFalha() throws IOException {
            MultipartFile arquivo = mock(MultipartFile.class);
            when(arquivo.getOriginalFilename()).thenReturn("extrato.xml");
            when(arquivo.getInputStream()).thenThrow(new IOException("falha de leitura"));

            assertFalse(parser.aceita(arquivo));
        }
    }

    @Nested
    @DisplayName("parsear() - Happy paths")
    class HappyPaths {

        @Test
        @DisplayName("processa tag transacao com campos principais preenchidos")
        void tagTransacao_processaCamposPrincipais() throws IOException {
            MockMultipartFile arquivo = fromFixture("extrato-valido-transacao.xml");

            ResultadoParser resultado = parser.parsear(arquivo, conta);

            assertAll(
                    () -> assertEquals(3, resultado.getTransacoes().size()),
                    () -> assertEquals(3, resultado.getTotalLinhas()),
                    () -> assertEquals(0, resultado.getLinhasInvalidas())
            );

            Transacao primeira = resultado.getTransacoes().get(0);
            assertAll(
                    () -> assertSame(conta, primeira.getConta()),
                    () -> assertEquals(LocalDate.of(2024, 1, 15), primeira.getData()),
                    () -> assertEquals("Supermercado", primeira.getDescricao()),
                    () -> assertEquals(0, primeira.getValor().compareTo(new BigDecimal("150.00"))),
                    () -> assertEquals(TipoTransacao.DEBITO, primeira.getTipo()),
                    () -> assertFalse(primeira.getCategorizada())
            );
        }

        @Test
        @DisplayName("processa tag lancamento")
        void tagLancamento_funciona() throws IOException {
            MockMultipartFile arquivo = fromFixture("extrato-valido-lancamento.xml");

            ResultadoParser resultado = parser.parsear(arquivo, conta);

            assertAll(
                    () -> assertEquals(1, resultado.getTransacoes().size()),
                    () -> assertEquals(1, resultado.getTotalLinhas()),
                    () -> assertEquals(0, resultado.getLinhasInvalidas()),
                    () -> assertEquals("Conta de luz", resultado.getTransacoes().get(0).getDescricao())
            );
        }

        @Test
        @DisplayName("processa tag movimento")
        void tagMovimento_funciona() throws IOException {
            MockMultipartFile arquivo = fromFixture("extrato-valido-movimento.xml");

            ResultadoParser resultado = parser.parsear(arquivo, conta);

            assertAll(
                    () -> assertEquals(1, resultado.getTransacoes().size()),
                    () -> assertEquals(1, resultado.getTotalLinhas()),
                    () -> assertEquals(0, resultado.getLinhasInvalidas()),
                    () -> assertEquals("Transferência recebida", resultado.getTransacoes().get(0).getDescricao())
            );
        }

        @Test
        @DisplayName("processa tag entry")
        void tagEntry_funciona() throws IOException {
            MockMultipartFile arquivo = fromFixture("extrato-valido-entry.xml");

            ResultadoParser resultado = parser.parsear(arquivo, conta);

            assertAll(
                    () -> assertEquals(1, resultado.getTransacoes().size()),
                    () -> assertEquals(1, resultado.getTotalLinhas()),
                    () -> assertEquals(0, resultado.getLinhasInvalidas()),
                    () -> assertEquals("Assinatura mensal", resultado.getTransacoes().get(0).getDescricao())
            );
        }

        @Test
        @DisplayName("transações retornadas têm conta informada e categorizada=false")
        void transacoesRetornadas_temContaECategorizadaFalse() throws IOException {
            MockMultipartFile arquivo = fromFixture("extrato-valido-transacao.xml");

            ResultadoParser resultado = parser.parsear(arquivo, conta);

            resultado.getTransacoes().forEach(transacao -> assertAll(
                    () -> assertSame(conta, transacao.getConta()),
                    () -> assertFalse(transacao.getCategorizada())
            ));
        }
    }

    @Nested
    @DisplayName("parsear() - Datas, valores e tipo")
    class DatasValoresETipo {

        @Test
        @DisplayName("aceita formatos de data yyyy-MM-dd, dd/MM/yyyy e dd-MM-yyyy")
        void aceitaMultiplosFormatosDeData() throws IOException {
            MockMultipartFile arquivo = fromFixture("extrato-valido-transacao.xml");

            ResultadoParser resultado = parser.parsear(arquivo, conta);

            assertAll(
                    () -> assertEquals(LocalDate.of(2024, 1, 15), resultado.getTransacoes().get(0).getData()),
                    () -> assertEquals(LocalDate.of(2024, 1, 16), resultado.getTransacoes().get(1).getData()),
                    () -> assertEquals(LocalDate.of(2024, 1, 17), resultado.getTransacoes().get(2).getData())
            );
        }

        @Test
        @DisplayName("valor negativo vira DEBITO e é armazenado positivo")
        void valorNegativo_viraDebitoEArmazenadoPositivo() throws IOException {
            MockMultipartFile arquivo = fromFixture("extrato-valido-transacao.xml");

            Transacao transacao = parser.parsear(arquivo, conta).getTransacoes().get(0);

            assertAll(
                    () -> assertEquals(TipoTransacao.DEBITO, transacao.getTipo()),
                    () -> assertEquals(0, transacao.getValor().compareTo(new BigDecimal("150.00")))
            );
        }

        @Test
        @DisplayName("valor brasileiro com R$, milhar e vírgula decimal é normalizado")
        void valorBrasileiro_comMoedaMilharEVirgulaDecimal_funciona() throws IOException {
            MockMultipartFile arquivo = fromFixture("extrato-valor-brasileiro.xml");

            Transacao transacao = parser.parsear(arquivo, conta).getTransacoes().get(0);

            assertAll(
                    () -> assertEquals(0, transacao.getValor().compareTo(new BigDecimal("1234.56"))),
                    () -> assertEquals(TipoTransacao.DEBITO, transacao.getTipo())
            );
        }

        @Test
        @DisplayName("tipo inválido com valor positivo vira CREDITO")
        void tipoInvalidoComValorPositivo_viraCredito() throws IOException {
            MockMultipartFile arquivo = fromFixture("extrato-tipo-invalido.xml");

            Transacao transacao = parser.parsear(arquivo, conta).getTransacoes().get(0);

            assertAll(
                    () -> assertEquals(TipoTransacao.CREDITO, transacao.getTipo()),
                    () -> assertEquals(0, transacao.getValor().compareTo(new BigDecimal("250.00")))
            );
        }
    }


    @Nested
    @DisplayName("parsear() - casos realistas")
    class CasosRealistasQueExpoemDefeitos {

        @Test
        @DisplayName("rejeita NF-e mesmo quando marcador aparece depois do preview inicial")
        void aceita_rejeitaNFeMesmoQuandoMarcadorApareceDepoisDoPreviewInicial() throws IOException {
            MockMultipartFile arquivo = fromFixture("xml-nfe-marcador-apos-preview.xml");

            assertFalse(parser.aceita(arquivo));
        }

        @Test
        @DisplayName("processa XML com namespace prefixado nas tags de transação")
        void xmlComNamespacePrefixado_processaTransacao() throws IOException {
            MockMultipartFile arquivo = fromFixture("extrato-namespace-prefixado.xml");

            ResultadoParser resultado = parser.parsear(arquivo, conta);
            Transacao transacao = resultado.getTransacoes().get(0);

            assertAll(
                    () -> assertEquals(1, resultado.getTransacoes().size()),
                    () -> assertEquals(1, resultado.getTotalLinhas()),
                    () -> assertEquals(0, resultado.getLinhasInvalidas()),
                    () -> assertEquals(LocalDate.of(2024, 4, 1), transacao.getData()),
                    () -> assertEquals("Compra com namespace", transacao.getDescricao()),
                    () -> assertEquals(0, transacao.getValor().compareTo(new BigDecimal("45.67"))),
                    () -> assertEquals(TipoTransacao.DEBITO, transacao.getTipo())
            );
        }

        @Test
        @DisplayName("processa campos com nomes em maiúsculo")
        void camposComNomesMaiusculos_processaTransacao() throws IOException {
            MockMultipartFile arquivo = fromFixture("extrato-campos-maiusculos.xml");

            ResultadoParser resultado = parser.parsear(arquivo, conta);
            Transacao transacao = resultado.getTransacoes().get(0);

            assertAll(
                    () -> assertEquals(1, resultado.getTransacoes().size()),
                    () -> assertEquals(1, resultado.getTotalLinhas()),
                    () -> assertEquals(0, resultado.getLinhasInvalidas()),
                    () -> assertEquals(LocalDate.of(2024, 4, 2), transacao.getData()),
                    () -> assertEquals("Pagamento com campos maiúsculos", transacao.getDescricao()),
                    () -> assertEquals(0, transacao.getValor().compareTo(new BigDecimal("120.00"))),
                    () -> assertEquals(TipoTransacao.DEBITO, transacao.getTipo())
            );
        }

        @Test
        @DisplayName("processa data ISO com horário usando a parte da data")
        void dataIsoComHorario_processaUsandoParteDaData() throws IOException {
            MockMultipartFile arquivo = fromFixture("extrato-data-com-hora.xml");

            ResultadoParser resultado = parser.parsear(arquivo, conta);
            Transacao transacao = resultado.getTransacoes().get(0);

            assertAll(
                    () -> assertEquals(1, resultado.getTransacoes().size()),
                    () -> assertEquals(1, resultado.getTotalLinhas()),
                    () -> assertEquals(0, resultado.getLinhasInvalidas()),
                    () -> assertEquals(LocalDate.of(2024, 4, 3), transacao.getData()),
                    () -> assertEquals("Compra com data e hora", transacao.getDescricao())
            );
        }

        @Test
        @DisplayName("processa todas as tags suportadas quando aparecem no mesmo XML")
        void tagsSuportadasMistas_processaTodas() throws IOException {
            MockMultipartFile arquivo = fromFixture("extrato-tags-mistas.xml");

            ResultadoParser resultado = parser.parsear(arquivo, conta);

            assertAll(
                    () -> assertEquals(2, resultado.getTransacoes().size()),
                    () -> assertEquals(2, resultado.getTotalLinhas()),
                    () -> assertEquals(0, resultado.getLinhasInvalidas()),
                    () -> assertEquals("Transação padrão", resultado.getTransacoes().get(0).getDescricao()),
                    () -> assertEquals("Lançamento misto", resultado.getTransacoes().get(1).getDescricao())
            );
        }
    }

    @Nested
    @DisplayName("parsear() - Tolerância a falhas")
    class ToleranciaAFalhas {

        @Test
        @DisplayName("transação sem data incrementa linhasInvalidas sem interromper o parse")
        void transacaoSemData_incrementaLinhasInvalidas() throws IOException {
            MockMultipartFile arquivo = fromFixture("extrato-com-data-invalida.xml");

            ResultadoParser resultado = parser.parsear(arquivo, conta);

            assertAll(
                    () -> assertEquals(1, resultado.getTransacoes().size()),
                    () -> assertEquals(2, resultado.getTotalLinhas()),
                    () -> assertEquals(1, resultado.getLinhasInvalidas()),
                    () -> assertEquals("Transação válida", resultado.getTransacoes().get(0).getDescricao())
            );
        }

        @Test
        @DisplayName("XML com tag conhecida e todos os nós inválidos retorna resultado vazio sem lançar exception")
        void parsear_tagConhecidaComTodosOsNosInvalidos_retornaResultadoVazioSemLancarException() throws IOException {
            MockMultipartFile arquivo = fromFixture("extrato-todos-nos-invalidos.xml");

            ResultadoParser resultado = assertDoesNotThrow(() -> parser.parsear(arquivo, conta));

            assertAll(
                    () -> assertEquals(2, resultado.getTotalLinhas()),
                    () -> assertEquals(2, resultado.getLinhasInvalidas()),
                    () -> assertTrue(resultado.getTransacoes().isEmpty())
            );
        }

        @Test
        @DisplayName("transação com valor inválido incrementa linhasInvalidas sem interromper o parse")
        void transacaoComValorInvalido_incrementaLinhasInvalidas() throws IOException {
            MockMultipartFile arquivo = fromFixture("extrato-com-valor-invalido.xml");

            ResultadoParser resultado = parser.parsear(arquivo, conta);

            assertAll(
                    () -> assertEquals(1, resultado.getTransacoes().size()),
                    () -> assertEquals(2, resultado.getTotalLinhas()),
                    () -> assertEquals(1, resultado.getLinhasInvalidas()),
                    () -> assertEquals("Transação válida", resultado.getTransacoes().get(0).getDescricao())
            );
        }

        @Test
        @DisplayName("XML sem tag de transação conhecida lança RuntimeException clara")
        void xmlSemTagConhecida_lancaRuntimeException() throws IOException {
            MockMultipartFile arquivo = fromFixture("extrato-sem-tag-conhecida.xml");

            RuntimeException ex = assertThrows(RuntimeException.class, () -> parser.parsear(arquivo, conta));

            assertTrue(ex.getMessage().contains("Nenhuma tag de transação reconhecida"));
        }

        @Test
        @DisplayName("XML malformado lança RuntimeException")
        void xmlMalformado_lancaRuntimeException() throws IOException {
            MockMultipartFile arquivo = fromFixture("extrato-malformado.xml");

            RuntimeException ex = assertThrows(RuntimeException.class, () -> parser.parsear(arquivo, conta));

            assertTrue(ex.getMessage().contains("Erro ao processar arquivo XML"));
        }

        @Test
        @DisplayName("IOException ao ler arquivo lança RuntimeException")
        void ioExceptionAoLerArquivo_lancaRuntimeException() throws IOException {
            MultipartFile arquivo = mock(MultipartFile.class);
            when(arquivo.getInputStream()).thenThrow(new IOException("falha de leitura"));

            RuntimeException ex = assertThrows(RuntimeException.class, () -> parser.parsear(arquivo, conta));

            assertTrue(ex.getMessage().contains("Erro ao processar arquivo XML"));
        }
    }

    @Nested
    @DisplayName("parsear() - Segurança")
    class Seguranca {

        @Test
        @DisplayName("XML com DOCTYPE malicioso é bloqueado")
        void xmlComDoctypeMalicioso_bloqueado() throws IOException {
            MockMultipartFile arquivo = fromFixture("extrato-doctype-malicioso.xml");

            RuntimeException ex = assertThrows(RuntimeException.class, () -> parser.parsear(arquivo, conta));

            assertTrue(ex.getMessage().contains("Erro ao processar arquivo XML"));
        }
    }
}