package bcd.appfinanceirobackend.service;

import bcd.appfinanceirobackend.dto.importacao.ImportacaoResponseDTO;
import bcd.appfinanceirobackend.exception.ResourceNotFoundException;
import bcd.appfinanceirobackend.model.Conta;
import bcd.appfinanceirobackend.model.Importacao;
import bcd.appfinanceirobackend.model.Transacao;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.model.enums.StatusImportacao;
import bcd.appfinanceirobackend.parser.ParserExtrato;
import bcd.appfinanceirobackend.repository.ContaRepository;
import bcd.appfinanceirobackend.repository.ImportacaoRepository;
import bcd.appfinanceirobackend.repository.TransacaoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ImportacaoService {

    private final List<ParserExtrato> parsers;
    private final ImportacaoRepository importacaoRepository;
    private final TransacaoRepository transacaoRepository;
    private final ContaRepository contaRepository;

    public ImportacaoService(List<ParserExtrato> parsers,
                             ImportacaoRepository importacaoRepository,
                             TransacaoRepository transacaoRepository,
                             ContaRepository contaRepository){
        this.contaRepository = contaRepository;
        this.transacaoRepository = transacaoRepository;
        this.parsers = parsers;
        this.importacaoRepository = importacaoRepository;
    }

    public ImportacaoResponseDTO processar(MultipartFile arquivo,
                                           UUID contaId,
                                           Usuario usuarioAutenticado) {

        Conta conta = contaRepository.findById(contaId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada"));
        Importacao importacao = new Importacao();
        importacao.setUsuario(usuarioAutenticado);
        importacao.setStatusImportacao(StatusImportacao.PENDENTE);
        importacao.setImportado_em(LocalDateTime.now());
        importacaoRepository.save(importacao);

        ParserExtrato parser = parsers.stream()
                .filter(p -> p.aceita(arquivo))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Formato de arquivo não suportado"));

        importacao.setStatusImportacao(StatusImportacao.PROCESSANDO);
        importacaoRepository.save(importacao);

        int sucessos = 0, falhas = 0;
        try {
            List<Transacao> transacoes = parser.parsear(arquivo, conta);
            for (Transacao t : transacoes) {
                try {
                    t.setImportacao(importacao);
                    t.setCategorizada(false);
                    transacaoRepository.save(t);
                    sucessos++;
                } catch (Exception e) {
                    falhas++;
                }
            }
            importacao.setStatusImportacao(StatusImportacao.CONCLUIDO);
        } catch (Exception e) {
            importacao.setStatusImportacao(StatusImportacao.ERRO);
        }

        importacao.setSucessos(sucessos);
        importacao.setFalhas(falhas);
        importacaoRepository.save(importacao);

        return toResponse(importacao);
    }

    public StatusImportacao buscarStatus(UUID importacaoID) {
        Importacao importacao = importacaoRepository.findById(importacaoID).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Importação não encontrada"));
        return importacao.getStatusImportacao();
    }




    private ImportacaoResponseDTO toResponse(Importacao importacao) {
        ImportacaoResponseDTO dto = new ImportacaoResponseDTO();
        dto.setId(importacao.getId());
        dto.setStatus(importacao.getStatusImportacao());
        dto.setSucessos(importacao.getSucessos());
        dto.setFalhas(importacao.getFalhas());
        dto.setImportadoEm(importacao.getImportado_em());
        return dto;
    }
}
