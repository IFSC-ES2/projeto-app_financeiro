package bcd.appfinanceirobackend.parser;

import bcd.appfinanceirobackend.model.Conta;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * Testes de contrato comuns a todas as implementações de {@link ParserExtrato}.
 *
 * Não substituem os testes específicos de CSV, TXT, XML e NF-e — validam apenas
 * as regras mínimas que todo parser deve respeitar, reforçando o padrão Strategy:
 * cada parser converte um arquivo em um {@link ResultadoParser} com transações de
 * domínio, contabiliza o que avaliou e tolera registros inválidos sem persistir
 * nem amarrar a transação a uma Importacao/Categoria (responsabilidade do
 * ImportacaoService).
 *
 * Os parsers são instanciados diretamente (sem contexto Spring), o que por si só
 * evidencia que não dependem de repositório nem executam persistência.
 */
@DisplayName("Contrato comum dos parsers de extrato (ParserExtrato)")
class ParserContratoTest {

    // --- Conteúdo inline com sucesso parcial (1 registro válido + 1 inválido) por formato ---

    private static final String CSV_PARCIAL =
            "2024-01-10,Compra valida,100.00,CREDITO\n"
                    + "linha,invalida\n"
                    + "2024-01-11,Outra valida,50.00,DEBITO\n";

    private static final String TXT_PARCIAL =
            "10/01/2024  Compra Valida  -100,00\n"
                    + "linha sem data valida aqui\n"
                    + "11/01/2024  Outra Compra  -50,00\n";

    private static final String XML_PARCIAL =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<extrato>"
                    + "  <transacao><data>2024-01-10</data><descricao>Ok</descricao><valor>-100.00</valor></transacao>"
                    + "  <transacao><data>data-invalida</data><descricao>Ruim</descricao><valor>50.00</valor></transacao>"
                    + "</extrato>";

    private static final String NFE_PARCIAL =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<NFe><infNFe versao=\"4.00\">"
                    + "  <ide><dhEmi>2024-01-20T09:15:00-03:00</dhEmi></ide>"
                    + "  <emit><CNPJ>11222333000181</CNPJ><xNome>Loja Teste</xNome></emit>"
                    + "  <det nItem=\"1\"><prod><xProd>Item Valido</xProd><vProd>10.00</vProd></prod></det>"
                    + "  <det nItem=\"2\"><prod><xProd>Item Zerado</xProd><vProd>0.00</vProd></prod></det>"
                    + "  <total><ICMSTot><vNF>10.00</vNF></ICMSTot></total>"
                    + "</infNFe></NFe>";

    /** Parser + fixture válida representativa de cada formato. */
    static Stream<Arguments> casosValidos() {
        return Stream.of(
                arguments("CSV", new ParserCSV(), "csv/extrato-valido.csv", "text/csv"),
                arguments("TXT", new ParserTXT(), "txt/extrato-colunar-valido.txt", "text/plain"),
                arguments("XML", new ParserXML(), "xml/extrato-valido-transacao.xml", "application/xml"),
                arguments("NF-e", new ParserNFe(), "nfe/nfe-valida-sem-nfeproc.xml", "application/xml")
        );
    }

    /** Parser + conteúdo inline com sucesso parcial de cada formato. */
    static Stream<Arguments> casosSucessoParcial() {
        return Stream.of(
                arguments("CSV", new ParserCSV(), "extrato.csv", CSV_PARCIAL, "text/csv"),
                arguments("TXT", new ParserTXT(), "extrato.txt", TXT_PARCIAL, "text/plain"),
                arguments("XML", new ParserXML(), "extrato.xml", XML_PARCIAL, "application/xml"),
                arguments("NF-e", new ParserNFe(), "nota.xml", NFE_PARCIAL, "application/xml")
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casosValidos")
    @DisplayName("aceita o arquivo do próprio formato")
    void aceitaProprioFormato(String nome, ParserExtrato parser, String fixture, String contentType)
            throws IOException {
        MultipartFile arquivo = ExtratoTestFiles.daFixture(fixture, contentType);

        assertTrue(parser.aceita(arquivo), nome + " deveria aceitar a própria fixture");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casosValidos")
    @DisplayName("retorna ResultadoParser com transações e contagem de linhas")
    void retornaResultadoComContagens(String nome, ParserExtrato parser, String fixture, String contentType)
            throws IOException {
        MultipartFile arquivo = ExtratoTestFiles.daFixture(fixture, contentType);

        ResultadoParser resultado = parser.parsear(arquivo, new Conta());

        assertAll(nome,
                () -> assertNotNull(resultado, "resultado não pode ser nulo"),
                () -> assertNotNull(resultado.getTransacoes(), "lista de transações não pode ser nula"),
                () -> assertFalse(resultado.getTransacoes().isEmpty(), "arquivo válido deve gerar ao menos 1 transação"),
                () -> assertTrue(resultado.getTotalLinhas() > 0, "deve contabilizar os registros avaliados")
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casosValidos")
    @DisplayName("transações seguem o contrato de domínio (conta recebida, sem Importacao/Categoria, sem persistência)")
    void transacoesSeguemContratoDeDominio(String nome, ParserExtrato parser, String fixture, String contentType)
            throws IOException {
        MultipartFile arquivo = ExtratoTestFiles.daFixture(fixture, contentType);
        Conta conta = new Conta();

        ResultadoParser resultado = parser.parsear(arquivo, conta);

        resultado.getTransacoes().forEach(transacao -> assertAll(nome,
                () -> assertSame(conta, transacao.getConta(), "deve usar a conta recebida"),
                () -> assertNull(transacao.getImportacao(), "parser não define Importacao"),
                () -> assertNull(transacao.getCategoria(), "parser não define Categoria"),
                () -> assertFalse(transacao.getCategorizada(), "transação importada começa não categorizada"),
                () -> assertNull(transacao.getId(), "parser não persiste (sem id atribuído)"),
                () -> assertNotNull(transacao.getData(), "data é obrigatória"),
                () -> assertNotNull(transacao.getTipo(), "tipo é obrigatório"),
                () -> assertNotNull(transacao.getValor(), "valor é obrigatório"),
                () -> assertTrue(transacao.getValor().signum() >= 0, "valor é armazenado como não negativo")
        ));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casosValidos")
    @DisplayName("totalLinhas equivale a transações válidas + linhas inválidas")
    void totalConfereComValidasMaisInvalidas(String nome, ParserExtrato parser, String fixture, String contentType)
            throws IOException {
        MultipartFile arquivo = ExtratoTestFiles.daFixture(fixture, contentType);

        ResultadoParser resultado = parser.parsear(arquivo, new Conta());

        assertEqualsTotal(nome, resultado);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casosSucessoParcial")
    @DisplayName("sucesso parcial: contabiliza falhas sem interromper as válidas")
    void sucessoParcial_contabilizaFalhasSemQuebrar(String nome, ParserExtrato parser,
                                                    String nomeArquivo, String conteudo, String contentType) {
        MultipartFile arquivo = ExtratoTestFiles.deTexto(nomeArquivo, conteudo, contentType);
        Conta conta = new Conta();

        ResultadoParser resultado = parser.parsear(arquivo, conta);

        assertAll(nome,
                () -> assertTrue(resultado.getTransacoes().size() >= 1, "deve preservar as transações válidas"),
                () -> assertTrue(resultado.getLinhasInvalidas() >= 1, "deve contabilizar a linha inválida"),
                () -> resultado.getTransacoes().forEach(t -> assertSame(conta, t.getConta())),
                () -> assertEqualsTotal(nome, resultado)
        );
    }

    /** Invariante comum: o total avaliado é a soma de válidas e inválidas. */
    private static void assertEqualsTotal(String nome, ResultadoParser resultado) {
        int esperado = resultado.getTransacoes().size() + resultado.getLinhasInvalidas();
        assertTrue(resultado.getTotalLinhas() == esperado,
                () -> nome + ": totalLinhas (" + resultado.getTotalLinhas()
                        + ") deve ser válidas+inválidas (" + esperado + ")");
    }
}
