import { useEffect, useMemo, useState } from 'react';
import ListaCategorias from '../components/categorias/ListaCategorias';
import ResumoCategorias from '../components/categorias/ResumoCategorias';
import LayoutPrivado from '../components/layout/LayoutPrivado';
import EstadoVazio from '../components/ui/EstadoVazio';
import MensagemAlerta from '../components/ui/MensagemAlerta';
import { obterMensagemErroApi } from '../services/apiError';
import { listarCategorias } from '../services/categoriaService';
import { listarContas } from '../services/contaService';
import { listarTransacoes } from '../services/transacaoService';
import type { CategoriaResponse } from '../types/categoria';
import type { ContaResponse } from '../types/conta';
import type { TransacaoResponse } from '../types/transacao';
import {
  alterarAnoMes,
  calcularResumoCategoriasMensal,
  formatarRotuloMes,
  montarGradienteCategorias,
  obterIntervaloMes,
  obterMesAtual,
} from '../utils/categorias';

const TAMANHO_PAGINA_TRANSACOES = 100;

const Categorias = () => {
  const [mesSelecionado, setMesSelecionado] = useState(obterMesAtual);
  const [categorias, setCategorias] = useState<CategoriaResponse[]>([]);
  const [contas, setContas] = useState<ContaResponse[]>([]);
  const [transacoes, setTransacoes] = useState<TransacaoResponse[]>([]);
  const [categoriasAbertas, setCategoriasAbertas] = useState<Set<string>>(new Set());
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState('');

  const intervaloMes = useMemo(() => obterIntervaloMes(mesSelecionado), [mesSelecionado]);
  const rotuloMes = useMemo(() => formatarRotuloMes(mesSelecionado), [mesSelecionado]);

  useEffect(() => {
    let ativo = true;

    const carregarDados = async () => {
      setCarregando(true);
      setErro('');

      try {
        const [categoriasCarregadas, contasCarregadas] = await Promise.all([listarCategorias(), listarContas()]);
        const transacoesCarregadas: TransacaoResponse[] = [];
        let paginaAtual = 0;
        let ultimaPagina = false;

        while (!ultimaPagina) {
          const pagina = await listarTransacoes({
            page: paginaAtual,
            size: TAMANHO_PAGINA_TRANSACOES,
            dataInicio: intervaloMes.dataInicio,
            dataFim: intervaloMes.dataFim,
          });

          transacoesCarregadas.push(...pagina.conteudo);
          ultimaPagina = pagina.ultima || paginaAtual + 1 >= pagina.totalPaginas;
          paginaAtual += 1;
        }

        if (!ativo) return;

        setCategorias(categoriasCarregadas);
        setContas(contasCarregadas);
        setTransacoes(transacoesCarregadas.filter((transacao) =>
          transacao.data >= intervaloMes.dataInicio
          && transacao.data <= intervaloMes.dataFim,
        ));
        setCategoriasAbertas(new Set());
      } catch (erroCapturado) {
        if (!ativo) return;
        setErro(obterMensagemErroApi(erroCapturado, 'Não foi possível carregar os gastos por categoria.'));
        setCategorias([]);
        setContas([]);
        setTransacoes([]);
      } finally {
        if (ativo) setCarregando(false);
      }
    };

    carregarDados();
    return () => { ativo = false; };
  }, [intervaloMes.dataFim, intervaloMes.dataInicio]);

  const resumoCategorias = useMemo(
    () => calcularResumoCategoriasMensal(categorias, transacoes),
    [categorias, transacoes],
  );
  const totalMovimentado = useMemo(
    () => resumoCategorias.reduce((total, categoria) => total + categoria.total, 0),
    [resumoCategorias],
  );
  const gradienteGrafico = useMemo(() => montarGradienteCategorias(resumoCategorias), [resumoCategorias]);
  const contasPorId = useMemo(() => new Map(contas.map((conta) => [conta.contaId, conta.nome])), [contas]);

  const alternarCategoria = (categoriaId: string) => {
    setCategoriasAbertas((atuais) => {
      const proximas = new Set(atuais);
      if (proximas.has(categoriaId)) proximas.delete(categoriaId);
      else proximas.add(categoriaId);
      return proximas;
    });
  };

  return (
    <LayoutPrivado titulo="Categorias" subtitulo="Acompanhe as movimentações mensais agrupadas por categoria.">
      <MensagemAlerta mensagem={erro} tipo="danger" />
      <ResumoCategorias
        gradienteGrafico={gradienteGrafico}
        mesSelecionado={mesSelecionado}
        rotuloMes={rotuloMes}
        totalMovimentado={totalMovimentado}
        onAlterarMes={(deslocamento) => setMesSelecionado((mes) => alterarAnoMes(mes, deslocamento))}
        onSelecionarMes={(mes) => setMesSelecionado(mes || obterMesAtual())}
      />

      {carregando ? (
        <section className="categories-list-panel">
          <div className="loading-inline" aria-live="polite">Carregando categorias...</div>
        </section>
      ) : resumoCategorias.length === 0 ? (
        <EstadoVazio
          titulo="Nenhuma categoria encontrada"
          descricao="Cadastre ou importe transações para visualizar as movimentações por categoria."
        />
      ) : (
        <ListaCategorias
          categorias={resumoCategorias}
          categoriasAbertas={categoriasAbertas}
          contasPorId={contasPorId}
          rotuloMes={rotuloMes}
          quantidadeTransacoes={transacoes.length}
          onAlternarCategoria={alternarCategoria}
        />
      )}
    </LayoutPrivado>
  );
};

export default Categorias;
