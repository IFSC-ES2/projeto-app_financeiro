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

class ParserCSVTest {

    private ParserCSV parserCSV;
    private Conta contaMock;

    @BeforeEach
    void setUp() {
        parserCSV = new ParserCSV();
        contaMock = new Conta();
    }

    @Test
    void deveFazerParseDeCsvValidoComSucesso() throws Exception {
        InputStream is = getClass().getResourceAsStream("/extratos/extrato-valido.csv");
        MockMultipartFile mockFile = new MockMultipartFile(
                "file", "extrato-valido.csv", "text/csv", is
        );

        List<Transacao> transacoes = parserCSV.parsear(mockFile, contaMock);

        assertEquals(2, transacoes.size());

        Transacao t1 = transacoes.get(0);
        assertEquals("Supermercado", t1.getDescricao());
        assertEquals(TipoTransacao.DEBITO, t1.getTipo());

        Transacao t2 = transacoes.get(1);
        assertEquals("Salário", t2.getDescricao());
        assertEquals(TipoTransacao.CREDITO, t2.getTipo());
    }
}