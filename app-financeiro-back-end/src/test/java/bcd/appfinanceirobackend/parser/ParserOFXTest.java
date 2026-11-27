package bcd.appfinanceirobackend.parser;

import bcd.appfinanceirobackend.exception.ErroEstruturalImportacaoException;
import bcd.appfinanceirobackend.model.Conta;
import bcd.appfinanceirobackend.model.Transacao;
import bcd.appfinanceirobackend.model.enums.TipoTransacao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ParserOFX")
class ParserOFXTest {

    private final ParserOFX parser = new ParserOFX();
    private final Conta conta = new Conta();

    private static MockMultipartFile ofx(String conteudo) {
        return ExtratoTestFiles.deTexto("extrato.ofx", conteudo, "application/x-ofx");
    }

    @Nested
    @DisplayName("aceita()")
    class Aceita {

        @Test
        @DisplayName("aceita .ofx, inclusive em maiúsculas, e recusa outros formatos")
        void aceitaSomenteOfx() {
            assertAll(
                    () -> assertTrue(parser.aceita(ofx("<OFX>"))),
                    () -> assertTrue(parser.aceita(ExtratoTestFiles.deTexto("EXTRATO.OFX", "<OFX>", "text/plain"))),
                    () -> assertFalse(parser.aceita(ExtratoTestFiles.deTexto("extrato.csv", "a", "text/csv"))),
                    () -> assertFalse(parser.aceita(new MockMultipartFile("arquivo", null, "text/plain", new byte[0])))
            );
        }
    }

    @Nested
    @DisplayName("parsear() - extrato do Banco do Brasil")
    class BancoDoBrasil {

        @Test
        @DisplayName("lê data, valor, tipo e descrição de cada <STMTTRN> do arquivo em Latin-1")
        void importaLancamentos() throws IOException {
            ResultadoParser resultado = parser.parsear(ExtratoTestFiles.daFixture("ofx/extrato-bb.ofx", "application/x-ofx"), conta);
            List<Transacao> transacoes = resultado.getTransacoes();

            assertAll(
                    () -> assertEquals(3, transacoes.size()),
                    () -> assertEquals(3, resultado.getTotalLinhas()),
                    () -> assertTrue(resultado.getErros().isEmpty()),
                    () -> assertEquals(LocalDate.of(2026, 2, 3), transacoes.get(0).getData()),
                    () -> assertEquals(TipoTransacao.CREDITO, transacoes.get(0).getTipo()),
                    () -> assertEquals(new BigDecimal("150.00"), transacoes.get(0).getValor()),
                    () -> assertEquals("Compra com Cartão - PADARIA EXEMPLO", transacoes.get(1).getDescricao()),
                    () -> assertEquals(TipoTransacao.DEBITO, transacoes.get(1).getTipo()),
                    () -> assertEquals(new BigDecimal("1250.75"), transacoes.get(2).getValor())
            );
        }

        @Test
        @DisplayName("remove CPF/CNPJ e data/hora do detalhe do Pix")
        void removeDocumentoDaDescricao() throws IOException {
            ResultadoParser resultado = parser.parsear(ExtratoTestFiles.daFixture("ofx/extrato-bb.ofx", "application/x-ofx"), conta);

            String descricao = resultado.getTransacoes().get(0).getDescricao();

            assertAll(
                    () -> assertEquals("Pix - Recebido - Fulano de Tal", descricao),
                    () -> assertFalse(descricao.matches(".*\\d{11}.*"), "CPF não pode ir para a descrição")
            );
        }

        @Test
        @DisplayName("OFX e CSV do mesmo extrato geram as mesmas transações, para a deduplicação reconhecer")
        void ofxECsvGeramAsMesmasTransacoes() throws IOException {
            List<Transacao> doOfx = parser
                    .parsear(ExtratoTestFiles.daFixture("ofx/extrato-bb.ofx", "application/x-ofx"), conta)
                    .getTransacoes();
            List<Transacao> doCsv = new ParserCSV()
                    .parsear(ExtratoTestFiles.daFixture("csv/extrato-bb.csv", "text/csv"), conta)
                    .getTransacoes();

            assertEquals(doOfx.size(), doCsv.size());

            for (int i = 0; i < doOfx.size(); i++) {
                Transacao ofx = doOfx.get(i);
                Transacao csv = doCsv.get(i);

                assertAll(
                        () -> assertEquals(ofx.getData(), csv.getData()),
                        () -> assertEquals(ofx.getDescricao(), csv.getDescricao()),
                        () -> assertEquals(0, ofx.getValor().compareTo(csv.getValor())),
                        () -> assertEquals(ofx.getTipo(), csv.getTipo())
                );
            }
        }
    }

    @Nested
    @DisplayName("parsear() - variações do formato")
    class Variacoes {

        @Test
        @DisplayName("SGML sem tags de fechamento, vírgula decimal e só MEMO também são lidos")
        void sgmlSemFechamento() throws IOException {
            ResultadoParser resultado = parser.parsear(
                    ExtratoTestFiles.daFixture("ofx/extrato-sgml-sem-fechamento.ofx", "application/x-ofx"),
                    conta
            );

            assertAll(
                    () -> assertEquals(2, resultado.getTransacoes().size()),
                    () -> assertEquals(new BigDecimal("45.90"), resultado.getTransacoes().get(0).getValor()),
                    () -> assertEquals("Supermercado Exemplo", resultado.getTransacoes().get(0).getDescricao()),
                    () -> assertEquals("Salario empresa ficticia", resultado.getTransacoes().get(1).getDescricao()),
                    () -> assertEquals(TipoTransacao.CREDITO, resultado.getTransacoes().get(1).getTipo())
            );
        }

        @Test
        @DisplayName("lançamento inválido vira erro com a linha do <STMTTRN> e não interrompe os demais")
        void registroInvalidoNaoInterrompe() {
            String conteudo = """
                    <OFX>
                    <BANKTRANLIST>
                    <STMTTRN>
                    <DTPOSTED>data-ruim</DTPOSTED>
                    <TRNAMT>-10.00</TRNAMT>
                    </STMTTRN>
                    <STMTTRN>
                    <DTPOSTED>20260101</DTPOSTED>
                    <TRNAMT>0.00</TRNAMT>
                    </STMTTRN>
                    <STMTTRN>
                    <DTPOSTED>20260102</DTPOSTED>
                    <TRNAMT>abc</TRNAMT>
                    </STMTTRN>
                    <STMTTRN>
                    <DTPOSTED>20260103</DTPOSTED>
                    <TRNAMT>-5.00</TRNAMT>
                    <NAME>Tarifa</NAME>
                    </STMTTRN>
                    </BANKTRANLIST>
                    </OFX>
                    """;

            ResultadoParser resultado = parser.parsear(ofx(conteudo), conta);

            assertAll(
                    () -> assertEquals(1, resultado.getTransacoes().size()),
                    () -> assertEquals(4, resultado.getTotalLinhas()),
                    () -> assertEquals(3, resultado.getErros().size()),
                    () -> assertEquals(3, resultado.getErros().get(0).getNumeroRegistro()),
                    () -> assertEquals("data", resultado.getErros().get(0).getCampo()),
                    () -> assertEquals("valor", resultado.getErros().get(1).getCampo()),
                    () -> assertEquals("valor", resultado.getErros().get(2).getCampo()),
                    () -> assertEquals(15, resultado.getNumeroRegistroTransacao(0))
            );
        }

        @Test
        @DisplayName("OFX sem lançamentos retorna resultado vazio")
        void semLancamentos() {
            ResultadoParser resultado = parser.parsear(ofx("<OFX><BANKTRANLIST></BANKTRANLIST></OFX>"), conta);

            assertAll(
                    () -> assertTrue(resultado.getTransacoes().isEmpty()),
                    () -> assertEquals(0, resultado.getTotalLinhas())
            );
        }

        @Test
        @DisplayName("arquivo sem a tag <OFX> gera erro estrutural")
        void semTagOfx() {
            assertThrows(ErroEstruturalImportacaoException.class, () -> parser.parsear(ofx("isto nao e um ofx"), conta));
        }
    }
}
