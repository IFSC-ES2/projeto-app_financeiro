package bcd.appfinanceirobackend.service;

import bcd.appfinanceirobackend.dto.conta.ContaEdicaoRequestDTO;
import bcd.appfinanceirobackend.dto.conta.ContaRequestDTO;
import bcd.appfinanceirobackend.dto.conta.ContaResponseDTO;
import bcd.appfinanceirobackend.exception.ResourceNotFoundException;
import bcd.appfinanceirobackend.model.Conta;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.repository.ContaRepository;
import bcd.appfinanceirobackend.repository.TransacaoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class ContaService {

    private final ContaRepository contaRepository;
    private final TransacaoRepository transacaoRepository;

    public ContaService (ContaRepository contaRepository,
                         TransacaoRepository transacaoRepository) {
        this.contaRepository = contaRepository;
        this.transacaoRepository = transacaoRepository;
    }

    public List<ContaResponseDTO> listarPorUsuario(Usuario usuarioAutenticado) {
        return contaRepository.findByUsuarioId(usuarioAutenticado.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ContaResponseDTO registrar(ContaRequestDTO dto, Usuario usuarioAutenticado) {
        if(dto.getNome() == null ||
                dto.getTipoConta() == null) throw new IllegalArgumentException(
                "Campos obrigatórios de uma conta não informados (nome e TipoConta)");

        Conta conta = new Conta();
        conta.setTipoConta(dto.getTipoConta());
        conta.setNome(dto.getNome());
        conta.setBanco(dto.getBanco());
        conta.setDescricao(dto.getDescricao());
        conta.setUsuario(usuarioAutenticado);
        contaRepository.save(conta);
        return toResponse(conta);
    }

    public void removerConta(Usuario usuario, UUID id) {
        Conta conta = contaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada"));
        if (!conta.getUsuario().getId().equals(usuario.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado a esta conta");
        }
        /* Apesar de o banco possuir ON DELETE CASCADE para a relação entre contas e transações,
         * a regra de negócio adotada no service bloqueia a exclusão de contas com transações vinculadas
         * para evitar perda acidental de histórico financeiro. O cascade permanece apenas como proteção estrutural
         * do banco, mas o fluxo da aplicação impede a remoção destrutiva.
        */
         if (transacaoRepository.existsByContaId(conta.getId())) {
             throw new ResponseStatusException(HttpStatus.CONFLICT,
                     "Não é possível remover uma conta com transações vinculadas");
         }
         contaRepository.delete(conta);
    }

    public ContaResponseDTO editar(UUID contaId, ContaEdicaoRequestDTO dto, Usuario usuarioAutenticado) {
        validarCamposObrigatoriosEdicao(dto);

        Conta conta = contaRepository.findById(contaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conta não encontrada"));

        if (!conta.getUsuario().getId().equals(usuarioAutenticado.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Conta não pertence ao usuário autenticado");
        }

        conta.setNome(dto.getNome());
        conta.setDescricao(dto.getDescricao());

        Conta contaAtualizada = contaRepository.save(conta);
        return toResponse(contaAtualizada);
    }

    private void validarCamposObrigatoriosEdicao(ContaEdicaoRequestDTO dto) {
        if (dto == null || dto.getNome() == null || dto.getNome().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campo obrigatório não informado: nome");
        }
    }

    public ContaResponseDTO toResponse(Conta conta) {
        ContaResponseDTO responseDTO = new ContaResponseDTO();
        responseDTO.setBanco(conta.getBanco());
        responseDTO.setNome(conta.getNome());
        responseDTO.setTipoConta(conta.getTipoConta());
        responseDTO.setContaId(conta.getId());
        responseDTO.setDescricao(conta.getDescricao());
        return responseDTO;
    }
}