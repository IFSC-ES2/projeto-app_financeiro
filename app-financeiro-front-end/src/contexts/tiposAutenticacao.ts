export interface DadosContextoAutenticacao {
  token: string | null;
  estaAutenticado: boolean;
  autenticacaoPronta: boolean;
  login: (email: string, senha: string) => Promise<void>;
  cadastrar: (nome: string, email: string, senha: string, cpf: string) => Promise<void>;
  sair: () => void;
}
