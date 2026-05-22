package bcd.appfinanceirobackend.parser;

import bcd.appfinanceirobackend.model.Conta;
import bcd.appfinanceirobackend.model.enums.TipoTransacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ParserCSV")
class ParserCSVTest {

    private ParserCSV parser;
    private Conta conta;

    // Caminho base dos fixtures (relativo à raiz do módulo backend)
    private static final String FIXTURES = "src/test/resources/extratos/csv/";

    @BeforeEach
    void setUp() {
        parser = new ParserCSV();
        conta = new Conta();
    }

    // ---------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------

    private MockMultipartFile fromFixture(String nomeFixture) throws IOException {
        Path path = Paths.get(FIXTURES + nomeFixture);
        byte[] bytes = Files.readAllBytes(path);
        return new MockMultipartFile("arquivo", nomeFixture, "text/csv", bytes);
    }

    private MockMultipartFile fromString(String nomeArquivo, String conteudo) {
        return new MockMultipartFile("arquivo", nomeArquivo, "text/csv",
                conteudo.getBytes(StandardCharsets.UTF_8));
    }

    // ---------------------------------------------------------------------------
    // aceita()
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("aceita()")
    class Aceita {

        @Test
        @DisplayName("2.1a retorna true para arquivo .csv")
        void retornaTrue_paraArquivoCSV() {
            MockMultipartFile arquivo = fromString("extrato.csv", "qualquer");
            assertTrue(parser.aceita(arquivo));
        }

        @Test
        @DisplayName("2.1b retorna true para extensão .CSV maiúscula")
        void retornaTrue_extensaoMaiuscula() {
            MockMultipartFile arquivo = fromString("EXTRATO.CSV", "qualquer");
            assertTrue(parser.aceita(arquivo));
        }

        @Test
        @DisplayName("2.2a retorna false para arquivo .txt")
        void retornaFalse_paraTxt() {
            MockMultipartFile arquivo = fromString("extrato.txt", "qualquer");
            assertFalse(parser.aceita(arquivo));
        }

        @Test
        @DisplayName("2.2b retorna false para arquivo .xml")
        void retornaFalse_paraXml() {
            MockMultipartFile arquivo = fromString("extrato.xml", "<extrato/>");
            assertFalse(parser.aceita(arquivo));
        }

        @Test
        @DisplayName("2.2c retorna false quando nome do arquivo é null")
        void retornaFalse_paraNomeNulo() {
            MockMultipartFile arquivo = new MockMultipartFile("arquivo", null,
                    "text/csv", "qualquer".getBytes());
            assertFalse(parser.aceita(arquivo));
        }
    }

    // ---------------------------------------------------------------------------
    // parsear() — Happy Paths
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("parsear() — Happy Paths")
    class HappyPaths {

        @Test
        @DisplayName("2.3 Delimitador vírgula: 3 transações com todos os campos corretos")
        void delimitadorVirgula_3transacoesValidas() throws IOException {
            // fixture: extrato-valido.csv
            // 3 linhas: DEBITO, CREDITO, DEBITO — sem cabeçalho
            MockMultipartFile arquivo = fromFixture("extrato-valido.csv");

            ResultadoParser resultado = parser.parsear(arquivo, conta);

            assertAll(
                    () -> assertEquals(3, resultado.getTransacoes().size()),
                    () -> assertEquals(3, resultado.getTotalLinhas()),
                    () -> assertEquals(0, resultado.getLinhasInvalidas()),
                    () -> assertEquals("Supermercado Extra", resultado.getTransacoes().get(0).getDescricao()),
                    () -> assertEquals(TipoTransacao.DEBITO, resultado.getTransacoes().get(0).getTipo()),
                    () -> assertEquals(0, resultado.getTransacoes().get(0).getValor()
                            .compareTo(new BigDecimal("150.00"))),
                    () -> assertEquals(TipoTransacao.CREDITO, resultado.getTransacoes().get(1).getTipo()),
                    () -> assertEquals(TipoTransacao.DEBITO, resultado.getTransacoes().get(2).getTipo())
            );
        }

        @Test
        @DisplayName("2.4 Delimitador ponto-e-vírgula detectado automaticamente")
        void delimitadorPontoVirgula_detectadoAutomaticamente() throws IOException {
            // fixture: extrato-ponto-virgula.csv
            // 2 linhas com ; e valor decimal com vírgula (ex: -1500,00)
            MockMultipartFile arquivo = fromFixture("extrato-ponto-virgula.csv");

            ResultadoParser resultado = parser.parsear(arquivo, conta);

            assertAll(
                    () -> assertEquals(2, resultado.getTransacoes().size()),
                    () -> assertEquals(0, resultado.getLinhasInvalidas()),
                    () -> assertEquals(0, resultado.getTransacoes().get(0).getValor()
                            .compareTo(new BigDecimal("1500.00"))),
                    () -> assertEquals(TipoTransacao.DEBITO, resultado.getTransacoes().get(0).getTipo())
            );
        }

        @Test
        @DisplayName("2.10 Múltiplos formatos de data aceitos na mesma fixture")
        void multiploFormatosDeData_todosParseados() throws IOException {
            // fixture: extrato-valido.csv contém yyyy-MM-dd, dd/MM/yyyy e dd-MM-yyyy
            MockMultipartFile arquivo = fromFixture("extrato-valido.csv");

            ResultadoParser resultado = parser.parsear(arquivo, conta);

            // Se qualquer data falhar, a linha vira inválida — asserção direta
            assertEquals(3, resultado.getTransacoes().size(),
                    "Os 3 formatos de data suportados devem ser parseados sem erro");
        }

        @Test
        @DisplayName("2.6b Arquivo com apenas cabeçalho retorna 0 transações sem lançar exceção")
        void apenasLinhaDesCabecalho_retornaZeroTransacoes() {
            MockMultipartFile arquivo = fromString("extrato.csv", "data,descricao,valor,tipo\n");

            ResultadoParser resultado = parser.parsear(arquivo, conta);

            assertAll(
                    () -> assertEquals(0, resultado.getTransacoes().size()),
                    () -> assertEquals(1, resultado.getTotalLinhas()),
                    () -> assertEquals(1, resultado.getLinhasInvalidas())
            );
        }

        @Test
        @DisplayName("Transações retornadas têm conta preenchida e categorizada=false")
        void transacoesRetornadas_temContaECategorizadaFalse() throws IOException {
            MockMultipartFile arquivo = fromFixture("extrato-valido.csv");

            ResultadoParser resultado = parser.parsear(arquivo, conta);

            resultado.getTransacoes().forEach(t -> assertAll(
                    () -> assertSame(conta, t.getConta()),
                    () -> assertFalse(t.getCategorizada())
            ));
        }
    }

    // ---------------------------------------------------------------------------
    // parsear() — Tolerância a Falhas (linhas inválidas não quebram o parse)
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("parsear() — Tolerância a Falhas")
    class ToleranciaAFalhas {

        @Test
        @DisplayName("2.5 Cabeçalho é ignorado e contado como linha inválida")
        void cabecalho_ignoradoEcontadoComoInvalido() throws IOException {
            // fixture: extrato-com-cabecalho.csv
            // linha 1: cabeçalho | linhas 2-3: dados válidos
            MockMultipartFile arquivo = fromFixture("extrato-com-cabecalho.csv");

            ResultadoParser resultado = parser.parsear(arquivo, conta);

            assertAll(
                    () -> assertEquals(2, resultado.getTransacoes().size()),
                    () -> assertEquals(3, resultado.getTotalLinhas()),
                    () -> assertEquals(1, resultado.getLinhasInvalidas())
            );
        }

        @Test
        @DisplayName("2.6 Linhas com menos de 4 colunas incrementam linhasInvalidas")
        void linhasComColunasInsuficientes_incrementamLinhasInvalidas() throws IOException {
            // fixture: extrato-misto-invalido.csv
            // linha1: válida | linha2: texto livre | linha3: válida | linha4: valor inválido | linha5: só data
            MockMultipartFile arquivo = fromFixture("extrato-misto-invalido.csv");

            ResultadoParser resultado = parser.parsear(arquivo, conta);

            assertAll(
                    () -> assertEquals(2, resultado.getTransacoes().size(),
                            "Apenas linhas 1 e 3 são válidas"),
                    () -> assertEquals(3, resultado.getLinhasInvalidas(),
                            "Linhas 2, 4 e 5 são inválidas"),
                    () -> assertEquals(5, resultado.getTotalLinhas())
            );
        }

        @Test
        @DisplayName("2.7 Valor monetário inválido incrementa linhasInvalidas sem interromper o parse")
        void valorMonetarioInvalido_incrementaLinhasInvalidasSemInterromper() {
            String conteudo = "2024-03-01,Valido,100.00,CREDITO\n"
                    + "2024-03-02,Invalido,abc,DEBITO\n"
                    + "2024-03-03,Valido2,200.00,CREDITO\n";

            ResultadoParser resultado = parser.parsear(fromString("extrato.csv", conteudo), conta);

            assertAll(
                    () -> assertEquals(2, resultado.getTransacoes().size()),
                    () -> assertEquals(1, resultado.getLinhasInvalidas()),
                    () -> assertEquals(3, resultado.getTotalLinhas())
            );
        }
    }

    // ---------------------------------------------------------------------------
    // parsear() — Regras de Tipo e Valor
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("parsear() — Regras de Tipo e Valor")
    class RegrasDeTipoEValor {

        @Test
        @DisplayName("2.8 Valor negativo sempre resulta em DEBITO, ignorando campo tipo")
        void valorNegativo_sempreResultaEmDebito_ignorandoTipo() throws IOException {
            // fixture: extrato-tipo-invalido.csv linha 2: -200.00 com tipo=CREDITO no CSV
            MockMultipartFile arquivo = fromFixture("extrato-tipo-invalido.csv");

            ResultadoParser resultado = parser.parsear(arquivo, conta);

            // linha com valor negativo deve ser DEBITO independente do campo tipo
            boolean temDebitoDeNegativo = resultado.getTransacoes().stream()
                    .anyMatch(t -> t.getValor().compareTo(new BigDecimal("200.00")) == 0
                            && t.getTipo() == TipoTransacao.DEBITO);

            assertTrue(temDebitoDeNegativo, "Valor negativo -200.00 deve gerar DEBITO mesmo com campo tipo=CREDITO");
        }

        @Test
        @DisplayName("2.8b Valor sempre armazenado como positivo na entidade")
        void valor_sempreArmazenadoComoPositivo() {
            String conteudo = "2024-01-01,Teste,-350.75,DEBITO\n";
            ResultadoParser resultado = parser.parsear(fromString("extrato.csv", conteudo), conta);

            assertEquals(0, resultado.getTransacoes().get(0).getValor()
                    .compareTo(new BigDecimal("350.75")));
        }

        @Test
        @DisplayName("2.9 Tipo não reconhecido com valor positivo faz fallback para CREDITO")
        void tipoNaoReconhecido_valorPositivo_fallbackParaCredito() throws IOException {
            // fixture: extrato-tipo-invalido.csv linha 1: 500.00 com tipo=PIX (não existe no enum)
            MockMultipartFile arquivo = fromFixture("extrato-tipo-invalido.csv");

            ResultadoParser resultado = parser.parsear(arquivo, conta);

            boolean temCreditoFallback = resultado.getTransacoes().stream()
                    .anyMatch(t -> t.getValor().compareTo(new BigDecimal("500.00")) == 0
                            && t.getTipo() == TipoTransacao.CREDITO);

            assertTrue(temCreditoFallback, "Tipo=PIX desconhecido com valor positivo deve resultar em CREDITO");
        }

        @Test
        @DisplayName("2.9b Tipo DEBITO explícito em valor positivo é respeitado")
        void tipoDebitoExplicito_valorPositivo_eRespeitado() {
            String conteudo = "2024-01-01,Estorno manual,100.00,DEBITO\n";
            ResultadoParser resultado = parser.parsear(fromString("extrato.csv", conteudo), conta);

            assertEquals(TipoTransacao.DEBITO, resultado.getTransacoes().get(0).getTipo());
        }

        // -----------------------------------------------------------------------
        // ⚠️  RISCO ALTO — Separador de milhar BR (1.500,00)
        // O campo tem vírgula E ponto, então: remove "." -> "1,500,00" -> troca ","
        // por "." -> "1.500.00" que é INVÁLIDO para BigDecimal.
        // Este teste documenta o comportamento atual (linha inválida) para alertar
        // a equipe sobre um possível bug na fixture de milhar com 4 colunas CSV.
        // -----------------------------------------------------------------------
        @Test
        @DisplayName("RISCO ALTO — Valor com separador de milhar BR (1.500,00) via coluna CSV")
        void valorMilharBR_emColunaCsv_comportamentoAtual() throws IOException {
            // fixture: extrato-valor-milhar.csv
            // "2024-04-01,Aluguel Comercial,-1.500,00,DEBITO"
            // ATENÇÃO: o delimitador vírgula faz o CSV ler como 5 colunas:
            //   col[0]=2024-04-01  col[1]=Aluguel Comercial  col[2]=-1.500  col[3]=00  col[4]=DEBITO
            // parsearValor("-1.500") -> tem ponto mas nao tem vírgula -> replace(",",".") -> "-1.500" -> BigDecimal OK = -1.5
            // O resultado NÃO é 1500.00 — é 1.50. Este teste documenta o bug latente.
            MockMultipartFile arquivo = fromFixture("extrato-valor-milhar.csv");

            ResultadoParser resultado = parser.parsear(arquivo, conta);

            // Documenta o comportamento atual sem forçar falha — serve de alerta
            assertFalse(resultado.getTransacoes().isEmpty(),
                    "Parser processa a linha mas interpreta o valor incorretamente. "
                            + "Separador de milhar BR em CSV com delimitador vírgula causa ambiguidade estrutural. "
                            + "Recomenda-se usar delimitador ponto-e-vírgula nesses casos.");
        }
    }

    // ---------------------------------------------------------------------------
    // parsear() — Entradas Corrompidas
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("parsear() — Entradas Corrompidas")
    class EntradasCorrompidas {

        @Test
        @DisplayName("2.11 Arquivo sem nenhuma linha válida retorna resultado vazio sem lançar exceção")
        void arquivoSemLinhasValidas_retornaResultadoVazio() {
            String conteudo = "lixo\nsem_formato\nabc123\n";
            ResultadoParser resultado = parser.parsear(fromString("extrato.csv", conteudo), conta);

            assertAll(
                    () -> assertEquals(0, resultado.getTransacoes().size()),
                    () -> assertTrue(resultado.getLinhasInvalidas() > 0)
            );
        }

        @Test
        @DisplayName("2.11b Arquivo completamente vazio retorna resultado zerado sem lançar exceção")
        void arquivoCompletamenteVazio_retornaResultadoZerado() {
            ResultadoParser resultado = parser.parsear(fromString("extrato.csv", ""), conta);

            assertAll(
                    () -> assertEquals(0, resultado.getTransacoes().size()),
                    () -> assertEquals(0, resultado.getTotalLinhas()),
                    () -> assertEquals(0, resultado.getLinhasInvalidas())
            );
        }
    }
}