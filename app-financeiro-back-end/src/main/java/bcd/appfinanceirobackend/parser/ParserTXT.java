package bcd.appfinanceirobackend.parser;

import bcd.appfinanceirobackend.model.Conta;
import bcd.appfinanceirobackend.model.Transacao;
import bcd.appfinanceirobackend.model.enums.TipoTransacao;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser para extratos bancários no formato TXT (texto simples / posicional).
 *
 * Muitos bancos brasileiros exportam extratos em TXT com layout posicional ou
 * colunar separado por espaços. Este parser adota duas estratégias em sequência:
 *
 * 1. Detecção por regex de padrão data + valor na linha (estratégia flexível).
 *    Funciona para a maioria dos formatos colunares e semi-estruturados.
 *
 * 2. Fallback para split por múltiplos espaços, cobrindo formatos tabulares
 *    onde os campos são alinhados com espaçamento fixo.
 *
 * Padrão reconhecido pela estratégia 1 (regex):
 *   [data] [qualquer texto] [valor]
 *   Exemplo: "15/01/2024   Supermercado Extra         -150,00"
 *
 * Padrão reconhecido pela estratégia 2 (split):
 *   data<TAB>descricao<TAB>valor
 *   Exemplo: "15/01/2024\tSupermercado\t-150,00"
 *
 * Linhas que não encaixam em nenhum padrão são ignoradas silenciosamente.
 */
@Component
public class ParserTXT implements ParserExtrato {

    /**
     * Regex que captura:
     * Grupo 1: data (dd/MM/yyyy, yyyy-MM-dd ou dd-MM-yyyy)
     * Grupo 2: descrição (qualquer texto no meio)
     * Grupo 3: valor monetário (com ou sem sinal, vírgula ou ponto decimal)
     */
    private static final Pattern PADRAO_LINHA = Pattern.compile(
            "^(\\d{2}[/\\-]\\d{2}[/\\-]\\d{2,4}|\\d{4}[/\\-]\\d{2}[/\\-]\\d{2})" // data
                    + "\\s+(.+?)\\s+"                                                // descricao
                    + "([+-]?\\d{1,3}(?:[.,]\\d{3})*(?:[.,]\\d{2})?)\\s*$"         // valor
    );

    private static final List<DateTimeFormatter> FORMATADORES = List.of(
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd/MM/yy")
    );

    @Override
    public boolean aceita(MultipartFile arquivo) {
        String nome = arquivo.getOriginalFilename();
        return nome != null && nome.toLowerCase().endsWith(".txt");
    }

    @Override
    public ResultadoParser parsear(MultipartFile arquivo, Conta conta) {
        List<Transacao> transacoes = new ArrayList<>();
        int totalLinhas = 0;
        int linhasInvalidas = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(arquivo.getInputStream(), StandardCharsets.UTF_8))) {

            String linha;
            while ((linha = reader.readLine()) != null) {
                linha = linha.trim();
                if (linha.isBlank()) {
                    totalLinhas++;
                    linhasInvalidas++;
                    continue;
                };

                Transacao transacao = parsearPorRegex(linha, conta);

                // Fallback: tenta split por múltiplos espaços ou tabulação
                if (transacao == null) {
                    transacao = parsearPorSplit(linha, conta);
                }

                if (transacao != null) {
                    transacoes.add(transacao);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar arquivo TXT: " + e.getMessage(), e);
        }

        return transacoes;
    }

    /** Estratégia 1: extrai campos via regex. */
    private Transacao parsearPorRegex(String linha, Conta conta) {
        Matcher matcher = PADRAO_LINHA.matcher(linha);
        if (!matcher.matches()) return null;

        LocalDate data = parsearData(matcher.group(1));
        if (data == null) return null;

        String descricao = matcher.group(2).trim();
        BigDecimal valor = parsearValor(matcher.group(3));
        if (valor == null) return null;

        return montarTransacao(conta, data, descricao, valor);
    }

    /** Estratégia 2: split por tabulação ou múltiplos espaços. */
    private Transacao parsearPorSplit(String linha, Conta conta) {
        // Divide por TAB ou por 2+ espaços consecutivos
        String[] partes = linha.split("\t|\\s{2,}");

        if (partes.length < 3) return null;

        LocalDate data = parsearData(partes[0].trim());
        if (data == null) return null;

        // Valor é sempre o último campo; descrição é tudo no meio
        BigDecimal valor = parsearValor(partes[partes.length - 1].trim());
        if (valor == null) return null;

        StringBuilder desc = new StringBuilder();
        for (int i = 1; i < partes.length - 1; i++) {
            if (!partes[i].isBlank()) {
                if (desc.length() > 0) desc.append(" ");
                desc.append(partes[i].trim());
            }
        }
        String descricao = desc.toString().isBlank() ? "Importado via TXT" : desc.toString();

        return montarTransacao(conta, data, descricao, valor);
    }

    private Transacao montarTransacao(Conta conta, LocalDate data,
                                      String descricao, BigDecimal valor) {
        TipoTransacao tipo = valor.compareTo(BigDecimal.ZERO) < 0
                ? TipoTransacao.DEBITO
                : TipoTransacao.CREDITO;

        Transacao transacao = new Transacao();
        transacao.setConta(conta);
        transacao.setData(data);
        transacao.setDescricao(descricao);
        transacao.setValor(valor.abs());
        transacao.setTipo(tipo);
        transacao.setCategorizada(false);
        return transacao;
    }

    private LocalDate parsearData(String valor) {
        for (DateTimeFormatter fmt : FORMATADORES) {
            try {
                return LocalDate.parse(valor.trim(), fmt);
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }

    private BigDecimal parsearValor(String valor) {
        if (valor == null || valor.isBlank()) return null;
        try {
            String normalizado = valor.replace("R$", "").replace(" ", "").trim();
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
}