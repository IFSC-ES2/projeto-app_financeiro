package bcd.appfinanceirobackend.service;

import bcd.appfinanceirobackend.dto.conta.ContaRequestDTO;
import bcd.appfinanceirobackend.dto.conta.ContaResponseDTO;
import bcd.appfinanceirobackend.model.Conta;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.repository.ContaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContaService {

    private final ContaRepository contaRepository;

    public ContaService (ContaRepository contaRepository) {this.contaRepository = contaRepository;}

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