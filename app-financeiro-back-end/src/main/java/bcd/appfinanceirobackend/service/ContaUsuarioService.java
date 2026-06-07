package bcd.appfinanceirobackend.service;

import bcd.appfinanceirobackend.dto.transacao.TransacaoRequestDTO;
import bcd.appfinanceirobackend.exception.ResourceNotFoundException;
import bcd.appfinanceirobackend.model.Conta;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.model.enums.TipoConta;
import bcd.appfinanceirobackend.model.enums.TipoPagamento;
import bcd.appfinanceirobackend.repository.ContaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ContaUsuarioService {

    private final ContaRepository contaRepository;

    public ContaUsuarioService(ContaRepository contaRepository) {
        this.contaRepository = contaRepository;
    }

    private Conta obterOuCriarContaDinheiro(Usuario usuario) {
        return contaRepository
                .findByUsuarioIdAndTipoContaAndNome(
                        usuario.getId(),
                        TipoConta.CARTEIRA,
                        "Dinheiro / Carteira"
                )
                .orElseGet(() -> {
                    Conta conta = new Conta();
                    conta.setNome("Dinheiro / Carteira");
                    conta.setTipoConta(TipoConta.CARTEIRA);
                    conta.setBanco("Dinheiro");
                    conta.setDescricao("Conta automática para transações em dinheiro");
                    conta.setUsuario(usuario);

                    return contaRepository.save(conta);
                });
    }

    public Conta resolverConta(TransacaoRequestDTO dto, Usuario usuarioAutenticado) {
        if (dto.getFormaPagamento() == TipoPagamento.DINHEIRO) {
            return obterOuCriarContaDinheiro(usuarioAutenticado);
        }

        Conta conta = contaRepository.findById(dto.getContaId())
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada"));

        if (!conta.getUsuario().getId().equals(usuarioAutenticado.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado a esta conta");
        }

        return conta;
    }
}
