package bcd.appfinanceirobackend.service;

import bcd.appfinanceirobackend.dto.importacao.ImportacaoResponseDTO;
import bcd.appfinanceirobackend.exception.ResourceNotFoundException;
import bcd.appfinanceirobackend.model.*;
import bcd.appfinanceirobackend.model.enums.FormatoArquivo;
import bcd.appfinanceirobackend.model.enums.StatusImportacao;
import bcd.appfinanceirobackend.model.enums.TipoTransacao;
import bcd.appfinanceirobackend.parser.ParserExtrato;
import bcd.appfinanceirobackend.parser.ResultadoParser;
import bcd.appfinanceirobackend.repository.ContaRepository;
import bcd.appfinanceirobackend.repository.ImportacaoRepository;
import bcd.appfinanceirobackend.repository.TransacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ImportacaoService")
class ImportacaoServiceTest {

    @Mock
    private ContaRepository contaRepository;

    @Mock
    private ImportacaoRepository importacaoRepository;

    @Mock
    private TransacaoRepository transacaoRepository;

    @Mock
    private SugestaoCategoriaService sugestaoCategoriaService;

    @Mock
    private ParserExtrato parserMock;

    private ImportacaoService service;

    private Usuario usuarioDono;
    private Usuario usuarioEstranho;
    private Conta conta;

    @BeforeEach
    void setUp() {
        service = new ImportacaoService(
                List.of(parserMock),
                importacaoRepository,
                transacaoRepository,
                contaRepository,
                sugestaoCategoriaService
        );

        usuarioDono = new Usuario();
        usuarioDono.setId(
                UUID.randomUUID()
        );

        usuarioEstranho = new Usuario();
        usuarioEstranho.setId(
                UUID.randomUUID()
        );

        conta = new Conta();
        conta.setId(
                UUID.randomUUID()
        );
        conta.setUsuario(
                usuarioDono
        );
    }

    private MockMultipartFile csvValido(
            String conteudo
    ) {
        return arquivo(
                "arquivo",
                "extrato.csv",
                "text/csv",
                conteudo
        );
    }

    private MockMultipartFile txtValido(
            String conteudo
    ) {
        return arquivo(
                "arquivo",
                "extrato.TXT",
                "text/plain",
                conteudo
        );
    }

    private MockMultipartFile xmlNFe(
            String conteudo
    ) {
        return arquivo(
                "arquivo",
                "nota.xml",
                "application/xml",
                conteudo
        );
    }

    private MockMultipartFile xmlGenerico(
            String conteudo
    ) {
        return arquivo(
                "arquivo",
                "extrato.xml",
                "application/xml",
                conteudo
        );
    }

    private MockMultipartFile arquivo(
            String name,
            String originalFilename,
            String contentType,
            String conteudo
    ) {
        return new MockMultipartFile(
                name,
                originalFilename,
                contentType,
                conteudo.getBytes(
                        StandardCharsets.UTF_8
                )
        );
    }

    private ResultadoParser resultadoComTransacoes(
            int quantidade,
            int linhasInvalidas
    ) {
        ResultadoParser resultado =
                new ResultadoParser();

        List<Transacao> transacoes =
                new ArrayList<>();

        for (
                int i = 0;
                i < quantidade;
                i++
        ) {
            Transacao transacao =
                    new Transacao();

            transacao.setValor(
                    BigDecimal.TEN
            );

            transacao.setData(
                    LocalDate.of(
                            2024,
                            1,
                            15
                    )
            );

            transacao.setDescricao(
                    "Transação importada " +
                            (i + 1)
            );

            transacao.setConta(
                    conta
            );

            transacao.setTipo(
                    TipoTransacao.DEBITO
            );

            transacoes.add(
                    transacao
            );
        }

        resultado.setTransacoes(
                transacoes
        );

        for (
                int i = 0;
                i < linhasInvalidas;
                i++
        ) {
            resultado.adicionarErro(
                    quantidade + i + 1,
                    "estrutura",
                    "Registro inválido para teste."
            );
        }

        resultado.setTotalLinhas(
                quantidade +
                        linhasInvalidas
        );

        return resultado;
    }

    private void mockContaDoUsuarioAutenticado() {
        when(
                contaRepository.findById(
                        conta.getId()
                )
        ).thenReturn(
                Optional.of(conta)
        );
    }

    private void mockParserAceitandoComResultado(
            ResultadoParser resultado
    ) {
        when(
                parserMock.aceita(any())
        ).thenReturn(true);

        when(
                parserMock.parsear(
                        any(),
                        eq(conta)
                )
        ).thenReturn(
                resultado
        );
    }

    private void mockSaveImportacaoRegistrandoStatus(
            List<StatusImportacao> statusSalvos
    ) {
        when(
                importacaoRepository.save(
                        any(
                                Importacao.class
                        )
                )
        ).thenAnswer(
                invocation -> {
                    Importacao importacao =
                            invocation.getArgument(0);

                    statusSalvos.add(
                            importacao
                                    .getStatusImportacao()
                    );

                    return importacao;
                }
        );
    }

    private void mockSaveImportacaoRegistrandoFormato(
            List<FormatoArquivo> formatosSalvos
    ) {
        when(
                importacaoRepository.save(
                        any(
                                Importacao.class
                        )
                )
        ).thenAnswer(
                invocation -> {
                    Importacao importacao =
                            invocation.getArgument(0);

                    formatosSalvos.add(
                            importacao
                                    .getFormatoArquivo()
                    );

                    return importacao;
                }
        );
    }

    private void assertNenhumaPersistenciaDeImportacaoOuTransacao() {
        verify(
                importacaoRepository,
                never()
        ).save(any());

        verify(
                transacaoRepository,
                never()
        ).save(any());
    }

    @Nested
    @DisplayName(
            "processar() - validações de acesso e entrada"
    )
    class ValidacoesDeAcesso {

        @Test
        @DisplayName(
                "Conta inexistente lança ResourceNotFoundException e interrompe o fluxo"
        )
        void contaInexistente_lancaResourceNotFoundException() {
            when(
                    contaRepository.findById(
                            any()
                    )
            ).thenReturn(
                    Optional.empty()
            );

            assertThrows(
                    ResourceNotFoundException.class,
                    () ->
                            service.processar(
                                    csvValido(
                                            "2024-01-15,Mercado,150.00,DEBITO"
                                    ),
                                    UUID.randomUUID(),
                                    usuarioDono
                            )
            );

            assertNenhumaPersistenciaDeImportacaoOuTransacao();

            verify(
                    parserMock,
                    never()
            ).aceita(any());

            verify(
                    parserMock,
                    never()
            ).parsear(
                    any(),
                    any()
            );
        }

        @Test
        @DisplayName(
                "Conta de outro usuário lança 403 FORBIDDEN e não consulta parser"
        )
        void contaDeOutroUsuario_lancaForbidden() {
            mockContaDoUsuarioAutenticado();

            ResponseStatusException ex =
                    assertThrows(
                            ResponseStatusException.class,
                            () ->
                                    service.processar(
                                            csvValido(
                                                    "2024-01-15,Mercado,150.00,DEBITO"
                                            ),
                                            conta.getId(),
                                            usuarioEstranho
                                    )
                    );

            assertAll(
                    () ->
                            assertEquals(
                                    HttpStatus.FORBIDDEN,
                                    ex.getStatusCode()
                            ),
                    ImportacaoServiceTest.this::
                            assertNenhumaPersistenciaDeImportacaoOuTransacao,
                    () ->
                            verify(
                                    parserMock,
                                    never()
                            ).aceita(any())
            );
        }

        @Test
        @DisplayName(
                "Arquivo vazio lança IllegalArgumentException antes de criar importação"
        )
        void arquivoVazio_lancaIllegalArgumentException() {
            mockContaDoUsuarioAutenticado();

            MockMultipartFile vazio =
                    new MockMultipartFile(
                            "arquivo",
                            "extrato.csv",
                            "text/csv",
                            new byte[0]
                    );

            IllegalArgumentException ex =
                    assertThrows(
                            IllegalArgumentException.class,
                            () ->
                                    service.processar(
                                            vazio,
                                            conta.getId(),
                                            usuarioDono
                                    )
                    );

            assertEquals(
                    "Arquivo vazio",
                    ex.getMessage()
            );

            assertNenhumaPersistenciaDeImportacaoOuTransacao();
        }

        @Test
        @DisplayName(
                "Nome de arquivo nulo lança IllegalArgumentException antes de criar importação"
        )
        void nomeArquivoNulo_lancaIllegalArgumentException() {
            mockContaDoUsuarioAutenticado();

            MockMultipartFile semNome =
                    new MockMultipartFile(
                            "arquivo",
                            null,
                            "text/csv",
                            "2024-01-15,Mercado,150.00,DEBITO"
                                    .getBytes(
                                            StandardCharsets.UTF_8
                                    )
                    );

            IllegalArgumentException ex =
                    assertThrows(
                            IllegalArgumentException.class,
                            () ->
                                    service.processar(
                                            semNome,
                                            conta.getId(),
                                            usuarioDono
                                    )
                    );

            assertEquals(
                    "Nome do arquivo inválido",
                    ex.getMessage()
            );

            assertNenhumaPersistenciaDeImportacaoOuTransacao();
        }

        @Test
        @DisplayName(
                "Extensão não suportada lança 400 BAD_REQUEST antes de criar importação"
        )
        void extensaoNaoSuportada_lancaBadRequest_semSalvarImportacao() {
            mockContaDoUsuarioAutenticado();

            MockMultipartFile pdf =
                    arquivo(
                            "arquivo",
                            "extrato.pdf",
                            "application/pdf",
                            "conteúdo qualquer"
                    );

            ResponseStatusException ex =
                    assertThrows(
                            ResponseStatusException.class,
                            () ->
                                    service.processar(
                                            pdf,
                                            conta.getId(),
                                            usuarioDono
                                    )
                    );

            assertEquals(
                    HttpStatus.BAD_REQUEST,
                    ex.getStatusCode()
            );

            assertNenhumaPersistenciaDeImportacaoOuTransacao();
        }

        @Test
        @DisplayName(
                "Extensão suportada, mas sem parser compatível, lança 400 e não processa transações"
        )
        void extensaoSuportadaMasNenhumParserAceita_lancaBadRequest_semProcessarTransacoes() {
            mockContaDoUsuarioAutenticado();

            when(
                    parserMock.aceita(any())
            ).thenReturn(false);

            when(
                    importacaoRepository.save(
                            any(
                                    Importacao.class
                            )
                    )
            ).thenAnswer(
                    invocation ->
                            invocation.getArgument(0)
            );

            ResponseStatusException ex =
                    assertThrows(
                            ResponseStatusException.class,
                            () ->
                                    service.processar(
                                            csvValido(
                                                    "2024-01-15,Mercado,150.00,DEBITO"
                                            ),
                                            conta.getId(),
                                            usuarioDono
                                    )
                    );

            assertEquals(
                    HttpStatus.BAD_REQUEST,
                    ex.getStatusCode()
            );

            verify(
                    transacaoRepository,
                    never()
            ).save(any());
        }

        @Test
        @DisplayName(
                "Extensão em maiúsculo é aceita pela detecção de formato"
        )
        void extensaoMaiuscula_detectaFormatoCorretamente() {
            mockContaDoUsuarioAutenticado();

            mockParserAceitandoComResultado(
                    resultadoComTransacoes(
                            1,
                            0
                    )
            );

            List<FormatoArquivo> formatosSalvos =
                    new ArrayList<>();

            mockSaveImportacaoRegistrandoFormato(
                    formatosSalvos
            );

            service.processar(
                    txtValido(
                            "15/01/2024\tMercado\t-150,00"
                    ),
                    conta.getId(),
                    usuarioDono
            );

            assertEquals(
                    FormatoArquivo.TXT,
                    formatosSalvos.getFirst()
            );
        }
    }

    @Nested
    @DisplayName(
            "processar() - fluxo de estados e persistência"
    )
    class FluxoDeEstados {

        @Test
        @DisplayName(
                "Happy path: transições PENDENTE → PROCESSANDO → CONCLUIDO"
        )
        void happyPath_transicaoDeEstadosCorreta() {
            mockContaDoUsuarioAutenticado();

            mockParserAceitandoComResultado(
                    resultadoComTransacoes(
                            3,
                            0
                    )
            );

            List<StatusImportacao> statusSalvos =
                    new ArrayList<>();

            mockSaveImportacaoRegistrandoStatus(
                    statusSalvos
            );

            ImportacaoResponseDTO dto =
                    service.processar(
                            csvValido(
                                    "2024-01-15,Mercado,150.00,DEBITO"
                            ),
                            conta.getId(),
                            usuarioDono
                    );

            assertAll(
                    () ->
                            assertEquals(
                                    List.of(
                                            StatusImportacao.PENDENTE,
                                            StatusImportacao.PROCESSANDO,
                                            StatusImportacao.CONCLUIDO
                                    ),
                                    statusSalvos
                            ),
                    () ->
                            assertEquals(
                                    StatusImportacao.CONCLUIDO,
                                    dto.getStatus()
                            ),
                    () ->
                            assertEquals(
                                    3,
                                    dto.getSucessos()
                            ),
                    () ->
                            assertEquals(
                                    0,
                                    dto.getFalhas()
                            ),
                    () ->
                            assertTrue(
                                    dto.getErros()
                                            .isEmpty()
                            )
            );
        }

        @Test
        @DisplayName(
                "Happy path: persiste nome do arquivo, formato, usuário e resumo final da importação"
        )
        void happyPath_persisteNomeArquivoFormatoUsuarioETotaisNaImportacao() {
            mockContaDoUsuarioAutenticado();

            mockParserAceitandoComResultado(
                    resultadoComTransacoes(
                            2,
                            1
                    )
            );

            when(
                    importacaoRepository.save(
                            any(
                                    Importacao.class
                            )
                    )
            ).thenAnswer(
                    invocation ->
                            invocation.getArgument(0)
            );

            ImportacaoResponseDTO dto =
                    service.processar(
                            csvValido(
                                    "2024-01-15,Mercado,150.00,DEBITO"
                            ),
                            conta.getId(),
                            usuarioDono
                    );

            assertAll(
                    () ->
                            assertEquals(
                                    2,
                                    dto.getSucessos()
                            ),
                    () ->
                            assertEquals(
                                    1,
                                    dto.getFalhas()
                            ),
                    () ->
                            assertEquals(
                                    1,
                                    dto.getErros()
                                            .size()
                            )
            );
        }

        @Test
        @DisplayName(
                "Exceção catastrófica no parser retorna mensagem segura"
        )
        void parserLancaExcecao_statusErroComMensagemSegura() {
            mockContaDoUsuarioAutenticado();

            when(
                    parserMock.aceita(any())
            ).thenReturn(true);

            when(
                    parserMock.parsear(
                            any(),
                            eq(conta)
                    )
            ).thenThrow(
                    new RuntimeException(
                            "arquivo corrompido"
                    )
            );

            List<StatusImportacao> statusSalvos =
                    new ArrayList<>();

            mockSaveImportacaoRegistrandoStatus(
                    statusSalvos
            );

            ImportacaoResponseDTO dto =
                    service.processar(
                            csvValido(
                                    "conteúdo inválido"
                            ),
                            conta.getId(),
                            usuarioDono
                    );

            assertAll(
                    () ->
                            assertEquals(
                                    StatusImportacao.ERRO,
                                    dto.getStatus()
                            ),
                    () ->
                            assertNotNull(
                                    dto.getMensagemErro()
                            ),
                    () ->
                            assertFalse(
                                    dto.getMensagemErro()
                                            .contains(
                                                    "arquivo corrompido"
                                            )
                            ),
                    () ->
                            assertEquals(
                                    "Não foi possível processar o arquivo. " +
                                            "Verifique se o formato e os campos obrigatórios " +
                                            "estão corretos e tente novamente.",
                                    dto.getMensagemErro()
                            ),
                    () ->
                            verify(
                                    transacaoRepository,
                                    never()
                            ).save(any())
            );
        }

        @Test
        @DisplayName(
                "Falha parcial ao salvar identifica a transação sem expor erro interno"
        )
        void saveDeTransacaoFalhaParcialmente_statusConcluido() {
            mockContaDoUsuarioAutenticado();

            mockParserAceitandoComResultado(
                    resultadoComTransacoes(
                            3,
                            2
                    )
            );

            List<StatusImportacao> statusSalvos =
                    new ArrayList<>();

            mockSaveImportacaoRegistrandoStatus(
                    statusSalvos
            );

            when(
                    transacaoRepository.save(
                            any(
                                    Transacao.class
                            )
                    )
            )
                    .thenReturn(
                            new Transacao()
                    )
                    .thenThrow(
                            new RuntimeException(
                                    "constraint violation"
                            )
                    )
                    .thenReturn(
                            new Transacao()
                    );

            ImportacaoResponseDTO dto =
                    service.processar(
                            csvValido(
                                    "2024-01-15,Mercado,50.00,DEBITO"
                            ),
                            conta.getId(),
                            usuarioDono
                    );

            assertAll(
                    () ->
                            assertEquals(
                                    StatusImportacao.CONCLUIDO,
                                    dto.getStatus()
                            ),
                    () ->
                            assertEquals(
                                    2,
                                    dto.getSucessos()
                            ),
                    () ->
                            assertEquals(
                                    3,
                                    dto.getFalhas()
                            ),
                    () ->
                            assertEquals(
                                    3,
                                    dto.getErros()
                                            .size()
                            ),
                    () ->
                            assertEquals(
                                    "transacao",
                                    dto.getErros()
                                            .getLast()
                                            .getCampo()
                            ),
                    () ->
                            assertTrue(
                                    dto.getErros()
                                            .getLast()
                                            .getMensagem()
                                            .contains(
                                                    "Transação importada 2"
                                            )
                            ),
                    () ->
                            assertFalse(
                                    dto.getErros()
                                            .getLast()
                                            .getMensagem()
                                            .contains(
                                                    "constraint violation"
                                            )
                            )
            );
        }

        @Test
        @DisplayName(
                "Transações salvas recebem a importação atual"
        )
        void transacoesRetornadas_recebemImportacaoECategorizadaFalse() {
            mockContaDoUsuarioAutenticado();

            mockParserAceitandoComResultado(
                    resultadoComTransacoes(
                            2,
                            0
                    )
            );

            when(
                    importacaoRepository.save(
                            any(
                                    Importacao.class
                            )
                    )
            ).thenAnswer(
                    invocation ->
                            invocation.getArgument(0)
            );

            service.processar(
                    csvValido(
                            "2024-01-15,Desc,10.00,DEBITO"
                    ),
                    conta.getId(),
                    usuarioDono
            );

            ArgumentCaptor<Transacao> captor =
                    ArgumentCaptor.forClass(
                            Transacao.class
                    );

            verify(
                    transacaoRepository,
                    times(2)
            ).save(
                    captor.capture()
            );

            captor.getAllValues()
                    .forEach(
                            transacao ->
                                    assertAll(
                                            () ->
                                                    assertNotNull(
                                                            transacao.getImportacao()
                                                    ),
                                            () ->
                                                    assertSame(
                                                            conta,
                                                            transacao.getConta()
                                                    )
                                    )
                    );
        }

        @Test
        @DisplayName(
                "Reimportação ignora transação equivalente"
        )
        void reimportacao_comTransacaoEquivalente_ignoraDuplicata() {
            mockContaDoUsuarioAutenticado();

            mockParserAceitandoComResultado(
                    resultadoComTransacoes(
                            1,
                            0
                    )
            );

            when(
                    importacaoRepository.save(
                            any(
                                    Importacao.class
                            )
                    )
            ).thenAnswer(
                    invocation ->
                            invocation.getArgument(0)
            );

            when(
                    transacaoRepository
                            .existsByContaIdAndDataAndDescricaoAndValorAndTipo(
                                    any(),
                                    any(),
                                    any(),
                                    any(),
                                    any()
                            )
            ).thenReturn(true);

            ImportacaoResponseDTO dto =
                    service.processar(
                            csvValido(
                                    "2024-01-15,Transação importada 1,10.00,DEBITO"
                            ),
                            conta.getId(),
                            usuarioDono
                    );

            assertAll(
                    () ->
                            assertEquals(
                                    0,
                                    dto.getSucessos()
                            ),
                    () ->
                            assertEquals(
                                    1,
                                    dto.getIgnoradasPorDuplicidade()
                            ),
                    () ->
                            verify(
                                    transacaoRepository,
                                    never()
                            ).save(any())
            );
        }

        @Test
        @DisplayName(
                "Arquivo XML com marcador NF-e detecta FormatoArquivo.NFE"
        )
        void xmlComMarcadorNFe_detectaFormatoNFE() {
            mockContaDoUsuarioAutenticado();

            mockParserAceitandoComResultado(
                    resultadoComTransacoes(
                            1,
                            0
                    )
            );

            List<FormatoArquivo> formatosSalvos =
                    new ArrayList<>();

            mockSaveImportacaoRegistrandoFormato(
                    formatosSalvos
            );

            service.processar(
                    xmlNFe(
                            "<nfeProc versao=\"4.00\"><NFe></NFe></nfeProc>"
                    ),
                    conta.getId(),
                    usuarioDono
            );

            assertEquals(
                    FormatoArquivo.NFE,
                    formatosSalvos.getFirst()
            );
        }

        @Test
        @DisplayName(
                "Arquivo XML sem marcador NF-e detecta FormatoArquivo.XML"
        )
        void xmlSemMarcadorNFe_detectaFormatoXML() {
            mockContaDoUsuarioAutenticado();

            mockParserAceitandoComResultado(
                    resultadoComTransacoes(
                            1,
                            0
                    )
            );

            List<FormatoArquivo> formatosSalvos =
                    new ArrayList<>();

            mockSaveImportacaoRegistrandoFormato(
                    formatosSalvos
            );

            service.processar(
                    xmlGenerico(
                            "<extrato><transacao><data>2024-01-15</data></transacao></extrato>"
                    ),
                    conta.getId(),
                    usuarioDono
            );

            assertEquals(
                    FormatoArquivo.XML,
                    formatosSalvos.getFirst()
            );
        }
    }

    @Nested
    @DisplayName(
            "buscarStatus()"
    )
    class BuscarStatus {

        private Importacao importacaoDoDono;

        @BeforeEach
        void setUpImportacao() {
            importacaoDoDono =
                    new Importacao();

            importacaoDoDono.setId(
                    UUID.randomUUID()
            );

            importacaoDoDono.setUsuario(
                    usuarioDono
            );

            importacaoDoDono.setStatusImportacao(
                    StatusImportacao.CONCLUIDO
            );
        }

        @Test
        @DisplayName(
                "Importação inexistente lança 404"
        )
        void importacaoInexistente_lanca404() {
            when(
                    importacaoRepository.findById(
                            any()
                    )
            ).thenReturn(
                    Optional.empty()
            );

            ResponseStatusException ex =
                    assertThrows(
                            ResponseStatusException.class,
                            () ->
                                    service.buscarStatus(
                                            UUID.randomUUID(),
                                            usuarioDono
                                    )
                    );

            assertEquals(
                    HttpStatus.NOT_FOUND,
                    ex.getStatusCode()
            );
        }

        @Test
        @DisplayName(
                "Importação de outro usuário lança 403"
        )
        void importacaoDeOutroUsuario_lanca403() {
            when(
                    importacaoRepository.findById(
                            importacaoDoDono.getId()
                    )
            ).thenReturn(
                    Optional.of(
                            importacaoDoDono
                    )
            );

            ResponseStatusException ex =
                    assertThrows(
                            ResponseStatusException.class,
                            () ->
                                    service.buscarStatus(
                                            importacaoDoDono.getId(),
                                            usuarioEstranho
                                    )
                    );

            assertEquals(
                    HttpStatus.FORBIDDEN,
                    ex.getStatusCode()
            );
        }

        @Test
        @DisplayName(
                "Dono consulta status com sucesso"
        )
        void donoConsulta_retornaStatusCorreto() {
            when(
                    importacaoRepository.findById(
                            importacaoDoDono.getId()
                    )
            ).thenReturn(
                    Optional.of(
                            importacaoDoDono
                    )
            );

            StatusImportacao status =
                    service.buscarStatus(
                            importacaoDoDono.getId(),
                            usuarioDono
                    );

            assertEquals(
                    StatusImportacao.CONCLUIDO,
                    status
            );
        }
    }
}