package bcd.appfinanceirobackend.service;

import bcd.appfinanceirobackend.model.Categoria;
import bcd.appfinanceirobackend.model.RegraCategorizacao;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.repository.RegraCategorizacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Sugere a categoria de uma transação a partir da descrição, usando a tabela regra_categorizacao:
 * regras globais (base versionada por migration) e regras aprendidas com a categorização manual
 * de cada usuário. As do usuário têm prioridade; entre regras do mesmo grupo vence o termo mais longo.
 */
@Service
public class SugestaoCategoriaService {

    // Termos curtos precisam ser palavra inteira ("uber" não pega "uberlandia"); a partir de 5 letras
    // valem como início de palavra ("supermercado" pega "supermercados", "autoposto" pega "autopostocentral").
    static final int TAMANHO_MINIMO_PREFIXO = 5;

    private static final int TAMANHO_MAXIMO_TERMO = 120;

    private static final int TAMANHO_MINIMO_TERMO_APRENDIDO = 3;

    private static final Pattern DIA_MES_NO_INICIO = Pattern.compile("^\\d{2}/\\d{2}\\s+");

    // Do mais longo para o mais curto; empate resolvido pelo texto, para a sugestão não depender da ordem do banco
    private static final Comparator<RegraCategorizacao> MAIS_ESPECIFICA_PRIMEIRO =
            Comparator.comparingInt((RegraCategorizacao regra) -> regra.getTermo().length())
                    .reversed()
                    .thenComparing(RegraCategorizacao::getTermo);

    private final RegraCategorizacaoRepository regraRepository;

    public SugestaoCategoriaService(RegraCategorizacaoRepository regraRepository) {
        this.regraRepository = regraRepository;
    }

    /**
     * Carrega as regras uma vez e devolve uma função de sugestão, para uma importação
     * com muitas linhas não consultar o banco a cada transação.
     */
    public Function<String, Categoria> sugestorPara(UUID usuarioId) {
        List<RegraCategorizacao> regras = regraRepository.findGlobaisEDoUsuario(usuarioId);

        List<RegraCategorizacao> doUsuario = regras.stream()
                .filter(regra -> regra.getUsuario() != null)
                .sorted(MAIS_ESPECIFICA_PRIMEIRO)
                .toList();

        List<RegraCategorizacao> globais = regras.stream()
                .filter(regra -> regra.getUsuario() == null)
                .sorted(MAIS_ESPECIFICA_PRIMEIRO)
                .toList();

        return descricao -> {
            if (descricao == null || descricao.isBlank()) {
                return null;
            }

            String texto = normalizar(descricao);
            Categoria categoria = primeiraQueCasa(doUsuario, texto);

            return categoria != null ? categoria : primeiraQueCasa(globais, texto);
        };
    }

    public Categoria sugerirCategoria(String descricao, UUID usuarioId) {
        return sugestorPara(usuarioId).apply(descricao);
    }

    /**
     * Guarda (ou atualiza) a regra "estabelecimento -> categoria" do usuário, para as próximas
     * importações já virem categorizadas. A regra é só dele: nomes de pessoas em Pix não vão para a base global.
     */
    @Transactional
    public void aprenderCategorizacao(String descricao, Categoria categoria, Usuario usuario) {
        String termo = extrairTermo(descricao);

        if (termo == null || categoria == null || usuario == null || usuario.getId() == null) {
            return;
        }

        RegraCategorizacao regra = regraRepository
                .findByUsuarioIdAndTermo(usuario.getId(), termo)
                .orElseGet(() -> {
                    RegraCategorizacao nova = new RegraCategorizacao();
                    nova.setTermo(termo);
                    nova.setUsuario(usuario);
                    return nova;
                });

        regra.setCategoria(categoria);
        regraRepository.save(regra);
    }

    /**
     * O estabelecimento costuma ser o último trecho da descrição importada
     * ("Debito visa electron brasil - 26/11 + natural" -> "+ natural").
     */
    static String extrairTermo(String descricao) {
        if (descricao == null || descricao.isBlank()) {
            return null;
        }

        int separador = descricao.lastIndexOf(" - ");
        String trecho = separador >= 0 ? descricao.substring(separador + 3) : descricao;
        String termo = DIA_MES_NO_INICIO.matcher(normalizar(trecho)).replaceFirst("").trim();

        if (termo.length() < TAMANHO_MINIMO_TERMO_APRENDIDO || termo.equals("transacao importada")) {
            return null;
        }

        return termo.length() > TAMANHO_MAXIMO_TERMO ? termo.substring(0, TAMANHO_MAXIMO_TERMO) : termo;
    }

    static boolean casa(String texto, String termo) {
        int inicio = texto.indexOf(termo);

        while (inicio >= 0) {
            int fim = inicio + termo.length();
            boolean comecaPalavra = inicio == 0 || !Character.isLetterOrDigit(texto.charAt(inicio - 1));
            boolean terminaBem = termo.length() >= TAMANHO_MINIMO_PREFIXO
                    || fim == texto.length()
                    || !Character.isLetter(texto.charAt(fim));

            if (comecaPalavra && terminaBem) {
                return true;
            }

            inicio = texto.indexOf(termo, inicio + 1);
        }

        return false;
    }

    static String normalizar(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .replaceAll("\\s+", " ")
                .trim();
    }

    private Categoria primeiraQueCasa(List<RegraCategorizacao> regras, String texto) {
        for (RegraCategorizacao regra : regras) {
            if (casa(texto, regra.getTermo())) {
                return regra.getCategoria();
            }
        }

        return null;
    }
}
