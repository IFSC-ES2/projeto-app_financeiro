package bcd.appfinanceirobackend.service;

import bcd.appfinanceirobackend.dto.transacao.TransacaoRequestDTO;
import bcd.appfinanceirobackend.dto.transacao.TransacaoResponseDTO;
import bcd.appfinanceirobackend.model.Transacao;
import bcd.appfinanceirobackend.repository.ContaRepository;
import org.springframework.stereotype.Service;

@Service
public class TransacaoService {

    public TransacaoResponseDTO registrarManual (TransacaoRequestDTO dto) {
        if(dto.getValor() == null ||
                dto.getContaId() == null ||
                dto.getData() == null) throw new IllegalArgumentException("Campos obrigatórios não informados");

        Transacao transacao = new Transacao();
        transacao.setCategorizada(true);
        transacao.setConta(ContaRepository. dto.getContaId());
    }

}
