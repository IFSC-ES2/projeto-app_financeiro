package bcd.appfinanceirobackend.model.enums;

/**
 * Sentido financeiro da transação.
 * Não representa forma nem condição de pagamento — isso é responsabilidade
 * de {@link TipoPagamento}. Boleto e parcelamento foram removidos daqui (issue #176).
 */
public enum TipoTransacao {
    DEBITO,
    CREDITO
}
