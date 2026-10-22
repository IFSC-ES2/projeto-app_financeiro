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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ParserTXT implements ParserExtrato {

    private static final Pattern PADRAO_LINHA = Pattern.compile(
            "^(\\d{2}[/\\-]\\d{2}[/\\-]\\d{2,4}|\\d{4}[/\\-]\\d{2}[/\\-]\\d{2})"
                    + "\\s+(.+?)\\s+"
                    + "(R\\$\\s*)?([+-]?\\d{1,3}(?:[.,]\\d{3})*(?:[.,]\\d{2})?)\\s*$"
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

        return nome != null
                && nome.toLowerCase().endsWith(".txt");
    }

    @Override
    public ResultadoParser parsear(
            MultipartFile arquivo,
            Conta conta
    ) {
        ResultadoParser resultado = new ResultadoParser();

        int totalLinhas = 0;
        int numeroLinhaFisica = 0;

        try (
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                arquivo.getInputStream(),
                                StandardCharsets.UTF_8
                        )
                )
        ) {
            String linha;

            while ((linha = reader.readLine()) != null) {
                numeroLinhaFisica++;

                String linhaLimpa = linha.trim();

                if (linhaLimpa.isBlank()) {
                    continue;
                }

                totalLinhas++;

                Transacao transacao = parsearPorRegex(
                        linhaLimpa,
                        conta
                );

                if (transacao == null) {
                    transacao = parsearPorSplit(
                            linhaLimpa,
                            conta
                    );
                }

                if (transacao != null) {
                    resultado.adicionarTransacao(
                            transacao,
                            numeroLinhaFisica
                    );

                } else {
                    registrarErroLinha(
                            resultado,
                            numeroLinhaFisica,
                            linhaLimpa
                    );
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(
                    "Erro ao processar arquivo TXT",
                    e
            );
        }

        resultado.setTotalLinhas(totalLinhas);

        return resultado;
    }

    private Transacao parsearPorRegex(
            String linha,
            Conta conta
    ) {
        Matcher matcher = PADRAO_LINHA.matcher(linha);

        if (!matcher.matches()) {
            return null;
        }

        LocalDate data = parsearData(
                matcher.group(1)
        );

        if (data == null) {
            return null;
        }

        BigDecimal valor = parsearValor(
                matcher.group(4)
        );

        if (valor == null) {
            return null;
        }

        return montarTransacao(
                conta,
                data,
                matcher.group(2).trim(),
                valor
        );
    }

    private Transacao parsearPorSplit(
            String linha,
            Conta conta
    ) {
        String[] partes = linha.split(
                "\\t|\\s{2,}"
        );

        if (partes.length < 2) {
            partes = linha.split("\\s+");
        }

        if (partes.length < 2) {
            return null;
        }

        LocalDate data = parsearData(
                partes[0].trim()
        );

        if (data == null) {
            return null;
        }

        /*
         * Compatibilidade com o comportamento anterior:
         * 15/01/2024 -50,00
         */
        if (partes.length == 2) {
            BigDecimal valor = parsearValor(
                    partes[1]
            );

            if (valor == null) {
                return null;
            }

            return montarTransacao(
                    conta,
                    data,
                    "Importado via TXT",
                    valor
            );
        }

        String ultimoCampo =
                partes[partes.length - 1].trim();

        String penultimoCampo =
                partes[partes.length - 2].trim();

        int limiteDescricao =
                partes.length - 1;

        if (penultimoCampo.equalsIgnoreCase("R$")) {
            ultimoCampo = penultimoCampo + ultimoCampo;
            limiteDescricao = partes.length - 2;
        }

        BigDecimal valor = parsearValor(
                ultimoCampo
        );

        if (valor == null) {
            return null;
        }

        StringBuilder descricao = new StringBuilder();

        for (int i = 1; i < limiteDescricao; i++) {
            if (partes[i].isBlank()) {
                continue;
            }

            if (descricao.length() > 0) {
                descricao.append(" ");
            }

            descricao.append(
                    partes[i].trim()
            );
        }

        String descricaoFinal =
                descricao.toString().isBlank()
                        ? "Importado via TXT"
                        : descricao.toString();

        return montarTransacao(
                conta,
                data,
                descricaoFinal,
                valor
        );
    }

    private void registrarErroLinha(
            ResultadoParser resultado,
            int numeroRegistro,
            String linha
    ) {
        String[] partes = linha.split(
                "\\t|\\s{2,}"
        );

        if (partes.length < 2) {
            partes = linha.split("\\s+");
        }

        if (partes.length < 2) {
            resultado.adicionarErro(
                    numeroRegistro,
                    "estrutura",
                    "Linha não corresponde ao formato esperado de data, descrição e valor."
            );

            return;
        }

        if (parsearData(partes[0].trim()) == null) {
            resultado.adicionarErro(
                    numeroRegistro,
                    "data",
                    "Data ausente ou em formato inválido."
            );

            return;
        }

        String ultimoCampo =
                partes[partes.length - 1].trim();

        if (
                partes.length >= 3 &&
                partes[partes.length - 2]
                        .trim()
                        .equalsIgnoreCase("R$")
        ) {
            ultimoCampo = "R$" + ultimoCampo;
        }

        if (parsearValor(ultimoCampo) == null) {
            resultado.adicionarErro(
                    numeroRegistro,
                    "valor",
                    "Valor monetário ausente ou inválido."
            );

            return;
        }

        resultado.adicionarErro(
                numeroRegistro,
                "estrutura",
                "Linha não corresponde ao formato esperado para uma transação."
        );
    }

    private Transacao montarTransacao(
            Conta conta,
            LocalDate data,
            String descricao,
            BigDecimal valor
    ) {
        TipoTransacao tipo =
                valor.compareTo(BigDecimal.ZERO) < 0
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
        if (
                valor == null ||
                valor.isBlank()
        ) {
            return null;
        }

        for (DateTimeFormatter formatter : FORMATADORES) {
            try {
                return LocalDate.parse(
                        valor.trim(),
                        formatter
                );

            } catch (DateTimeParseException ignored) {
            }
        }

        return null;
    }

    private BigDecimal parsearValor(String valor) {
        if (
                valor == null ||
                valor.isBlank()
        ) {
            return null;
        }

        try {
            String normalizado = valor
                    .replace("R$", "")
                    .replace(" ", "")
                    .trim();

            if (
                    normalizado.contains(",") &&
                    normalizado.contains(".")
            ) {
                normalizado = normalizado
                        .replace(".", "")
                        .replace(",", ".");

            } else {
                normalizado = normalizado.replace(",", ".");
            }

            return new BigDecimal(normalizado);

        } catch (NumberFormatException e) {
            return null;
        }
    }
}