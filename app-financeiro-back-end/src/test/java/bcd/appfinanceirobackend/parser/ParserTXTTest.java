package bcd.appfinanceirobackend.parser;

import bcd.appfinanceirobackend.model.Conta;
import bcd.appfinanceirobackend.parser.ResultadoParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;

class ParserTXTTest {

    private ParserTXT parserTXT;
    private Conta contaMock;

    @BeforeEach
    void setUp() {
        parserTXT = new ParserTXT();
        contaMock = new Conta();
    }

    @Test
    void deveValidarContadoresDeLinhasEmArquivoTXT() throws Exception {
        String txt = "2024-01-15   Supermercado   -150.00   DEBITO\n" +
                     "2024-01-16   Salário         5000.00   CREDITO\n" +
                     "Isso não é uma transação financeira";
        MockMultipartFile file = new MockMultipartFile("file", "extrato.txt", "text/plain", txt.getBytes());

        ResultadoParser resultado = parserTXT.parsear(file, contaMock);

        assertEquals(2, resultado.getTransacoes().size());
        assertEquals(3, resultado.getTotalLinhas());
        assertEquals(1, resultado.getLinhasInvalidas());
    }

    @Test
    void deveRetornarListaVaziaSeArquivoVazioSemLancarExcecao() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "vazio.txt", "text/plain", "".getBytes());
        ResultadoParser resultado = parserTXT.parsear(file, contaMock);
        
        assertTrue(resultado.getTransacoes().isEmpty());
        assertEquals(0, resultado.getTotalLinhas());
        assertEquals(0, resultado.getLinhasInvalidas());
    }

    @Test
    void deveRejeitarArquivoCsvNoAceita() {
        MockMultipartFile fileCsv = new MockMultipartFile("file", "extrato.csv", "text/csv", "conteudo".getBytes());
        assertFalse(parserTXT.aceita(fileCsv));
    }
}