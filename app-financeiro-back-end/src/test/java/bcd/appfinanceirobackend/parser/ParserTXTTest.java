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

@DisplayName("ParserTXT")
class ParserTXTTest {

    private ParserTXT parser;
    private Conta conta;

    @BeforeEach
    void setUp() {
        parser = new ParserTXT();
        conta = new Conta();
    }

    private MockMultipartFile fromFixture(String nomeFixture) throws IOException {
        ClassPathResource resource = new ClassPathResource("extratos/txt/" + nomeFixture);
        byte[] bytes = resource.getInputStream().readAllBytes();
        return new MockMultipartFile("arquivo", nomeFixture, "text/plain", bytes);
    }

    private MockMultipartFile fromString(String nomeArquivo, String conteudo) {
        return new MockMultipartFile(
                "arquivo",
                nomeArquivo,
                "text/plain",
                conteudo.getBytes(StandardCharsets.UTF_8)
        );
    }

    @Nested
    @DisplayName("aceita()")
    class Aceita {

        @Test
        @DisplayName("retorna true para arquivo .txt")
        void retornaTrue_paraArquivoTxt() {
            MockMultipartFile arquivo = fromString("extrato.txt", "qualquer");

            assertTrue(parser.aceita(arquivo));
        }

        @Test
        @DisplayName("retorna true para extensão .TXT maiúscula")
        void retornaTrue_extensaoMaiuscula() {
            MockMultipartFile arquivo = fromString("EXTRATO.TXT", "qualquer");

            assertTrue(parser.aceita(arquivo));
        }

        @Test
        @DisplayName("retorna false para arquivo .csv")
        void retornaFalse_paraCsv() {
            MockMultipartFile arquivo = fromString("extrato.csv", "2024-01-01,Teste,10.00,CREDITO");

            assertFalse(parser.aceita(arquivo));
        }

        @Test
        @DisplayName("retorna false para arquivo .xml")
        void retornaFalse_paraXml() {
            MockMultipartFile arquivo = fromString("extrato.xml", "<extrato/>");

            assertFalse(parser.aceita(arquivo));
        }

        @Test
        @DisplayName("retorna false quando nome do arquivo é null")
        void retornaFalse_paraNomeNulo() {
            MockMultipartFile arquivo = new MockMultipartFile(
                    "arquivo",
                    null,
                    "text/plain",
                    "qualquer".getBytes(StandardCharsets.UTF_8)
            );

            assertFalse(parser.aceita(arquivo));
        }
    }

    @Nested
    @DisplayName("parsear() - caminhos principais")
    class CaminhosPrincipais {

        @Test
        @DisplayName("processa linhas com data, descrição e valor")
        void processaLinhasValidas() throws IOException {
            MockMultipartFile arquivo = fromFixture("extrato-regex-valido.txt");

            ResultadoParser resultado = parser.parsear(arquivo, conta);

            assertAll(
                    () -> assertEquals(3, resultado.getTransacoes().size()),
                    () -> assertEquals(3, resultado.getTotalLinhas()),
                    () -> assertEquals(0, resultado.getLinhasInvalidas()),
                    () -> assertEquals("Supermercado Extra", resultado.getTransacoes().get(0).getDescricao()),
                    () -> assertEquals(LocalDate.of(2024, 1, 15), resultado.getTransacoes().get(0).getData()),
                    () -> assertEquals(TipoTransacao.DEBITO, resultado.getTransacoes().get(0).getTipo()),
                    () -> assertEquals(0, resultado.getTransacoes().get(0).getValor()
                            .compareTo(new BigDecimal("150.00"))),
                    () -> assertEquals(TipoTransacao.CREDITO, resultado.getTransacoes().get(1).getTipo()),
                    () -> assertEquals(TipoTransacao.DEBITO, resultado.getTransacoes().get(2).getTipo())
            );
        }

        @Test
        @DisplayName("processa formato tabulado")
        void processaFormatoTabulado() throws IOException {
            MockMultipartFile arquivo = fromFixture("extrato-tab-valido.txt");

            ResultadoParser resultado = parser.parsear(arquivo, conta);

            assertAll(
                    () -> assertEquals(2, resultado.getTransacoes().size()),
                    () -> assertEquals(2, resultado.getTotalLinhas()),
                    () -> assertEquals(0, resultado.getLinhasInvalidas()),
                    () -> assertEquals("Padaria Central", resultado.getTransacoes().get(0).getDescricao()),
                    () -> assertEquals(0, resultado.getTransacoes().get(0).getValor()
                            .compareTo(new BigDecimal("45.90"))),
                    () -> assertEquals(TipoTransacao.DEBITO, resultado.getTransacoes().get(0).getTipo()),
                    () -> assertEquals("Transferencia recebida", resultado.getTransacoes().get(1).getDescricao()),
                    () -> assertEquals(TipoTransacao.CREDITO, resultado.getTransacoes().get(1).getTipo())
            );
        }

        @Test
        @DisplayName("processa formato colunar com múltiplos espaços")
        void processaFormatoColunarComMultiplosEspacos() throws IOException {
            MockMultipartFile arquivo = fromFixture("extrato-colunar-valido.txt");

            ResultadoParser resultado = parser.parsear(arquivo, conta);

            Transacao transacao = resultado.getTransacoes().get(0);
            assertAll(
                    () -> assertEquals(1, resultado.getTransacoes().size()),
                    () -> assertEquals(1, resultado.getTotalLinhas()),
                    () -> assertEquals(0, resultado.getLinhasInvalidas()),
                    () -> assertEquals("Aluguel Comercial", transacao.getDescricao()),
                    () -> assertEquals(LocalDate.of(2024, 1, 5), transacao.getData()),
                    () -> assertEquals(TipoTransacao.DEBITO, transacao.getTipo()),
                    () -> assertEquals(0, transacao.getValor().compareTo(new BigDecimal("1234.56")))
            );
        }

        @Test
        @DisplayName("transações retornadas têm conta preenchida e categorizada=false")
        void transacoesRetornadas_temContaECategorizadaFalse() throws IOException {
            MockMultipartFile arquivo = fromFixture("extrato-regex-valido.txt");

            ResultadoParser resultado = parser.parsear(arquivo, conta);

            resultado.getTransacoes().forEach(transacao -> assertAll(
                    () -> assertSame(conta, transacao.getConta()),
                    () -> assertFalse(transacao.getCategorizada())
            ));
        }
    }

    @Nested
    @DisplayName("parsear() - datas")
    class Datas {

        @Test
        @DisplayName("aceita múltiplos formatos de data suportados")
        void aceitaMultiplosFormatosDeData() throws IOException {
            MockMultipartFile arquivo = fromFixture("extrato-datas-validas.txt");

            ResultadoParser resultado = parser.parsear(arquivo, conta);

            assertAll(
                    () -> assertEquals(4, resultado.getTransacoes().size()),
                    () -> assertEquals(0, resultado.getLinhasInvalidas()),
                    () -> assertEquals(LocalDate.of(2024, 1, 15), resultado.getTransacoes().get(0).getData()),
                    () -> assertEquals(LocalDate.of(2024, 1, 16), resultado.getTransacoes().get(1).getData()),
                    () -> assertEquals(LocalDate.of(2024, 1, 17), resultado.getTransacoes().get(2).getData()),
                    () -> assertEquals(LocalDate.of(2024, 1, 18), resultado.getTransacoes().get(3).getData())
            );
        }

        @Test
        @DisplayName("data inválida incrementa linhasInvalidas sem interromper o parse")
        void dataInvalida_incrementaLinhasInvalidasSemInterromper() throws IOException {
            MockMultipartFile arquivo = fromFixture("extrato-data-invalida.txt");

            ResultadoParser resultado = parser.parsear(arquivo, conta);

            assertAll(
                    () -> assertEquals(1, resultado.getTransacoes().size()),
                    () -> assertEquals(2, resultado.getTotalLinhas()),
                    () -> assertEquals(1, resultado.getLinhasInvalidas()),
                    () -> assertEquals("Linha valida", resultado.getTransacoes().get(0).getDescricao())
            );
        }
    }

    @Nested
    @DisplayName("parsear() - regras de tipo e valor")
    class RegrasDeTipoEValor {

        @Test
        @DisplayName("valor negativo resulta em DEBITO e é armazenado positivo")
        void valorNegativo_resultaEmDebitoEArmazenaPositivo() {
            String conteudo = "15/01/2024 Compra teste -350,75\n";

            ResultadoParser resultado = parser.parsear(fromString("extrato.txt", conteudo), conta);

            Transacao transacao = resultado.getTransacoes().get(0);
            assertAll(
                    () -> assertEquals(TipoTransacao.DEBITO, transacao.getTipo()),
                    () -> assertEquals(0, transacao.getValor().compareTo(new BigDecimal("350.75")))
            );
        }

        @Test
        @DisplayName("valor positivo resulta em CREDITO")
        void valorPositivo_resultaEmCredito() {
            String conteudo = "15/01/2024 Saldo inicial +950,00\n";

            ResultadoParser resultado = parser.parsear(fromString("extrato.txt", conteudo), conta);

            Transacao transacao = resultado.getTransacoes().get(0);
            assertAll(
                    () -> assertEquals(TipoTransacao.CREDITO, transacao.getTipo()),
                    () -> assertEquals(0, transacao.getValor().compareTo(new BigDecimal("950.00")))
            );
        }

        @Test
        @DisplayName("valor brasileiro com R$ e milhar é normalizado em formato colunar")
        void valorBrasileiroComMoedaEMilhar_normalizadoEmFormatoColunar() {
            String conteudo = "05/01/2024  Aluguel Comercial  R$ -1.234,56\n";

            ResultadoParser resultado = parser.parsear(fromString("extrato.txt", conteudo), conta);

            Transacao transacao = resultado.getTransacoes().get(0);
            assertAll(
                    () -> assertEquals(TipoTransacao.DEBITO, transacao.getTipo()),
                    () -> assertEquals(0, transacao.getValor().compareTo(new BigDecimal("1234.56")))
            );
        }

        @Test
        @DisplayName("valor monetário inválido incrementa linhasInvalidas sem interromper o parse")
        void valorMonetarioInvalido_incrementaLinhasInvalidasSemInterromper() throws IOException {
            MockMultipartFile arquivo = fromFixture("extrato-valor-invalido.txt");

            ResultadoParser resultado = parser.parsear(arquivo, conta);

            assertAll(
                    () -> assertEquals(1, resultado.getTransacoes().size()),
                    () -> assertEquals(2, resultado.getTotalLinhas()),
                    () -> assertEquals(1, resultado.getLinhasInvalidas()),
                    () -> assertEquals("Linha valida", resultado.getTransacoes().get(0).getDescricao())
            );
        }
    }

    @Nested
    @DisplayName("parsear() - casos realistas que expõem defeitos")
    class CasosRealistasQueExpoemDefeitos {

        @Test
        @DisplayName("aceita valor com quatro dígitos sem separador de milhar")
        void valorComQuatroDigitosSemSeparadorDeMilhar_deveSerAceito() {
            String conteudo = "15/01/2024 Salario Empresa 1500,00\n";

            ResultadoParser resultado = parser.parsear(fromString("extrato.txt", conteudo), conta);

            assertAll(
                    () -> assertEquals(1, resultado.getTransacoes().size()),
                    () -> assertEquals(1, resultado.getTotalLinhas()),
                    () -> assertEquals(0, resultado.getLinhasInvalidas()),
                    () -> assertEquals("Salario Empresa", resultado.getTransacoes().get(0).getDescricao()),
                    () -> assertEquals(TipoTransacao.CREDITO, resultado.getTransacoes().get(0).getTipo()),
                    () -> assertEquals(0, resultado.getTransacoes().get(0).getValor()
                            .compareTo(new BigDecimal("1500.00")))
            );
        }

        @Test
        @DisplayName("aceita valor negativo com quatro dígitos sem separador de milhar")
        void valorNegativoComQuatroDigitosSemSeparadorDeMilhar_deveSerAceito() {
            String conteudo = "16/01/2024 Aluguel Residencial -1500,00\n";

            ResultadoParser resultado = parser.parsear(fromString("extrato.txt", conteudo), conta);

            assertAll(
                    () -> assertEquals(1, resultado.getTransacoes().size()),
                    () -> assertEquals(1, resultado.getTotalLinhas()),
                    () -> assertEquals(0, resultado.getLinhasInvalidas()),
                    () -> assertEquals("Aluguel Residencial", resultado.getTransacoes().get(0).getDescricao()),
                    () -> assertEquals(TipoTransacao.DEBITO, resultado.getTransacoes().get(0).getTipo()),
                    () -> assertEquals(0, resultado.getTransacoes().get(0).getValor()
                            .compareTo(new BigDecimal("1500.00")))
            );
        }

        @Test
        @DisplayName("aceita prefixo R$ também em linha com espaçamento simples")
        void valorComPrefixoMoedaEmEspacamentoSimples_deveSerAceito() {
            String conteudo = "17/01/2024 Compra mercado R$ -123,45\n";

            ResultadoParser resultado = parser.parsear(fromString("extrato.txt", conteudo), conta);

            assertAll(
                    () -> assertEquals(1, resultado.getTransacoes().size()),
                    () -> assertEquals(1, resultado.getTotalLinhas()),
                    () -> assertEquals(0, resultado.getLinhasInvalidas()),
                    () -> assertEquals("Compra mercado", resultado.getTransacoes().get(0).getDescricao()),
                    () -> assertEquals(TipoTransacao.DEBITO, resultado.getTransacoes().get(0).getTipo()),
                    () -> assertEquals(0, resultado.getTransacoes().get(0).getValor()
                            .compareTo(new BigDecimal("123.45")))
            );
        }
    }

    @Nested
    @DisplayName("parsear() - tolerância a falhas")
    class ToleranciaAFalhas {

        @Test
        @DisplayName("linhas em branco são ignoradas e não entram no total")
        void linhasEmBranco_ignoradasENaoEntramNoTotal() {
            String conteudo = "\n"
                    + "15/01/2024 Compra valida -20,00\n"
                    + "   \n"
                    + "16/01/2024 Receita valida 30,00\n";

            ResultadoParser resultado = parser.parsear(fromString("extrato.txt", conteudo), conta);

            assertAll(
                    () -> assertEquals(2, resultado.getTransacoes().size()),
                    () -> assertEquals(2, resultado.getTotalLinhas()),
                    () -> assertEquals(0, resultado.getLinhasInvalidas())
            );
        }

        @Test
        @DisplayName("linha sem data nem valor é contada como inválida")
        void linhaSemDataNemValor_contadaComoInvalida() {
            String conteudo = "cabecalho qualquer\n"
                    + "15/01/2024 Compra valida -20,00\n";

            ResultadoParser resultado = parser.parsear(fromString("extrato.txt", conteudo), conta);

            assertAll(
                    () -> assertEquals(1, resultado.getTransacoes().size()),
                    () -> assertEquals(2, resultado.getTotalLinhas()),
                    () -> assertEquals(1, resultado.getLinhasInvalidas())
            );
        }

        @Test
        @DisplayName("arquivo sem nenhuma linha válida retorna resultado vazio sem lançar exceção")
        void arquivoSemLinhasValidas_retornaResultadoVazio() {
            String conteudo = "lixo\nsem formato\nabc123\n";

            ResultadoParser resultado = parser.parsear(fromString("extrato.txt", conteudo), conta);

            assertAll(
                    () -> assertEquals(0, resultado.getTransacoes().size()),
                    () -> assertEquals(3, resultado.getTotalLinhas()),
                    () -> assertEquals(3, resultado.getLinhasInvalidas())
            );
        }

        @Test
        @DisplayName("arquivo completamente vazio retorna resultado zerado sem lançar exceção")
        void arquivoCompletamenteVazio_retornaResultadoZerado() {
            ResultadoParser resultado = parser.parsear(fromString("extrato.txt", ""), conta);

            assertAll(
                    () -> assertEquals(0, resultado.getTransacoes().size()),
                    () -> assertEquals(0, resultado.getTotalLinhas()),
                    () -> assertEquals(0, resultado.getLinhasInvalidas())
            );
        }
    }

    @Nested
    @DisplayName("parsear() - entradas corrompidas")
    class EntradasCorrompidas {

        @Test
        @DisplayName("IOException ao ler arquivo lança RuntimeException")
        void ioExceptionAoLerArquivo_lancaRuntimeException() throws IOException {
            MultipartFile arquivo = mock(MultipartFile.class);
            when(arquivo.getOriginalFilename()).thenReturn("extrato.txt");
            when(arquivo.getInputStream()).thenThrow(new IOException("falha simulada"));

            RuntimeException ex = assertThrows(RuntimeException.class, () -> parser.parsear(arquivo, conta));

            assertTrue(ex.getMessage().contains("Erro ao processar arquivo TXT"));
        }
    }
}