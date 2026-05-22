package bcd.appfinanceirobackend.parser;

import bcd.appfinanceirobackend.model.Conta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;

class ParserXMLTest {

    private ParserXML parserXML;
    private Conta contaMock;

    @BeforeEach
    void setUp() {
        parserXML = new ParserXML();
        contaMock = new Conta();
    }

    @Test
    void deveValidarContadoresDeLinhasEmArquivoXML() throws Exception {
        // XML com 1 transação válida e 1 inválida (sem valor)
        String xml = "<extrato>" +
                     "<transacao><data>2024-01-15</data><descricao>Mercado</descricao><valor>-150.00</valor><tipo>DEBITO</tipo></transacao>" +
                     "<transacao><data>2024-01-16</data><descricao>Invalida</descricao></transacao>" +
                     "</extrato>";
        MockMultipartFile file = new MockMultipartFile("file", "extrato.xml", "application/xml", xml.getBytes());

        ResultadoParser resultado = parserXML.parsear(file, contaMock);

        assertEquals(1, resultado.getTransacoes().size());
        assertEquals(2, resultado.getTotalLinhas());
        assertEquals(1, resultado.getLinhasInvalidas());
    }

    @Test
    void deveRetornarListaVaziaSeArquivoVazioSemLancarExcecao() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "vazio.xml", "application/xml", "".getBytes());
        
        ResultadoParser resultado = parserXML.parsear(file, contaMock);
        
        assertTrue(resultado.getTransacoes().isEmpty());
        assertEquals(0, resultado.getTotalLinhas());
        assertEquals(0, resultado.getLinhasInvalidas());
    }
}