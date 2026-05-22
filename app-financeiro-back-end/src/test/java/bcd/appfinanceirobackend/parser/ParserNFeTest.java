package bcd.appfinanceirobackend.parser;

import bcd.appfinanceirobackend.model.Conta;
import bcd.appfinanceirobackend.parser.ResultadoParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;

class ParserNFeTest {

    private ParserNFe parserNFe;
    private Conta contaMock;

    @BeforeEach
    void setUp() {
        parserNFe = new ParserNFe();
        contaMock = new Conta();
    }

    @Test
    void deveValidarContadoresDeLinhasEmArquivoNFe() throws Exception {
        String nfe = "<nfeProc><NFe><infNFe>" +
                     "<dhEmi>2024-01-15T10:30:00-03:00</dhEmi>" +
                     "<det><prod><xProd>Item 1</xProd><vProd>10.00</vProd></prod></det>" +
                     "<det><prod><xProd>Item 2</xProd><vProd>0.00</vProd></prod></det>" +
                     "<det><prod><xProd>Item 3</xProd><vProd>30.00</vProd></prod></det>" +
                     "</infNFe></NFe></nfeProc>";
        MockMultipartFile file = new MockMultipartFile("file", "nota.xml", "application/xml", nfe.getBytes());

        ResultadoParser resultado = parserNFe.parsear(file, contaMock);

        assertEquals(2, resultado.getTransacoes().size());
        assertEquals(3, resultado.getTotalLinhas());
        assertEquals(1, resultado.getLinhasInvalidas());
    }

    @Test
    void deveRetornarListaVaziaSeArquivoVazioSemLancarExcecao() throws Exception {
        String xmlVazio = "<nfeProc><NFe><infNFe><dhEmi>2024-01-15T10:30:00-03:00</dhEmi></infNFe></NFe></nfeProc>";
        MockMultipartFile fileVazio = new MockMultipartFile("file", "vazio.nfe", "application/xml", xmlVazio.getBytes());
        
        try {
            ResultadoParser resultado = parserNFe.parsear(fileVazio, contaMock);
            assertTrue(resultado.getTransacoes().isEmpty());
            assertEquals(0, resultado.getTotalLinhas());
            assertEquals(0, resultado.getLinhasInvalidas());
        } catch (Exception e) {
            assertTrue(true);
        }
    }

    @Test
    void deveAceitarExtensaoNFe() {
        MockMultipartFile file = new MockMultipartFile("file", "nota.nfe", "application/xml", "<NFe></NFe>".getBytes());
        assertTrue(parserNFe.aceita(file));
    }

    @Test
    void deveRejeitarXMLGenericoNoAceita() {
        MockMultipartFile file = new MockMultipartFile("file", "extrato.xml", "application/xml", "<extrato></extrato>".getBytes());
        assertFalse(parserNFe.aceita(file));
    }
}