import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Interceptor: injeta o token em toda requisição autenticada
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export type TipoTransacao = 'DEBITO' | 'CREDITO' | 'PARCELAMENTO' | 'BOLETO';
export type TipoPagamento = 'PIX' | 'CARTAO_DEBITO' | 'CARTAO_CREDITO' | 'DINHEIRO' | 'BOLETO' | 'TED_DOC';

export type TipoConta = 'CORRENTE' | 'POUPANCA' | 'CARTAO_CREDITO' | 'CARTEIRA';

export interface ContaRequest {
  nome: string;
  tipoConta: TipoConta;
  banco?: string;
  descricao?: string;
}

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
  contaId: string | null;
}

export interface TransacaoResponse extends TransacaoRequest {
  transacaoId: string;
}

export const listarContas = async () => {
  const { data } = await api.get<ContaResponse[]>('/contas');
  return data;
};

export const registrarConta = async (conta: ContaRequest) => {
  const { data } = await api.post<ContaResponse>('/contas/registrar', conta);
  return data;
};

export const listarCategorias = async () => {
  const { data } = await api.get<CategoriaResponse[]>('/categorias');
  return data;
};

export const registrarTransacaoManual = async (transacao: TransacaoRequest) => {
  const { data } = await api.post<TransacaoResponse>('/transacoes/manual', transacao);
  return data;
};

export default api;
