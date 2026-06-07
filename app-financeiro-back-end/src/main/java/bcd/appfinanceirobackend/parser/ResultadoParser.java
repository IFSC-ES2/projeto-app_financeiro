package bcd.appfinanceirobackend.parser;

import bcd.appfinanceirobackend.model.Transacao;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Resultado padronizado produzido por qualquer implementação de ParserExtrato.
 *
 * Este objeto permite que o ImportacaoService trate todos os parsers da mesma
 * forma, independentemente do formato original do arquivo.
 *
 * Regras do contrato:
 * - transacoes contém apenas registros convertidos com sucesso;
 * - linhasInvalidas representa registros encontrados, mas descartados por erro;
 * - totalLinhas representa a quantidade de registros avaliados pelo parser.
 *
 * Em um sucesso parcial:
 * - transacoes pode conter uma ou mais transações válidas;
 * - linhasInvalidas deve ser maior que zero;
 * - o parser não deve lançar exceção apenas por existirem registros inválidos.
 *
 * Exceções devem ser reservadas para falhas estruturais do arquivo, como XML
 * malformado, arquivo ilegível ou ausência completa de estrutura reconhecível.
 */
@Getter
@Setter
public class ResultadoParser {

    /**
     * Transações válidas extraídas do arquivo.
     *
     * Cada transação deve conter os campos de domínio interpretados pelo parser,
     * mas não deve conter Importacao nem Categoria definida pelo parser.
     */
    private List<Transacao> transacoes = new ArrayList<>();

    /**
     * Quantidade de registros ignorados por inconsistência de dados.
     *
     * Exemplos:
     * - data inválida;
     * - valor monetário inválido;
     * - campos obrigatórios ausentes;
     * - linha/registro em estrutura não reconhecida.
     */
    private int linhasInvalidas;

    /**
     * Quantidade total de registros avaliados pelo parser.
     *
     * Para CSV/TXT, normalmente representa linhas não vazias avaliadas.
     * Para XML/NF-e, representa elementos de transação/item encontrados.
     */
    private int totalLinhas;
}
