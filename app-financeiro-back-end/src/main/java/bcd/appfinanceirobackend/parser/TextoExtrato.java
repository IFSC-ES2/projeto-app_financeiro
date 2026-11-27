package bcd.appfinanceirobackend.parser;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * Utilidades de texto compartilhadas pelos parsers de extrato bancário.
 */
final class TextoExtrato {

    private static final Pattern DATA_HORA_NO_INICIO = Pattern.compile("^\\d{2}/\\d{2}(\\s+\\d{2}:\\d{2})?\\s+");

    // CPF (11 dígitos) e CNPJ (14) aparecem nos detalhes de Pix; não devem ir para a descrição gravada
    private static final Pattern DOCUMENTO = Pattern.compile("\\b\\d{11,14}\\b");

    // Código de terminal no fim da compra no cartão: "MAIS NATURAL - 0010-"
    private static final Pattern CODIGO_NO_FIM = Pattern.compile("\\s*-\\s*\\d+-?\\s*$");

    private TextoExtrato() {
    }

    /**
     * Bancos exportam em UTF-8 (com ou sem BOM), Windows-1252 ou Latin-1.
     * Tenta na ordem do mais estrito para o mais permissivo.
     */
    static String decodificar(byte[] bytes) {
        String texto = tentarConverter(bytes, StandardCharsets.UTF_8);

        if (texto == null) {
            texto = tentarConverter(bytes, Charset.forName("Windows-1252"));
        }

        return texto != null ? texto : new String(bytes, StandardCharsets.ISO_8859_1);
    }

    /**
     * Monta a descrição a partir do tipo do lançamento e do detalhe do banco:
     * "Compra com Cartão" + "15/02 12:30 MAIS NATURAL - 0010-" -> "Compra com Cartão - MAIS NATURAL".
     * O mesmo formato vale para OFX e CSV, para a deduplicação reconhecer o mesmo lançamento nos dois.
     */
    static String montarDescricaoBancaria(String lancamento, String detalhe) {
        String tipo = lancamento == null ? "" : lancamento.trim().replaceAll("\\s+", " ");
        String detalheLimpo = limparDetalhe(detalhe);

        if (tipo.isBlank()) {
            return detalheLimpo.isBlank() ? "Transação importada" : detalheLimpo;
        }

        return detalheLimpo.isBlank() ? tipo : tipo + " - " + detalheLimpo;
    }

    static String limparDetalhe(String detalhe) {
        if (detalhe == null) {
            return "";
        }

        String limpo = DATA_HORA_NO_INICIO.matcher(detalhe.trim()).replaceFirst("");
        limpo = DOCUMENTO.matcher(limpo).replaceAll(" ");
        limpo = CODIGO_NO_FIM.matcher(limpo).replaceFirst("");

        return limpo.replaceAll("\\s+", " ").trim();
    }

    private static String tentarConverter(byte[] bytes, Charset charset) {
        CharsetDecoder decoder = charset.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);

        try {
            return decoder.decode(ByteBuffer.wrap(bytes)).toString();
        } catch (CharacterCodingException e) {
            return null;
        }
    }
}
