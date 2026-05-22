package bcd.appfinanceirobackend.parser;

import bcd.appfinanceirobackend.model.Conta;
import bcd.appfinanceirobackend.parser.ResultadoParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;

class ParserCSVTest {

    private ParserCSV parserCSV;
    private Conta contaMock;

    @BeforeEach
    void setUp() {
        parserCSV = new ParserCSV();
        contaMock = new Conta();
    }

    @Test
    void deveValidarContadoresDeLinhasEIgnorarCabecalho() throws Exception {
        String csv = "Data;Descricao;Valor;Tipo\n" +
                     "2024-01-15;Supermercado;-150.00;DEBITO\n" +
                     "16/01/2024;Salário;5000.00;CREDITO";
        MockMultipartFile file = new MockMultipartFile("file", "extrato.csv", "text/csv", csv.getBytes());

        ResultadoParser resultado = parserCSV.parsear(file, contaMock);

        assertEquals(2, resultado.getTransacoes().size());
        assertEquals(2, resultado.getTotalLinhas());
        assertEquals(0, resultado.getLinhasInvalidas());
    }

    @Test
    void deveRetornarListaVaziaSeArquivoVazioSemLancarExcecao() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "vazio.csv", "text/csv", "".getBytes());
        ResultadoParser resultado = parserCSV.parsear(file, contaMock);
        
        assertTrue(resultado.getTransacoes().isEmpty());
        assertEquals(0, resultado.getTotalLinhas());
        assertEquals(0, resultado.getLinhasInvalidas());
    }

    @Test
    void deveRejeitarArquivoTxtNoAceita() {
        MockMultipartFile fileTxt = new MockMultipartFile("file", "extrato.txt", "text/plain", "conteudo".getBytes());
        assertFalse(parserCSV.aceita(fileTxt));
    }
}