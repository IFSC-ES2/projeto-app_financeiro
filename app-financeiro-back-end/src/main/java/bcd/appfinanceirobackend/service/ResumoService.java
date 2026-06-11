package bcd.appfinanceirobackend.service;

import bcd.appfinanceirobackend.dto.resumo.GrupoCategoriaDTO;
import bcd.appfinanceirobackend.dto.resumo.GrupoPagamentoDTO;
import bcd.appfinanceirobackend.dto.resumo.ResumoMensalDTO;
import bcd.appfinanceirobackend.exception.ResourceNotFoundException;
import bcd.appfinanceirobackend.model.Categoria;
import bcd.appfinanceirobackend.model.Transacao;
import bcd.appfinanceirobackend.model.Usuario;
import bcd.appfinanceirobackend.model.enums.TipoPagamento;
import bcd.appfinanceirobackend.model.enums.TipoTransacao;
import bcd.appfinanceirobackend.repository.CategoriaRepository;
import bcd.appfinanceirobackend.repository.TransacaoRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Service
public class ResumoService {

    private static final String CHAVE_NAO_INFORMADO = "NAO_INFORMADO";

    public record PeriodoResumo(Integer ano, Integer mes, LocalDate dataInicio, LocalDate dataFim){ }
    private final TransacaoRepository transacaoRepository;
    private final CategoriaRepository categoriaRepository;

    public ResumoService(
            TransacaoRepository transacaoRepository,
            CategoriaRepository categoriaRepository
    ) {
        this.transacaoRepository = transacaoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    public ResumoMensalDTO gerarResumoMensal(Usuario usuario, Integer ano, Integer mes){
        validarUsuarioAutenticado(usuario);
        PeriodoResumo periodoResumoAtual = resolverPeriodo(ano, mes);
        PeriodoResumo periodoResumoAnterior = calcularPeriodoAnterior(periodoResumoAtual);
        List<Transacao> transacoesMesAtual = transacaoRepository.findAllByContaUsuarioIdAndDataBetween(
                usuario.getId(),
                periodoResumoAtual.dataInicio(),
                periodoResumoAtual.dataFim()
        );
        List<Transacao> transacoesMesAnterior = transacaoRepository.findAllByContaUsuarioIdAndDataBetween(
                usuario.getId(),
                periodoResumoAnterior.dataInicio(),
                periodoResumoAnterior.dataFim()
        );
        BigDecimal totalRecebidoMesAtual = somarReceitas(transacoesMesAtual);
        BigDecimal totalGastoMesAtual = somarGastos(transacoesMesAtual);
        BigDecimal saldoMesAtual = totalRecebidoMesAtual.subtract(totalGastoMesAtual);
        BigDecimal totalGastoMesAnterior = somarGastos(transacoesMesAnterior);
        BigDecimal variacaoPercentualGastos =
                calcularVariacaoPercentualGastos(totalGastoMesAtual, totalGastoMesAnterior);
        List<GrupoCategoriaDTO> gruposCategoria = montarGruposCategoria(transacoesMesAtual);
        ResumoMensalDTO resumoMensalDTO = new ResumoMensalDTO();
        if(gruposCategoria.isEmpty()){
            resumoMensalDTO.setCategoriaMaiorGastoTotal(BigDecimal.ZERO);
            resumoMensalDTO.setPossuiTransacoes(false);
        }
        else {
            GrupoCategoriaDTO maiorLista = gruposCategoria.getFirst();
            resumoMensalDTO.setCategoriaMaiorGastoId(maiorLista.getCategoriaID());
            resumoMensalDTO.setCategoriaMaiorGastoNome(maiorLista.getNome());
            resumoMensalDTO.setCategoriaMaiorGastoTotal(maiorLista.getTotal());
            resumoMensalDTO.setPossuiTransacoes(true);
        }
        resumoMensalDTO.setAno(periodoResumoAtual.ano());
        resumoMensalDTO.setMes(periodoResumoAtual.mes());
        resumoMensalDTO.setDataInicio(periodoResumoAtual.dataInicio());
        resumoMensalDTO.setDataFim(periodoResumoAtual.dataFim());
        resumoMensalDTO.setTotalRecebido(totalRecebidoMesAtual);
        resumoMensalDTO.setTotalGasto(totalGastoMesAtual);
        resumoMensalDTO.setSaldo(saldoMesAtual);
        resumoMensalDTO.setVariacaoPercentualGastos(variacaoPercentualGastos);
        return resumoMensalDTO;
    }

    public List<GrupoPagamentoDTO> agruparFormaPagamento(Usuario usuarioAutenticado) {
        validarUsuarioAutenticado(usuarioAutenticado);

        List<Transacao> transacoes = transacaoRepository.findAllByContaUsuarioId(usuarioAutenticado.getId());

        if (transacoes.isEmpty()) {
            return new ArrayList<>();
        }

        BigDecimal totalGeral = BigDecimal.ZERO;
        Map<String, List<Transacao>> agrupamentoPagamentos = new HashMap<>();

        for (Transacao transacao : transacoes) {
            totalGeral = totalGeral.add(transacao.getValor());

            String chaveAgrupamento = obterChaveAgrupamento(transacao.getFormaPagamento());

            agrupamentoPagamentos
                    .computeIfAbsent(chaveAgrupamento, chave -> new ArrayList<>())
                    .add(transacao);
        }

        if (totalGeral.compareTo(BigDecimal.ZERO) == 0) {
            return new ArrayList<>();
        }

        List<GrupoPagamentoDTO> gruposPagamento = new ArrayList<>();

        for (Map.Entry<String, List<Transacao>> entry : agrupamentoPagamentos.entrySet()) {
            gruposPagamento.add(criarGrupoPagamentoDTO(entry, totalGeral));
        }

        return gruposPagamento;
    }

    public List<GrupoCategoriaDTO> agruparPorCategoria(Usuario usuarioAutenticado, Integer ano, Integer mes){
            validarUsuarioAutenticado(usuarioAutenticado);
            PeriodoResumo periodoSolicitado = resolverPeriodo(ano, mes);
            List<Transacao> transacoes = transacaoRepository.findAllByContaUsuarioIdAndDataBetween(
                    usuarioAutenticado.getId(),
                    periodoSolicitado.dataInicio(),
                    periodoSolicitado.dataFim()
            );
            return montarGruposCategoria(transacoes);
    }

    private List<GrupoCategoriaDTO> montarGruposCategoria(List<Transacao> transacoes) {
        if(transacoes.isEmpty()){
            return new ArrayList<>();
        }
        List<Transacao> gastos = new ArrayList<>();
        for (Transacao transacao : transacoes){
            if(transacao.getTipo() == TipoTransacao.DEBITO) {
                gastos.add(transacao);
            }
        }
        if(gastos.isEmpty()) return new ArrayList<>();
        BigDecimal totalGastoGeral = somarGastos(gastos);
        Map<String, List<Transacao>> agrupamentoCategorias = new HashMap<>();
        String chave = "";
        for (Transacao transacao: gastos) {
            if(transacao.getCategoria()!= null){
                chave = String.valueOf(transacao.getCategoria().getId());
            } else {
                chave = CHAVE_NAO_INFORMADO;
            }
            agrupamentoCategorias.
                    computeIfAbsent(chave, key -> new ArrayList<>())
                    .add(transacao);
        }
        List<GrupoCategoriaDTO> grupoCategorias = new ArrayList<>();

        for (Map.Entry<String, List<Transacao>> entry : agrupamentoCategorias.entrySet()) {
            grupoCategorias.add(criarGrupoCategoriaDTO(entry, totalGastoGeral));
        }
        grupoCategorias.sort(
                //Comparing ordena lista do menor para o maior sempre, nessa linha ele faz isso baseado no getTotal
                //o reversed está presente para inverter essa ordenação!
                Comparator.comparing(GrupoCategoriaDTO::getTotal).reversed()
        );
        return grupoCategorias;
    }

    private GrupoCategoriaDTO criarGrupoCategoriaDTO(Map.Entry<String, List<Transacao>> entry, BigDecimal totalGeral){
        String chaveAgrupamento = entry.getKey();
        List<Transacao> transacoesDoGrupo = entry.getValue();
        BigDecimal totalGrupo = BigDecimal.ZERO;

        for (Transacao transacao : transacoesDoGrupo) {
            totalGrupo = totalGrupo.add(transacao.getValor());
        }

        BigDecimal percentualGrupo = totalGrupo
                .multiply(BigDecimal.valueOf(100))
                .divide(totalGeral, 2, RoundingMode.HALF_UP);

        GrupoCategoriaDTO grupoCategoriaDTO = new GrupoCategoriaDTO();

        if(obterCategoriaId(chaveAgrupamento) != null){
            Categoria categoria = categoriaRepository.findById(
                            Objects.requireNonNull(obterCategoriaId(chaveAgrupamento)))
                    .orElseThrow(() -> new ResourceNotFoundException("Categoria Inexistente"));

            grupoCategoriaDTO.setCategoriaID(categoria.getId());
            grupoCategoriaDTO.setNome(categoria.getNome());
            grupoCategoriaDTO.setIcone(categoria.getIcone());
            grupoCategoriaDTO.setCor(categoria.getCor());
        } else {
            grupoCategoriaDTO.setNome("Sem categoria");
        }

        grupoCategoriaDTO.setTotal(totalGrupo);
        grupoCategoriaDTO.setQuantidade(transacoesDoGrupo.size());
        grupoCategoriaDTO.setPercentual(percentualGrupo);

        return grupoCategoriaDTO;
    }

    private GrupoPagamentoDTO criarGrupoPagamentoDTO(Map.Entry<String, List<Transacao>> entry, BigDecimal totalGeral) {
        String chaveAgrupamento = entry.getKey();
        List<Transacao> transacoesDoGrupo = entry.getValue();

        BigDecimal totalGrupo = BigDecimal.ZERO;

        for (Transacao transacao : transacoesDoGrupo) {
            totalGrupo = totalGrupo.add(transacao.getValor());
        }

        BigDecimal percentualGrupo = totalGrupo
                .multiply(BigDecimal.valueOf(100))
                .divide(totalGeral, 2, RoundingMode.HALF_UP);

        GrupoPagamentoDTO grupoPagamentoDTO = new GrupoPagamentoDTO();
        grupoPagamentoDTO.setFormaPagamento(obterFormaPagamento(chaveAgrupamento));
        grupoPagamentoDTO.setRotulo(obterRotulo(chaveAgrupamento));
        grupoPagamentoDTO.setTotal(totalGrupo);
        grupoPagamentoDTO.setQuantidade(transacoesDoGrupo.size());
        grupoPagamentoDTO.setPercentual(percentualGrupo);

        return grupoPagamentoDTO;
    }


    private String obterChaveAgrupamento(TipoPagamento formaPagamento) {
        if (formaPagamento == null) {
            return CHAVE_NAO_INFORMADO;
        }

        return formaPagamento.name();
    }



    private TipoPagamento obterFormaPagamento(String chaveAgrupamento) {
        if (CHAVE_NAO_INFORMADO.equals(chaveAgrupamento)) {
            return null;
        }

        return TipoPagamento.valueOf(chaveAgrupamento);
    }

    private String obterRotulo(String chaveAgrupamento) {
        if (CHAVE_NAO_INFORMADO.equals(chaveAgrupamento)) {
            return "Não informado";
        }

        TipoPagamento formaPagamento = TipoPagamento.valueOf(chaveAgrupamento);

        return switch (formaPagamento) {
            case PIX -> "Pix";
            case CARTAO_DEBITO -> "Cartão de débito";
            case CARTAO_CREDITO -> "Cartão de crédito";
            case DINHEIRO -> "Dinheiro";
            case BOLETO -> "Boleto";
            case TED_DOC -> "TED/DOC";
        };
    }

    private UUID obterCategoriaId(String chaveAgrupamento){
        if(CHAVE_NAO_INFORMADO.equals(chaveAgrupamento)){
            return null;
        }
        return UUID.fromString(chaveAgrupamento);
    }

    private void validarUsuarioAutenticado(Usuario usuario){
        if (usuario == null || usuario.getId() == null) {
            throw new AccessDeniedException("Usuário autenticado não encontrado.");
        }
    }
    private PeriodoResumo resolverPeriodo(Integer ano, Integer mes){
        if(ano == null && mes == null){
            ano = LocalDate.now().getYear();
            mes = LocalDate.now().getMonth().getValue();
        }
        else if (ano != null && mes == null){
            throw new IllegalArgumentException("Apenas o ano foi informado, informe o mês e o ano");
        }
        else if (ano == null) {
            throw new IllegalArgumentException("Apenas o mês foi informado, informe o ano e o mês");
        }
        if(mes < 1 || mes > 12){
            throw new IllegalArgumentException("Mês informado inválido");
        }
        LocalDate dataInicio = LocalDate.of(ano, mes, 1);
        LocalDate dataFim = YearMonth.of(ano, mes).atEndOfMonth();
        return new PeriodoResumo(ano, mes, dataInicio, dataFim);
    }

    private PeriodoResumo calcularPeriodoAnterior(PeriodoResumo periodoAtual){
        YearMonth dataReferenciaAnterior =
                YearMonth.of(periodoAtual.ano(), periodoAtual.mes()).minusMonths(1);
        LocalDate dataInicioAnterior = LocalDate.of(dataReferenciaAnterior.getYear(),
                dataReferenciaAnterior.getMonth(), 1);
        LocalDate dataFimAnterior = dataReferenciaAnterior.atEndOfMonth();
        return new PeriodoResumo(dataReferenciaAnterior.getYear(),
                dataReferenciaAnterior.getMonth().getValue(),
                dataInicioAnterior, dataFimAnterior);
    }

    private BigDecimal somarReceitas(List<Transacao> transacoes){
        BigDecimal total = BigDecimal.ZERO;
        for (Transacao transacao: transacoes) {
            if(transacao.getTipo() == TipoTransacao.CREDITO){
                total = total.add(transacao.getValor());
            }
        }
        return total;
    }

    private BigDecimal somarGastos(List<Transacao> transacoes){
        BigDecimal total = BigDecimal.ZERO;
        for (Transacao transacao: transacoes) {
            if(transacao.getTipo() == TipoTransacao.DEBITO){
                total = total.add(transacao.getValor());
            }
        }
        return total;
    }

    private BigDecimal calcularVariacaoPercentualGastos(BigDecimal gastoAtual, BigDecimal gastoAnterior){
        if(gastoAnterior.equals(BigDecimal.ZERO) && gastoAtual.equals(BigDecimal.ZERO)){
            return BigDecimal.ZERO;
        }
        else if (gastoAnterior.equals(BigDecimal.ZERO) && gastoAtual.compareTo(BigDecimal.ZERO) > 0) {
            return BigDecimal.valueOf(100);
        }

        BigDecimal diferenca = gastoAtual.subtract(gastoAnterior);
        return diferenca.
                divide(gastoAnterior, 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

}