import axios from 'axios';
import { limparSessao, obterAccessToken } from '../utils/authStorage';

declare module 'axios' {
  export interface AxiosRequestConfig {
    ignorarLogoutAutomatico?: boolean;
  }
}

const api = axios.create({
  baseURL: 'http://localhost:8080',
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
    if (
      axios.isAxiosError(erro) &&
      !erro.config?.ignorarLogoutAutomatico &&
      erro.response?.status === 401
    ) {
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

export interface ContaEdicaoRequest {
  nome: string;
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
  categorizada: boolean;
}

export interface ResumoPagamentoResponse {
  formaPagamento: TipoPagamento | null;
  rotulo: string;
  total: number;
  quantidade: number;
  percentual: number;
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

export interface CategorizarTransacaoResponse{
  categoriaId: string;
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

export const editarTransacao = async (transacaoId: string, transacao: TransacaoRequest) => {
  const { data } = await api.put<TransacaoResponse>(`/transacoes/${transacaoId}`, transacao);
  return data;
};

export const excluirTransacao = async (transacaoId: string) => {
  await api.delete(`/transacoes/${transacaoId}`);
};

export const buscarResumoPorPagamento = async () => {
  const { data } = await api.get<ResumoPagamentoResponse[]>('/resumo/pagamentos', {
    ignorarLogoutAutomatico: true,
  });

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

export interface PaginaResponse<T> {
  conteudo: T[];
  pagina: number;
  tamanho: number;
  totalElementos: number;
  totalPaginas: number;
  primeira: boolean;
  ultima: boolean;
}

export interface FiltroTransacoesParams {
  page?: number;
  size?: number;
  dataInicio?: string;
  dataFim?: string;
  categoriaId?: string;
  contaId?: string;
  tipo?: TipoTransacao | '';
}

export const listarTransacoes = async (params: FiltroTransacoesParams = {}) => {
  const query: Record<string, string> = {};
  if (params.page != null) query.page = String(params.page);
  if (params.size != null) query.size = String(params.size);
  if (params.dataInicio) query.dataInicio = params.dataInicio;
  if (params.dataFim) query.dataFim = params.dataFim;
  if (params.categoriaId) query.categoriaId = params.categoriaId;
  if (params.contaId) query.contaId = params.contaId;
  if (params.tipo) query.tipo = params.tipo;

  const { data } = await api.get<PaginaResponse<TransacaoResponse>>('/transacoes', { params: query });
  return data;
};

export const categorizarTransacao = async (transacaoId: string, categoriaId: string) => {
  const { data } = await api.patch<TransacaoResponse>(`/transacoes/${transacaoId}/categoria`, {
    categoriaId,
  });

  return data;
};

export default api;
