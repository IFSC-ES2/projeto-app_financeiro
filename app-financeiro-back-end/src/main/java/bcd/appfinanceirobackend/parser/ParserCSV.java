package bcd.appfinanceirobackend.parser;

import bcd.appfinanceirobackend.exception.ErroEstruturalImportacaoException;
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

    /**
     * Mantém a linha física original mesmo que linhas vazias sejam ignoradas.
     */
    private record LinhaFisica(
            int numeroLinha,
            String conteudo
    ) {
    }

    private record RegistroCsv(
            int numeroLinha,
            String[] colunas
    ) {
    }

    @Override
    public boolean aceita(MultipartFile arquivo) {
        String nome = arquivo.getOriginalFilename();

        return nome != null &&
                nome.toLowerCase().endsWith(".csv");
    }

    @Override
    public ResultadoParser parsear(
            MultipartFile arquivo,
            Conta conta
    ) {
        try {
            String conteudo = removerBom(
                    lerConteudoDoArquivo(arquivo)
            );

            List<LinhaFisica> linhas =
                    quebrarEmLinhasUteis(conteudo);

            if (linhas.isEmpty()) {
                ResultadoParser resultado =
                        new ResultadoParser();

                resultado.adicionarErro(
                        null,
                        "estrutura",
                        "O arquivo CSV não possui registros para importação."
                );

                return resultado;
            }

            char delimitador =
                    detectarDelimitador(
                            linhas.getFirst().conteudo()
                    );

            List<RegistroCsv> registros =
                    lerRegistrosCsv(
                            linhas,
                            delimitador
                    );

            if (registros.isEmpty()) {
                ResultadoParser resultado =
                        new ResultadoParser();

                resultado.adicionarErro(
                        null,
                        "estrutura",
                        "Não foi possível identificar registros no arquivo CSV."
                );

                return resultado;
            }

            RegistroCsv primeiroRegistro =
                    registros.getFirst();

            String[] cabecalho =
                    primeiroRegistro.colunas();

            if (ehCabecalhoNubankConta(cabecalho)) {
                return processarCsvNubankConta(
                        registros,
                        conta
                );
            }

            /*
             * O campo Identificador é específico do layout Nubank.
             * Se ele aparece, mas as demais colunas obrigatórias não,
             * tratamos como cabeçalho Nubank estruturalmente inválido.
             *
             * Isso não interfere com o cabeçalho legado:
             * data,descricao,valor,tipo
             */
            if (pareceCabecalhoNubankConta(cabecalho)) {
                throw new ErroEstruturalImportacaoException(
                        primeiroRegistro.numeroLinha(),
                        "cabecalho",
                        "Cabeçalho Nubank inválido. Verifique as colunas obrigatórias Data, Valor, Identificador e Descrição."
                );
            }

            return processarCsvLegado(
                    registros,
                    conta
            );

        } catch (ErroEstruturalImportacaoException e) {
            throw e;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Erro ao processar arquivo CSV",
                    e
            );
        }
    }

    private String lerConteudoDoArquivo(
            MultipartFile arquivo
    ) throws IOException {
        byte[] bytes = arquivo.getBytes();

        String textoUtf8 =
                tentarConverter(
                        bytes,
                        StandardCharsets.UTF_8
                );

        if (textoUtf8 != null) {
            return textoUtf8;
        }

        String textoWindows1252 =
                tentarConverter(
                        bytes,
                        Charset.forName(
                                "Windows-1252"
                        )
                );

        if (textoWindows1252 != null) {
            return textoWindows1252;
        }

        return new String(
                bytes,
                StandardCharsets.ISO_8859_1
        );
    }

    private String tentarConverter(
            byte[] bytes,
            Charset charset
    ) {
        CharsetDecoder decoder =
                charset.newDecoder()
                        .onMalformedInput(
                                CodingErrorAction.REPORT
                        )
                        .onUnmappableCharacter(
                                CodingErrorAction.REPORT
                        );

        try {
            return decoder
                    .decode(
                            ByteBuffer.wrap(bytes)
                    )
                    .toString();

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
     * Linhas vazias continuam sendo ignoradas como registros,
     * mas o numeroLinha preserva a posição física original.
     */
    private List<LinhaFisica> quebrarEmLinhasUteis(
            String conteudo
    ) {
        List<LinhaFisica> linhas =
                new ArrayList<>();

        String normalizado =
                conteudo
                        .replace("\r\n", "\n")
                        .replace("\r", "\n");

        String[] linhasFisicas =
                normalizado.split("\n", -1);

        for (
                int i = 0;
                i < linhasFisicas.length;
                i++
        ) {
            String linha =
                    linhasFisicas[i].trim();

            if (linha.isBlank()) {
                continue;
            }

            linhas.add(
                    new LinhaFisica(
                            i + 1,
                            linha
                    )
            );
        }

        return linhas;
    }

    private char detectarDelimitador(
            String primeiraLinha
    ) throws Exception {
        char melhorDelimitador = ',';
        int maiorQuantidadeColunas = 1;

        char[] candidatos =
                new char[]{',', ';', '\t'};

        for (char candidato : candidatos) {
            String[] registro =
                    lerRegistroCsv(
                            primeiraLinha,
                            candidato
                    );

            if (registro == null) {
                continue;
            }

            if (
                    registro.length >
                            maiorQuantidadeColunas
            ) {
                maiorQuantidadeColunas =
                        registro.length;

                melhorDelimitador =
                        candidato;
            }
        }

        return melhorDelimitador;
    }

    private List<RegistroCsv> lerRegistrosCsv(
            List<LinhaFisica> linhas,
            char delimitador
    ) throws Exception {
        List<RegistroCsv> registros =
                new ArrayList<>();

        for (LinhaFisica linha : linhas) {
            String[] colunas =
                    lerRegistroCsv(
                            linha.conteudo(),
                            delimitador
                    );

            if (colunas == null) {
                continue;
            }

            registros.add(
                    new RegistroCsv(
                            linha.numeroLinha(),
                            colunas
                    )
            );
        }

        return registros;
    }

    /**
     * Usa OpenCSV para preservar campos entre aspas e descrições
     * contendo o próprio delimitador.
     */
    private String[] lerRegistroCsv(
            String linha,
            char delimitador
    ) throws Exception {
        try (
                CSVReader reader =
                        new CSVReaderBuilder(
                                new StringReader(linha)
                        )
                                .withCSVParser(
                                        new CSVParserBuilder()
                                                .withSeparator(
                                                        delimitador
                                                )
                                                .build()
                                )
                                .build()
        ) {
            return reader.readNext();
        }
    }

    private boolean ehCabecalhoNubankConta(
            String[] cabecalho
    ) {
        return encontrarIndiceDaColuna(
                cabecalho,
                "data"
        ) >= 0
                && encontrarIndiceDaColuna(
                cabecalho,
                "valor"
        ) >= 0
                && encontrarIndiceDaColuna(
                cabecalho,
                "identificador"
        ) >= 0
                && encontrarIndiceDaColuna(
                cabecalho,
                "descricao"
        ) >= 0;
    }

    private boolean pareceCabecalhoNubankConta(
            String[] cabecalho
    ) {
        return encontrarIndiceDaColuna(
                cabecalho,
                "identificador"
        ) >= 0;
    }

    private ResultadoParser processarCsvNubankConta(
            List<RegistroCsv> registros,
            Conta conta
    ) {
        ResultadoParser resultado =
                new ResultadoParser();

        RegistroCsv registroCabecalho =
                registros.getFirst();

        String[] cabecalho =
                registroCabecalho.colunas();

        int indiceData =
                encontrarIndiceDaColuna(
                        cabecalho,
                        "data"
                );

        int indiceValor =
                encontrarIndiceDaColuna(
                        cabecalho,
                        "valor"
                );

        int indiceDescricao =
                encontrarIndiceDaColuna(
                        cabecalho,
                        "descricao"
                );

        if (
                indiceData < 0 ||
                indiceValor < 0 ||
                indiceDescricao < 0
        ) {
            throw new ErroEstruturalImportacaoException(
                    registroCabecalho.numeroLinha(),
                    "cabecalho",
                    "Cabeçalho Nubank inválido. Verifique as colunas obrigatórias Data, Valor, Identificador e Descrição."
            );
        }

        int totalLinhas = 0;

        for (
                int i = 1;
                i < registros.size();
                i++
        ) {
            RegistroCsv registro =
                    registros.get(i);

            String[] linha =
                    registro.colunas();

            int numeroRegistro =
                    registro.numeroLinha();

            totalLinhas++;

            if (
                    !possuiIndicesObrigatorios(
                            linha,
                            indiceData,
                            indiceValor,
                            indiceDescricao
                    )
            ) {
                resultado.adicionarErro(
                        numeroRegistro,
                        "estrutura",
                        "Registro não possui todas as colunas obrigatórias."
                );

                continue;
            }

            LocalDate data =
                    parsearData(
                            linha[indiceData]
                    );

            if (data == null) {
                resultado.adicionarErro(
                        numeroRegistro,
                        "data",
                        "Data inválida. Verifique o formato informado."
                );

                continue;
            }

            BigDecimal valor =
                    parsearValor(
                            linha[indiceValor]
                    );

            if (valor == null) {
                resultado.adicionarErro(
                        numeroRegistro,
                        "valor",
                        "Valor monetário inválido."
                );

                continue;
            }

            if (
                    valor.compareTo(
                            BigDecimal.ZERO
                    ) == 0
            ) {
                resultado.adicionarErro(
                        numeroRegistro,
                        "valor",
                        "O valor da transação não pode ser zero."
                );

                continue;
            }

            String descricao =
                    limparDescricao(
                            linha[
                                    indiceDescricao
                                    ]
                    );

            Transacao transacao =
                    criarTransacao(
                            conta,
                            data,
                            descricao,
                            valor.abs(),
                            resolverTipoNubankConta(
                                    valor
                            )
                    );

            resultado.adicionarTransacao(
                    transacao,
                    numeroRegistro
            );
        }

        resultado.setTotalLinhas(
                totalLinhas
        );

        return resultado;
    }

    private ResultadoParser processarCsvLegado(
            List<RegistroCsv> registros,
            Conta conta
    ) {
        ResultadoParser resultado =
                new ResultadoParser();

        int totalLinhas = 0;

        for (RegistroCsv registro : registros) {
            String[] linha =
                    registro.colunas();

            int numeroRegistro =
                    registro.numeroLinha();

            totalLinhas++;

            if (linha.length != 4) {
                resultado.adicionarErro(
                        numeroRegistro,
                        "estrutura",
                        "Registro deve possuir data, descrição, valor e tipo."
                );

                continue;
            }

            LocalDate data =
                    parsearData(
                            linha[0]
                    );

            if (data == null) {
                resultado.adicionarErro(
                        numeroRegistro,
                        "data",
                        "Data inválida. Verifique o formato informado."
                );

                continue;
            }

            BigDecimal valor =
                    parsearValor(
                            linha[2]
                    );

            if (valor == null) {
                resultado.adicionarErro(
                        numeroRegistro,
                        "valor",
                        "Valor monetário inválido."
                );

                continue;
            }

            if (
                    valor.compareTo(
                            BigDecimal.ZERO
                    ) == 0
            ) {
                resultado.adicionarErro(
                        numeroRegistro,
                        "valor",
                        "O valor da transação não pode ser zero."
                );

                continue;
            }

            String descricao =
                    limparDescricao(
                            linha[1]
                    );

            TipoTransacao tipo =
                    parsearTipoLegado(
                            linha[3],
                            valor
                    );

            Transacao transacao =
                    criarTransacao(
                            conta,
                            data,
                            descricao,
                            valor.abs(),
                            tipo
                    );

            resultado.adicionarTransacao(
                    transacao,
                    numeroRegistro
            );
        }

        resultado.setTotalLinhas(
                totalLinhas
        );

        return resultado;
    }

    private int encontrarIndiceDaColuna(
            String[] cabecalho,
            String nomeProcurado
    ) {
        for (
                int i = 0;
                i < cabecalho.length;
                i++
        ) {
            String normalizado =
                    normalizarTexto(
                            cabecalho[i]
                    );

            if (
                    normalizado.equals(
                            nomeProcurado
                    )
            ) {
                return i;
            }
        }

        return -1;
    }

    private boolean possuiIndicesObrigatorios(
            String[] linha,
            int... indices
    ) {
        for (int indice : indices) {
            if (
                    indice < 0 ||
                    indice >= linha.length
            ) {
                return false;
            }
        }

        return true;
    }

    private String normalizarTexto(
            String texto
    ) {
        if (texto == null) {
            return "";
        }

        String normalizado =
                texto
                        .replace(BOM, "")
                        .replace("\"", "")
                        .trim()
                        .toLowerCase();

        return Normalizer
                .normalize(
                        normalizado,
                        Normalizer.Form.NFD
                )
                .replaceAll(
                        "\\p{M}",
                        ""
                );
    }

    private String limparDescricao(
            String descricao
    ) {
        if (descricao == null) {
            return "Transação importada";
        }

        String descricaoLimpa =
                descricao
                        .replace(BOM, "")
                        .replace("\"", "")
                        .trim();

        return descricaoLimpa.isBlank()
                ? "Transação importada"
                : descricaoLimpa;
    }

    private LocalDate parsearData(
            String valor
    ) {
        if (valor == null) {
            return null;
        }

        String valorLimpo =
                valor
                        .replace(BOM, "")
                        .trim();

        for (
                DateTimeFormatter formatter :
                FORMATADORES
        ) {
            try {
                return LocalDate.parse(
                        valorLimpo,
                        formatter
                );

            } catch (
                    DateTimeParseException ignored
            ) {
            }
        }

        return null;
    }

    private BigDecimal parsearValor(
            String valor
    ) {
        if (valor == null) {
            return null;
        }

        try {
            String normalizado =
                    valor
                            .replace(BOM, "")
                            .replace("\"", "")
                            .replace("R$", "")
                            .trim();

            boolean negativo = false;

            if (
                    normalizado.startsWith("(") &&
                    normalizado.endsWith(")")
            ) {
                negativo = true;

                normalizado =
                        normalizado
                                .substring(
                                        1,
                                        normalizado.length() - 1
                                )
                                .trim();
            }

            if (
                    normalizado.startsWith("-")
            ) {
                negativo = true;

                normalizado =
                        normalizado
                                .substring(1)
                                .trim();
            }

            if (
                    normalizado.endsWith("-")
            ) {
                negativo = true;

                normalizado =
                        normalizado
                                .substring(
                                        0,
                                        normalizado.length() - 1
                                )
                                .trim();
            }

            normalizado =
                    normalizado.replace(
                            " ",
                            ""
                    );

            if (normalizado.isBlank()) {
                return null;
            }

            if (
                    normalizado.contains(",") &&
                    normalizado.contains(".")
            ) {
                normalizado =
                        normalizado
                                .replace(".", "")
                                .replace(",", ".");

            } else if (
                    normalizado.contains(",")
            ) {
                normalizado =
                        normalizado.replace(
                                ",",
                                "."
                        );
            }

            BigDecimal resultado =
                    new BigDecimal(
                            normalizado
                    );

            return negativo
                    ? resultado.negate()
                    : resultado;

        } catch (
                NumberFormatException e
        ) {
            return null;
        }
    }

    private TipoTransacao resolverTipoNubankConta(
            BigDecimal valor
    ) {
        return valor.compareTo(
                BigDecimal.ZERO
        ) > 0
                ? TipoTransacao.CREDITO
                : TipoTransacao.DEBITO;
    }

    private TipoTransacao parsearTipoLegado(
            String tipoTexto,
            BigDecimal valor
    ) {
        if (
                valor.compareTo(
                        BigDecimal.ZERO
                ) < 0
        ) {
            return TipoTransacao.DEBITO;
        }

        try {
            return TipoTransacao.valueOf(
                    normalizarTexto(
                            tipoTexto
                    ).toUpperCase()
            );

        } catch (
                IllegalArgumentException e
        ) {
            return TipoTransacao.CREDITO;
        }
    }

    private Transacao criarTransacao(
            Conta conta,
            LocalDate data,
            String descricao,
            BigDecimal valor,
            TipoTransacao tipo
    ) {
        Transacao transacao =
                new Transacao();

        transacao.setConta(conta);
        transacao.setData(data);
        transacao.setDescricao(descricao);
        transacao.setValor(valor);
        transacao.setTipo(tipo);
        transacao.setCategorizada(false);

        return transacao;
    }
}