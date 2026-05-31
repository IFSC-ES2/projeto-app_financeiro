package bcd.appfinanceirobackend.repository.spec;

import bcd.appfinanceirobackend.model.Transacao;
import bcd.appfinanceirobackend.model.enums.TipoTransacao;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Filtros componíveis para a listagem de transações. Cada filtro opcional
 * retorna {@code null} quando o parâmetro não é informado, sendo ignorado
 * ao compor a Specification com {@code and(...)}.
 */
public final class TransacaoSpecs {

    private TransacaoSpecs() {
    }

    public static Specification<Transacao> daConta(UUID usuarioId) {
        return (root, query, cb) -> cb.equal(root.get("conta").get("usuario").get("id"), usuarioId);
    }

    public static Specification<Transacao> dataDe(LocalDate dataInicio) {
        if (dataInicio == null) return null;
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("data"), dataInicio);
    }

    public static Specification<Transacao> dataAte(LocalDate dataFim) {
        if (dataFim == null) return null;
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("data"), dataFim);
    }

    public static Specification<Transacao> daCategoria(UUID categoriaId) {
        if (categoriaId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("categoria").get("id"), categoriaId);
    }

    public static Specification<Transacao> doTipo(TipoTransacao tipo) {
        if (tipo == null) return null;
        return (root, query, cb) -> cb.equal(root.get("tipo"), tipo);
    }
}
