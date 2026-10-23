package bcd.appfinanceirobackend.parser;

import bcd.appfinanceirobackend.exception.ErroEstruturalImportacaoException;
import bcd.appfinanceirobackend.model.Conta;
import bcd.appfinanceirobackend.model.Transacao;
import bcd.appfinanceirobackend.model.enums.TipoTransacao;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Component
@Order(2)
public class ParserXML implements ParserExtrato {

    private static final List<String> TAGS_TRANSACAO = List.of(
            "transacao",
            "lancamento",
            "movimento",
            "entry"
    );

    private static final List<DateTimeFormatter> FORMATADORES = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy")
    );

    @Override
    public boolean aceita(MultipartFile arquivo) {
        String nome = arquivo.getOriginalFilename();

        if (
                nome == null ||
                !nome.toLowerCase().endsWith(".xml")
        ) {
            return false;
        }

        try (InputStream is = arquivo.getInputStream()) {
            byte[] preview = is.readNBytes(2000);

            String inicio = new String(preview)
                    .toLowerCase();

            return !inicio.contains("nfeproc")
                    && !inicio.contains("<nfe");

        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public ResultadoParser parsear(
            MultipartFile arquivo,
            Conta conta
    ) {
        ResultadoParser resultado = new ResultadoParser();

        int totalLinhas = 0;

        try {
            DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance();

            factory.setFeature(
                    "http://apache.org/xml/features/disallow-doctype-decl",
                    true
            );

            factory.setNamespaceAware(true);

            DocumentBuilder builder =
                    factory.newDocumentBuilder();

            Document doc =
                    builder.parse(
                            arquivo.getInputStream()
                    );

            doc.getDocumentElement()
                    .normalize();

            List<Element> elementos =
                    new ArrayList<>();

            for (String tag : TAGS_TRANSACAO) {
                NodeList nos =
                        doc.getElementsByTagNameNS(
                                "*",
                                tag
                        );

                for (int i = 0; i < nos.getLength(); i++) {
                    elementos.add(
                            (Element) nos.item(i)
                    );
                }

                nos =
                        doc.getElementsByTagNameNS(
                                "*",
                                tag.toUpperCase()
                        );

                for (int i = 0; i < nos.getLength(); i++) {
                    elementos.add(
                            (Element) nos.item(i)
                    );
                }
            }

            if (elementos.isEmpty()) {
                throw new ErroEstruturalImportacaoException(
                        null,
                        "estrutura",
                        "Nenhuma tag de transação reconhecida no XML. Tags suportadas: " +
                                TAGS_TRANSACAO
                );
            }

            for (int i = 0; i < elementos.size(); i++) {
                Element elemento = elementos.get(i);

                int numeroRegistro = i + 1;

                totalLinhas++;

                LocalDate data = parsearData(
                        texto(
                                elemento,
                                "data"
                        )
                );

                if (data == null) {
                    resultado.adicionarErro(
                            numeroRegistro,
                            "data",
                            "Campo data ausente ou em formato inválido."
                    );

                    continue;
                }

                BigDecimal valor = parsearValor(
                        texto(
                                elemento,
                                "valor"
                        )
                );

                if (valor == null) {
                    resultado.adicionarErro(
                            numeroRegistro,
                            "valor",
                            "Campo valor ausente ou inválido."
                    );

                    continue;
                }

                String descricao = texto(
                        elemento,
                        "descricao"
                );

                TipoTransacao tipo = parsearTipo(
                        texto(
                                elemento,
                                "tipo"
                        ),
                        valor
                );

                Transacao transacao = new Transacao();

                transacao.setConta(conta);
                transacao.setData(data);
                transacao.setDescricao(descricao);
                transacao.setValor(valor.abs());
                transacao.setTipo(tipo);
                transacao.setCategorizada(false);

                resultado.adicionarTransacao(
                        transacao,
                        numeroRegistro
                );
            }

        } catch (ErroEstruturalImportacaoException e) {
            throw e;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Erro ao processar arquivo XML",
                    e
            );
        }

        resultado.setTotalLinhas(totalLinhas);

        return resultado;
    }

    private String texto(
            Element elemento,
            String tag
    ) {
        NodeList todos =
                elemento.getElementsByTagNameNS(
                        "*",
                        "*"
                );

        for (int i = 0; i < todos.getLength(); i++) {
            if (
                    todos.item(i).getLocalName() != null &&
                    todos.item(i)
                            .getLocalName()
                            .equalsIgnoreCase(tag)
            ) {
                return todos.item(i)
                        .getTextContent()
                        .trim();
            }
        }

        NodeList todosSemNamespace =
                elemento.getElementsByTagName("*");

        for (
                int i = 0;
                i < todosSemNamespace.getLength();
                i++
        ) {
            if (
                    todosSemNamespace
                            .item(i)
                            .getNodeName()
                            .equalsIgnoreCase(tag)
            ) {
                return todosSemNamespace
                        .item(i)
                        .getTextContent()
                        .trim();
            }
        }

        return "";
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
                        valor,
                        formatter
                );

            } catch (DateTimeParseException ignored) {
            }
        }

        if (valor.length() >= 10) {
            try {
                return LocalDate.parse(
                        valor.substring(0, 10)
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

    private TipoTransacao parsearTipo(
            String tipoStr,
            BigDecimal valor
    ) {
        if (valor.compareTo(BigDecimal.ZERO) < 0) {
            return TipoTransacao.DEBITO;
        }

        try {
            return TipoTransacao.valueOf(
                    tipoStr.toUpperCase()
            );

        } catch (IllegalArgumentException e) {
            return TipoTransacao.CREDITO;
        }
    }
}