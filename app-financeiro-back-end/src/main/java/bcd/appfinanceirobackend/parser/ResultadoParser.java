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
 * Regras do contrato:
 * - transacoes contém apenas registros convertidos com sucesso;
 * - erros contém detalhes dos registros rejeitados;
 * - linhasInvalidas representa registros encontrados, mas descartados;
 * - totalLinhas representa a quantidade de registros avaliados;
 * - numerosRegistroTransacoes preserva a origem de cada transação válida.
 *
 * Em sucesso parcial, registros inválidos não interrompem o processamento
 * dos registros válidos.
 */
@Getter
@Setter
public class ResultadoParser {

    private List<Transacao> transacoes = new ArrayList<>();

    /**
     * Mantém a correspondência por índice com a lista transacoes.
     *
     * Exemplo:
     * transacoes[0] veio da linha física 3
     * numerosRegistroTransacoes[0] = 3
     */
    private List<Integer> numerosRegistroTransacoes = new ArrayList<>();

    private List<ErroImportacaoDTO> erros = new ArrayList<>();

    private int linhasInvalidas;

    private int totalLinhas;

    /**
     * Deve ser utilizado pelos parsers para preservar a origem da transação.
     */
    public void adicionarTransacao(
            Transacao transacao,
            Integer numeroRegistro
    ) {
        transacoes.add(transacao);
        numerosRegistroTransacoes.add(numeroRegistro);
    }

    /**
     * Retorna o registro de origem da transação de determinado índice.
     *
     * Retorna null para resultados antigos/montados manualmente que não
     * possuam a informação de origem.
     */
    public Integer getNumeroRegistroTransacao(int indice) {
        if (
                indice < 0 ||
                indice >= numerosRegistroTransacoes.size()
        ) {
            return null;
        }

        return numerosRegistroTransacoes.get(indice);
    }

    /**
     * Adiciona um registro rejeitado e mantém o contador sincronizado.
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