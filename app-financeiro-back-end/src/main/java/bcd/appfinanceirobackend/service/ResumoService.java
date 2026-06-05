package bcd.appfinanceirobackend.service;

import bcd.appfinanceirobackend.dto.resumo.GrupoPagamentoDTO;
import bcd.appfinanceirobackend.model.Transacao;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.model.enums.TipoPagamento;
import bcd.appfinanceirobackend.repository.TransacaoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ResumoService {

    private final TransacaoRepository transacaoRepository;

    public ResumoService(TransacaoRepository transacaoRepository){
        this.transacaoRepository = transacaoRepository;
    }

    public List<GrupoPagamentoDTO> agruparFormaPagamento(Usuario usuarioAutenticado) {
        List<GrupoPagamentoDTO> listaDePagamentoAgrupados = new ArrayList<>();
        List<Transacao> transacoes = transacaoRepository.findAllByContaUsuarioId(usuarioAutenticado.getId());
        if (transacoes.isEmpty()) return listaDePagamentoAgrupados;
        BigDecimal totalGeral = new BigDecimal(0);
        Map<TipoPagamento, List<Transacao>> agrupamentoPagamentos = new HashMap<>();
        for (Transacao t: transacoes) {
            totalGeral = totalGeral.add(t.getValor());
            agrupamentoPagamentos.putIfAbsent(t.getFormaPagamento(), new ArrayList<>());
            agrupamentoPagamentos.get(t.getFormaPagamento()).add(t);
        }
        for (Map.Entry<TipoPagamento, List<Transacao>> entry: agrupamentoPagamentos.entrySet()) {
            GrupoPagamentoDTO grupoPagamentoDTO = getGrupoPagamentoDTO(entry, totalGeral);
            listaDePagamentoAgrupados.add(grupoPagamentoDTO);
        }
        return listaDePagamentoAgrupados;
    }

    private GrupoPagamentoDTO getGrupoPagamentoDTO(Map.Entry<TipoPagamento, List<Transacao>> entry, BigDecimal totalGeral) {
        BigDecimal totalGrupo = new BigDecimal(0);
        BigDecimal percentualGrupo = new BigDecimal(0);
        int quantidade = 0;
        for(Transacao t: entry.getValue()){
            quantidade++;
            totalGrupo = totalGrupo.add(t.getValor());
        }
        percentualGrupo = totalGrupo.divide(totalGeral, 2, RoundingMode.HALF_UP).multiply(new BigDecimal(100));
        GrupoPagamentoDTO grupoPagamentoDTO = new GrupoPagamentoDTO();
        grupoPagamentoDTO.setFormaPagamento(entry.getKey());
        grupoPagamentoDTO.setTotal(totalGrupo);
        grupoPagamentoDTO.setQuantidade(quantidade);
        grupoPagamentoDTO.setPercentual(percentualGrupo);
        return grupoPagamentoDTO;
    }

}
