package bcd.appfinanceirobackend.parser;

import bcd.appfinanceirobackend.exception.ErroEstruturalImportacaoException;
import bcd.appfinanceirobackend.model.Conta;
import bcd.appfinanceirobackend.model.Transacao;
import bcd.appfinanceirobackend.model.enums.TipoTransacao;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extrato bancário em OFX (Open Financial Exchange), o formato que a maioria dos bancos
 * brasileiros exporta para "Money/Quicken".
 *
 * O OFX 1.x é SGML: muitos bancos omitem as tags de fechamento dos campos
 * (&lt;TRNAMT&gt;-10.00 sem &lt;/TRNAMT&gt;). Por isso os campos são lidos por expressão regular
 * até o próximo "&lt;" ou fim de linha, e não por um parser XML.
 */
@Component
public class ParserOFX implements ParserExtrato {

    private static final int TAMANHO_MAXIMO_DESCRICAO = 255;

    private static final Pattern INICIO_TRANSACAO = Pattern.compile("<STMTTRN>", Pattern.CASE_INSENSITIVE);

    private static final Pattern FIM_TRANSACAO =
            Pattern.compile("</STMTTRN>|<STMTTRN>|</BANKTRANLIST>", Pattern.CASE_INSENSITIVE);

    private static final DateTimeFormatter DATA_OFX = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public boolean aceita(MultipartFile arquivo) {
        String nome = arquivo.getOriginalFilename();
        return nome != null && nome.toLowerCase().endsWith(".ofx");
    }

    @Override
    public ResultadoParser parsear(MultipartFile arquivo, Conta conta) {
        String conteudo;

        try {
            conteudo = TextoExtrato.decodificar(arquivo.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Erro ao ler o arquivo OFX", e);
        }

        if (!conteudo.toUpperCase().contains("<OFX>")) {
            throw new ErroEstruturalImportacaoException(
                    1,
                    "estrutura",
                    "Arquivo OFX inválido: a tag <OFX> não foi encontrada."
            );
        }

        ResultadoParser resultado = new ResultadoParser();
        int totalLinhas = 0;

        Matcher inicio = INICIO_TRANSACAO.matcher(conteudo);

        while (inicio.find()) {
            int comeco = inicio.end();
            Matcher fim = FIM_TRANSACAO.matcher(conteudo);
            String bloco = fim.find(comeco) ? conteudo.substring(comeco, fim.start()) : conteudo.substring(comeco);
            int numeroRegistro = numeroDaLinha(conteudo, inicio.start());

            totalLinhas++;

            LocalDate data = parsearData(campo(bloco, "DTPOSTED"));

            if (data == null) {
                resultado.adicionarErro(
                        numeroRegistro,
                        "data",
                        "Data inválida ou ausente em <DTPOSTED>."
                );

                continue;
            }

            BigDecimal valor = parsearValor(campo(bloco, "TRNAMT"));

            if (valor == null) {
                resultado.adicionarErro(
                        numeroRegistro,
                        "valor",
                        "Valor monetário inválido ou ausente em <TRNAMT>."
                );

                continue;
            }

            if (valor.compareTo(BigDecimal.ZERO) == 0) {
                resultado.adicionarErro(
                        numeroRegistro,
                        "valor",
                        "O valor da transação não pode ser zero."
                );

                continue;
            }

            String descricao = TextoExtrato.montarDescricaoBancaria(campo(bloco, "NAME"), campo(bloco, "MEMO"));

            Transacao transacao = new Transacao();
            transacao.setConta(conta);
            transacao.setData(data);
            transacao.setDescricao(
                    descricao.length() > TAMANHO_MAXIMO_DESCRICAO
                            ? descricao.substring(0, TAMANHO_MAXIMO_DESCRICAO)
                            : descricao
            );
            transacao.setValor(valor.abs());
            // O sinal do valor manda; TRNTYPE varia entre bancos (DEBIT, PAYMENT, POS, XFER...)
            transacao.setTipo(valor.signum() > 0 ? TipoTransacao.CREDITO : TipoTransacao.DEBITO);
            transacao.setCategorizada(false);

            resultado.adicionarTransacao(transacao, numeroRegistro);
        }

        resultado.setTotalLinhas(totalLinhas);

        return resultado;
    }

    /** Valor da tag com ou sem fechamento: lê até o próximo "<" ou fim de linha. */
    private String campo(String bloco, String tag) {
        Matcher matcher = Pattern
                .compile("<" + tag + ">([^<\\r\\n]*)", Pattern.CASE_INSENSITIVE)
                .matcher(bloco);

        return matcher.find() ? matcher.group(1).trim() : null;
    }

    /** DTPOSTED vem como AAAAMMDD[hhmmss[.xxx]][[-3:BRT]]; só os 8 primeiros dígitos importam. */
    private LocalDate parsearData(String valor) {
        if (valor == null || valor.length() < 8) {
            return null;
        }

        try {
            return LocalDate.parse(valor.substring(0, 8), DATA_OFX);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** O padrão é ponto decimal, mas alguns bancos brasileiros exportam com vírgula. */
    private BigDecimal parsearValor(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        try {
            return new BigDecimal(valor.replace(" ", "").replace(",", "."));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private int numeroDaLinha(String conteudo, int indice) {
        int linha = 1;

        for (int i = 0; i < indice; i++) {
            if (conteudo.charAt(i) == '\n') {
                linha++;
            }
        }

        return linha;
    }
}
