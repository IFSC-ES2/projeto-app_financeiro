package bcd.appfinanceirobackend.dto.comum;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Envelope de resposta paginada. Evita serializar {@code Page<T>} direto
 * (instável/deprecado no Spring Boot 3.3+) expondo um contrato estável.
 */
public record PaginaDTO<T>(
        List<T> conteudo,
        int pagina,
        int tamanho,
        long totalElementos,
        int totalPaginas,
        boolean primeira,
        boolean ultima
) {
    public static <E, T> PaginaDTO<T> de(Page<E> page, Function<E, T> mapper) {
        return new PaginaDTO<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}
