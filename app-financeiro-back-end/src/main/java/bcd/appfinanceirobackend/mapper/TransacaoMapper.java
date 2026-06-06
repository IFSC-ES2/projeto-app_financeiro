package bcd.appfinanceirobackend.mapper;

import bcd.appfinanceirobackend.dto.transacao.TransacaoResponseDTO;
import bcd.appfinanceirobackend.model.Transacao;

public class TransacaoMapper {

    public TransacaoMapper() {}

    public TransacaoResponseDTO toResponse(Transacao transacao) {
        TransacaoResponseDTO responseDTO = new TransacaoResponseDTO();
        responseDTO.setTransacaoId(transacao.getId());
        responseDTO.setData(transacao.getData());
        responseDTO.setValor(transacao.getValor());
        responseDTO.setFormaPagamento(transacao.getFormaPagamento());
        responseDTO.setTipoTransacao(transacao.getTipo());
        responseDTO.setDescricao(transacao.getDescricao());
        responseDTO.setContaId(transacao.getConta().getId());
        responseDTO.setCategorizada(transacao.getCategorizada());
        responseDTO.setCategoriaId(
                transacao.getCategoria() != null ? transacao.getCategoria().getId() : null
        );
        responseDTO.setImportacaoId(
                transacao.getImportacao() != null ? transacao.getImportacao().getId() : null
        );
        return responseDTO;
    }
}
