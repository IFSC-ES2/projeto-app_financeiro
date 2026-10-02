package bcd.appfinanceirobackend.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import bcd.appfinanceirobackend.model.Transacao;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.repository.TransacaoRepository;

@Service 
public class ExtratoFuturoService {
    private final TransacaoRepository transacaoRepository;

    public ExtratoFuturoService(TransacaoRepository transacaoRepository){
        this.transacaoRepository =transacaoRepository;
    }

    public List<Transacao> listarTransacoesFuturas(
        Usuario usuario, 
        LocalDate inicio, 
        LocalDate fim
    ){
        return transacaoRepository.findAllByContaUsuarioIdAndFuturaTrueAndDataBetweenOrderByDataAsc(
            usuario.getId(), inicio, fim);
    }
}
