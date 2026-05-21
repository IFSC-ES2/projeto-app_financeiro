package bcd.appfinanceirobackend.parser;

import bcd.appfinanceirobackend.model.Conta;
import bcd.appfinanceirobackend.model.Transacao;
import bcd.appfinanceirobackend.model.ResultadoParse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

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
    void deveFazerParseDeTxtColunarComSucesso() throws Exception {
        String txt = "2024-01-15   Supermercado   -150.00   DEBITO\n" +
                     "2024-01-16   Salário         5000.00   CREDITO";
        MockMultipartFile file = new MockMultipartFile("file", "extrato.txt", "text/plain", txt.getBytes());

        ResultadoParse resultado = parserTXT.parsear(file, contaMock);
        List<Transacao> transacoes = resultado.getTransacoes();

        assertEquals(2, transacoes.size());
        assertEquals(2, resultado.getTotalLinhas());
        assertEquals(0, resultado.getLinhasInvalidas());
        
        assertEquals("Supermercado", transacoes.get(0).getDescricao());
        assertEquals("Salário", transacoes.get(1).getDescricao());
    }

    @Test
    void deveUsarFallbackDeSplitPorTabulacaoSeRegexFalhar() throws Exception {
        // Linha sem formatação perfeita de regex, mas separada por TAB
        String txt = "2024-01-15\tMercado\t-150.00\tDEBITO";
        MockMultipartFile file = new MockMultipartFile("file", "extrato.txt", "text/plain", txt.getBytes());

        ResultadoParse resultado = parserTXT.parsear(file, contaMock);
        assertEquals(1, resultado.getTransacoes().size());
        assertEquals(1, resultado.getTotalLinhas());
        assertEquals(0, resultado.getLinhasInvalidas());
    }

    @Test
    void deveContarLinhaTotalmenteInvalida() throws Exception {
        String txt = "Isso não é uma transação financeira";
        MockMultipartFile file = new MockMultipartFile("file", "extrato.txt", "text/plain", txt.getBytes());

        ResultadoParse resultado = parserTXT.parsear(file, contaMock);
        assertTrue(resultado.getTransacoes().isEmpty());
        assertEquals(1, resultado.getTotalLinhas());
        assertEquals(1, resultado.getLinhasInvalidas());
    }

    @Test
    void deveLidarComArquivoVazio() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "vazio.txt", "text/plain", "".getBytes());
        ResultadoParse resultado = parserTXT.parsear(file, contaMock);
        assertTrue(resultado.getTransacoes().isEmpty());
    }
}