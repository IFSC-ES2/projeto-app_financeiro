package bcd.appfinanceirobackend.service;

import bcd.appfinanceirobackend.dto.conta.ContaRequestDTO;
import bcd.appfinanceirobackend.dto.conta.ContaResponseDTO;
import bcd.appfinanceirobackend.dto.transacao.TransacaoResponseDTO;
import bcd.appfinanceirobackend.model.Conta;
import bcd.appfinanceirobackend.model.Transacao;
import bcd.appfinanceirobackend.model.Usuario;
import org.springframework.stereotype.Service;

@Service
public class ContaService   {

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
