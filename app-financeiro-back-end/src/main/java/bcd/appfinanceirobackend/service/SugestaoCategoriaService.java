package bcd.appfinanceirobackend.service;

import bcd.appfinanceirobackend.model.Categoria;
import bcd.appfinanceirobackend.repository.CategoriaRepository;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class SugestaoCategoriaService {

    private final CategoriaRepository categoriaRepository;

    public SugestaoCategoriaService(CategoriaRepository categoriaRepository){
        this.categoriaRepository = categoriaRepository;
    }

    private static final Map<String, List<String>> PALAVRAS_CHAVE = Map.of(
            "Alimentação",  List.of("mercado", "supermercado", "padaria", "restaurante", "lanchonete", "ifood", "rappi"),
            "Transporte",   List.of("uber", "99", "combustivel", "gasolina", "onibus", "metro", "estacionamento"),
            "Saúde",        List.of("farmacia", "hospital", "clinica", "medico", "laboratorio", "drogaria"),
            "Lazer",        List.of("netflix", "spotify", "cinema", "teatro", "steam", "jogos"),
            "Habitação",    List.of("aluguel", "condominio", "agua", "luz", "energia", "gas"),
            "Serviços",     List.of("internet", "telefone", "celular", "tim", "claro", "vivo"),
            "Manutenção",   List.of("oficina", "reparo", "conserto", "ferragem", "material")
    );

    public Categoria sugerirCategoria (String descricao) {
        if(descricao == null || descricao.isBlank()){
            return null;
        }
        String descricaoMinuscula = descricao.toLowerCase();
        String descricaoNormalizada = normalizarTexto(descricaoMinuscula);
        for (Map.Entry<String, List<String>> entry: PALAVRAS_CHAVE.entrySet()) {
            boolean encontrou = entry.getValue().stream().anyMatch( palavraChave ->
                    contemPalavra(descricaoNormalizada, palavraChave));
            if(encontrou) {
                return categoriaRepository.findByNomeAndPadraoTrue(entry.getKey()).orElse(null);
            }
        }
        return null;
    }


    private String normalizarTexto(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .trim();
    }

    private boolean contemPalavra(String descricao, String palavraChave) {
        return descricao.matches(".*\\b" + Pattern.quote(palavraChave) + "\\b.*");
    }

}
