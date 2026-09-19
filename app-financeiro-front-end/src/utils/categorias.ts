import type { CategoriaResponse } from '../types/categoria';
import type { TransacaoResponse } from '../types/transacao';

export interface ResumoCategoriaMensal {
  id: string;
  nome: string;
  cor: string;
  total: number;
  quantidade: number;
  percentual: number;
  transacoes: TransacaoResponse[];
}

type TransacaoComCategoriaFlexivel = TransacaoResponse & {
  categoriaID?: string | null;
  categoria?: { id?: string | null; categoriaId?: string | null; categoriaID?: string | null } | null;
};

const normalizarValor = (valor: number | string | null | undefined) => Math.abs(Number(valor || 0));

const obterAnoMesValido = (anoMes: string) => {
  const [anoTexto, mesTexto] = anoMes.split('-');
  const ano = Number(anoTexto);
  const mes = Number(mesTexto);

  if (!Number.isInteger(ano) || !Number.isInteger(mes) || mes < 1 || mes > 12) {
    const hoje = new Date();
    return { ano: hoje.getFullYear(), mes: hoje.getMonth() + 1 };
  }

  return { ano, mes };
};

const formatarAnoMes = (data: Date) => `${data.getFullYear()}-${String(data.getMonth() + 1).padStart(2, '0')}`;

const obterCategoriaIdTransacao = (transacao: TransacaoResponse) => {
  const transacaoFlexivel = transacao as TransacaoComCategoriaFlexivel;
  return transacaoFlexivel.categoriaId ?? transacaoFlexivel.categoriaID ?? transacaoFlexivel.categoria?.categoriaId
    ?? transacaoFlexivel.categoria?.categoriaID ?? transacaoFlexivel.categoria?.id ?? null;
};

const gerarCorCategoria = (categoria: CategoriaResponse | undefined, chave: string) => {
  if (categoria?.cor?.trim()) return categoria.cor;
  const base = categoria?.nome || chave;
  const hash = Array.from(base).reduce((acumulador, caractere) => acumulador + caractere.charCodeAt(0), 0);
  return `hsl(${hash % 360} 76% 57%)`;
};

export const obterMesAtual = () => formatarAnoMes(new Date());

export const obterIntervaloMes = (anoMes: string) => {
  const { ano, mes } = obterAnoMesValido(anoMes);
  const mesFormatado = String(mes).padStart(2, '0');
  const ultimoDia = String(new Date(ano, mes, 0).getDate()).padStart(2, '0');
  return { dataInicio: `${ano}-${mesFormatado}-01`, dataFim: `${ano}-${mesFormatado}-${ultimoDia}` };
};

export const alterarAnoMes = (anoMes: string, deslocamento: number) => {
  const { ano, mes } = obterAnoMesValido(anoMes);
  return formatarAnoMes(new Date(ano, mes - 1 + deslocamento, 1));
};

export const formatarRotuloMes = (anoMes: string) => {
  const { ano, mes } = obterAnoMesValido(anoMes);
  const texto = new Intl.DateTimeFormat('pt-BR', { month: 'long', year: 'numeric' }).format(new Date(ano, mes - 1, 1));
  return texto.charAt(0).toUpperCase() + texto.slice(1);
};

export const ehGastoTransacao = (transacao: TransacaoResponse) =>
  transacao.tipoTransacao === 'DEBITO' || Number(transacao.valor || 0) < 0;

export const calcularResumoCategoriasMensal = (
  categorias: CategoriaResponse[],
  transacoes: TransacaoResponse[],
): ResumoCategoriaMensal[] => {
  const categoriasPorId = new Map(categorias.map((categoria) => [categoria.categoriaId, categoria]));
  const grupos = new Map<string, Omit<ResumoCategoriaMensal, 'percentual'>>();

  transacoes.filter(ehGastoTransacao).forEach((transacao) => {
    const categoriaId = obterCategoriaIdTransacao(transacao);
    const id = categoriaId ?? 'nao-informado';
    const categoria = categoriaId ? categoriasPorId.get(categoriaId) : undefined;
    const grupo = grupos.get(id);

    if (grupo) {
      grupo.total += normalizarValor(transacao.valor);
      grupo.quantidade += 1;
      grupo.transacoes.push(transacao);
      return;
    }

    grupos.set(id, {
      id,
      nome: categoria?.nome ?? (categoriaId ? 'Categoria não encontrada' : 'Não informado'),
      cor: gerarCorCategoria(categoria, id),
      total: normalizarValor(transacao.valor),
      quantidade: 1,
      transacoes: [transacao],
    });
  });

  categorias.forEach((categoria) => {
    if (!grupos.has(categoria.categoriaId)) {
      grupos.set(categoria.categoriaId, {
        id: categoria.categoriaId,
        nome: categoria.nome,
        cor: gerarCorCategoria(categoria, categoria.categoriaId),
        total: 0,
        quantidade: 0,
        transacoes: [],
      });
    }
  });

  const totalGeral = Array.from(grupos.values()).reduce((total, grupo) => total + grupo.total, 0);
  return Array.from(grupos.values())
    .map((grupo) => ({
      ...grupo,
      percentual: totalGeral > 0 ? (grupo.total / totalGeral) * 100 : 0,
      transacoes: [...grupo.transacoes].sort((a, b) => b.data.localeCompare(a.data)),
    }))
    .sort((a, b) => b.total - a.total || a.nome.localeCompare(b.nome, 'pt-BR'));
};

export const montarGradienteCategorias = (categorias: ResumoCategoriaMensal[]) => {
  const categoriasComGasto = categorias.filter((categoria) => categoria.total > 0);
  if (categoriasComGasto.length === 0) return 'conic-gradient(var(--sb-border) 0deg 360deg)';

  let inicio = 0;
  const fatias = categoriasComGasto.map((categoria) => {
    const fim = inicio + (categoria.percentual / 100) * 360;
    const fatia = `${categoria.cor} ${inicio.toFixed(2)}deg ${fim.toFixed(2)}deg`;
    inicio = fim;
    return fatia;
  });

  return `conic-gradient(${fatias.join(', ')})`;
};
