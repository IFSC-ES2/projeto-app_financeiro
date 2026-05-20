package bcd.appfinanceirobackend.parser;

import bcd.appfinanceirobackend.model.Transacao;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ResultadoParser {
    private List<Transacao> transacoes;
    private int linhasInvalidas;
    private int totalLinhas;
}
