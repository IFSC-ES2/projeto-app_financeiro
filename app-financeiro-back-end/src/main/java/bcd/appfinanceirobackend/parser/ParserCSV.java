package bcd.appfinanceirobackend.parser;

import bcd.appfinanceirobackend.model.Conta;
import bcd.appfinanceirobackend.model.Transacao;
import bcd.appfinanceirobackend.model.enums.TipoTransacao;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Parser para extratos bancários no formato CSV.
 *
 * Detecta automaticamente o delimitador (vírgula, ponto e vírgula ou tabulação)
 * lendo a primeira linha do arquivo antes de processá-lo.
 *
 * Formato esperado das colunas (ordem obrigatória):
 *   data | descricao | valor | tipo
 *
 * Exemplos de linha válida:
 *   2024-01-15;Supermercado Extra;-150,00;DEBITO
 *   2024-01-20,Salário,5000.00,CREDITO
 *
 * Regras de interpretação:
 * - Valores negativos são sempre tratados como DEBITO, independente do campo tipo
 * - Datas aceitas: yyyy-MM-dd, dd/MM/yyyy, dd-MM-yyyy
 * - Separador decimal aceito: ponto ou vírgula
 * - Linhas de cabeçalho são ignoradas automaticamente (detecção por ausência de data válida)
 * Linhas inválidas não interrompem a importação. Elas são ignoradas e
 * contabilizadas em ResultadoParser.linhasInvalidas.
 */
@Component
public class ParserCSV implements ParserExtrato {

    private static final List<DateTimeFormatter> FORMATADORES = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy")
    );

    private static final String BOM = "\uFEFF";

    @Override
    public boolean aceita(MultipartFile arquivo) {
        String nome = arquivo.getOriginalFilename();
        return nome != null && nome.toLowerCase().endsWith(".csv");
    }

    @Override
    public ResultadoParser parsear(MultipartFile arquivo, Conta conta) {
        List<Transacao> transacoes = new ArrayList<>();
        ResultadoParser resultadoParser = new ResultadoParser();
        int linhasInvalidas = resultadoParser.getLinhasInvalidas();
        int totalLinhas = resultadoParser.getTotalLinhas();

        try {
            String conteudo = lerConteudoDoArquivo(arquivo);
            char delimitador = detectarDelimitador(conteudo);

            try (CSVReader reader = new CSVReaderBuilder(
                    new InputStreamReader(arquivo.getInputStream(), StandardCharsets.UTF_8))
                    .withCSVParser(new CSVParserBuilder().withSeparator(delimitador).build())
                    .build()) {

                String[] linha;
                while ((linha = reader.readNext()) != null) {
                    totalLinhas++;
                    if (linha.length != 4) {
                        linhasInvalidas++;
                        continue;
                    }

                    // Ignora cabeçalho: primeira coluna não é uma data válida
                    LocalDate data = parsearData(linha[0].trim());
                    if (data == null) {
                        linhasInvalidas++;
                        continue;
                    }

                    String descricao = linha[1].trim();
                    BigDecimal valor = parsearValor(linha[2].trim());
                    if (valor == null) {
                        linhasInvalidas++;
                        continue;
                    };

                    TipoTransacao tipo = parsearTipo(linha[3].trim(), valor);

                    // Valor sempre positivo na entidade; o tipo indica a direção
                    Transacao transacao = new Transacao();
                    transacao.setConta(conta);
                    transacao.setData(data);
                    transacao.setDescricao(descricao);
                    transacao.setValor(valor.abs());
                    transacao.setTipo(tipo);
                    transacao.setCategorizada(false);

                    transacoes.add(transacao);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar arquivo CSV: " + e.getMessage(), e);
        }
        resultadoParser.setTransacoes(transacoes);
        resultadoParser.setLinhasInvalidas(linhasInvalidas);
        resultadoParser.setTotalLinhas(totalLinhas);
        return resultadoParser;
    }

    private String lerConteudoDoArquivo(MultipartFile arquivo) {
        byte[] bytes;

        try {
            bytes = arquivo.getBytes();
        } catch (IOException e) {
            throw new IllegalArgumentException("Não foi possível ler o arquivo CSV.", e);
        }

        String textoUtf8 = tentarConverter(bytes, StandardCharsets.UTF_8);

        if (textoUtf8 != null) {
            return textoUtf8;
        }

        String textoWindows1252 = tentarConverter(bytes, Charset.forName("Windows-1252"));

        if (textoWindows1252 != null) {
            return textoWindows1252;
        }

        return new String(bytes, StandardCharsets.ISO_8859_1);
    }

    private String tentarConverter(byte[] bytes, Charset charset) {
        CharsetDecoder decoder = charset.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);

        try {
            return decoder.decode(ByteBuffer.wrap(bytes)).toString();
        } catch (CharacterCodingException e) {
            return null;
        }
    }


    /**
     * Detecta o delimitador mais provável analisando a primeira linha do arquivo.
     * Prioridade: ponto e vírgula > vírgula > tabulação.
     */
    private char detectarDelimitador(String conteudo) {
        String primeiraLinha = conteudo.split("\n")[0];
        if (primeiraLinha.contains(";")) return ';';
        if (primeiraLinha.contains(",")) return ',';
        return '\t';
    }

    /**
     * Tenta parsear a data nos formatos suportados.
     * Retorna null se nenhum formato for compatível (indica cabeçalho ou linha inválida).
     */
    private LocalDate parsearData(String valor) {
        for (DateTimeFormatter fmt : FORMATADORES) {
            try {
                return LocalDate.parse(valor, fmt);
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }

    /**
     * Parseia o valor monetário aceitando ponto ou vírgula como separador decimal.
     * Retorna null se o valor não puder ser interpretado.
     */
    private BigDecimal parsearValor(String valor) {
        try {
            // Remove separadores de milhar e normaliza o decimal
            String normalizado = valor
                    .replace("R$", "")
                    .replace(" ", "")
                    .trim();

            // Se tiver vírgula e ponto, o ponto é separador de milhar (ex: 1.500,00)
            if (normalizado.contains(",") && normalizado.contains(".")) {
                normalizado = normalizado.replace(".", "").replace(",", ".");
            } else {
                normalizado = normalizado.replace(",", ".");
            }

            return new BigDecimal(normalizado);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Determina o TipoTransacao. Valores negativos são sempre DEBITO.
     * Para valores positivos, tenta interpretar o campo tipo do CSV.
     */
    private TipoTransacao parsearTipo(String tipoStr, BigDecimal valor) {
        if (valor.compareTo(BigDecimal.ZERO) < 0) return TipoTransacao.DEBITO;

        try {
            return TipoTransacao.valueOf(tipoStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Fallback: positivo sem tipo reconhecido é CREDITO
            return TipoTransacao.CREDITO;
        }
    }
}