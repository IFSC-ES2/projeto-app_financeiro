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
