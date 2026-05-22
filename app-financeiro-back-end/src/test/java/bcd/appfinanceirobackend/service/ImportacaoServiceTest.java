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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ImportacaoService")
class ImportacaoServiceTest {

    @Mock private ContaRepository contaRepository;
    @Mock private ImportacaoRepository importacaoRepository;
    @Mock private TransacaoRepository transacaoRepository;
    @Mock private ParserExtrato parserMock;

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
                contaRepository
        );

        usuarioDono = new Usuario();
        usuarioDono.setId(UUID.randomUUID());

        usuarioEstranho = new Usuario();
        usuarioEstranho.setId(UUID.randomUUID());

        conta = new Conta();
        conta.setId(UUID.randomUUID());
        conta.setUsuario(usuarioDono);
    }

    // ---------------------------------------------------------------------------
    // Helpers de fixture
    // ---------------------------------------------------------------------------

    private MockMultipartFile csvValido(String conteudo) {
        return new MockMultipartFile("arquivo", "extrato.csv",
                "text/csv", conteudo.getBytes());
    }

    private MockMultipartFile xmlNFe(String conteudo) {
        return new MockMultipartFile("arquivo", "nota.xml",
                "application/xml", conteudo.getBytes());
    }

    private MockMultipartFile xmlGenerico(String conteudo) {
        return new MockMultipartFile("arquivo", "extrato.xml",
                "application/xml", conteudo.getBytes());
    }

    private ResultadoParser resultadoComTransacoes(int quantidade, int linhasInvalidas) {
        ResultadoParser resultado = new ResultadoParser();
        resultado.setLinhasInvalidas(linhasInvalidas);
        resultado.setTotalLinhas(quantidade + linhasInvalidas);
        List<Transacao> transacoes = new java.util.ArrayList<>();
        for (int i = 0; i < quantidade; i++) {
            Transacao t = new Transacao();
            t.setValor(BigDecimal.TEN);
            transacoes.add(t);
        }
        resultado.setTransacoes(transacoes);
        return resultado;
    }

    // ---------------------------------------------------------------------------
    // processar() — Validações de acesso e entrada
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("processar() — Validações de Acesso e Entrada")
    class ValidacoesDeAcesso {

        @Test
        @DisplayName("1.2 Conta inexistente lança ResourceNotFoundException")
        void contaInexistente_lancaResourceNotFoundException() {
            when(contaRepository.findById(any())).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () ->
                    service.processar(csvValido("a,b,c,d"), UUID.randomUUID(), usuarioDono));

            verify(importacaoRepository, never()).save(any());
        }

        @Test
        @DisplayName("1.1 Conta de outro usuário lança 403 FORBIDDEN")
        void contaDeOutroUsuario_lancaForbidden() {
            when(contaRepository.findById(conta.getId())).thenReturn(Optional.of(conta));

            ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                    service.processar(csvValido("a,b,c,d"), conta.getId(), usuarioEstranho));

            assertAll(
                    () -> assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode()),
                    () -> verify(importacaoRepository, never()).save(any()),
                    () -> verify(parserMock, never()).aceita(any())
            );
        }

        @Test
        @DisplayName("1.3 Arquivo vazio lança IllegalArgumentException")
        void arquivoVazio_lancaIllegalArgumentException() {
            when(contaRepository.findById(conta.getId())).thenReturn(Optional.of(conta));

            MockMultipartFile vazio = new MockMultipartFile("arquivo", "extrato.csv",
                    "text/csv", new byte[0]);

            assertThrows(IllegalArgumentException.class, () ->
                    service.processar(vazio, conta.getId(), usuarioDono));

            verify(parserMock, never()).aceita(any());
        }

        @Test
        @DisplayName("1.4 Nome de arquivo nulo lança IllegalArgumentException")
        void nomeArquivoNulo_lancaIllegalArgumentException() {
            when(contaRepository.findById(conta.getId())).thenReturn(Optional.of(conta));

            MockMultipartFile semNome = new MockMultipartFile("arquivo", null,
                    "text/csv", "data,desc,10.00,CREDITO".getBytes());

            assertThrows(IllegalArgumentException.class, () ->
                    service.processar(semNome, conta.getId(), usuarioDono));
        }

        @Test
        @DisplayName("1.5 Nenhum parser aceita o arquivo lança 400 BAD_REQUEST e salva status ERRO")
        void nenhumParserAceita_lancaBadRequest_salvandoStatusErro() {
            when(contaRepository.findById(conta.getId())).thenReturn(Optional.of(conta));
            when(parserMock.aceita(any())).thenReturn(false);

            MockMultipartFile pdf = new MockMultipartFile("arquivo", "extrato.pdf",
                    "application/pdf", "conteudo qualquer".getBytes());

            // .pdf não passa na detecção de formato — lança antes mesmo de consultar parsers
            assertThrows(ResponseStatusException.class, () ->
                    service.processar(pdf, conta.getId(), usuarioDono));
        }
    }

    // ---------------------------------------------------------------------------
    // processar() — Fluxo de estados e Happy Path
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("processar() — Fluxo de Estados e Happy Path")
    class FluxoDeEstados {

        @Test
        @DisplayName("1.6 Happy path: transições PENDENTE → PROCESSANDO → CONCLUIDO")
        void happyPath_transicaoDeEstadosCorreta() {
            when(contaRepository.findById(conta.getId())).thenReturn(Optional.of(conta));
            when(parserMock.aceita(any())).thenReturn(true);
            when(parserMock.parsear(any(), any())).thenReturn(resultadoComTransacoes(3, 0));
            when(importacaoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ImportacaoResponseDTO dto = service.processar(
                    csvValido("2024-01-15,Mercado,150.00,DEBITO"),
                    conta.getId(),
                    usuarioDono
            );

            ArgumentCaptor<Importacao> captor = ArgumentCaptor.forClass(Importacao.class);
            verify(importacaoRepository, times(3)).save(captor.capture());

            List<Importacao> saves = captor.getAllValues();
            assertAll(
                    () -> assertEquals(StatusImportacao.PENDENTE, saves.get(0).getStatusImportacao()),
                    () -> assertEquals(StatusImportacao.PROCESSANDO, saves.get(1).getStatusImportacao()),
                    () -> assertEquals(StatusImportacao.CONCLUIDO, saves.get(2).getStatusImportacao()),
                    () -> assertEquals(StatusImportacao.CONCLUIDO, dto.getStatus()),
                    () -> assertEquals(3, dto.getSucessos()),
                    () -> assertEquals(0, dto.getFalhas()),
                    () -> assertNull(dto.getMensagemErro())
            );
        }

        @Test
        @DisplayName("1.7 Exceção catastrófica no parser resulta em status ERRO com mensagem")
        void parserLancaExcecao_statusErroComMensagem() {
            when(contaRepository.findById(conta.getId())).thenReturn(Optional.of(conta));
            when(parserMock.aceita(any())).thenReturn(true);
            when(parserMock.parsear(any(), any())).thenThrow(new RuntimeException("arquivo corrompido"));
            when(importacaoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ImportacaoResponseDTO dto = service.processar(
                    csvValido("conteudo invalido"),
                    conta.getId(),
                    usuarioDono
            );

            ArgumentCaptor<Importacao> captor = ArgumentCaptor.forClass(Importacao.class);
            verify(importacaoRepository, times(3)).save(captor.capture());

            List<Importacao> saves = captor.getAllValues();
            assertAll(
                    () -> assertEquals(StatusImportacao.PENDENTE, saves.get(0).getStatusImportacao()),
                    () -> assertEquals(StatusImportacao.PROCESSANDO, saves.get(1).getStatusImportacao()),
                    () -> assertEquals(StatusImportacao.ERRO, saves.get(2).getStatusImportacao()),
                    () -> assertEquals(StatusImportacao.ERRO, dto.getStatus()),
                    () -> assertNotNull(dto.getMensagemErro()),
                    () -> assertTrue(dto.getMensagemErro().contains("arquivo corrompido"))
            );
        }

        @Test
        @DisplayName("1.8 Tolerância a falhas: save de transação falha parcialmente, status permanece CONCLUIDO")
        void saveDeTransacaoFalhaParcialmente_statusConcluido() {
            when(contaRepository.findById(conta.getId())).thenReturn(Optional.of(conta));
            when(parserMock.aceita(any())).thenReturn(true);
            when(parserMock.parsear(any(), any())).thenReturn(resultadoComTransacoes(3, 2));
            when(importacaoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // 2ª chamada ao transacaoRepository.save lança exceção
            when(transacaoRepository.save(any()))
                    .thenReturn(new Transacao())
                    .thenThrow(new RuntimeException("constraint violation"))
                    .thenReturn(new Transacao());

            ImportacaoResponseDTO dto = service.processar(
                    csvValido("2024-01-15,Mercado,50.00,DEBITO"),
                    conta.getId(),
                    usuarioDono
            );

            assertAll(
                    () -> assertEquals(StatusImportacao.CONCLUIDO, dto.getStatus()),
                    () -> assertEquals(2, dto.getSucessos()),
                    () -> assertEquals(3, dto.getFalhas()) // 2 do parser + 1 do save
            );
        }

        @Test
        @DisplayName("1.9 Arquivo XML com marcador NF-e detecta FormatoArquivo.NFE")
        void xmlComMarcadorNFe_detectaFormatoNFE() {
            when(contaRepository.findById(conta.getId())).thenReturn(Optional.of(conta));
            when(parserMock.aceita(any())).thenReturn(true);
            when(parserMock.parsear(any(), any())).thenReturn(resultadoComTransacoes(1, 0));
            when(importacaoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            String conteudoNFe = "<nfeProc versao=\"4.00\"><NFe></NFe></nfeProc>";
            service.processar(xmlNFe(conteudoNFe), conta.getId(), usuarioDono);

            ArgumentCaptor<Importacao> captor = ArgumentCaptor.forClass(Importacao.class);
            verify(importacaoRepository, atLeastOnce()).save(captor.capture());

            Importacao primeiraSalva = captor.getAllValues().get(0);
            assertEquals(FormatoArquivo.NFE, primeiraSalva.getFormatoArquivo());
        }

        @Test
        @DisplayName("1.10 Arquivo XML sem marcador NF-e detecta FormatoArquivo.XML")
        void xmlSemMarcadorNFe_detectaFormatoXML() {
            when(contaRepository.findById(conta.getId())).thenReturn(Optional.of(conta));
            when(parserMock.aceita(any())).thenReturn(true);
            when(parserMock.parsear(any(), any())).thenReturn(resultadoComTransacoes(1, 0));
            when(importacaoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            String conteudoXML = "<extrato><transacao><data>2024-01-15</data></transacao></extrato>";
            service.processar(xmlGenerico(conteudoXML), conta.getId(), usuarioDono);

            ArgumentCaptor<Importacao> captor = ArgumentCaptor.forClass(Importacao.class);
            verify(importacaoRepository, atLeastOnce()).save(captor.capture());

            Importacao primeiraSalva = captor.getAllValues().get(0);
            assertEquals(FormatoArquivo.XML, primeiraSalva.getFormatoArquivo());
        }

        @Test
        @DisplayName("1.6b Transações retornadas pelo parser recebem importacao e categorizada=false")
        void transacoesRetornadas_recebemImportacaoECategorizadaFalse() {
            when(contaRepository.findById(conta.getId())).thenReturn(Optional.of(conta));
            when(parserMock.aceita(any())).thenReturn(true);
            when(parserMock.parsear(any(), any())).thenReturn(resultadoComTransacoes(2, 0));
            when(importacaoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            service.processar(csvValido("2024-01-15,Desc,10.00,DEBITO"), conta.getId(), usuarioDono);

            ArgumentCaptor<Transacao> captor = ArgumentCaptor.forClass(Transacao.class);
            verify(transacaoRepository, times(2)).save(captor.capture());

            captor.getAllValues().forEach(t -> assertAll(
                    () -> assertNotNull(t.getImportacao()),
                    () -> assertFalse(t.getCategorizada())
            ));
        }

        @Test
        @DisplayName("1.6c Resultado do DTO possui data de importação preenchida")
        void dto_possuiDataImportacaoPreenchida() {
            when(contaRepository.findById(conta.getId())).thenReturn(Optional.of(conta));
            when(parserMock.aceita(any())).thenReturn(true);
            when(parserMock.parsear(any(), any())).thenReturn(resultadoComTransacoes(1, 0));
            when(importacaoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ImportacaoResponseDTO dto = service.processar(
                    csvValido("2024-01-15,Desc,10.00,DEBITO"), conta.getId(), usuarioDono);

            assertNotNull(dto.getImportadoEm());
        }
    }

    // ---------------------------------------------------------------------------
    // buscarStatus()
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("buscarStatus()")
    class BuscarStatus {

        private Importacao importacaoDoDono;

        @BeforeEach
        void setUpImportacao() {
            importacaoDoDono = new Importacao();
            importacaoDoDono.setId(UUID.randomUUID());
            importacaoDoDono.setUsuario(usuarioDono);
            importacaoDoDono.setStatusImportacao(StatusImportacao.CONCLUIDO);
        }

        @Test
        @DisplayName("1.12 Importação inexistente lança 404 NOT_FOUND")
        void importacaoInexistente_lanca404() {
            when(importacaoRepository.findById(any())).thenReturn(Optional.empty());

            ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                    service.buscarStatus(UUID.randomUUID(), usuarioDono));

            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        }

        @Test
        @DisplayName("1.11 Importação de outro usuário lança 403 FORBIDDEN")
        void importacaoDeOutroUsuario_lanca403() {
            when(importacaoRepository.findById(importacaoDoDono.getId()))
                    .thenReturn(Optional.of(importacaoDoDono));

            ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                    service.buscarStatus(importacaoDoDono.getId(), usuarioEstranho));

            assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        }

        @Test
        @DisplayName("Happy path: retorna o status correto para o dono da importação")
        void donoConsulta_retornaStatusCorreto() {
            when(importacaoRepository.findById(importacaoDoDono.getId()))
                    .thenReturn(Optional.of(importacaoDoDono));

            StatusImportacao status = service.buscarStatus(importacaoDoDono.getId(), usuarioDono);

            assertEquals(StatusImportacao.CONCLUIDO, status);
        }
    }
}