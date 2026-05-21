package bcd.appfinanceirobackend.parser;

import bcd.appfinanceirobackend.model.Conta;
import bcd.appfinanceirobackend.model.Transacao;
import bcd.appfinanceirobackend.model.enums.TipoTransacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.InputStream;
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
    void deveFazerParseDeNFeValidaComSucesso() throws Exception {
        InputStream is = getClass().getResourceAsStream("/extratos/extrato-valido-nfe.xml");
        MockMultipartFile mockFile = new MockMultipartFile("file", "extrato-valido-nfe.xml", "application/xml", is);

        List<Transacao> transacoes = parserNFe.parsear(mockFile, contaMock);

        assertFalse(transacoes.isEmpty());
        assertEquals(TipoTransacao.DEBITO, transacoes.get(0).getTipo());
    }
}