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

class ParserTXTTest {

    private ParserTXT parserTXT;
    private Conta contaMock;

    @BeforeEach
    void setUp() {
        parserTXT = new ParserTXT();
        contaMock = new Conta();
    }

    @Test
    void deveFazerParseDeTxtValidoComSucesso() throws Exception {
        InputStream is = getClass().getResourceAsStream("/extratos/extrato-valido.txt");
        MockMultipartFile mockFile = new MockMultipartFile("file", "extrato-valido.txt", "text/plain", is);

        List<Transacao> transacoes = parserTXT.parsear(mockFile, contaMock);

        assertFalse(transacoes.isEmpty());
        assertEquals(TipoTransacao.DEBITO, transacoes.get(0).getTipo());
    }
}