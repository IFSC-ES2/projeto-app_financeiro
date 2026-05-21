package bcd.appfinanceirobackend.service;

import bcd.appfinanceirobackend.dto.importacao.ImportacaoResponseDTO;
import bcd.appfinanceirobackend.exception.ResourceNotFoundException;
import bcd.appfinanceirobackend.model.Conta;
import bcd.appfinanceirobackend.model.Importacao;
import bcd.appfinanceirobackend.model.Transacao;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.model.enums.FormatoArquivo;
import bcd.appfinanceirobackend.model.enums.StatusImportacao;
import bcd.appfinanceirobackend.parser.ParserExtrato;
import bcd.appfinanceirobackend.parser.ResultadoParser;
import bcd.appfinanceirobackend.repository.ContaRepository;
import bcd.appfinanceirobackend.repository.ImportacaoRepository;
import bcd.appfinanceirobackend.repository.TransacaoRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.InputStream;
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

        if (!conta.getUsuario().getId().equals(usuarioAutenticado.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Conta não pertence ao usuário autenticado");
        }

        if(arquivo.isEmpty()) throw new IllegalArgumentException("Arquivo vazio");

        boolean verificaNFe = false;

        try (InputStream is = arquivo.getInputStream()) {
            byte[] preview = is.readNBytes(500);
            String inicio = new String(preview).toLowerCase();
            if(inicio.contains("nfeproc") || inicio.contains("<nfe")){
                verificaNFe = true;
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Conteúdo do arquivo inválido");
        }


        String nome = arquivo.getOriginalFilename();
        if(nome==null) throw new IllegalArgumentException("Nome do arquivo inválido");
        Importacao importacao = new Importacao();
        importacao.setNome_arquivo(arquivo.getOriginalFilename());
        importacao.setFormatoArquivo(detectarFormato(nome, verificaNFe));
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
            ResultadoParser resultado = parser.parsear(arquivo, conta);
            falhas = resultado.getLinhasInvalidas();
            for (Transacao t: resultado.getTransacoes()){
                try {
                    t.setImportacao(importacao);
                    t.setCategorizada(false);
                    transacaoRepository.save(t);
                    sucessos++;
                }catch (Exception e){
                    falhas++;
                }
            }
            importacao.setTotal_linhas(resultado.getTotalLinhas());
            importacao.setStatusImportacao(StatusImportacao.CONCLUIDO);
        } catch (Exception e) {
            importacao.setStatusImportacao(StatusImportacao.ERRO);
            importacao.setMensagemErro(e.getMessage());
        }

        importacao.setSucessos(sucessos);
        importacao.setFalhas(falhas);
        importacaoRepository.save(importacao);

        return toResponse(importacao);
    }

    private FormatoArquivo detectarFormato(String nomeArquivo, boolean verificaNfe) {
        String extensaoArquivo = nomeArquivo.toLowerCase();
        if(extensaoArquivo.endsWith(".csv")){
            return FormatoArquivo.CSV;
        }
        if (extensaoArquivo.endsWith(".txt")) {
            return FormatoArquivo.TXT;
        }
        if (extensaoArquivo.endsWith(".xml")) {
            return verificaNfe ? FormatoArquivo.NFE : FormatoArquivo.XML;
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato do Arquivo não suportado");

    }

    public StatusImportacao buscarStatus(UUID importacaoID, Usuario usuarioAutenticado) {
        Importacao importacao = importacaoRepository.findById(importacaoID).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Importação não encontrada"));
        if (!importacao.getUsuario().getId().equals(usuarioAutenticado.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Importação não pertence ao usuário autenticado");
        }
        return importacao.getStatusImportacao();
    }




    private ImportacaoResponseDTO toResponse(Importacao importacao) {
        ImportacaoResponseDTO dto = new ImportacaoResponseDTO();
        dto.setId(importacao.getId());
        dto.setStatus(importacao.getStatusImportacao());
        dto.setSucessos(importacao.getSucessos());
        dto.setFalhas(importacao.getFalhas());
        dto.setImportadoEm(importacao.getImportado_em());
        dto.setMensagemErro(importacao.getMensagemErro());
        return dto;
    }
}
