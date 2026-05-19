package bcd.appfinanceirobackend.service;

import bcd.appfinanceirobackend.dto.importacao.ImportacaoResponseDTO;
import bcd.appfinanceirobackend.model.Importacao;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.model.enums.StatusImportacao;
import bcd.appfinanceirobackend.parser.ParserExtrato;
import bcd.appfinanceirobackend.repository.ContaRepository;
import bcd.appfinanceirobackend.repository.ImportacaoRepository;
import bcd.appfinanceirobackend.repository.TransacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
        
    }

    public StatusImportacao buscarStatus() {

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
