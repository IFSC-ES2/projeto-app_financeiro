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
import java.util.ArrayList;
import java.util.List;

/**
 * Parser para Nota Fiscal Eletrônica (NF-e) no padrão SEFAZ (XML).
 *
 * Detecta arquivos NF-e pela presença das tags <nfeProc> ou <NFe> no início
 * do conteúdo. Suporta NF-e com ou sem o envelope <nfeProc>.
 *
 * Estratégia de extração:
 * - Uma NF-e representa uma única compra, gerada como uma transação do tipo DEBITO
 *   cujo valor total é o campo <vNF> (valor total da nota)
 * - A descrição é composta pelo CNPJ do emitente + razão social (xNome)
 * - A data é extraída do campo <dhEmi> (data de emissão)
 * - Os itens individuais (<det>) são ignorados por padrão para não inflar o extrato;
 *   se o projeto evoluir para categorização por item, esse comportamento pode ser
 *   alterado via configuração
 *
 * Tags SEFAZ utilizadas:
 *   <ide>/<dhEmi>     → data de emissão
 *   <emit>/<xNome>    → razão social do emitente
 *   <emit>/<CNPJ>     → CNPJ do emitente
 *   <total>/<ICMSTot>/<vNF> → valor total da nota
 */
@Component
@Order(1)
public class ParserNFe implements ParserExtrato {

    private static final DateTimeFormatter FORMATO_NFE =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
    private static final DateTimeFormatter FORMATO_NFE_SEM_TZ =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Override
    public boolean aceita(MultipartFile arquivo) {
        String nome = arquivo.getOriginalFilename();
        if (nome == null) return false;

        String nomeLower = nome.toLowerCase();
        // Aceita .xml e .nfe
        if (!nomeLower.endsWith(".xml") && !nomeLower.endsWith(".nfe")) return false;

        // Confirma pelo conteúdo que é uma NF-e
        try (InputStream is = arquivo.getInputStream()) {
            byte[] preview = is.readNBytes(500);
            String inicio = new String(preview).toLowerCase();
            return inicio.contains("nfeproc") || inicio.contains("<nfe");
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
            // Namespace-aware para processar NF-e corretamente
            factory.setNamespaceAware(true);
            // Proteção contra XXE
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(arquivo.getInputStream());
            doc.getDocumentElement().normalize();

            // Extrai data de emissão
            LocalDate dataEmissao = extrairDataEmissao(doc);
            if (dataEmissao == null) {
                throw new RuntimeException("Campo <dhEmi> não encontrado ou em formato inválido na NF-e.");
            }

            // Extrai emitente
            String nomeEmitente = primeiroTexto(doc, "xNome");
            String cnpjEmitente = primeiroTexto(doc, "CNPJ");
            String descricao = montarDescricao(nomeEmitente, cnpjEmitente);

            // Itera sobre os itens da nota (<det>) gerando uma transação por produto
            NodeList itens = doc.getElementsByTagName("det");

            if (itens.getLength() == 0) {
                throw new RuntimeException("Nenhum item <det> encontrado na NF-e.");
            }

            for (int i = 0; i < itens.getLength(); i++) {
                Element item = (Element) itens.item(i);

                String descricaoProduto = primeiroTextoElemento(item, "xProd");
                BigDecimal valorProduto = parsearBigDecimal(primeiroTextoElemento(item, "vProd"));

                if (valorProduto == null || valorProduto.compareTo(BigDecimal.ZERO) <= 0) {
                    linhasInvalidas++;
                    totalLinhas++;
                    continue;
                }

                Transacao transacao = new Transacao();
                transacao.setConta(conta);
                transacao.setData(dataEmissao);
                transacao.setDescricao(descricaoProduto.isBlank() ? "Item NF-e #" + (i + 1) : descricaoProduto);
                transacao.setValor(valorProduto);
                transacao.setTipo(TipoTransacao.DEBITO);
                transacao.setCategorizada(false);

                transacoes.add(transacao);
            }

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar NF-e: " + e.getMessage(), e);
        }

        ResultadoParser resultado = new ResultadoParser();
        resultado.setTotalLinhas(totalLinhas);
        resultado.setTransacoes(transacoes);
        resultado.setLinhasInvalidas(linhasInvalidas);
        return resultado;
    }

    /**
     * Extrai a data de emissão do campo <dhEmi>.
     * O padrão SEFAZ usa ISO 8601 com timezone (2024-01-15T10:30:00-03:00).
     * Fallback para formato sem timezone para NF-e malformadas.
     */
    private LocalDate extrairDataEmissao(Document doc) {
        String dhEmi = primeiroTexto(doc, "dhEmi");
        if (dhEmi == null || dhEmi.isBlank()) return null;

        try {
            return LocalDate.parse(dhEmi, FORMATO_NFE);
        } catch (Exception e1) {
            try {
                return LocalDate.parse(dhEmi, FORMATO_NFE_SEM_TZ);
            } catch (Exception e2) {
                // Último fallback: tenta extrair só a data (primeiros 10 chars)
                try {
                    return LocalDate.parse(dhEmi.substring(0, 10));
                } catch (Exception e3) {
                    return null;
                }
            }
        }
    }

    /** Extrai o texto de uma tag filha dentro de um Element específico. */
    private String primeiroTextoElemento(Element elemento, String tag) {
        NodeList nos = elemento.getElementsByTagName(tag);
        if (nos.getLength() == 0) return "";
        return nos.item(0).getTextContent().trim();
    }
    
    /** Busca o texto do primeiro elemento com a tag informada no documento. */
    private String primeiroTexto(Document doc, String tag) {
        NodeList nos = doc.getElementsByTagName(tag);
        if (nos.getLength() == 0) return "";
        return nos.item(0).getTextContent().trim();
    }

    private BigDecimal parsearBigDecimal(String valor) {
        if (valor == null || valor.isBlank()) return null;
        try {
            return new BigDecimal(valor.replace(",", "."));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String montarDescricao(String nome, String cnpj) {
        if (nome == null || nome.isBlank()) return "NF-e importada";
        if (cnpj == null || cnpj.isBlank()) return nome;
        return nome + " (CNPJ: " + cnpj + ")";
    }
}