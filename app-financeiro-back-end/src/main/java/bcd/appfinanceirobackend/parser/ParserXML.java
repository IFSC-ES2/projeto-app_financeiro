package bcd.appfinanceirobackend.parser;

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

/**
 * Parser para extratos bancários no formato XML genérico (não NF-e).
 *
 * Este parser trata extratos exportados por bancos no formato XML próprio,
 * diferenciando-se do ParserNFe que trata o padrão SEFAZ.
 *
 * A detecção é feita pela extensão .xml combinada com a ausência da tag
 * raiz <nfeProc> ou <NFe>, que caracteriza uma NF-e.
 *
 * Estrutura XML esperada:
 * <extrato>
 *   <transacao>
 *     <data>2024-01-15</data>
 *     <descricao>Supermercado</descricao>
 *     <valor>-150.00</valor>
 *     <tipo>DEBITO</tipo>           <!-- opcional -->
 *   </transacao>
 * </extrato>
 *
 * Tags alternativas também aceitas: <lancamento>, <movimento>, <entry>
 */
@Component
@Order(2)
public class ParserXML implements ParserExtrato {

    private static final List<String> TAGS_TRANSACAO = List.of(
            "transacao", "lancamento", "movimento", "entry"
    );

    private static final List<DateTimeFormatter> FORMATADORES = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy")
    );

    @Override
    public boolean aceita(MultipartFile arquivo) {
        String nome = arquivo.getOriginalFilename();
        if (nome == null || !nome.toLowerCase().endsWith(".xml")) return false;

        // Lê os primeiros bytes para verificar se NÃO é uma NF-e
        try (InputStream is = arquivo.getInputStream()) {
            byte[] preview = is.readNBytes(500);
            String inicio = new String(preview).toLowerCase();
            // NF-e é tratada pelo ParserNFe — este parser rejeita
            return !inicio.contains("nfeproc") && !inicio.contains("<nfe");
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public ResultadoParser parsear(MultipartFile arquivo, Conta conta) {
        List<Transacao> transacoes = new ArrayList<>();
        int linhasInvalidas = 0;
        int totalLinhas = 0;

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // Desativa DTD externo para evitar XXE
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(arquivo.getInputStream());
            doc.getDocumentElement().normalize();

            // Tenta cada tag conhecida até encontrar elementos
            NodeList nos = null;
            for (String tag : TAGS_TRANSACAO) {
                nos = doc.getElementsByTagName(tag);
                if (nos.getLength() > 0) break;
            }

            if (nos == null || nos.getLength() == 0) {
                throw new RuntimeException("Nenhuma tag de transação reconhecida no XML. " +
                        "Tags suportadas: " + TAGS_TRANSACAO);
            }

            for (int i = 0; i < nos.getLength(); i++) {
                Element el = (Element) nos.item(i);

                LocalDate data = parsearData(texto(el, "data"));
                if (data == null) {
                    linhasInvalidas++;
                    totalLinhas++;
                    continue;
                }

                BigDecimal valor = parsearValor(texto(el, "valor"));
                if (valor == null) {
                    linhasInvalidas++;
                    totalLinhas++;
                    continue;
                }

                String descricao = texto(el, "descricao");
                TipoTransacao tipo = parsearTipo(texto(el, "tipo"), valor);

                Transacao transacao = new Transacao();
                transacao.setConta(conta);
                transacao.setData(data);
                transacao.setDescricao(descricao);
                transacao.setValor(valor.abs());
                transacao.setTipo(tipo);
                transacao.setCategorizada(false);

                transacoes.add(transacao);
            }

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar arquivo XML: " + e.getMessage(), e);
        }
        ResultadoParser resultado = new ResultadoParser();
        resultado.setLinhasInvalidas(linhasInvalidas);
        resultado.setTransacoes(transacoes);
        resultado.setTotalLinhas(totalLinhas);
        return resultado;
    }

    /** Extrai o texto de uma tag filha pelo nome, retornando "" se ausente. */
    private String texto(Element elemento, String tag) {
        NodeList nos = elemento.getElementsByTagName(tag);
        if (nos.getLength() == 0) return "";
        return nos.item(0).getTextContent().trim();
    }

    private LocalDate parsearData(String valor) {
        for (DateTimeFormatter fmt : FORMATADORES) {
            try {
                return LocalDate.parse(valor, fmt);
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

    private TipoTransacao parsearTipo(String tipoStr, BigDecimal valor) {
        if (valor.compareTo(BigDecimal.ZERO) < 0) return TipoTransacao.DEBITO;
        try {
            return TipoTransacao.valueOf(tipoStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return TipoTransacao.CREDITO;
        }
    }
}