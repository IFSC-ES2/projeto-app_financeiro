package bcd.appfinanceirobackend.exception;

import bcd.appfinanceirobackend.dto.importacao.ErroImportacaoDTO;
import lombok.Getter;

/**
 * Representa um problema esperado na estrutura de um arquivo de importação.
 *
 * Diferente de uma falha técnica inesperada, este erro pode ser apresentado
 * ao usuário de forma segura.
 */
@Getter
public class ErroEstruturalImportacaoException extends RuntimeException {

    private final Integer numeroRegistro;
    private final String campo;

    public ErroEstruturalImportacaoException(
            Integer numeroRegistro,
            String campo,
            String mensagem
    ) {
        super(mensagem);
        this.numeroRegistro = numeroRegistro;
        this.campo = campo;
    }

    public ErroImportacaoDTO toErroImportacaoDTO() {
        ErroImportacaoDTO erro = new ErroImportacaoDTO();
        erro.setNumeroRegistro(numeroRegistro);
        erro.setCampo(campo);
        erro.setMensagem(getMessage());
        return erro;
    }
}