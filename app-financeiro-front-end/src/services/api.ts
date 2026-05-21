import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export type TipoTransacao = 'DEBITO' | 'CREDITO' | 'PARCELAMENTO' | 'BOLETO';
export type TipoPagamento = 'PIX' | 'CARTAO_DEBITO' | 'CARTAO_CREDITO' | 'DINHEIRO' | 'BOLETO' | 'TED_DOC';

export interface ContaResponse {
  contaId: string;
  nome: string;
  tipoConta: string;
  banco?: string;
  descricao?: string;
}

export interface CategoriaResponse {
  categoriaId: string;
  nome: string;
  icone?: string;
  cor?: string;
  padrao: boolean;
}

export interface TransacaoRequest {
  valor: number;
  data: string;
  descricao?: string;
  tipoTransacao: TipoTransacao;
  formaPagamento?: TipoPagamento;
  categoriaId?: string | null;
  contaId: string;
}

export interface TransacaoResponse {
  transacaoId: string;
  valor: number;
  data: string;
  descricao?: string;
  tipoTransacao: TipoTransacao;
  formaPagamento?: TipoPagamento;
  categoriaId?: string | null;
  contaId: string;
  importacaoId?: string | null;
}

export const listarContas = async (): Promise<ContaResponse[]> => {
  const { data } = await api.get<ContaResponse[]>('/contas');
  return data;
};

export const listarCategorias = async (): Promise<CategoriaResponse[]> => {
  const { data } = await api.get<CategoriaResponse[]>('/categorias');
  return data;
};

export const registrarTransacaoManual = async (transacao: TransacaoRequest): Promise<TransacaoResponse> => {
  const { data } = await api.post<TransacaoResponse>('/transacoes/manual', transacao);
  return data;
};

export const listarTransacoes = async (): Promise<TransacaoResponse[]> => {
  const { data } = await api.get<TransacaoResponse[]>('/transacoes');
  return data;
};

export default api;
