package bcd.appfinanceirobackend.service;

import bcd.appfinanceirobackend.dto.importacao.ErroImportacaoDTO;
import bcd.appfinanceirobackend.dto.importacao.ImportacaoResponseDTO;
import bcd.appfinanceirobackend.exception.ErroEstruturalImportacaoException;
import bcd.appfinanceirobackend.exception.ResourceNotFoundException;
import bcd.appfinanceirobackend.model.Categoria;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ImportacaoService {

    private static final String MENSAGEM_SEM_TRANSACOES_VALIDAS =
            "Nenhuma transação válida foi encontrada no arquivo. " +
            "Revise os registros rejeitados e verifique os campos obrigatórios de data, descrição e valor.";

    private static final String MENSAGEM_ERRO_PROCESSAMENTO =
            "Não foi possível processar o arquivo. " +
            "Verifique se o formato e os campos obrigatórios estão corretos e tente novamente.";

    private final List<ParserExtrato> parsers;
    private final ImportacaoRepository importacaoRepository;
    private final TransacaoRepository transacaoRepository;
    private final ContaRepository contaRepository;
    private final SugestaoCategoriaService sugestaoCategoriaService;

    public ImportacaoService(
            List<ParserExtrato> parsers,
            ImportacaoRepository importacaoRepository,
            TransacaoRepository transacaoRepository,
            ContaRepository contaRepository,
            SugestaoCategoriaService sugestaoCategoriaService
    ) {
        this.parsers = parsers;
        this.importacaoRepository = importacaoRepository;
        this.transacaoRepository = transacaoRepository;
        this.contaRepository = contaRepository;
        this.sugestaoCategoriaService = sugestaoCategoriaService;
    }

    public ImportacaoResponseDTO processar(
            MultipartFile arquivo,
            UUID contaId,
            Usuario usuarioAutenticado
    ) {
        Conta conta = contaRepository
                .findById(contaId)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Conta não encontrada"
                        )
                );

        if (
                !conta.getUsuario()
                        .getId()
                        .equals(usuarioAutenticado.getId())
        ) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Conta não pertence ao usuário autenticado"
            );
        }

        String nome = arquivo.getOriginalFilename();

        if (
                nome == null ||
                nome.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Nome do arquivo inválido"
            );
        }

        if (arquivo.isEmpty()) {
            throw new IllegalArgumentException(
                    "Arquivo vazio"
            );
        }

        boolean verificaNFe = verificarSeNFe(
                arquivo
        );

        Importacao importacao = new Importacao();

        importacao.setNome_arquivo(nome);

        importacao.setFormatoArquivo(
                detectarFormato(
                        nome,
                        verificaNFe
                )
        );

        importacao.setUsuario(
                usuarioAutenticado
        );

        importacao.setStatusImportacao(
                StatusImportacao.PENDENTE
        );

        importacao.setImportado_em(
                LocalDateTime.now()
        );

        importacaoRepository.save(
                importacao
        );

        ParserExtrato parser = parsers.stream()
                .filter(
                        p -> p.aceita(arquivo)
                )
                .findFirst()
                .orElseThrow(
                        () -> new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Formato de arquivo não suportado"
                        )
                );

        importacao.setStatusImportacao(
                StatusImportacao.PROCESSANDO
        );

        importacaoRepository.save(
                importacao
        );

        int sucessos = 0;
        int falhas = 0;
        int falhasPersistencia = 0;
        int ignoradasPorDuplicidade = 0;

        List<ErroImportacaoDTO> erros =
                new ArrayList<>();

        try {
            ResultadoParser resultado =
                    parser.parsear(
                            arquivo,
                            conta
                    );

            erros.addAll(
                    resultado.getErros()
            );

            if (
                    resultado
                            .getTransacoes()
                            .isEmpty()
            ) {
                importacao.setStatusImportacao(
                        StatusImportacao.ERRO
                );

                importacao.setTotal_linhas(
                        resultado.getTotalLinhas()
                );

                importacao.setSucessos(0);

                importacao.setFalhas(
                        resultado.getLinhasInvalidas()
                );

                importacao.setIgnoradasPorDuplicidade(0);

                importacao.setMensagemErro(
                        MENSAGEM_SEM_TRANSACOES_VALIDAS
                );

                importacaoRepository.save(
                        importacao
                );

                return toResponse(
                        importacao,
                        erros
                );
            }

            falhas =
                    resultado.getLinhasInvalidas();

            List<Transacao> transacoes =
                    resultado.getTransacoes();

            for (int i = 0; i < transacoes.size(); i++) {
                Transacao transacao =
                        transacoes.get(i);

                Integer numeroRegistro =
                        resultado.getNumeroRegistroTransacao(i);

                try {
                    boolean duplicada =
                            transacaoRepository
                                    .existsByContaIdAndDataAndDescricaoAndValorAndTipo(
                                            conta.getId(),
                                            transacao.getData(),
                                            transacao.getDescricao(),
                                            transacao.getValor(),
                                            transacao.getTipo()
                                    );

                    if (duplicada) {
                        ignoradasPorDuplicidade++;
                        continue;
                    }

                    Categoria categoria =
                            sugestaoCategoriaService
                                    .sugerirCategoria(
                                            transacao.getDescricao()
                                    );

                    if (categoria != null) {
                        transacao.setCategoria(
                                categoria
                        );

                        transacao.setCategorizada(
                                true
                        );
                    }

                    transacao.setImportacao(
                            importacao
                    );

                    transacaoRepository.save(
                            transacao
                    );

                    sucessos++;

                } catch (Exception e) {
                    falhas++;
                    falhasPersistencia++;

                    erros.add(
                            criarErroPersistencia(
                                    transacao,
                                    numeroRegistro
                            )
                    );

                    log.error(
                            "Erro ao persistir transação durante a importação {}",
                            importacao.getId(),
                            e
                    );
                }
            }

            importacao.setTotal_linhas(
                    resultado.getTotalLinhas()
            );

            /*
             * Duplicidade não é falha técnica.
             *
             * Porém, se havia transações válidas para persistir,
             * todas falharam e nenhuma foi salva, a importação não
             * pode ser considerada concluída.
             */
            if (
                    sucessos == 0 &&
                    falhasPersistencia > 0
            ) {
                importacao.setStatusImportacao(
                        StatusImportacao.ERRO
                );

                importacao.setMensagemErro(
                        MENSAGEM_SEM_TRANSACOES_VALIDAS
                );

            } else {
                importacao.setStatusImportacao(
                        StatusImportacao.CONCLUIDO
                );
            }

        } catch (ErroEstruturalImportacaoException e) {
            falhas++;

            erros.add(
                    e.toErroImportacaoDTO()
            );

            importacao.setStatusImportacao(
                    StatusImportacao.ERRO
            );

            importacao.setMensagemErro(
                    MENSAGEM_SEM_TRANSACOES_VALIDAS
            );

        } catch (Exception e) {
            importacao.setStatusImportacao(
                    StatusImportacao.ERRO
            );

            importacao.setMensagemErro(
                    MENSAGEM_ERRO_PROCESSAMENTO
            );

            log.error(
                    "Erro inesperado ao processar importação {}",
                    importacao.getId(),
                    e
            );
        }

        importacao.setSucessos(
                sucessos
        );

        importacao.setFalhas(
                falhas
        );

        importacao.setIgnoradasPorDuplicidade(
                ignoradasPorDuplicidade
        );

        importacaoRepository.save(
                importacao
        );

        return toResponse(
                importacao,
                erros
        );
    }

    private boolean verificarSeNFe(
            MultipartFile arquivo
    ) {
        try (
                InputStream is =
                        arquivo.getInputStream()
        ) {
            byte[] preview =
                    is.readNBytes(500);

            String inicio =
                    new String(preview)
                            .toLowerCase();

            return inicio.contains("nfeproc")
                    || inicio.contains("<nfe");

        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Conteúdo do arquivo inválido"
            );
        }
    }

    private ErroImportacaoDTO criarErroPersistencia(
            Transacao transacao,
            Integer numeroRegistro
    ) {
        ErroImportacaoDTO erro =
                new ErroImportacaoDTO();

        erro.setNumeroRegistro(
                numeroRegistro
        );

        erro.setCampo(
                "transacao"
        );

        StringBuilder mensagem =
                new StringBuilder(
                        "Não foi possível salvar a transação"
                );

        if (
                transacao.getDescricao() != null &&
                !transacao.getDescricao().isBlank()
        ) {
            mensagem
                    .append(" \"")
                    .append(
                            transacao.getDescricao()
                    )
                    .append("\"");
        }

        if (transacao.getData() != null) {
            mensagem
                    .append(" de ")
                    .append(
                            transacao.getData()
                    );
        }

        mensagem.append(".");

        erro.setMensagem(
                mensagem.toString()
        );

        return erro;
    }

    private FormatoArquivo detectarFormato(
            String nomeArquivo,
            boolean verificaNFe
    ) {
        String extensaoArquivo =
                nomeArquivo.toLowerCase();

        if (extensaoArquivo.endsWith(".csv")) {
            return FormatoArquivo.CSV;
        }

        if (extensaoArquivo.endsWith(".txt")) {
            return FormatoArquivo.TXT;
        }

        if (extensaoArquivo.endsWith(".xml")) {
            return verificaNFe
                    ? FormatoArquivo.NFE
                    : FormatoArquivo.XML;
        }

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Formato do Arquivo não suportado"
        );
    }

    public StatusImportacao buscarStatus(
            UUID importacaoID,
            Usuario usuarioAutenticado
    ) {
        Importacao importacao =
                importacaoRepository
                        .findById(importacaoID)
                        .orElseThrow(
                                () -> new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Importação não encontrada"
                                )
                        );

        if (
                !importacao
                        .getUsuario()
                        .getId()
                        .equals(usuarioAutenticado.getId())
        ) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Importação não pertence ao usuário autenticado"
            );
        }

        return importacao
                .getStatusImportacao();
    }

    private ImportacaoResponseDTO toResponse(
            Importacao importacao,
            List<ErroImportacaoDTO> erros
    ) {
        ImportacaoResponseDTO dto =
                new ImportacaoResponseDTO();

        dto.setId(
                importacao.getId()
        );

        dto.setStatus(
                importacao.getStatusImportacao()
        );

        dto.setSucessos(
                importacao.getSucessos()
        );

        dto.setFalhas(
                importacao.getFalhas()
        );

        dto.setIgnoradasPorDuplicidade(
                importacao.getIgnoradasPorDuplicidade()
        );

        dto.setImportadoEm(
                importacao.getImportado_em()
        );

        dto.setMensagemErro(
                importacao.getMensagemErro()
        );

        dto.setErros(
                new ArrayList<>(erros)
        );

        return dto;
    }
}