package bcd.appfinanceirobackend.parser;

import bcd.appfinanceirobackend.model.Conta;
import bcd.appfinanceirobackend.model.Transacao;
import bcd.appfinanceirobackend.model.enums.TipoTransacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
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
    void deveFazerParseDeXmlValidoComSucesso() throws Exception {
        InputStream is = getClass().getResourceAsStream("/extratos/extrato-valido.xml");
        MockMultipartFile mockFile = new MockMultipartFile("file", "extrato-valido.xml", "application/xml", is);

        List<Transacao> transacoes = parserXML.parsear(mockFile, contaMock);

        assertEquals(2, transacoes.size());

        Transacao t1 = transacoes.get(0);
        assertEquals(LocalDate.of(2024, 1, 15), t1.getData());
        assertEquals("Supermercado", t1.getDescricao());
        assertTrue(t1.getValor().compareTo(new BigDecimal("-150.00")) == 0 || t1.getValor().compareTo(new BigDecimal("150.00")) == 0);
        assertEquals(TipoTransacao.DEBITO, t1.getTipo());

        Transacao t2 = transacoes.get(1);
        assertEquals(LocalDate.of(2024, 1, 16), t2.getData());
        assertEquals("Salário", t2.getDescricao());
        assertTrue(t2.getValor().compareTo(new BigDecimal("5000.00")) == 0);
        assertEquals(TipoTransacao.CREDITO, t2.getTipo());
    }

    @Test
    void deveBloquearAtaqueXXE() throws Exception {
        InputStream is = getClass().getResourceAsStream("/extratos/extrato-malicioso.xml");
        MockMultipartFile mockFile = new MockMultipartFile("file", "extrato-malicioso.xml", "application/xml", is);

        // Verifica se o parser lança uma exceção ao tentar ler um arquivo com tentativa de injeção XXE
        assertThrows(Exception.class, () -> {
            parserXML.parsear(mockFile, contaMock);
        }, "O parser deveria ter bloqueado a injeção XXE e lançado uma exceção.");
    }
}