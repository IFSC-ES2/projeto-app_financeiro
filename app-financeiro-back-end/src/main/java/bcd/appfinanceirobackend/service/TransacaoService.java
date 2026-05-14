package bcd.appfinanceirobackend.service;

import bcd.appfinanceirobackend.dto.transacao.TransacaoRequestDTO;
import bcd.appfinanceirobackend.dto.transacao.TransacaoResponseDTO;
import bcd.appfinanceirobackend.exception.ResourceNotFoundException;
import bcd.appfinanceirobackend.model.Conta;
import bcd.appfinanceirobackend.model.Transacao;
import bcd.appfinanceirobackend.repository.ContaRepository;
import bcd.appfinanceirobackend.repository.TransacaoRepository;
import org.springframework.stereotype.Service;

@Service
public class TransacaoService {

    private final TransacaoRepository transacaoRepository;
    private final ContaRepository contaRepository;

    public TransacaoService(TransacaoRepository transacaoRepository, ContaRepository contaRepository) {
        this.transacaoRepository = transacaoRepository;
        this.contaRepository = contaRepository;
    }

    public TransacaoResponseDTO registrarManual (TransacaoRequestDTO dto) {
        if(dto.getValor() == null ||
                dto.getContaId() == null ||
                dto.getData() == null) throw new IllegalArgumentException(
                        "Campos obrigatórios de uma transação não informados");

        Conta conta = contaRepository.findById(dto.getContaId())
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada"));

        Transacao transacao = new Transacao();
        transacao.setCategorizada(true);
        transacao.setConta(conta);
        transacao.setValor(dto.getValor());
        transacao.setData(dto.getData());
        transacao.setDescricao(dto.getDescricao());
        transacao.setTipo(dto.getTipoTransacao());
        transacao.setFormaPagamento(dto.getFormaPagamento());
        transacaoRepository.save(transacao);

        return toResponse(transacao);

    }

    public TransacaoResponseDTO toResponse(Transacao transacao) {
        TransacaoResponseDTO responseDTO = new TransacaoResponseDTO();
        responseDTO.setData(transacao.getData());
        responseDTO.setValor(transacao.getValor());
        responseDTO.setFormaPagamento(transacao.getFormaPagamento());
        responseDTO.setTipoTransacao(transacao.getTipo());
        responseDTO.setDescricao(transacao.getDescricao());
        responseDTO.setContaId(transacao.getConta().getId());
        responseDTO.setCategoriaId(transacao.getCategoria().getId());
        return responseDTO;
    }

}
