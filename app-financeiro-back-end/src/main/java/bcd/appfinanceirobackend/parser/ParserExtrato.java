package bcd.appfinanceirobackend.parser;

import bcd.appfinanceirobackend.model.Conta;
import org.springframework.web.multipart.MultipartFile;

/**
 * Contrato comum para os parsers de importação de extratos.
 *
 * Esta interface representa a Strategy usada pelo módulo de importação.
 * Cada implementação deve reconhecer um formato específico de arquivo
 * e converter os registros válidos em transações de domínio.
 *
 * Responsabilidades do parser:
 * - identificar se aceita ou não o arquivo recebido;
 * - interpretar o conteúdo do arquivo no formato suportado;
 * - criar objetos Transacao somente para registros válidos;
 * - preencher conta, data, descrição, valor, tipo e categorizada=false;
 * - contabilizar registros inválidos em ResultadoParser.linhasInvalidas;
 * - retornar o total de registros avaliados em ResultadoParser.totalLinhas.
 *
 * Responsabilidades do ImportacaoService:
 * - validar usuário autenticado e propriedade da conta;
 * - validar upload vazio ou nome inválido;
 * - selecionar o parser compatível;
 * - criar e atualizar a entidade Importacao;
 * - categorizar as transações importadas;
 * - associar a Importacao em cada Transacao;
 * - persistir as transações válidas;
 * - contabilizar sucessos e falhas de persistência/categorização.
 *
 * O parser não deve:
 * - persistir dados;
 * - acessar repositories;
 * - definir Importacao;
 * - definir Categoria;
 * - assumir o usuário autenticado;
 * - alterar status da importação.
 */
public interface ParserExtrato {

    /**
     * Indica se este parser é compatível com o arquivo recebido.
     *
     * Deve retornar true somente quando o parser reconhecer o formato esperado.
     * A decisão pode considerar extensão, nome do arquivo e, quando necessário,
     * uma prévia do conteúdo. O Content-Type enviado pelo cliente não deve ser
     * usado como única fonte de decisão, pois pode ser genérico ou incorreto.
     *
     * Exemplos:
     * - ParserCSV aceita arquivos .csv;
     * - ParserTXT aceita arquivos .txt;
     * - ParserNFe aceita XML/NFE quando identifica tags de NF-e;
     * - ParserXML aceita XML genérico, mas deve rejeitar NF-e.
     *
     * Em caso de erro ao ler o arquivo para detecção, deve retornar false.
     *
     * @param arquivo arquivo recebido via upload
     * @return true se este parser reconhece e aceita processar o arquivo
     */
    boolean aceita(MultipartFile arquivo);

    /**
     * Converte o conteúdo do arquivo em transações válidas.
     *
     * Em caso de sucesso total ou parcial, deve retornar ResultadoParser.
     * Sucesso parcial significa que alguns registros foram convertidos em
     * transações e outros foram ignorados por inconsistência. Nesses casos,
     * as transações válidas devem ser retornadas normalmente e os registros
     * inválidos devem ser somados em linhasInvalidas.
     *
     * O parser deve lançar RuntimeException apenas quando o arquivo inteiro
     * estiver corrompido, ilegível ou estruturalmente incompatível com o formato
     * esperado, impossibilitando o processamento.
     *
     * As transações retornadas devem conter:
     * - conta recebida por parâmetro;
     * - data;
     * - descrição;
     * - valor normalizado, preferencialmente positivo;
     * - tipo da transação;
     * - categorizada=false.
     *
     * As transações retornadas não devem conter:
     * - importacao;
     * - categoria definida automaticamente;
     * - qualquer informação de persistência.
     *
     * @param arquivo arquivo recebido via upload
     * @param conta conta à qual as transações serão vinculadas
     * @return resultado contendo transações válidas, total de registros avaliados e quantidade de inválidos
     * @throws RuntimeException quando o arquivo não puder ser processado estruturalmente
     */
    ResultadoParser parsear(MultipartFile arquivo, Conta conta);
}
