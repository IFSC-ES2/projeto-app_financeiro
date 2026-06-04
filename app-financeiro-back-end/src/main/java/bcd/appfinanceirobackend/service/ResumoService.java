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
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class ResumoService {

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

        BigDecimal totalGeral = transacoes.stream()
                .map(Transacao::getValor)
                .filter(valor -> valor != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalGeral.compareTo(BigDecimal.ZERO) == 0) {
            return new ArrayList<>();
        }

        Map<TipoPagamento, List<Transacao>> agrupamentoPagamentos = new EnumMap<>(TipoPagamento.class);

        for (Transacao transacao : transacoes) {
            if (transacao.getFormaPagamento() == null || transacao.getValor() == null) {
                continue;
            }

            agrupamentoPagamentos
                    .computeIfAbsent(transacao.getFormaPagamento(), chave -> new ArrayList<>())
                    .add(transacao);
        }

        List<GrupoPagamentoDTO> listaDePagamentoAgrupados = new ArrayList<>();

        for (Map.Entry<TipoPagamento, List<Transacao>> entry : agrupamentoPagamentos.entrySet()) {
            listaDePagamentoAgrupados.add(getGrupoPagamentoDTO(entry, totalGeral));
        }

        return listaDePagamentoAgrupados;
    }

    private GrupoPagamentoDTO getGrupoPagamentoDTO(Map.Entry<TipoPagamento, List<Transacao>> entry, BigDecimal totalGeral) {
        BigDecimal totalGrupo = entry.getValue().stream()
                .map(Transacao::getValor)
                .filter(valor -> valor != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal percentualGrupo = totalGrupo
                .multiply(BigDecimal.valueOf(100))
                .divide(totalGeral, 2, RoundingMode.HALF_UP);

        GrupoPagamentoDTO grupoPagamentoDTO = new GrupoPagamentoDTO();
        grupoPagamentoDTO.setFormaPagamento(entry.getKey());
        grupoPagamentoDTO.setTotal(totalGrupo);
        grupoPagamentoDTO.setQuantidade(entry.getValue().size());
        grupoPagamentoDTO.setPercentual(percentualGrupo);

        return grupoPagamentoDTO;
    }
}