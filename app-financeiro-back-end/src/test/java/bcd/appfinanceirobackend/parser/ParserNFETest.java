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

@DisplayName("ParserNFe")
class ParserNFETest {

    private ParserNFe parser;
    private Conta conta;

    private static final String FIXTURES = "extratos/nfe/";

    @BeforeEach
    void setUp() {
        parser = new ParserNFe();
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
        @DisplayName("retorna true para XML com envelope nfeProc")
        void retornaTrue_paraXmlComNfeProc() throws IOException {
            MockMultipartFile arquivo = fromFixture("nfe-valida-com-nfeproc.xml");

            assertTrue(parser.aceita(arquivo));
        }

        @Test
        @DisplayName("retorna true para XML com NFe direto")
        void retornaTrue_paraXmlComNFeDireto() throws IOException {
            MockMultipartFile arquivo = fromFixture("nfe-valida-sem-nfeproc.xml");

            assertTrue(parser.aceita(arquivo));
        }

        @Test
        @DisplayName("retorna true para arquivo .nfe com conteúdo NF-e")
        void retornaTrue_paraArquivoNfe() throws IOException {
            MockMultipartFile arquivo = fromFixture("nfe-extensao-nfe.nfe");

            assertTrue(parser.aceita(arquivo));
        }

        @Test
        @DisplayName("retorna true para extensão .XML maiúscula")
        void retornaTrue_paraExtensaoXmlMaiuscula() throws IOException {
            MockMultipartFile arquivo = fromFixtureAs("nfe-valida-com-nfeproc.xml", "NOTA.XML", "application/xml");

            assertTrue(parser.aceita(arquivo));
        }

        @Test
        @DisplayName("retorna false para XML genérico sem marcador de NF-e")
        void retornaFalse_paraXmlGenerico() throws IOException {
            MockMultipartFile arquivo = fromFixture("nfe-xml-generico.xml");

            assertFalse(parser.aceita(arquivo));
        }

        @Test
        @DisplayName("retorna false para extensão não suportada")
        void retornaFalse_paraExtensaoNaoSuportada() {
            MockMultipartFile arquivo = fromString("nota.txt", "<nfeProc><NFe/></nfeProc>");

            assertFalse(parser.aceita(arquivo));
        }

        @Test
        @DisplayName("retorna false quando nome do arquivo é null")
        void retornaFalse_paraNomeNulo() {
            MockMultipartFile arquivo = new MockMultipartFile(
                    "arquivo",
                    null,
                    "application/xml",
                    "<nfeProc><NFe/></nfeProc>".getBytes(StandardCharsets.UTF_8)
            );

            assertFalse(parser.aceita(arquivo));
        }

        @Test
        @DisplayName("retorna false quando não consegue ler o arquivo")
        void retornaFalse_quandoInputStreamFalha() throws IOException {
            MultipartFile arquivo = mock(MultipartFile.class);
            when(arquivo.getOriginalFilename()).thenReturn("nota.xml");
            when(arquivo.getInputStream()).thenThrow(new IOException("falha de leitura"));

            assertFalse(parser.aceita(arquivo));
        }
    }

    @Nested
    @DisplayName("parsear() - Happy Paths")
    class HappyPaths {

        @Test
        @DisplayName("gera uma transação por item det com campos principais preenchidos")
        void nfeValida_comNfeProc_geraTransacoesPorItem() throws IOException {
            MockMultipartFile arquivo = fromFixture("nfe-valida-com-nfeproc.xml");

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
                    () -> assertEquals("Arroz Tipo 1 5kg", primeira.getDescricao()),
                    () -> assertEquals(0, primeira.getValor().compareTo(new BigDecimal("25.90"))),
                    () -> assertEquals(TipoTransacao.DEBITO, primeira.getTipo()),
                    () -> assertFalse(primeira.getCategorizada())
            );
        }

        @Test
        @DisplayName("processa XML com NFe direto sem envelope nfeProc")
        void nfeDireta_semEnvelope_funciona() throws IOException {
            MockMultipartFile arquivo = fromFixture("nfe-valida-sem-nfeproc.xml");

            ResultadoParser resultado = parser.parsear(arquivo, conta);

            assertAll(
                    () -> assertEquals(2, resultado.getTransacoes().size()),
                    () -> assertEquals(2, resultado.getTotalLinhas()),
                    () -> assertEquals(0, resultado.getLinhasInvalidas()),
                    () -> assertEquals("Café Torrado 500g", resultado.getTransacoes().get(0).getDescricao()),
                    () -> assertEquals(0, resultado.getTransacoes().get(1).getValor()
                            .compareTo(new BigDecimal("8.75")))
            );
        }

        @Test
        @DisplayName("processa arquivo .nfe com conteúdo XML de NF-e")
        void arquivoNfe_comConteudoXml_funciona() throws IOException {
            MockMultipartFile arquivo = fromFixture("nfe-extensao-nfe.nfe");

            ResultadoParser resultado = parser.parsear(arquivo, conta);

            Transacao transacao = resultado.getTransacoes().get(0);

            assertAll(
                    () -> assertEquals(1, resultado.getTransacoes().size()),
                    () -> assertEquals(1, resultado.getTotalLinhas()),
                    () -> assertEquals(0, resultado.getLinhasInvalidas()),
                    () -> assertEquals("Produto Arquivo NFE", transacao.getDescricao()),
                    () -> assertTrue(transacao.getValor().compareTo(BigDecimal.ZERO) > 0),
                    () -> assertEquals(TipoTransacao.DEBITO, transacao.getTipo()),
                    () -> assertSame(conta, transacao.getConta()),
                    () -> assertFalse(transacao.getCategorizada())
            );
        }

        @Test
        @DisplayName("processa NF-e com namespace padrão")
        void nfeComNamespace_funciona() throws IOException {
            MockMultipartFile arquivo = fromFixture("nfe-com-namespace.xml");

            ResultadoParser resultado = parser.parsear(arquivo, conta);

            assertAll(
                    () -> assertEquals(2, resultado.getTransacoes().size()),
                    () -> assertEquals(2, resultado.getTotalLinhas()),
                    () -> assertEquals(0, resultado.getLinhasInvalidas()),
                    () -> assertEquals(LocalDate.of(2024, 5, 20), resultado.getTransacoes().get(0).getData()),
                    () -> assertEquals("Produto com Namespace", resultado.getTransacoes().get(0).getDescricao())
            );
        }

        @Test
        @DisplayName("todas as transações retornadas pertencem à conta informada e são débito")
        void transacoesRetornadas_temContaTipoDebitoECategorizadaFalse() throws IOException {
            MockMultipartFile arquivo = fromFixture("nfe-valida-com-nfeproc.xml");

            ResultadoParser resultado = parser.parsear(arquivo, conta);

            resultado.getTransacoes().forEach(transacao -> assertAll(
                    () -> assertSame(conta, transacao.getConta()),
                    () -> assertEquals(TipoTransacao.DEBITO, transacao.getTipo()),
                    () -> assertFalse(transacao.getCategorizada())
            ));
        }
    }

    @Nested
    @DisplayName("parsear() - Datas de emissão")
    class DatasDeEmissao {

        @Test
        @DisplayName("data com timezone yyyy-MM-dd'T'HH:mm:ssXXX é parseada corretamente")
        void dataComTimezone_funciona() throws IOException {
            MockMultipartFile arquivo = fromFixture("nfe-valida-com-nfeproc.xml");

            ResultadoParser resultado = parser.parsear(arquivo, conta);

            assertEquals(LocalDate.of(2024, 1, 15), resultado.getTransacoes().get(0).getData());
        }

        @Test
        @DisplayName("data sem timezone yyyy-MM-dd'T'HH:mm:ss é parseada corretamente")
        void dataSemTimezone_funciona() throws IOException {
            MockMultipartFile arquivo = fromFixture("nfe-data-sem-timezone.xml");

            ResultadoParser resultado = parser.parsear(arquivo, conta);

            assertEquals(LocalDate.of(2024, 2, 10), resultado.getTransacoes().get(0).getData());
        }

        @Test
        @DisplayName("data apenas com yyyy-MM-dd usa fallback dos 10 primeiros caracteres")
        void dataApenasData_funcionaPeloFallback() throws IOException {
            MockMultipartFile arquivo = fromFixture("nfe-data-apenas-data.xml");

            ResultadoParser resultado = parser.parsear(arquivo, conta);

            assertEquals(LocalDate.of(2024, 3, 5), resultado.getTransacoes().get(0).getData());
        }

        @Test
        @DisplayName("ausência de dhEmi lança RuntimeException clara")
        void semDhEmi_lancaRuntimeException() throws IOException {
            MockMultipartFile arquivo = fromFixture("nfe-sem-dhemi.xml");

            RuntimeException ex = assertThrows(RuntimeException.class, () -> parser.parsear(arquivo, conta));

            assertTrue(ex.getMessage().contains("Campo <dhEmi>"));
        }

        @Test
        @DisplayName("dhEmi inválido lança RuntimeException clara")
        void dhEmiInvalido_lancaRuntimeException() throws IOException {
            MockMultipartFile arquivo = fromFixture("nfe-dhemi-invalido.xml");

            RuntimeException ex = assertThrows(RuntimeException.class, () -> parser.parsear(arquivo, conta));

            assertTrue(ex.getMessage().contains("Campo <dhEmi>"));
        }

        @Test
        @DisplayName("NF-e antiga com dEmi, sem dhEmi, documenta limitação atual")
        void nfeAntigaComDEmi_semDhEmi_naoSuportada() throws IOException {
            MockMultipartFile arquivo = fromFixture("nfe-realista-versao-110-com-demi.xml");

            RuntimeException ex = assertThrows(RuntimeException.class, () -> parser.parsear(arquivo, conta));

            assertTrue(ex.getMessage().contains("Campo <dhEmi>")
                    || ex.getMessage().contains("Erro ao processar NF-e"));
        }
    }

    @Nested
    @DisplayName("parsear() - Itens da NF-e")
    class ItensDaNFe {

        @Test
        @DisplayName("NF-e sem item det lança RuntimeException")
        void semDet_lancaRuntimeException() throws IOException {
            MockMultipartFile arquivo = fromFixture("nfe-sem-det.xml");

            RuntimeException ex = assertThrows(RuntimeException.class, () -> parser.parsear(arquivo, conta));

            assertTrue(ex.getMessage().contains("Nenhum item <det>"));
        }

        @Test
        @DisplayName("item com vProd zero é contado como inválido e não gera transação")
        void itemComValorZero_contaComoInvalido() throws IOException {
            MockMultipartFile arquivo = fromFixture("nfe-item-valor-zero.xml");

            ResultadoParser resultado = parser.parsear(arquivo, conta);

            assertAll(
                    () -> assertEquals(1, resultado.getTransacoes().size()),
                    () -> assertEquals(2, resultado.getTotalLinhas()),
                    () -> assertEquals(1, resultado.getLinhasInvalidas()),
                    () -> assertEquals("Produto Valido", resultado.getTransacoes().get(0).getDescricao())
            );
        }

        @Test
        @DisplayName("item sem vProd é contado como inválido e não gera transação")
        void itemSemVProd_contaComoInvalido() throws IOException {
            MockMultipartFile arquivo = fromFixture("nfe-item-sem-vprod.xml");

            ResultadoParser resultado = parser.parsear(arquivo, conta);

            assertAll(
                    () -> assertEquals(1, resultado.getTransacoes().size()),
                    () -> assertEquals(2, resultado.getTotalLinhas()),
                    () -> assertEquals(1, resultado.getLinhasInvalidas()),
                    () -> assertEquals("Produto Com Valor", resultado.getTransacoes().get(0).getDescricao())
            );
        }

        @Test
        @DisplayName("item sem xProd usa descrição fallback com índice do item")
        void itemSemXProd_usaDescricaoFallback() throws IOException {
            MockMultipartFile arquivo = fromFixture("nfe-item-sem-xprod.xml");

            ResultadoParser resultado = parser.parsear(arquivo, conta);

            assertAll(
                    () -> assertEquals(1, resultado.getTransacoes().size()),
                    () -> assertEquals("Item NF-e #1", resultado.getTransacoes().get(0).getDescricao()),
                    () -> assertEquals(0, resultado.getTransacoes().get(0).getValor()
                            .compareTo(new BigDecimal("12.34")))
            );
        }
    }

    @Nested
    @DisplayName("parsear() - Segurança e arquivos inválidos")
    class SegurancaEArquivosInvalidos {

        @Test
        @DisplayName("XML com DOCTYPE malicioso é bloqueado pela proteção contra XXE")
        void doctypeMalicioso_lancaRuntimeException() throws IOException {
            MockMultipartFile arquivo = fromFixture("nfe-doctype-malicioso.xml");

            RuntimeException ex = assertThrows(RuntimeException.class, () -> parser.parsear(arquivo, conta));

            assertTrue(ex.getMessage().contains("DOCTYPE") || ex.getMessage().contains("Erro ao processar NF-e"));
        }

        @Test
        @DisplayName("XML malformado lança RuntimeException de processamento")
        void xmlMalformado_lancaRuntimeException() {
            MockMultipartFile arquivo = fromString(
                    "nfe-malformada.xml",
                    "<nfeProc><NFe><infNFe><ide><dhEmi>2024-01-15T10:30:00-03:00</dhEmi></ide>"
            );

            RuntimeException ex = assertThrows(RuntimeException.class, () -> parser.parsear(arquivo, conta));

            assertTrue(ex.getMessage().contains("Erro ao processar NF-e"));
        }

        @Test
        @DisplayName("IOException ao ler arquivo lança RuntimeException de processamento")
        void ioExceptionAoLerArquivo_lancaRuntimeException() throws IOException {
            MultipartFile arquivo = mock(MultipartFile.class);
            when(arquivo.getInputStream()).thenThrow(new IOException("falha de leitura"));

            RuntimeException ex = assertThrows(RuntimeException.class, () -> parser.parsear(arquivo, conta));

            assertTrue(ex.getMessage().contains("Erro ao processar NF-e"));
        }

        @Test
        @DisplayName("XML genérico rejeitado por aceita() não deve ser usado como NF-e")
        void xmlGenerico_naoEhAceitoComoNFe() throws IOException {
            MockMultipartFile arquivo = fromFixture("nfe-xml-generico.xml");

            assertFalse(parser.aceita(arquivo));
        }
    }
}