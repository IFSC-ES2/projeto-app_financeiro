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
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Component
public class ParserCSV implements ParserExtrato {

    private static final String BOM = "\uFEFF";

    private static final List<DateTimeFormatter> FORMATADORES = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy")
    );

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

            char delimitador = detectarDelimitador(linhas.getFirst());

            List<String[]> registros = lerRegistrosCsv(linhas, delimitador);

            if (registros.isEmpty()) {
                return new ResultadoParser();
            }

            String[] cabecalho = registros.getFirst();

            if (ehCabecalhoNubankConta(cabecalho)) {
                return processarCsvNubankConta(registros, conta);
            }

            return processarCsvLegado(registros, conta);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar arquivo CSV: " + e.getMessage(), e);
        }
    }

    /**
     * Lê o conteúdo do arquivo tentando UTF-8 primeiro.
     *
     * Se UTF-8 não funcionar, tenta Windows-1252.
     * Se Windows-1252 também não funcionar, usa ISO-8859-1 como fallback final.
     *
     * Isso ajuda em extratos reais, porque arquivos exportados por bancos podem
     * vir com acentos em diferentes encodings.
     */
    private String lerConteudoDoArquivo(MultipartFile arquivo) throws IOException {
        byte[] bytes = arquivo.getBytes();

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
     * O CharsetDecoder é usado para detectar erro real de encoding.
     * Usar new String(bytes, UTF_8) diretamente pode mascarar caracteres inválidos.
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

    /**
     * Remove o BOM do início do arquivo, quando existir.
     *
     * Sem isso, um cabeçalho como "Data" pode ser lido como "﻿Data",
     * fazendo a comparação de nomes de coluna falhar.
     */
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
     * Quebra o conteúdo do arquivo em linhas úteis.
     *
     * Linhas em branco são ignoradas para não serem contabilizadas como falhas
     * falsas no parser.
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
     * Detecta o delimitador mais provável usando a primeira linha útil.
     *
     * Para o extrato bancário Nubank:
     * Data,Valor,Identificador,Descrição
     *
     * O delimitador detectado será vírgula.
     */
    private char detectarDelimitador(String primeiraLinha) throws Exception {
        char melhorDelimitador = ',';
        int maiorQuantidadeColunas = 1;

        char[] candidatos = new char[]{',', ';', '\t'};

        for (char candidato : candidatos) {
            List<String[]> registros = lerRegistrosCsv(List.of(primeiraLinha), candidato);

            if (registros.isEmpty()) {
                continue;
            }

            int quantidadeColunas = registros.getFirst().length;

            if (quantidadeColunas > maiorQuantidadeColunas) {
                maiorQuantidadeColunas = quantidadeColunas;
                melhorDelimitador = candidato;
            }
        }

        return melhorDelimitador;
    }

    /**
     * Lê as linhas usando OpenCSV.
     *
     * Isso evita erro com campos que possuem vírgula dentro de aspas.
     * Mesmo que o extrato bancário Nubank não dependa muito disso, é mais seguro
     * manter a leitura robusta para CSV real.
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
     * Verifica se o cabeçalho corresponde ao formato do extrato bancário Nubank.
     *
     * Para reconhecer esse layout, são obrigatórias as colunas:
     * Data, Valor, Identificador e Descrição.
     *
     * O Identificador é utilizado apenas para reconhecer o formato do arquivo
     * neste momento e não é persistido na entidade Transacao.
     */
    private boolean ehCabecalhoNubankConta(String[] cabecalho) {
        return encontrarIndiceDaColuna(cabecalho, "data") >= 0
                && encontrarIndiceDaColuna(cabecalho, "valor") >= 0
                && encontrarIndiceDaColuna(cabecalho, "identificador") >= 0
                && encontrarIndiceDaColuna(cabecalho, "descricao") >= 0;
    }

    /**
     * Processa o extrato bancário real do Nubank.
     *
     * Mapeamento:
     * Data          -> data da transação
     * Valor         -> valor e tipo da transação
     * Descrição     -> descrição da transação
     * Identificador -> usado apenas para identificar o layout e ignorado no domínio
     *
     * Regra do extrato bancário:
     * Valor positivo -> CREDITO
     * Valor negativo -> DEBITO
     * Valor salvo    -> sempre positivo
     */
    private ResultadoParser processarCsvNubankConta(List<String[]> registros, Conta conta) {
        ResultadoParser resultado = new ResultadoParser();

        List<Transacao> transacoes = new ArrayList<>();
        int totalLinhas = 0;
        int linhasInvalidas = 0;

        String[] cabecalho = registros.getFirst();

        int indiceData = encontrarIndiceDaColuna(cabecalho, "data");
        int indiceValor = encontrarIndiceDaColuna(cabecalho, "valor");
        int indiceDescricao = encontrarIndiceDaColuna(cabecalho, "descricao");

        if (indiceData < 0 || indiceValor < 0 || indiceDescricao < 0) {
            throw new IllegalArgumentException("Cabeçalho Nubank Conta inválido");
        }

        for (int i = 1; i < registros.size(); i++) {
            String[] linha = registros.get(i);

            totalLinhas++;

            if (!possuiIndicesObrigatorios(linha, indiceData, indiceValor, indiceDescricao)) {
                linhasInvalidas++;
                continue;
            }

            LocalDate data = parsearData(linha[indiceData]);
            BigDecimal valor = parsearValor(linha[indiceValor]);
            String descricao = limparDescricao(linha[indiceDescricao]);

            if (data == null || valor == null || valor.compareTo(BigDecimal.ZERO) == 0) {
                linhasInvalidas++;
                continue;
            }

            TipoTransacao tipo = resolverTipoNubankConta(valor);

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
     * Mantém o comportamento legado do CSV já aceito pelo sistema.
     *
     * Formato legado:
     * data,descricao,valor,tipo
     *
     * A manutenção desse fallback evita quebrar testes e arquivos antigos.
     */
    private ResultadoParser processarCsvLegado(List<String[]> registros, Conta conta) {
        ResultadoParser resultado = new ResultadoParser();

        List<Transacao> transacoes = new ArrayList<>();
        int totalLinhas = 0;
        int linhasInvalidas = 0;

        for (String[] linha : registros) {
            totalLinhas++;

            if (linha.length != 4) {
                linhasInvalidas++;
                continue;
            }

            LocalDate data = parsearData(linha[0]);
            BigDecimal valor = parsearValor(linha[2]);

            if (data == null || valor == null || valor.compareTo(BigDecimal.ZERO) == 0) {
                linhasInvalidas++;
                continue;
            }

            String descricao = limparDescricao(linha[1]);
            TipoTransacao tipo = parsearTipoLegado(linha[3], valor);

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
     * Encontra o índice de uma coluna no cabeçalho.
     *
     * A comparação usa normalização para aceitar:
     * "Descrição"
     * "descricao"
     * " Descrição "
     * "﻿Descrição"
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
     * Verifica se a linha tem todos os índices que serão acessados.
     *
     * Isso evita ArrayIndexOutOfBoundsException quando o CSV possui linha quebrada
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
     * Normaliza texto para comparação.
     *
     * Exemplo:
     * "Descrição" vira "descricao"
     * "﻿Data" vira "data"
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

        return Normalizer
                .normalize(normalizado, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }

    /**
     * Limpa a descrição que será salva na transação.
     *
     * Se a descrição vier vazia, usa um texto padrão para evitar descrição nula.
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
     * Converte texto em LocalDate.
     *
     * O extrato bancário Nubank usa dd/MM/yyyy.
     * O CSV legado também pode usar yyyy-MM-dd ou dd-MM-yyyy.
     */
    private LocalDate parsearData(String valor) {
        if (valor == null) {
            return null;
        }

        String valorLimpo = valor
                .replace(BOM, "")
                .trim();

        for (DateTimeFormatter formatter : FORMATADORES) {
            try {
                return LocalDate.parse(valorLimpo, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }

        return null;
    }

    /**
     * Converte texto monetário em BigDecimal.
     *
     * Casos aceitos:
     * 80        -> 80
     * -80       -> -80
     * -8.91     -> -8.91
     * -494.1    -> -494.1
     * 1.234,56  -> 1234.56
     * 1234,56   -> 1234.56
     * R$ 100,00 -> 100.00
     *
     * Importante:
     * se o valor contém apenas ponto, o ponto é tratado como decimal.
     * Isso evita transformar -8.91 em -891.
     */
    private BigDecimal parsearValor(String valor) {
        if (valor == null) {
            return null;
        }

        try {
            String normalizado = valor
                    .replace(BOM, "")
                    .replace("\"", "")
                    .replace("R$", "")
                    .trim();

            boolean negativo = false;

            if (normalizado.startsWith("(") && normalizado.endsWith(")")) {
                negativo = true;
                normalizado = normalizado.substring(1, normalizado.length() - 1).trim();
            }

            if (normalizado.startsWith("-")) {
                negativo = true;
                normalizado = normalizado.substring(1).trim();
            }

            if (normalizado.endsWith("-")) {
                negativo = true;
                normalizado = normalizado.substring(0, normalizado.length() - 1).trim();
            }

            normalizado = normalizado.replace(" ", "");

            if (normalizado.isBlank()) {
                return null;
            }

            if (normalizado.contains(",") && normalizado.contains(".")) {
                normalizado = normalizado
                        .replace(".", "")
                        .replace(",", ".");
            } else if (normalizado.contains(",")) {
                normalizado = normalizado.replace(",", ".");
            }

            BigDecimal valorDecimal = new BigDecimal(normalizado);

            if (negativo) {
                return valorDecimal.negate();
            }

            return valorDecimal;

        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Regra do extrato bancário Nubank.
     *
     * Valor positivo significa entrada de dinheiro na conta.
     * Valor negativo significa saída de dinheiro da conta.
     */
    private TipoTransacao resolverTipoNubankConta(BigDecimal valor) {
        if (valor.compareTo(BigDecimal.ZERO) > 0) {
            return TipoTransacao.CREDITO;
        }

        return TipoTransacao.DEBITO;
    }

    /**
     * Regra do CSV legado.
     *
     * Valores negativos continuam sendo DEBITO.
     * Para valores positivos, tenta usar o campo tipo informado no CSV.
     * Se o tipo não for reconhecido, faz fallback para CREDITO.
     */
    private TipoTransacao parsearTipoLegado(String tipoTexto, BigDecimal valor) {
        if (valor.compareTo(BigDecimal.ZERO) < 0) {
            return TipoTransacao.DEBITO;
        }

        try {
            return TipoTransacao.valueOf(normalizarTexto(tipoTexto).toUpperCase());
        } catch (IllegalArgumentException e) {
            return TipoTransacao.CREDITO;
        }
    }

    /**
     * Centraliza a criação da Transacao.
     *
     * O parser define apenas os dados extraídos do arquivo.
     * Categoria e Importacao continuam sendo responsabilidades do ImportacaoService.
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