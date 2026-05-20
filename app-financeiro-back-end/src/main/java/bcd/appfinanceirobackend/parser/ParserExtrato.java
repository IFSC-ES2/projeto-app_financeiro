package bcd.appfinanceirobackend.parser;

import bcd.appfinanceirobackend.model.Conta;
import bcd.appfinanceirobackend.model.Transacao;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Interface Strategy para parsers de extrato.
 * Cada implementação é responsável por detectar se aceita o arquivo
 * e por converter seu conteúdo em uma lista de transações.
 */
public interface ParserExtrato {

    /**
     * Verifica se este parser é capaz de processar o arquivo informado.
     * A detecção deve ser baseada na extensão e/ou no conteúdo do arquivo,
     * nunca apenas no Content-Type enviado pelo cliente.
     *
     * @param arquivo arquivo recebido via upload
     * @return true se este parser aceita processar o arquivo
     */
    boolean aceita(MultipartFile arquivo);

    /**
     * Converte o conteúdo do arquivo em uma lista de transações prontas
     * para persistência. As transações retornadas NÃO devem ter importacao,
     * conta ou categorizada definidos — isso é responsabilidade do ImportacaoService.
     *
     * @param arquivo arquivo recebido via upload
     * @param conta   conta à qual as transações serão vinculadas
     * @return um objeto da classe resultadoParser
     * @throws RuntimeException se o arquivo estiver corrompido ou em formato inválido
     */
    ResultadoParser parsear(MultipartFile arquivo, Conta conta);
}