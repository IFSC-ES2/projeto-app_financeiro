package bcd.appfinanceirobackend.service;

import bcd.appfinanceirobackend.dto.transacao.TransacaoRequestDTO;
import bcd.appfinanceirobackend.dto.transacao.TransacaoResponseDTO;
import bcd.appfinanceirobackend.exception.ResourceNotFoundException;
import bcd.appfinanceirobackend.model.Conta;
import bcd.appfinanceirobackend.model.Transacao;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.repository.ContaRepository;
import bcd.appfinanceirobackend.repository.TransacaoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import bcd.appfinanceirobackend.model.enums.TipoPagamento;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class TransacaoService {

    private final TransacaoRepository transacaoRepository;
    private final ContaRepository contaRepository;

    public TransacaoService(TransacaoRepository transacaoRepository, ContaRepository contaRepository) {
        this.transacaoRepository = transacaoRepository;
        this.contaRepository = contaRepository;
    }

    public TransacaoResponseDTO registrarManual (TransacaoRequestDTO dto, Usuario usuarioAutenticado) {
        if(dto.getValor() == null ||
                dto.getContaId() == null ||
                dto.getData() == null ||
        dto.getTipoTransacao() == null) throw new IllegalArgumentException(
                        "Campos obrigatórios não informados");

        if(dto.getValor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor informado deve ser maior que zero");
        }

        Conta conta = contaRepository.findById(dto.getContaId())
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada"));

        if(!conta.getUsuario().getId().equals(usuarioAutenticado.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado a esta conta");
        }

        Transacao transacao = new Transacao();
        transacao.setCategorizada(true);
        transacao.setConta(conta);
        transacao.setValor(dto.getValor());

        transacao.setData(dto.getData());
        if(transacao.getData().isAfter(LocalDate.now())) transacao.setFutura(true);
        transacao.setDescricao(dto.getDescricao());
        transacao.setTipo(dto.getTipoTransacao());
        transacao.setFormaPagamento(dto.getFormaPagamento());
        Transacao transacaoSalva = transacaoRepository.save(transacao);

        return toResponse(transacaoSalva);

    }

    public TransacaoResponseDTO toResponse(Transacao transacao) {
        TransacaoResponseDTO responseDTO = new TransacaoResponseDTO();
        responseDTO.setTransacaoId(transacao.getId());
        responseDTO.setData(transacao.getData());
        responseDTO.setValor(transacao.getValor());
        responseDTO.setFormaPagamento(transacao.getFormaPagamento());
        responseDTO.setTipoTransacao(transacao.getTipo());
        responseDTO.setDescricao(transacao.getDescricao());
        responseDTO.setContaId(transacao.getConta().getId());
        responseDTO.setCategoriaId(
                    transacao.getCategoria() != null ? transacao.getCategoria().getId() : null
        );
        responseDTO.setImportacaoId(
                    transacao.getImportacao() != null ? transacao.getImportacao().getId() : null
        );
        return responseDTO;
    }

}
