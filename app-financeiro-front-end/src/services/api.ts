import axios from 'axios';
import { limparSessao, obterAccessToken } from '../utils/authStorage';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use((config) => {
  const token = obterAccessToken();

  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

api.interceptors.response.use(
  (response) => response,
  (erro: unknown) => {
    if (axios.isAxiosError(erro) && (erro.response?.status === 401 || erro.response?.status === 403)) {
      limparSessao();
      window.dispatchEvent(new Event('smartbudget:unauthorized'));
    }

    return Promise.reject(erro);
  }
);

export type TipoTransacao = 'DEBITO' | 'CREDITO' | 'PARCELAMENTO' | 'BOLETO';
export type TipoPagamento = 'PIX' | 'CARTAO_DEBITO' | 'CARTAO_CREDITO' | 'DINHEIRO' | 'BOLETO' | 'TED_DOC';
export type TipoConta = 'CORRENTE' | 'POUPANCA' | 'CARTAO_CREDITO' | 'CARTEIRA';

export interface TokenDTO {
  accessToken: string;
  tipo: string;
  expiracao?: string;
}

export interface LoginRequest {
  email: string;
  senha: string;
}

export interface CadastroRequest {
  nome: string;
  email: string;
  cpf: string;
  senha: string;
}

export interface ContaRequest {
  nome: string;
  tipoConta: TipoConta;
  banco?: string;
  descricao?: string;
}

export interface ContaResponse {
  contaId: string;
  nome: string;
  tipoConta: TipoConta;
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

export interface TransacaoResponse {
  transacaoId: string;
  valor: number;
  data: string;
  descricao?: string;
  tipoTransacao: TipoTransacao;
  formaPagamento?: TipoPagamento;
  importacaoId?: string | null;
  categoriaId?: string | null;
  contaId: string | null;
}

export type StatusImportacao = 'PENDENTE' | 'PROCESSANDO' | 'CONCLUIDO' | 'ERRO';

export interface ImportacaoResponse {
  id: string;
  status: StatusImportacao;
  sucessos: number;
  falhas: number;
  importadoEm: string;
  mensagemErro?: string | null;
}

interface ErroApiPayload {
  erro?: unknown;
  message?: unknown;
}

const obterTextoSeguro = (valor: unknown) => (typeof valor === 'string' && valor.trim() ? valor : undefined);

export const obterMensagemErroApi = (erro: unknown, fallback: string) => {
  if (!axios.isAxiosError(erro)) return fallback;

  const dados = erro.response?.data as ErroApiPayload | undefined;
  return obterTextoSeguro(dados?.erro) || obterTextoSeguro(dados?.message) || fallback;
};

export const obterStatusHttp = (erro: unknown) => (axios.isAxiosError(erro) ? erro.response?.status : undefined);

export const loginUsuario = async (credenciais: LoginRequest) => {
  const { data } = await api.post<TokenDTO>('/auth/login', credenciais);
  return data;
};

export const login = async (email: string, senha: string) => loginUsuario({ email, senha });

export const cadastrarUsuario = async (cadastro: CadastroRequest) => {
  const { data } = await api.post<TokenDTO>('/auth/register', cadastro);
  return data;
};

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

export const criarImportacao = async (arquivo: File, contaId: string) => {
  const formData = new FormData();
  formData.append('arquivo', arquivo);
  formData.append('contaId', contaId);

  const { data } = await api.post<ImportacaoResponse>('/importacoes', formData);
  return data;
};

export const consultarStatusImportacao = async (importacaoId: string) => {
  const { data } = await api.get<StatusImportacao>(`/importacoes/${importacaoId}/status`);
  return data;
};

export const listarTransacoes = async () => {
  const { data } = await api.get<TransacaoResponse[]>('/transacoes');
  return data;
};

export default api;
