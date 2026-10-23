package bcd.appfinanceirobackend.parser;

import bcd.appfinanceirobackend.dto.importacao.ErroImportacaoDTO;
import bcd.appfinanceirobackend.exception.ErroEstruturalImportacaoException;
import bcd.appfinanceirobackend.model.Conta;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Detalhamento de erros dos parsers - issue #248")
class ParserErrosDetalhadosTest {

    private final Conta conta = new Conta();

    @Test
    @DisplayName(
            "CSV informa linha física correta mesmo com linha vazia"
    )
    void csvMantemLinhaFisica() {
        String conteudo =
                "2024-01-10,Compra valida,100.00,CREDITO\n" +
                "\n" +
                "linha,invalida\n" +
                "2024-01-11,Outra valida,50.00,DEBITO\n";

        ResultadoParser resultado =
                new ParserCSV().parsear(
                        arquivo(
                                "extrato.csv",
                                "text/csv",
                                conteudo
                        ),
                        conta
                );

        ErroImportacaoDTO erro =
                resultado.getErros().getFirst();

        assertAll(
                () -> assertEquals(
                        2,
                        resultado.getTransacoes().size()
                ),
                () -> assertEquals(
                        1,
                        resultado.getErros().size()
                ),
                () -> assertEquals(
                        3,
                        erro.getNumeroRegistro()
                ),
                () -> assertEquals(
                        "estrutura",
                        erro.getCampo()
                ),
                () -> assertFalse(
                        erro.getMensagem().isBlank()
                ),
                () -> assertEquals(
                        1,
                        resultado.getNumeroRegistroTransacao(0)
                ),
                () -> assertEquals(
                        4,
                        resultado.getNumeroRegistroTransacao(1)
                )
        );
    }

    @Test
    @DisplayName(
            "TXT informa linha física correta mesmo com linha vazia"
    )
    void txtMantemLinhaFisica() {
        String conteudo =
                "10/01/2024  Compra Valida  -100,00\n" +
                "\n" +
                "linha sem data valida aqui\n" +
                "11/01/2024  Outra Compra  -50,00\n";

        ResultadoParser resultado =
                new ParserTXT().parsear(
                        arquivo(
                                "extrato.txt",
                                "text/plain",
                                conteudo
                        ),
                        conta
                );

        ErroImportacaoDTO erro =
                resultado.getErros().getFirst();

        assertAll(
                () -> assertEquals(
                        2,
                        resultado.getTransacoes().size()
                ),
                () -> assertEquals(
                        1,
                        resultado.getErros().size()
                ),
                () -> assertEquals(
                        3,
                        erro.getNumeroRegistro()
                ),
                () -> assertEquals(
                        "data",
                        erro.getCampo()
                ),
                () -> assertFalse(
                        erro.getMensagem().isBlank()
                ),
                () -> assertEquals(
                        1,
                        resultado.getNumeroRegistroTransacao(0)
                ),
                () -> assertEquals(
                        4,
                        resultado.getNumeroRegistroTransacao(1)
                )
        );
    }

    @Test
    @DisplayName(
            "TXT continua aceitando linha somente com data e valor"
    )
    void txtDataEValorContinuaValido() {
        ResultadoParser resultado =
                new ParserTXT().parsear(
                        arquivo(
                                "extrato.txt",
                                "text/plain",
                                "15/01/2024 -50,00"
                        ),
                        conta
                );

        assertAll(
                () -> assertEquals(
                        1,
                        resultado.getTransacoes().size()
                ),
                () -> assertEquals(
                        "Importado via TXT",
                        resultado
                                .getTransacoes()
                                .getFirst()
                                .getDescricao()
                ),
                () -> assertTrue(
                        resultado.getErros().isEmpty()
                )
        );
    }

    @Test
    @DisplayName(
            "XML mantém registros válidos e detalha registro inválido"
    )
    void xmlDetalhaRegistroInvalido() {
        String conteudo =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<extrato>" +
                "<transacao>" +
                "<data>2024-01-10</data>" +
                "<descricao>Primeira</descricao>" +
                "<valor>-10.00</valor>" +
                "</transacao>" +
                "<transacao>" +
                "<data>data-invalida</data>" +
                "<descricao>Inválida</descricao>" +
                "<valor>20.00</valor>" +
                "</transacao>" +
                "<transacao>" +
                "<data>2024-01-12</data>" +
                "<descricao>Terceira</descricao>" +
                "<valor>-30.00</valor>" +
                "</transacao>" +
                "</extrato>";

        ResultadoParser resultado =
                new ParserXML().parsear(
                        arquivo(
                                "extrato.xml",
                                "application/xml",
                                conteudo
                        ),
                        conta
                );

        assertAll(
                () -> assertEquals(
                        2,
                        resultado.getTransacoes().size()
                ),
                () -> assertEquals(
                        1,
                        resultado.getErros().size()
                ),
                () -> assertEquals(
                        2,
                        resultado
                                .getErros()
                                .getFirst()
                                .getNumeroRegistro()
                ),
                () -> assertEquals(
                        "data",
                        resultado
                                .getErros()
                                .getFirst()
                                .getCampo()
                ),
                () -> assertFalse(
                        resultado
                                .getErros()
                                .getFirst()
                                .getMensagem()
                                .isBlank()
                ),
                () -> assertEquals(
                        3,
                        resultado.getNumeroRegistroTransacao(1)
                )
        );
    }

    @Test
    @DisplayName(
            "NF-e mantém itens válidos e detalha vProd inválido"
    )
    void nfeDetalhaRegistroInvalido() {
        String conteudo =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<NFe>" +
                "<infNFe versao=\"4.00\">" +
                "<ide>" +
                "<dhEmi>2024-01-20T09:15:00-03:00</dhEmi>" +
                "</ide>" +
                "<det nItem=\"1\">" +
                "<prod>" +
                "<xProd>Primeiro</xProd>" +
                "<vProd>10.00</vProd>" +
                "</prod>" +
                "</det>" +
                "<det nItem=\"2\">" +
                "<prod>" +
                "<xProd>Inválido</xProd>" +
                "<vProd>0.00</vProd>" +
                "</prod>" +
                "</det>" +
                "<det nItem=\"3\">" +
                "<prod>" +
                "<xProd>Terceiro</xProd>" +
                "<vProd>30.00</vProd>" +
                "</prod>" +
                "</det>" +
                "</infNFe>" +
                "</NFe>";

        ResultadoParser resultado =
                new ParserNFe().parsear(
                        arquivo(
                                "nota.xml",
                                "application/xml",
                                conteudo
                        ),
                        conta
                );

        assertAll(
                () -> assertEquals(
                        2,
                        resultado.getTransacoes().size()
                ),
                () -> assertEquals(
                        1,
                        resultado.getErros().size()
                ),
                () -> assertEquals(
                        2,
                        resultado
                                .getErros()
                                .getFirst()
                                .getNumeroRegistro()
                ),
                () -> assertEquals(
                        "vProd",
                        resultado
                                .getErros()
                                .getFirst()
                                .getCampo()
                ),
                () -> assertFalse(
                        resultado
                                .getErros()
                                .getFirst()
                                .getMensagem()
                                .isBlank()
                ),
                () -> assertEquals(
                        3,
                        resultado.getNumeroRegistroTransacao(1)
                )
        );
    }

    @Test
    @DisplayName(
            "cabeçalho Nubank inválido gera erro estrutural seguro"
    )
    void csvCabecalhoNubankInvalido() {
        String conteudo =
                "Data,Valor,Identificador\n" +
                "10/01/2024,-10,id-1\n";

        ErroEstruturalImportacaoException erro =
                assertThrows(
                        ErroEstruturalImportacaoException.class,
                        () -> new ParserCSV().parsear(
                                arquivo(
                                        "extrato.csv",
                                        "text/csv",
                                        conteudo
                                ),
                                conta
                        )
                );

        assertAll(
                () -> assertEquals(
                        1,
                        erro.getNumeroRegistro()
                ),
                () -> assertEquals(
                        "cabecalho",
                        erro.getCampo()
                ),
                () -> assertFalse(
                        erro.getMessage().isBlank()
                )
        );
    }

    @Test
    @DisplayName(
            "XML sem estrutura reconhecida gera erro estrutural seguro"
    )
    void xmlSemEstruturaGeraErroSeguro() {
        ErroEstruturalImportacaoException erro =
                assertThrows(
                        ErroEstruturalImportacaoException.class,
                        () -> new ParserXML().parsear(
                                arquivo(
                                        "extrato.xml",
                                        "application/xml",
                                        "<extrato><saldo>100</saldo></extrato>"
                                ),
                                conta
                        )
                );

        assertAll(
                () -> assertEquals(
                        "estrutura",
                        erro.getCampo()
                ),
                () -> assertFalse(
                        erro.getMessage().isBlank()
                )
        );
    }

    @Test
    @DisplayName(
            "NF-e sem dhEmi gera erro estrutural seguro"
    )
    void nfeSemDhEmiGeraErroSeguro() {
        String conteudo =
                "<NFe>" +
                "<infNFe>" +
                "<det nItem=\"1\">" +
                "<prod>" +
                "<xProd>Produto</xProd>" +
                "<vProd>10.00</vProd>" +
                "</prod>" +
                "</det>" +
                "</infNFe>" +
                "</NFe>";

        ErroEstruturalImportacaoException erro =
                assertThrows(
                        ErroEstruturalImportacaoException.class,
                        () -> new ParserNFe().parsear(
                                arquivo(
                                        "nota.xml",
                                        "application/xml",
                                        conteudo
                                ),
                                conta
                        )
                );

        assertAll(
                () -> assertEquals(
                        "dhEmi",
                        erro.getCampo()
                ),
                () -> assertTrue(
                        erro.getMessage().contains("<dhEmi>")
                )
        );
    }

    private MockMultipartFile arquivo(
            String nome,
            String contentType,
            String conteudo
    ) {
        return new MockMultipartFile(
                "arquivo",
                nome,
                contentType,
                conteudo.getBytes(
                        StandardCharsets.UTF_8
                )
        );
    }
}