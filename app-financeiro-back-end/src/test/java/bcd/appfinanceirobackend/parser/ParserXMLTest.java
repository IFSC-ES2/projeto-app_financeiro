package bcd.appfinanceirobackend.parser;

import bcd.appfinanceirobackend.model.Conta;
import bcd.appfinanceirobackend.model.Transacao;
import bcd.appfinanceirobackend.model.ResultadoParse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.util.List;

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
    void deveFazerParseComSucessoEValidarValorAbsoluto() throws Exception {
        String xml = "<extrato><transacao><data>2024-01-15</data><descricao>Mercado</descricao><valor>-150.00</valor><tipo>DEBITO</tipo></transacao></extrato>";
        MockMultipartFile file = new MockMultipartFile("file", "extrato.xml", "application/xml", xml.getBytes());

        ResultadoParse resultado = parserXML.parsear(file, contaMock);
        assertEquals(1, resultado.getTransacoes().size());
        
        // Verificação assertiva do valor absoluto exigida no review
        assertEquals(new BigDecimal("150.00"), resultado.getTransacoes().get(0).getValor());
    }

    @Test
    void deveAceitarTagsAlternativas() throws Exception {
        String xml = "<extrato><lancamento><data>2024-01-15</data><descricao>Mercado</descricao><valor>150.00</valor><tipo>DEBITO</tipo></lancamento></extrato>";
        MockMultipartFile file = new MockMultipartFile("file", "extrato.xml", "application/xml", xml.getBytes());

        ResultadoParse resultado = parserXML.parsear(file, contaMock);
        assertEquals(1, resultado.getTransacoes().size());
    }

    @Test
    void deveRejeitarNoAceitaSeForArquivoNFe() {
        String xmlNfe = "<nfeProc><NFe><infNFe></infNFe></NFe></nfeProc>";
        MockMultipartFile file = new MockMultipartFile("file", "extrato.xml", "application/xml", xmlNfe.getBytes());
        assertFalse(parserXML.aceita(file));
    }

    @Test
    void deveLidarComArquivoVazio() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "vazio.xml", "application/xml", "".getBytes());
        ResultadoParse resultado = parserXML.parsear(file, contaMock);
        assertTrue(resultado.getTransacoes().isEmpty());
    }

    @Test
    void deveBloquearAtaqueXXE() {
        String xxe = "<?xml version=\"1.0\"?><!DOCTYPE foo [<!ENTITY xxe SYSTEM \"file:///etc/passwd\">]><extrato><transacao><data>&xxe;</data></transacao></extrato>";
        MockMultipartFile file = new MockMultipartFile("file", "malicioso.xml", "application/xml", xxe.getBytes());

        assertThrows(Exception.class, () -> parserXML.parsear(file, contaMock));
    }
}