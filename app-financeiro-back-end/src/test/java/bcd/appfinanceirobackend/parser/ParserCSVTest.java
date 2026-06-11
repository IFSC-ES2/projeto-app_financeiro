package bcd.appfinanceirobackend.parser;

import bcd.appfinanceirobackend.model.Conta;
import bcd.appfinanceirobackend.model.enums.TipoTransacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ParserCSV")
class ParserCSVTest {

    private ParserCSV parser;
    private Conta conta;

    @BeforeEach
    void setUp() {
        parser = new ParserCSV();
        conta = new Conta();
    }

    private MockMultipartFile fromFixture(String nomeFixture) throws IOException {
        ClassPathResource resource = new ClassPathResource("extratos/csv/" + nomeFixture);
        byte[] bytes = resource.getInputStream().readAllBytes();
        return new MockMultipartFile("arquivo", nomeFixture, "text/csv", bytes);
    }

    private MockMultipartFile fromString(String nomeArquivo, String conteudo) {
        return new MockMultipartFile(
                "arquivo",
                nomeArquivo,
                "text/csv",
                conteudo.getBytes(StandardCharsets.UTF_8)
        );
    }

    @Nested
    @DisplayName("aceita()")
    class Aceita {

        @Test
        @DisplayName("retorna true para arquivo .csv")
        void retornaTrue_paraArquivoCSV() {
            MockMultipartFile arquivo = fromString("extrato.csv", "qualquer");

            assertTrue(parser.aceita(arquivo));
        }

        @Test
        @DisplayName("retorna true para extensão .CSV maiúscula")
        void retornaTrue_extensaoMaiuscula() {
            MockMultipartFile arquivo = fromString("EXTRATO.CSV", "qualquer");

            assertTrue(parser.aceita(arquivo));
        }

        @Test
        @DisplayName("retorna false para arquivo .txt")
        void retornaFalse_paraTxt() {
            MockMultipartFile arquivo = fromString("extrato.txt", "qualquer");

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
                    "text/csv",
                    "qualquer".getBytes(StandardCharsets.UTF_8)
            );

            assertFalse(parser.aceita(arquivo));
        }
    }

    @Nested
    @DisplayName("parsear() - Happy paths")
    class HappyPaths {

        @Test
        @DisplayName("delimitador vírgula: 3 transações com campos corretos")
        void delimitadorVirgula_3transacoesValidas() throws IOException {
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
        @DisplayName("extrato bancário Nubank: valores negativos com ponto decimal viram DEBITO")
        void extratoBancarioNubank_valoresNegativosComPontoDecimal_viramDebito() {
            String conteudo = """
            Data,Valor,Identificador,Descrição
            08/06/2026,-8.91,6a26e7fc-e0ff-459a-bcbc-e8a5b774f068,Transferência enviada pelo Pix
            08/06/2026,-494.1,6a26fb54-15a4-4ad4-9dd7-82c8324803e3,Pagamento de fatura
            """;

            MockMultipartFile arquivo = fromString("nubank-conta.csv", conteudo);

            ResultadoParser resultado = parser.parsear(arquivo, conta);

            assertAll(
                    () -> assertEquals(2, resultado.getTransacoes().size()),
                    () -> assertEquals(2, resultado.getTotalLinhas()),
                    () -> assertEquals(0, resultado.getLinhasInvalidas()),

                    () -> assertEquals(TipoTransacao.DEBITO, resultado.getTransacoes().get(0).getTipo()),
                    () -> assertEquals(0, resultado.getTransacoes().get(0).getValor()
                            .compareTo(new BigDecimal("8.91"))),

                    () -> assertEquals(TipoTransacao.DEBITO, resultado.getTransacoes().get(1).getTipo()),
                    () -> assertEquals(0, resultado.getTransacoes().get(1).getValor()
                            .compareTo(new BigDecimal("494.1")))
            );
        }

        @Test
        @DisplayName("extrato bancário Nubank: importa Data,Valor,Identificador,Descrição")
        void extratoBancarioNubank_importaDataValorIdentificadorDescricao() {
            String conteudo = """
            Data,Valor,Identificador,Descrição
            03/06/2026,80,6a1fb213-a773-4548-a024-b4470e91312a,Resgate RDB
            03/06/2026,-80,6a1fb229-140f-475c-b676-7295f4258c0f,Transferência enviada pelo Pix
            08/06/2026,500,6a26e7e1-75af-454d-9f60-8324dda51a05,Transferência recebida pelo Pix
            08/06/2026,-494.1,6a26fb54-15a4-4ad4-9dd7-82c8324803e3,Pagamento de fatura
            """;

            MockMultipartFile arquivo = fromString("nubank-conta.csv", conteudo);

            ResultadoParser resultado = parser.parsear(arquivo, conta);

            assertAll(
                    () -> assertEquals(4, resultado.getTransacoes().size()),
                    () -> assertEquals(4, resultado.getTotalLinhas()),
                    () -> assertEquals(0, resultado.getLinhasInvalidas()),

                    () -> assertEquals("Resgate RDB", resultado.getTransacoes().get(0).getDescricao()),
                    () -> assertEquals(TipoTransacao.CREDITO, resultado.getTransacoes().get(0).getTipo()),
                    () -> assertEquals(0, resultado.getTransacoes().get(0).getValor()
                            .compareTo(new BigDecimal("80"))),

                    () -> assertEquals("Transferência enviada pelo Pix", resultado.getTransacoes().get(1).getDescricao()),
                    () -> assertEquals(TipoTransacao.DEBITO, resultado.getTransacoes().get(1).getTipo()),
                    () -> assertEquals(0, resultado.getTransacoes().get(1).getValor()
                            .compareTo(new BigDecimal("80"))),

                    () -> assertEquals("Transferência recebida pelo Pix", resultado.getTransacoes().get(2).getDescricao()),
                    () -> assertEquals(TipoTransacao.CREDITO, resultado.getTransacoes().get(2).getTipo()),
                    () -> assertEquals(0, resultado.getTransacoes().get(2).getValor()
                            .compareTo(new BigDecimal("500"))),

                    () -> assertEquals("Pagamento de fatura", resultado.getTransacoes().get(3).getDescricao()),
                    () -> assertEquals(TipoTransacao.DEBITO, resultado.getTransacoes().get(3).getTipo()),
                    () -> assertEquals(0, resultado.getTransacoes().get(3).getValor()
                            .compareTo(new BigDecimal("494.1")))
            );
        }

        @Test
        @DisplayName("extrato Nubank com BOM no cabeçalho é reconhecido corretamente")
        void extratoNubankComBom_noCabecalho_eReconhecido() {
            String conteudo = "\uFEFFdate,title,amount\n"
                    + "2026-05-30,Mais Natural,\"7,00\"\n";

            MockMultipartFile arquivo = fromString("nubank-real.csv", conteudo);

            ResultadoParser resultado = parser.parsear(arquivo, conta);

            assertAll(
                    () -> assertEquals(1, resultado.getTransacoes().size()),
                    () -> assertEquals(1, resultado.getTotalLinhas()),
                    () -> assertEquals(0, resultado.getLinhasInvalidas()),
                    () -> assertEquals("Mais Natural", resultado.getTransacoes().get(0).getDescricao()),
                    () -> assertEquals(TipoTransacao.DEBITO, resultado.getTransacoes().get(0).getTipo()),
                    () -> assertEquals(0, resultado.getTransacoes().get(0).getValor()
                            .compareTo(new BigDecimal("7.00")))
            );
        }

        @Test
        @DisplayName("extrato Nubank com linhas inválidas não interrompe importação")
        void extratoNubankComLinhasInvalidas_naoInterrompeImportacao() {
            String conteudo = """
            date,title,amount
            2026-05-30,Mais Natural,"7,00"
            data-invalida,Mercado,"15,99"
            2026-05-08,Pagamento recebido,"- 866,89"
            2026-05-10,Linha Valor Invalido,abc
            """;

            MockMultipartFile arquivo = fromString("nubank-real.csv", conteudo);

            ResultadoParser resultado = parser.parsear(arquivo, conta);

            assertAll(
                    () -> assertEquals(2, resultado.getTransacoes().size()),
                    () -> assertEquals(4, resultado.getTotalLinhas()),
                    () -> assertEquals(2, resultado.getLinhasInvalidas()),

                    () -> assertEquals(TipoTransacao.DEBITO, resultado.getTransacoes().get(0).getTipo()),
                    () -> assertEquals(TipoTransacao.CREDITO, resultado.getTransacoes().get(1).getTipo())
            );
        }

        @Test
        @DisplayName("delimitador ponto-e-vírgula é detectado automaticamente")
        void delimitadorPontoVirgula_detectadoAutomaticamente() throws IOException {
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
        @DisplayName("múltiplos formatos de data são aceitos")
        void multiploFormatosDeData_todosParseados() throws IOException {
            MockMultipartFile arquivo = fromFixture("extrato-valido.csv");

            ResultadoParser resultado = parser.parsear(arquivo, conta);

            assertEquals(3, resultado.getTransacoes().size());
        }

        @Test
        @DisplayName("arquivo apenas com cabeçalho retorna 0 transações sem lançar exceção")
        void apenasLinhaDeCabecalho_retornaZeroTransacoes() {
            MockMultipartFile arquivo = fromString("extrato.csv", "data,descricao,valor,tipo\n");

            ResultadoParser resultado = parser.parsear(arquivo, conta);

            assertAll(
                    () -> assertEquals(0, resultado.getTransacoes().size()),
                    () -> assertEquals(1, resultado.getTotalLinhas()),
                    () -> assertEquals(1, resultado.getLinhasInvalidas())
            );
        }

        @Test
        @DisplayName("transações retornadas têm conta preenchida e categorizada=false")
        void transacoesRetornadas_temContaECategorizadaFalse() throws IOException {
            MockMultipartFile arquivo = fromFixture("extrato-valido.csv");

            ResultadoParser resultado = parser.parsear(arquivo, conta);

            resultado.getTransacoes().forEach(transacao -> assertAll(
                    () -> assertSame(conta, transacao.getConta()),
                    () -> assertFalse(transacao.getCategorizada())
            ));
        }
    }

    @Nested
    @DisplayName("parsear() - Tolerância a falhas")
    class ToleranciaAFalhas {

        @Test
        @DisplayName("cabeçalho é ignorado e contado como linha inválida")
        void cabecalho_ignoradoEContadoComoInvalido() throws IOException {
            MockMultipartFile arquivo = fromFixture("extrato-com-cabecalho.csv");

            ResultadoParser resultado = parser.parsear(arquivo, conta);

            assertAll(
                    () -> assertEquals(2, resultado.getTransacoes().size()),
                    () -> assertEquals(3, resultado.getTotalLinhas()),
                    () -> assertEquals(1, resultado.getLinhasInvalidas())
            );
        }

        @Test
        @DisplayName("linhas com menos de 4 colunas incrementam linhasInvalidas")
        void linhasComColunasInsuficientes_incrementamLinhasInvalidas() throws IOException {
            MockMultipartFile arquivo = fromFixture("extrato-misto-invalido.csv");

            ResultadoParser resultado = parser.parsear(arquivo, conta);

            assertAll(
                    () -> assertEquals(2, resultado.getTransacoes().size()),
                    () -> assertEquals(3, resultado.getLinhasInvalidas()),
                    () -> assertEquals(5, resultado.getTotalLinhas())
            );
        }

        @Test
        @DisplayName("linhas com mais de 4 colunas são rejeitadas para evitar parse ambíguo")
        void linhasComColunasExtras_saoRejeitadas() throws IOException {
            MockMultipartFile arquivo = fromFixture("extrato-valor-br-sem-aspas-invalido.csv");

            ResultadoParser resultado = parser.parsear(arquivo, conta);

            assertAll(
                    () -> assertEquals(0, resultado.getTransacoes().size()),
                    () -> assertEquals(1, resultado.getLinhasInvalidas()),
                    () -> assertEquals(1, resultado.getTotalLinhas())
            );
        }

        @Test
        @DisplayName("valor monetário inválido incrementa linhasInvalidas sem interromper o parse")
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

    @Nested
    @DisplayName("parsear() - Regras de tipo e valor")
    class RegrasDeTipoEValor {

        @Test
        @DisplayName("valor negativo sempre resulta em DEBITO, ignorando campo tipo")
        void valorNegativo_sempreResultaEmDebito_ignorandoTipo() throws IOException {
            MockMultipartFile arquivo = fromFixture("extrato-tipo-invalido.csv");

            ResultadoParser resultado = parser.parsear(arquivo, conta);

            boolean temDebitoDeNegativo = resultado.getTransacoes().stream()
                    .anyMatch(transacao -> transacao.getValor().compareTo(new BigDecimal("200.00")) == 0
                            && transacao.getTipo() == TipoTransacao.DEBITO);

            assertTrue(temDebitoDeNegativo);
        }

        @Test
        @DisplayName("valor sempre é armazenado como positivo na entidade")
        void valor_sempreArmazenadoComoPositivo() {
            String conteudo = "2024-01-01,Teste,-350.75,DEBITO\n";

            ResultadoParser resultado = parser.parsear(fromString("extrato.csv", conteudo), conta);

            assertEquals(0, resultado.getTransacoes().get(0).getValor()
                    .compareTo(new BigDecimal("350.75")));
        }

        @Test
        @DisplayName("tipo não reconhecido com valor positivo faz fallback para CREDITO")
        void tipoNaoReconhecido_valorPositivo_fallbackParaCredito() throws IOException {
            MockMultipartFile arquivo = fromFixture("extrato-tipo-invalido.csv");

            ResultadoParser resultado = parser.parsear(arquivo, conta);

            boolean temCreditoFallback = resultado.getTransacoes().stream()
                    .anyMatch(transacao -> transacao.getValor().compareTo(new BigDecimal("500.00")) == 0
                            && transacao.getTipo() == TipoTransacao.CREDITO);

            assertTrue(temCreditoFallback);
        }

        @Test
        @DisplayName("tipo DEBITO explícito em valor positivo é respeitado")
        void tipoDebitoExplicito_valorPositivo_eRespeitado() {
            String conteudo = "2024-01-01,Estorno manual,100.00,DEBITO\n";

            ResultadoParser resultado = parser.parsear(fromString("extrato.csv", conteudo), conta);

            assertEquals(TipoTransacao.DEBITO, resultado.getTransacoes().get(0).getTipo());
        }
    }

    @Nested
    @DisplayName("parsear() - Entradas corrompidas")
    class EntradasCorrompidas {

        @Test
        @DisplayName("arquivo sem nenhuma linha válida retorna resultado vazio sem lançar exceção")
        void arquivoSemLinhasValidas_retornaResultadoVazio() {
            String conteudo = "lixo\nsem_formato\nabc123\n";

            ResultadoParser resultado = parser.parsear(fromString("extrato.csv", conteudo), conta);

            assertAll(
                    () -> assertEquals(0, resultado.getTransacoes().size()),
                    () -> assertTrue(resultado.getLinhasInvalidas() > 0)
            );
        }

        @Test
        @DisplayName("arquivo completamente vazio retorna resultado zerado sem lançar exceção")
        void arquivoCompletamenteVazio_retornaResultadoZerado() {
            ResultadoParser resultado = parser.parsear(fromString("extrato.csv", ""), conta);

            assertAll(
                    () -> assertEquals(0, resultado.getTransacoes().size()),
                    () -> assertEquals(0, resultado.getTotalLinhas()),
                    () -> assertEquals(0, resultado.getLinhasInvalidas())
            );
        }
    }

    @Nested
    @DisplayName("parsear() - Tipo textual legado (regressão #176)")
    class TipoTextualLegado {

        @Test
        @DisplayName("tipo BOLETO legado com valor positivo não quebra e faz fallback para CREDITO")
        void tipoBoletoPositivo_naoQuebra_viraCredito() {
            String conteudo = "2024-01-10,Recebimento,300.00,BOLETO\n";

            ResultadoParser resultado = parser.parsear(fromString("extrato.csv", conteudo), conta);

            assertAll(
                    () -> assertEquals(1, resultado.getTransacoes().size()),
                    () -> assertEquals(0, resultado.getLinhasInvalidas()),
                    () -> assertEquals(TipoTransacao.CREDITO, resultado.getTransacoes().get(0).getTipo()),
                    // o parser deriva apenas o sentido financeiro; não infere forma de pagamento do extrato
                    () -> assertNull(resultado.getTransacoes().get(0).getFormaPagamento())
            );
        }

        @Test
        @DisplayName("tipo BOLETO legado com valor negativo vira DEBITO pela regra do sinal")
        void tipoBoletoNegativo_viraDebito() {
            String conteudo = "2024-01-10,Pagamento boleto,-300.00,BOLETO\n";

            ResultadoParser resultado = parser.parsear(fromString("extrato.csv", conteudo), conta);

            assertAll(
                    () -> assertEquals(1, resultado.getTransacoes().size()),
                    () -> assertEquals(0, resultado.getLinhasInvalidas()),
                    () -> assertEquals(TipoTransacao.DEBITO, resultado.getTransacoes().get(0).getTipo())
            );
        }

        @Test
        @DisplayName("tipo PARCELAMENTO legado com valor negativo vira DEBITO")
        void tipoParcelamentoNegativo_viraDebito() {
            String conteudo = "2024-01-11,Parcela cartao,-150.00,PARCELAMENTO\n";

            ResultadoParser resultado = parser.parsear(fromString("extrato.csv", conteudo), conta);

            assertAll(
                    () -> assertEquals(1, resultado.getTransacoes().size()),
                    () -> assertEquals(0, resultado.getLinhasInvalidas()),
                    () -> assertEquals(TipoTransacao.DEBITO, resultado.getTransacoes().get(0).getTipo())
            );
        }

        @Test
        @DisplayName("arquivo só com tipos legados não lança exceção e só produz DEBITO/CREDITO")
        void arquivoComTiposLegados_naoLancaEProduzApenasDebitoOuCredito() {
            String conteudo = "2024-01-10,Boleto a pagar,-300.00,BOLETO\n"
                    + "2024-01-11,Parcela cartao,-150.00,PARCELAMENTO\n"
                    + "2024-01-12,Recebimento,80.00,BOLETO\n";

            ResultadoParser resultado = parser.parsear(fromString("extrato.csv", conteudo), conta);

            assertAll(
                    () -> assertEquals(3, resultado.getTransacoes().size()),
                    () -> assertEquals(0, resultado.getLinhasInvalidas()),
                    () -> assertTrue(resultado.getTransacoes().stream().allMatch(transacao ->
                            transacao.getTipo() == TipoTransacao.DEBITO
                                    || transacao.getTipo() == TipoTransacao.CREDITO))
            );
        }
    }
}