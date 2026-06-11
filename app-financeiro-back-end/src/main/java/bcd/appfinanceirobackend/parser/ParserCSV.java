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
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.*;
import java.text.Normalizer;
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
        try {
            String conteudo = lerConteudoDoArquivo(arquivo);
            conteudo = removerBom(conteudo);

            List<String> linhas = quebrarEmLinhasUteis(conteudo);

            if (linhas.isEmpty()) {
                return new ResultadoParser();
            }

            char delimitador = detectarDelimitador(linhas);

            List<String[]> registros = lerRegistrosCsv(linhas, delimitador);

            if (registros.isEmpty()) {
                return new ResultadoParser();
            }

            String[] cabecalho = registros.getFirst();

            if (ehCabecalhoNubank(cabecalho)) {
                return processarCsvNubank(registros, conta);
            }
//        try {
//            String conteudo = lerConteudoDoArquivo(arquivo);
//            conteudo = removerBom(conteudo);
//            char delimitador = quebrarEmLinhasUteis(conteudo);
//
//            try (CSVReader reader = new CSVReaderBuilder(
//                    new InputStreamReader(arquivo.getInputStream(), StandardCharsets.UTF_8))
//                    .withCSVParser(new CSVParserBuilder().withSeparator(delimitador).build())
//                    .build()) {
//
//                String[] linha;
//                while ((linha = reader.readNext()) != null) {
//                    totalLinhas++;
//                    if (linha.length != 4) {
//                        linhasInvalidas++;
//                        continue;
//                    }
//
//                    // Ignora cabeçalho: primeira coluna não é uma data válida
//                    LocalDate data = parsearData(linha[0].trim());
//                    if (data == null) {
//                        linhasInvalidas++;
//                        continue;
//                    }
//
//                    String descricao = linha[1].trim();
//                    BigDecimal valor = parsearValor(linha[2].trim());
//                    if (valor == null) {
//                        linhasInvalidas++;
//                        continue;
//                    };
//
//                    TipoTransacao tipo = parsearTipo(linha[3].trim(), valor);
//
//                    // Valor sempre positivo na entidade; o tipo indica a direção
//                    Transacao transacao = new Transacao();
//                    transacao.setConta(conta);
//                    transacao.setData(data);
//                    transacao.setDescricao(descricao);
//                    transacao.setValor(valor.abs());
//                    transacao.setTipo(tipo);
//                    transacao.setCategorizada(false);
//
//                    transacoes.add(transacao);
//                }
//            }
        }catch (Exception e) {
            throw new RuntimeException("Erro ao processar arquivo CSV: " + e.getMessage(), e);
        }

    }

    /**
     * Lê os bytes do arquivo tentando UTF-8 primeiro.
     * Se UTF-8 não funcionar, tenta Windows-1252.
     * Se também não funcionar, usa ISO-8859-1 como fallback final.
     *
     * Isso ajuda em extratos reais, porque alguns bancos exportam arquivos com
     * acentos e caracteres especiais fora de UTF-8.
     */

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

    /**
     * Tenta converter os bytes usando um charset específico.
     *
     * O uso de CharsetDecoder é importante porque:
     * new String(bytes, UTF_8) pode mascarar erro de encoding,
     * enquanto o decoder consegue reportar que a conversão falhou.
     */

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

    private String removerBom(String conteudo) {
        if (conteudo == null) {
            return "";
        }

        if (conteudo.startsWith(BOM)) {
            return conteudo.substring(1);
        }

        return conteudo;
    }

    /**
     * Quebra o conteúdo em linhas úteis.
     *
     * Linhas em branco são ignoradas para não virarem falhas falsas
     * na importação.
     */

    private List<String> quebrarEmLinhasUteis(String conteudo) {
        List<String> linhasUteis = new ArrayList<>();

        String conteudoNormalizado = conteudo
                .replace("\r\n", "\n")
                .replace("\r", "\n");

        for (String linha : conteudoNormalizado.split("\n")) {
            String linhaLimpa = linha.trim();

            if (!linhaLimpa.isBlank()) {
                linhasUteis.add(linhaLimpa);
            }
        }

        return linhasUteis;
    }


    /**
     * Detecta o delimitador comparando vírgula, ponto e vírgula e tabulação.
     *
     * Para o Nubank, a linha:
     * date,title,amount
     *
     * gera 3 colunas com vírgula, então a vírgula será escolhida.
     */
    private char detectarDelimitador(List<String> linhas) throws Exception {
        char melhorDelimitador = ',';
        int maiorQuantidadeColunas = 1;

        char[] candidatos = new char[]{',', ';', '\t'};

        for (char candidato : candidatos) {
            List<String[]> registros = lerRegistrosCsv(List.of(linhas.get(0)), candidato);
            int quantidadeColunas = registros.get(0).length;

            if (quantidadeColunas > maiorQuantidadeColunas) {
                maiorQuantidadeColunas = quantidadeColunas;
                melhorDelimitador = candidato;
            }
        }

        return melhorDelimitador;
    }

    /**
     * Lê as linhas com OpenCSV.
     *
     * Isso é importante porque o Nubank usa valores assim:
     * "7,00"
     *
     * Se você usar split(",") simples, esse valor quebra em duas colunas:
     * "7"
     * "00"
     *
     * Com OpenCSV, o conteúdo entre aspas é preservado corretamente.
     */
    private List<String[]> lerRegistrosCsv(List<String> linhas, char delimitador) throws Exception {
        String conteudo = String.join("\n", linhas);
        List<String[]> registros = new ArrayList<>();

        try (CSVReader reader = new CSVReaderBuilder(new StringReader(conteudo))
                .withCSVParser(new CSVParserBuilder()
                        .withSeparator(delimitador)
                        .build())
                .build()) {

            String[] linha;

            while ((linha = reader.readNext()) != null) {
                registros.add(linha);
            }
        }

        return registros;
    }

    /**
     * Verifica se o CSV tem o layout real do Nubank.
     *
     * Layout esperado:
     * date,title,amount
     */
    private boolean ehCabecalhoNubank(String[] cabecalho) {
        return encontrarIndiceDaColuna(cabecalho, "date") >= 0
                && encontrarIndiceDaColuna(cabecalho, "title") >= 0
                && encontrarIndiceDaColuna(cabecalho, "amount") >= 0;
    }

    /**
     * Processa o CSV real do Nubank.
     *
     * Regras:
     * - date vira data da transação;
     * - title vira descrição;
     * - amount vira valor;
     * - amount positivo vira DEBITO, porque representa compra/gasto;
     * - amount negativo vira CREDITO, porque representa pagamento recebido;
     * - valor é salvo sempre positivo na entidade.
     */
    private ResultadoParser processarCsvNubank(List<String[]> registros, Conta conta) {
        ResultadoParser resultado = new ResultadoParser();

        List<Transacao> transacoes = new ArrayList<>();
        int totalLinhas = 0;
        int linhasInvalidas = 0;

        String[] cabecalho = registros.getFirst();

        int indiceData = encontrarIndiceDaColuna(cabecalho, "date");
        int indiceDescricao = encontrarIndiceDaColuna(cabecalho, "title");
        int indiceValor = encontrarIndiceDaColuna(cabecalho, "amount");

        if (indiceData < 0 || indiceDescricao < 0 || indiceValor < 0) {
            throw new IllegalArgumentException("Cabeçalho Nubank inválido");
        }

        for (int i = 1; i < registros.size(); i++) {
            String[] linha = registros.get(i);

            totalLinhas++;

            if (!possuiIndicesObrigatorios(linha, indiceData, indiceDescricao, indiceValor)) {
                linhasInvalidas++;
                continue;
            }

            LocalDate data = parsearData(linha[indiceData]);
            String descricao = limparDescricao(linha[indiceDescricao]);
            BigDecimal valor = parsearValor(linha[indiceValor]);

            if (data == null || valor == null || valor.compareTo(BigDecimal.ZERO) == 0) {
                linhasInvalidas++;
                continue;
            }

            TipoTransacao tipo = resolverTipoNubank(valor);

            Transacao transacao = criarTransacao(
                    conta,
                    data,
                    descricao,
                    valor.abs(),
                    tipo
            );

            transacoes.add(transacao);
        }

        resultado.setTransacoes(transacoes);
        resultado.setTotalLinhas(totalLinhas);
        resultado.setLinhasInvalidas(linhasInvalidas);

        return resultado;
    }

    /**
     * Encontra o índice de uma coluna dentro do cabeçalho.
     *
     * Exemplo:
     * date,title,amount
     *
     * encontrarIndiceDaColuna(cabecalho, "amount")
     * retorna 2.
     */
    private int encontrarIndiceDaColuna(String[] cabecalho, String nomeProcurado) {
        for (int i = 0; i < cabecalho.length; i++) {
            String colunaNormalizada = normalizarTexto(cabecalho[i]);

            if (colunaNormalizada.equals(nomeProcurado)) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Normaliza textos para comparação.
     *
     * Usado principalmente para comparar cabeçalhos, como:
     * "﻿date" -> "date"
     * " Date " -> "date"
     * "Descrição" -> "descricao"
     */
    private String normalizarTexto(String texto) {
        if (texto == null) {
            return "";
        }

        String normalizado = texto
                .replace(BOM, "")
                .replace("\"", "")
                .trim()
                .toLowerCase();

        normalizado = Normalizer
                .normalize(normalizado, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        return normalizado;
    }

    /**
     * Garante que a linha tem as posições que serão acessadas.
     *
     * Isso evita ArrayIndexOutOfBoundsException quando uma linha vier quebrada
     * ou com colunas faltando.
     */
    private boolean possuiIndicesObrigatorios(String[] linha, int... indices) {
        for (int indice : indices) {
            if (indice < 0 || indice >= linha.length) {
                return false;
            }
        }

        return true;
    }

    /**
     * Limpa a descrição da transação.
     *
     * Se a descrição vier vazia, usa um fallback para evitar salvar descrição nula
     * ou em branco.
     */
    private String limparDescricao(String descricao) {
        if (descricao == null) {
            return "Transação importada";
        }

        String descricaoLimpa = descricao
                .replace(BOM, "")
                .replace("\"", "")
                .trim();

        if (descricaoLimpa.isBlank()) {
            return "Transação importada";
        }

        return descricaoLimpa;
    }

    /**
     * Regra específica do extrato Nubank de cartão.
     *
     * No arquivo enviado:
     * - compras aparecem com amount positivo;
     * - pagamento recebido aparece com amount negativo.
     */
    private TipoTransacao resolverTipoNubank(BigDecimal valor) {
        if (valor.compareTo(BigDecimal.ZERO) > 0) {
            return TipoTransacao.DEBITO;
        }

        return TipoTransacao.CREDITO;
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

    /**
     * Centraliza a criação da Transacao.
     *
     * Assim, tanto o fluxo Nubank quanto o fluxo legado criam transações com
     * a mesma estrutura mínima.
     */
    private Transacao criarTransacao(
            Conta conta,
            LocalDate data,
            String descricao,
            BigDecimal valor,
            TipoTransacao tipo
    ) {
        Transacao transacao = new Transacao();

        transacao.setConta(conta);
        transacao.setData(data);
        transacao.setDescricao(descricao);
        transacao.setValor(valor);
        transacao.setTipo(tipo);
        transacao.setCategorizada(false);

        return transacao;
    }
}