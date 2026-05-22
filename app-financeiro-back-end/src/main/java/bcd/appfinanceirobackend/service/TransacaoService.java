package bcd.appfinanceirobackend.service;

import bcd.appfinanceirobackend.dto.transacao.TransacaoRequestDTO;
import bcd.appfinanceirobackend.dto.transacao.TransacaoResponseDTO;
import bcd.appfinanceirobackend.exception.ResourceNotFoundException;
import bcd.appfinanceirobackend.model.Categoria;
import bcd.appfinanceirobackend.model.Conta;
import bcd.appfinanceirobackend.model.Transacao;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.repository.CategoriaRepository;
import bcd.appfinanceirobackend.repository.ContaRepository;
import bcd.appfinanceirobackend.repository.TransacaoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class TransacaoService {

    private final TransacaoRepository transacaoRepository;
    private final ContaRepository contaRepository;
    private final CategoriaRepository categoriaRepository;

    public TransacaoService(TransacaoRepository transacaoRepository,
                            ContaRepository contaRepository,
                            CategoriaRepository categoriaRepository) {
        this.transacaoRepository = transacaoRepository;
        this.contaRepository = contaRepository;
        this.categoriaRepository = categoriaRepository;
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

    public TransacaoResponseDTO categorizar(UUID transacaoId, UUID categoriaId, Usuario usuarioAutenticado){
        Transacao transacao = transacaoRepository.findById(transacaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Transacao não encontrada"));

        if(!transacao.getConta().getUsuario().getId().equals(usuarioAutenticado.getId())){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado a essa transação");
        }

        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));

        transacao.setCategoria(categoria);
        transacao.setCategorizada(true);
        transacaoRepository.save(transacao);
        return toResponse(transacao);
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
