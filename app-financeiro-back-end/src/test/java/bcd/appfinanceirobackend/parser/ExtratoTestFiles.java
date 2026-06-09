package bcd.appfinanceirobackend.parser;

import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Helpers compartilhados para montar arquivos de extrato em testes de parser.
 * Centraliza a leitura de fixtures (src/test/resources/extratos/...) e a criação
 * de arquivos a partir de conteúdo inline, evitando duplicação entre os testes
 * específicos de cada formato e o teste de contrato comum.
 */
final class ExtratoTestFiles {

    private ExtratoTestFiles() {
    }

    /**
     * Carrega uma fixture de extratos/{caminhoRelativo} como MultipartFile.
     * O nome do arquivo é derivado do caminho para preservar a extensão usada
     * na detecção dos parsers.
     */
    static MockMultipartFile daFixture(String caminhoRelativo, String contentType) throws IOException {
        ClassPathResource recurso = new ClassPathResource("extratos/" + caminhoRelativo);
        byte[] bytes = recurso.getInputStream().readAllBytes();
        String nome = caminhoRelativo.substring(caminhoRelativo.lastIndexOf('/') + 1);
        return new MockMultipartFile("arquivo", nome, contentType, bytes);
    }

    /** Monta um MultipartFile a partir de conteúdo inline. */
    static MockMultipartFile deTexto(String nomeArquivo, String conteudo, String contentType) {
        return new MockMultipartFile(
                "arquivo",
                nomeArquivo,
                contentType,
                conteudo.getBytes(StandardCharsets.UTF_8)
        );
    }
}
