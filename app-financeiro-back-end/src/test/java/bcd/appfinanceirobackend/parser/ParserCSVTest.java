package bcd.appfinanceirobackend.parser;

import bcd.appfinanceirobackend.model.Conta;
import bcd.appfinanceirobackend.model.Transacao;
import bcd.appfinanceirobackend.model.ResultadoParse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

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
    void deveFazerParseDeCsvComPontoEVirgulaEIgnorarCabecalho() throws Exception {
        String csv = "Data;Descricao;Valor;Tipo\n" +
                     "2024-01-15;Supermercado;-150.00;DEBITO\n" +
                     "16/01/2024;Salário;5000.00;CREDITO";
        MockMultipartFile file = new MockMultipartFile("file", "extrato.csv", "text/csv", csv.getBytes());

        ResultadoParse resultado = parserCSV.parsear(file, contaMock);
        List<Transacao> transacoes = resultado.getTransacoes();

        assertEquals(2, transacoes.size());
        assertEquals(3, resultado.getTotalLinhas()); // 1 cabeçalho + 2 válidas
        assertEquals(1, resultado.getLinhasInvalidas()); // O cabeçalho conta como inválida pro parse

        assertEquals(LocalDate.of(2024, 1, 15), transacoes.get(0).getData());
        assertEquals(LocalDate.of(2024, 1, 16), transacoes.get(1).getData());
    }

    @Test
    void deveAceitarDelimitadorVirgulaEValorComVirgulaDecimal() throws Exception {
        String csv = "2024-01-15,Supermercado,\"-150,00\",DEBITO";
        MockMultipartFile file = new MockMultipartFile("file", "extrato.csv", "text/csv", csv.getBytes());

        ResultadoParse resultado = parserCSV.parsear(file, contaMock);
        assertEquals(1, resultado.getTransacoes().size());
        assertEquals(new BigDecimal("150.00"), resultado.getTransacoes().get(0).getValor());
    }

    @Test
    void deveAceitarDelimitadorTabulacao() throws Exception {
        String csv = "2024-01-15\tSupermercado\t-150.00\tDEBITO";
        MockMultipartFile file = new MockMultipartFile("file", "extrato.csv", "text/csv", csv.getBytes());

        ResultadoParse resultado = parserCSV.parsear(file, contaMock);
        assertEquals(1, resultado.getTransacoes().size());
    }

    @Test
    void deveLidarComArquivoVazio() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "vazio.csv", "text/csv", "".getBytes());
        ResultadoParse resultado = parserCSV.parsear(file, contaMock);
        
        assertTrue(resultado.getTransacoes().isEmpty());
        assertEquals(0, resultado.getTotalLinhas());
    }

    @Test
    void deveContarTodasLinhasComoInvalidasSeOArquivoForLixo() throws Exception {
        String csv = "linha 1 invalida\nlinha 2 invalida\nlinha 3 invalida";
        MockMultipartFile file = new MockMultipartFile("file", "lixo.csv", "text/csv", csv.getBytes());
        
        ResultadoParse resultado = parserCSV.parsear(file, contaMock);
        
        assertTrue(resultado.getTransacoes().isEmpty());
        assertEquals(3, resultado.getTotalLinhas());
        assertEquals(3, resultado.getLinhasInvalidas());
    }
}