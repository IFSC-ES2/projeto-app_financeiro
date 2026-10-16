package bcd.appfinanceirobackend.parser;

import bcd.appfinanceirobackend.dto.importacao.ErroImportacaoDTO;
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
 * - erros contém os detalhes dos registros rejeitados;
 * - linhasInvalidas representa registros encontrados, mas descartados por erro;
 * - totalLinhas representa a quantidade de registros avaliados pelo parser.
 *
 * Em um sucesso parcial:
 * - transacoes pode conter uma ou mais transações válidas;
 * - erros pode conter uma ou mais falhas detalhadas;
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
     * Detalhes dos registros que não puderam ser processados.
     *
     * Cada erro pode informar:
     * - número do registro;
     * - campo relacionado;
     * - mensagem compreensível para o usuário.
     */
    private List<ErroImportacaoDTO> erros = new ArrayList<>();

    /**
     * Quantidade de registros ignorados por inconsistência de dados.
     *
     * Exemplos:
     * - data inválida;
     * - valor monetário inválido;
     * - campos obrigatórios ausentes;
     * - linha/registro em estrutura não reconhecida.
     *
     * Sempre que possível, utilize adicionarErro() para manter este contador
     * sincronizado com a lista de erros.
     */
    private int linhasInvalidas;

    /**
     * Quantidade total de registros avaliados pelo parser.
     *
     * Para CSV/TXT, normalmente representa linhas não vazias avaliadas.
     * Para XML/NF-e, representa elementos de transação/item encontrados.
     */
    private int totalLinhas;

    /**
     * Registra uma falha de processamento e incrementa automaticamente
     * a quantidade de linhas inválidas.
     *
     * @param numeroRegistro posição do registro que apresentou erro
     * @param campo campo relacionado ao problema, quando identificável
     * @param mensagem descrição compreensível da falha
     */
    public void adicionarErro(
            Integer numeroRegistro,
            String campo,
            String mensagem
    ) {
        ErroImportacaoDTO erro = new ErroImportacaoDTO();

        erro.setNumeroRegistro(numeroRegistro);
        erro.setCampo(campo);
        erro.setMensagem(mensagem);

        erros.add(erro);
        linhasInvalidas++;
    }
}