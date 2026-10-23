package bcd.appfinanceirobackend.parser;

import bcd.appfinanceirobackend.dto.importacao.ErroImportacaoDTO;
import bcd.appfinanceirobackend.model.Conta;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DisplayName("Detalhamento de erros dos parsers - issue #248")
class ParserErrosDetalhadosTest {

    private final Conta conta = new Conta();

    @Test
    @DisplayName(
            "CSV detalha registro inválido e continua importando o registro seguinte"
    )
    void csvDetalhaErroEContinuaImportacao() {
        String conteudo = """
                Data,Valor,Identificador,Descrição
                data-invalida,-80,identificador-1,Registro inválido
                08/06/2026,-50,identificador-2,Registro válido
                """;

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
                resultado
                        .getErros()
                        .getFirst();

        assertAll(
                () ->
                        assertEquals(
                                1,
                                resultado
                                        .getErros()
                                        .size()
                        ),
                () ->
                        assertEquals(
                                1,
                                resultado
                                        .getTransacoes()
                                        .size()
                        ),
                () ->
                        assertEquals(
                                2,
                                erro.getNumeroRegistro()
                        ),
                () ->
                        assertEquals(
                                "data",
                                erro.getCampo()
                        ),
                () ->
                        assertFalse(
                                erro.getMensagem()
                                        .isBlank()
                        ),
                () ->
                        assertEquals(
                                "Registro válido",
                                resultado
                                        .getTransacoes()
                                        .getFirst()
                                        .getDescricao()
                        ),
                () ->
                        assertEquals(
                                3,
                                resultado
                                        .getNumeroRegistroTransacao(
                                                0
                                        )
                        )
        );
    }

    @Test
    @DisplayName(
            "TXT detalha registro inválido e continua importando o registro seguinte"
    )
    void txtDetalhaErroEContinuaImportacao() {
        String conteudo =
                "data-invalida  Registro inválido  -10,00\n" +
                "11/01/2024  Registro válido  -50,00\n";

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
                resultado
                        .getErros()
                        .getFirst();

        assertAll(
                () ->
                        assertEquals(
                                1,
                                resultado
                                        .getErros()
                                        .size()
                        ),
                () ->
                        assertEquals(
                                1,
                                resultado
                                        .getTransacoes()
                                        .size()
                        ),
                () ->
                        assertEquals(
                                1,
                                erro.getNumeroRegistro()
                        ),
                () ->
                        assertEquals(
                                "data",
                                erro.getCampo()
                        ),
                () ->
                        assertFalse(
                                erro.getMensagem()
                                        .isBlank()
                        ),
                () ->
                        assertEquals(
                                "Registro válido",
                                resultado
                                        .getTransacoes()
                                        .getFirst()
                                        .getDescricao()
                        ),
                () ->
                        assertEquals(
                                2,
                                resultado
                                        .getNumeroRegistroTransacao(
                                                0
                                        )
                        )
        );
    }

    @Test
    @DisplayName(
            "XML detalha registro inválido e continua importando o registro seguinte"
    )
    void xmlDetalhaErroEContinuaImportacao() {
        String conteudo = """
                <?xml version="1.0" encoding="UTF-8"?>
                <extrato>
                    <transacao>
                        <data>data-invalida</data>
                        <descricao>Registro inválido</descricao>
                        <valor>-10.00</valor>
                    </transacao>
                    <transacao>
                        <data>2024-01-11</data>
                        <descricao>Registro válido</descricao>
                        <valor>-50.00</valor>
                    </transacao>
                </extrato>
                """;

        ResultadoParser resultado =
                new ParserXML().parsear(
                        arquivo(
                                "extrato.xml",
                                "application/xml",
                                conteudo
                        ),
                        conta
                );

        ErroImportacaoDTO erro =
                resultado
                        .getErros()
                        .getFirst();

        assertAll(
                () ->
                        assertEquals(
                                1,
                                resultado
                                        .getErros()
                                        .size()
                        ),
                () ->
                        assertEquals(
                                1,
                                resultado
                                        .getTransacoes()
                                        .size()
                        ),
                () ->
                        assertEquals(
                                1,
                                erro.getNumeroRegistro()
                        ),
                () ->
                        assertEquals(
                                "data",
                                erro.getCampo()
                        ),
                () ->
                        assertFalse(
                                erro.getMensagem()
                                        .isBlank()
                        ),
                () ->
                        assertEquals(
                                "Registro válido",
                                resultado
                                        .getTransacoes()
                                        .getFirst()
                                        .getDescricao()
                        ),
                () ->
                        assertEquals(
                                2,
                                resultado
                                        .getNumeroRegistroTransacao(
                                                0
                                        )
                        )
        );
    }

    @Test
    @DisplayName(
            "NF-e detalha item inválido e continua importando o item seguinte"
    )
    void nfeDetalhaErroEContinuaImportacao() {
        String conteudo = """
                <?xml version="1.0" encoding="UTF-8"?>
                <NFe>
                    <infNFe versao="4.00">
                        <ide>
                            <dhEmi>2024-01-20T09:15:00-03:00</dhEmi>
                        </ide>
                        <det nItem="1">
                            <prod>
                                <xProd>Item inválido</xProd>
                                <vProd>0.00</vProd>
                            </prod>
                        </det>
                        <det nItem="2">
                            <prod>
                                <xProd>Item válido</xProd>
                                <vProd>50.00</vProd>
                            </prod>
                        </det>
                    </infNFe>
                </NFe>
                """;

        ResultadoParser resultado =
                new ParserNFe().parsear(
                        arquivo(
                                "nota.xml",
                                "application/xml",
                                conteudo
                        ),
                        conta
                );

        ErroImportacaoDTO erro =
                resultado
                        .getErros()
                        .getFirst();

        assertAll(
                () ->
                        assertEquals(
                                1,
                                resultado
                                        .getErros()
                                        .size()
                        ),
                () ->
                        assertEquals(
                                1,
                                resultado
                                        .getTransacoes()
                                        .size()
                        ),
                () ->
                        assertEquals(
                                1,
                                erro.getNumeroRegistro()
                        ),
                () ->
                        assertEquals(
                                "vProd",
                                erro.getCampo()
                        ),
                () ->
                        assertFalse(
                                erro.getMensagem()
                                        .isBlank()
                        ),
                () ->
                        assertEquals(
                                "Item válido",
                                resultado
                                        .getTransacoes()
                                        .getFirst()
                                        .getDescricao()
                        ),
                () ->
                        assertEquals(
                                2,
                                resultado
                                        .getNumeroRegistroTransacao(
                                                0
                                        )
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