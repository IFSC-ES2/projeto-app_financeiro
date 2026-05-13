package bcd.appfinanceirobackend.service;

import bcd.appfinanceirobackend.dto.transacao.TransacaoRequestDTO;
import bcd.appfinanceirobackend.dto.transacao.TransacaoResponseDTO;
import bcd.appfinanceirobackend.model.Conta;
import bcd.appfinanceirobackend.model.Transacao;
import bcd.appfinanceirobackend.repository.ContaRepository;
import bcd.appfinanceirobackend.repository.TransacaoRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TransacaoService {

    private TransacaoRepository transacaoRepository;
    private ContaRepository contaRepository;

    public TransacaoService(TransacaoRepository transacaoRepository, ContaRepository contaRepository) {
        this.transacaoRepository = transacaoRepository;
        this.contaRepository = contaRepository;
    }

    public TransacaoResponseDTO registrarManual (TransacaoRequestDTO dto) {
        if(dto.getValor() == null ||
                dto.getContaId() == null ||
                dto.getData() == null) throw new IllegalArgumentException("Campos obrigatórios não informados");

        Optional<Conta> conta = contaRepository.findById(dto.getContaId());

        Transacao transacao = new Transacao();
        transacao.setCategorizada(true);
        transacao.setConta(conta);
        transacao.setValor(dto.getValor());
        transacao.setData(dto.getData());
        transacao.setDescricao(dto.getDescricao());
        transacao.setTipo(dto.getTipoTransacao());
        transacao.setFormaPagamento(dto.getFormaPagamento());
        transacaoRepository.save(transacao);

    }

    public TransacaoResponseDTO toResponse(Transacao transacao) {
        
    }

}
