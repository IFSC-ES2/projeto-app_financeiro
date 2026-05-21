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

class ParserNFeTest {

    private ParserNFe parserNFe;
    private Conta contaMock;

    @BeforeEach
    void setUp() {
        parserNFe = new ParserNFe();
        contaMock = new Conta();
    }

    @Test
    void deveFazerParseDeNFeComEnvelopeEContar3Itens() throws Exception {
        String nfe = "<nfeProc><NFe><infNFe>" +
                     "<det><prod><xProd>Item 1</xProd><vProd>10.00</vProd></prod></det>" +
                     "<det><prod><xProd>Item 2</xProd><vProd>20.00</vProd></prod></det>" +
                     "<det><prod><xProd>Item 3</xProd><vProd>30.00</vProd></prod></det>" +
                     "</infNFe></NFe></nfeProc>";
        MockMultipartFile file = new MockMultipartFile("file", "nota.xml", "application/xml", nfe.getBytes());

        ResultadoParse resultado = parserNFe.parsear(file, contaMock);
        assertEquals(3, resultado.getTransacoes().size());
        assertEquals(new BigDecimal("10.00"), resultado.getTransacoes().get(0).getValor());
        assertEquals(new BigDecimal("30.00"), resultado.getTransacoes().get(2).getValor());
    }

    @Test
    void deveFazerParseDeNFeSemEnvelope() throws Exception {
        String nfe = "<NFe><infNFe>" +
                     "<det><prod><xProd>Item 1</xProd><vProd>10.00</vProd></prod></det>" +
                     "</infNFe></NFe>";
        MockMultipartFile file = new MockMultipartFile("file", "nota.xml", "application/xml", nfe.getBytes());

        ResultadoParse resultado = parserNFe.parsear(file, contaMock);
        assertEquals(1, resultado.getTransacoes().size());
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

    @Test
    void deveLidarComArquivoVazio() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "vazio.nfe", "application/xml", "".getBytes());
        ResultadoParse resultado = parserNFe.parsear(file, contaMock);
        assertTrue(resultado.getTransacoes().isEmpty());
    }
}