package bcd.appfinanceirobackend.service;

import bcd.appfinanceirobackend.dto.comum.PaginaDTO;
import bcd.appfinanceirobackend.dto.transacao.TransacaoRequestDTO;
import bcd.appfinanceirobackend.dto.transacao.TransacaoResponseDTO;
import bcd.appfinanceirobackend.exception.ResourceNotFoundException;
import bcd.appfinanceirobackend.mapper.TransacaoMapper;
import bcd.appfinanceirobackend.model.Categoria;
import bcd.appfinanceirobackend.model.Conta;
import bcd.appfinanceirobackend.model.Transacao;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.repository.CategoriaRepository;
import bcd.appfinanceirobackend.model.enums.TipoConta;
import bcd.appfinanceirobackend.model.enums.TipoTransacao;
import bcd.appfinanceirobackend.repository.ContaRepository;
import bcd.appfinanceirobackend.repository.TransacaoRepository;
import bcd.appfinanceirobackend.repository.spec.TransacaoSpecs;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import bcd.appfinanceirobackend.model.enums.TipoPagamento;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class TransacaoService {

    private final TransacaoRepository transacaoRepository;
    private final ContaUsuarioService contaUsuarioService;
    private final TransacaoMapper transacaoMapper;
    private final CategoriaService categoriaService;

    public TransacaoService(TransacaoRepository transacaoRepository,
                            ContaUsuarioService contaUsuarioService,
                            TransacaoMapper transacaoMapper, CategoriaService categoriaService) {
        this.transacaoRepository = transacaoRepository;
        this.contaUsuarioService = contaUsuarioService;
        this.transacaoMapper = transacaoMapper;
        this.categoriaService = categoriaService;
    }

    public TransacaoResponseDTO registrarManual (TransacaoRequestDTO dto, Usuario usuarioAutenticado) {
        validarCamposObrigatorios(dto);

        Conta conta = contaUsuarioService.resolverConta(dto, usuarioAutenticado);

        Transacao transacao = new Transacao();
        transacao.setConta(conta);
        transacao.setValor(dto.getValor());

        transacao.setData(dto.getData());
        if(transacao.getData().isAfter(LocalDate.now())) transacao.setFutura(true);
        transacao.setDescricao(dto.getDescricao());
        transacao.setTipo(dto.getTipoTransacao());
        transacao.setFormaPagamento(dto.getFormaPagamento());

        if (dto.getCategoriaId() != null) {
            Categoria categoria = categoriaService.buscarCategoriaPermitida(dto.getCategoriaId(), usuarioAutenticado);

            transacao.setCategoria(categoria);
            transacao.setCategorizada(true);
        } else {
            transacao.setCategorizada(false);
        }

        Transacao transacaoSalva = transacaoRepository.save(transacao);

        return transacaoMapper.toResponse(transacaoSalva);
    }

    public TransacaoResponseDTO editar(UUID transacaoId, TransacaoRequestDTO dto, Usuario usuarioAutenticado) {
        validarCamposObrigatorios(dto);

        Transacao transacao = buscarTransacaoDoUsuario(transacaoId, usuarioAutenticado);
        Conta conta = contaUsuarioService.resolverConta(dto, usuarioAutenticado);

        transacao.setConta(conta);
        transacao.setValor(dto.getValor());
        transacao.setData(dto.getData());
        transacao.setFutura(dto.getData().isAfter(LocalDate.now()));
        transacao.setDescricao(dto.getDescricao());
        transacao.setTipo(dto.getTipoTransacao());
        transacao.setFormaPagamento(dto.getFormaPagamento());

        if (dto.getCategoriaId() != null) {
            Categoria categoria = categoriaService.buscarCategoriaPermitida(dto.getCategoriaId(), usuarioAutenticado);

            transacao.setCategoria(categoria);
            transacao.setCategorizada(true);
        } else {
            transacao.setCategoria(null);
            transacao.setCategorizada(false);
        }

        return transacaoMapper.toResponse(transacaoRepository.save(transacao));
    }

    public void excluir(UUID transacaoId, Usuario usuarioAutenticado) {
        Transacao transacao = buscarTransacaoDoUsuario(transacaoId, usuarioAutenticado);
        transacaoRepository.delete(transacao);
    }

    private Transacao buscarTransacaoDoUsuario(UUID transacaoId, Usuario usuarioAutenticado) {
        Transacao transacao = transacaoRepository.findById(transacaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Transação não encontrada"));

        if (!transacao.getConta().getUsuario().getId().equals(usuarioAutenticado.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado a essa transação");
        }

        return transacao;
    }

    public PaginaDTO<TransacaoResponseDTO> listarTransacoesPorUsuario(Usuario usuarioAutenticado,
                                                                      LocalDate dataInicio,
                                                                      LocalDate dataFim,
                                                                      UUID categoriaId,
                                                                      TipoTransacao tipo,
                                                                      UUID contaId,
                                                                      Pageable pageable) {
        Specification<Transacao> filtro = Specification.where(TransacaoSpecs.daConta(usuarioAutenticado.getId()))
                .and(TransacaoSpecs.dataDe(dataInicio))
                .and(TransacaoSpecs.dataAte(dataFim))
                .and(TransacaoSpecs.daCategoria(categoriaId))
                .and(TransacaoSpecs.doTipo(tipo))
                .and(TransacaoSpecs.daContaEspecifica(contaId));

        return PaginaDTO.de(transacaoRepository.findAll(filtro, pageable), transacaoMapper::toResponse);
    }

    public TransacaoResponseDTO categorizar(UUID transacaoId, UUID categoriaId, Usuario usuarioAutenticado){
        Transacao transacao = transacaoRepository.findById(transacaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Transacao não encontrada"));

        if(!transacao.getConta().getUsuario().getId().equals(usuarioAutenticado.getId())){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado a essa transação");
        }

        Categoria categoria = categoriaService.buscarCategoriaPermitida(categoriaId, usuarioAutenticado);

        transacao.setCategoria(categoria);
        transacao.setCategorizada(true);
        transacaoRepository.save(transacao);
        return transacaoMapper.toResponse(transacao);
    }



    // Utils

    private void validarCamposObrigatorios(TransacaoRequestDTO dto) {
        if (dto.getValor() == null ||
                dto.getData() == null ||
                dto.getTipoTransacao() == null ||
                dto.getFormaPagamento() == null) {
            throw new IllegalArgumentException("Campos obrigatórios não informados");
        }

        boolean pagamentoEmDinheiro = dto.getFormaPagamento() == TipoPagamento.DINHEIRO;

        if (!pagamentoEmDinheiro && dto.getContaId() == null) {
            throw new IllegalArgumentException("Campos obrigatórios não informados");
        }

        if (dto.getValor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor informado deve ser maior que zero");
        }
    }


}
