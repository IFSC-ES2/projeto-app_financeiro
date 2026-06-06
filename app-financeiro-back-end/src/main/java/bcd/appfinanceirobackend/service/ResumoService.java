package bcd.appfinanceirobackend.service;

import bcd.appfinanceirobackend.dto.resumo.GrupoPagamentoDTO;
import bcd.appfinanceirobackend.model.Transacao;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.model.enums.TipoPagamento;
import bcd.appfinanceirobackend.repository.TransacaoRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ResumoService {

    private static final String CHAVE_NAO_INFORMADO = "NAO_INFORMADO";

    private final TransacaoRepository transacaoRepository;

    public ResumoService(TransacaoRepository transacaoRepository) {
        this.transacaoRepository = transacaoRepository;
    }

    public List<GrupoPagamentoDTO> agruparFormaPagamento(Usuario usuarioAutenticado) {
        if (usuarioAutenticado == null || usuarioAutenticado.getId() == null) {
            throw new AccessDeniedException("Usuário autenticado não encontrado.");
        }

        List<Transacao> transacoes = transacaoRepository.findAllByContaUsuarioId(usuarioAutenticado.getId());

        if (transacoes.isEmpty()) {
            return new ArrayList<>();
        }

        BigDecimal totalGeral = BigDecimal.ZERO;
        Map<String, List<Transacao>> agrupamentoPagamentos = new HashMap<>();

        for (Transacao transacao : transacoes) {
            totalGeral = totalGeral.add(transacao.getValor());

            String chaveAgrupamento = obterChaveAgrupamento(transacao.getFormaPagamento());

            agrupamentoPagamentos
                    .computeIfAbsent(chaveAgrupamento, chave -> new ArrayList<>())
                    .add(transacao);
        }

        if (totalGeral.compareTo(BigDecimal.ZERO) == 0) {
            return new ArrayList<>();
        }

        List<GrupoPagamentoDTO> gruposPagamento = new ArrayList<>();

        for (Map.Entry<String, List<Transacao>> entry : agrupamentoPagamentos.entrySet()) {
            gruposPagamento.add(criarGrupoPagamentoDTO(entry, totalGeral));
        }

        return gruposPagamento;
    }

    private String obterChaveAgrupamento(TipoPagamento formaPagamento) {
        if (formaPagamento == null) {
            return CHAVE_NAO_INFORMADO;
        }

        return formaPagamento.name();
    }

    private GrupoPagamentoDTO criarGrupoPagamentoDTO(Map.Entry<String, List<Transacao>> entry, BigDecimal totalGeral) {
        String chaveAgrupamento = entry.getKey();
        List<Transacao> transacoesDoGrupo = entry.getValue();

        BigDecimal totalGrupo = BigDecimal.ZERO;

        for (Transacao transacao : transacoesDoGrupo) {
            totalGrupo = totalGrupo.add(transacao.getValor());
        }

        BigDecimal percentualGrupo = totalGrupo
                .multiply(BigDecimal.valueOf(100))
                .divide(totalGeral, 2, RoundingMode.HALF_UP);

        GrupoPagamentoDTO grupoPagamentoDTO = new GrupoPagamentoDTO();
        grupoPagamentoDTO.setFormaPagamento(obterFormaPagamento(chaveAgrupamento));
        grupoPagamentoDTO.setRotulo(obterRotulo(chaveAgrupamento));
        grupoPagamentoDTO.setTotal(totalGrupo);
        grupoPagamentoDTO.setQuantidade(transacoesDoGrupo.size());
        grupoPagamentoDTO.setPercentual(percentualGrupo);

        return grupoPagamentoDTO;
    }

    private TipoPagamento obterFormaPagamento(String chaveAgrupamento) {
        if (CHAVE_NAO_INFORMADO.equals(chaveAgrupamento)) {
            return null;
        }

        return TipoPagamento.valueOf(chaveAgrupamento);
    }

    private String obterRotulo(String chaveAgrupamento) {
        if (CHAVE_NAO_INFORMADO.equals(chaveAgrupamento)) {
            return "Não informado";
        }

        TipoPagamento formaPagamento = TipoPagamento.valueOf(chaveAgrupamento);

        return switch (formaPagamento) {
            case PIX -> "Pix";
            case CARTAO_DEBITO -> "Cartão de débito";
            case CARTAO_CREDITO -> "Cartão de crédito";
            case DINHEIRO -> "Dinheiro";
            case BOLETO -> "Boleto";
            case TED_DOC -> "TED/DOC";
        };
    }
}