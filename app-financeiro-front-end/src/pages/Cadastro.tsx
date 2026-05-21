import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAutenticacao } from '../contexts/ContextoAutenticacao';
import LayoutAutenticacao from '../components/layout/LayoutAutenticacao';
import CampoFormulario from '../components/ui/CampoFormulario';
import BotaoCarregando from '../components/ui/BotaoCarregando';
import MensagemAlerta from '../components/ui/MensagemAlerta';
import { useFormulario } from '../hooks/useFormulario';
import { formatarCpf, isCpfValido } from '../utils/cpf';

type CamposCadastro = {
  nome: string;
  email: string;
  cpf: string;
  senha: string;
  confirmacao: string;
};

const validar = (valores: CamposCadastro) => {
  const erros: Partial<CamposCadastro> = {};
  if (!valores.nome.trim()) erros.nome = 'Nome é obrigatório.';
  if (!valores.email) erros.email = 'E-mail é obrigatório.';
  else if (!/\S+@\S+\.\S+/.test(valores.email)) erros.email = 'E-mail inválido.';
  if (!valores.cpf) erros.cpf = 'CPF é obrigatório.';
  else if (!isCpfValido(valores.cpf)) erros.cpf = 'CPF inválido.';
  if (!valores.senha) erros.senha = 'Senha é obrigatória.';
  else if (valores.senha.length < 6) erros.senha = 'Mínimo de 6 caracteres.';
  if (!valores.confirmacao) erros.confirmacao = 'Confirme sua senha.';
  else if (valores.confirmacao !== valores.senha) erros.confirmacao = 'As senhas não coincidem.';
  return erros;
};

const Cadastro: React.FC = () => {
  const { cadastrar } = useAutenticacao();
  const navigate = useNavigate();
  const [erro, setErro] = useState('');
  const [carregando, setCarregando] = useState(false);

  const { valores, erros, tocados, aoAlterar, aoSair, eValido, setValores } =
    useFormulario<CamposCadastro>({
      valoresIniciais: { nome: '', email: '', cpf: '', senha: '', confirmacao: '' },
      validar,
    });

  const aoAlterarCpf = (e: React.ChangeEvent<HTMLInputElement>) => {
    const formatado = formatarCpf(e.target.value);
    setValores((prev) => ({ ...prev, cpf: formatado }));
  };

  const aoEnviar = async (e: React.FormEvent) => {
    e.preventDefault();
    setErro('');
    if (!eValido()) return;

    setCarregando(true);
    try {
      const cpfSoDigitos = valores.cpf.replace(/\D/g, '');
      await cadastrar(valores.nome, valores.email, valores.senha, cpfSoDigitos);
      navigate('/login');
    } catch (err: any) {
      const status = err?.response?.status;
      const msg = err?.response?.data?.erro;
      if (status === 409) setErro(msg || 'E-mail ou CPF já cadastrado.');
      else if (status === 400) setErro(msg || 'Dados inválidos. Verifique os campos.');
      else setErro('Não foi possível criar a conta. Tente novamente.');
    } finally {
      setCarregando(false);
    }
  };

  return (
    <LayoutAutenticacao
      tituloPainel="Comece agora!"
      subtituloPainel="Crie sua conta e tenha controle total das suas finanças com o SmartBudget."
    >
      <div className="mb-4">

        <h2 className="fw-bold mb-1" style={{ color: 'var(--sb-text)', fontSize: '1.8rem' }}>
          Criar conta
        </h2>
        <p className="text-muted small mb-0">Preencha seus dados para começar</p>
      </div>

      <MensagemAlerta mensagem={erro} tipo="danger" />

      <form onSubmit={aoEnviar} noValidate>
        <CampoFormulario
          rotulo="Nome completo"
          id="nome"
          name="nome"
          type="text"
          value={valores.nome}
          onChange={aoAlterar}
          onBlur={aoSair}
          placeholder="Seu nome"
          autoComplete="name"
          erro={erros.nome}
          tocado={tocados.nome}
          icone={
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
              <circle cx="12" cy="7" r="4" />
            </svg>
          }
        />

        <CampoFormulario
          rotulo="E-mail"
          id="email"
          name="email"
          type="email"
          value={valores.email}
          onChange={aoAlterar}
          onBlur={aoSair}
          placeholder="seu@email.com"
          autoComplete="email"
          erro={erros.email}
          tocado={tocados.email}
          icone={
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M20 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2z" />
              <polyline points="22,6 12,13 2,6" />
            </svg>
          }
        />

        <CampoFormulario
          rotulo="CPF"
          id="cpf"
          name="cpf"
          type="text"
          value={valores.cpf}
          onChange={aoAlterarCpf}
          onBlur={aoSair}
          placeholder="000.000.000-00"
          inputMode="numeric"
          erro={erros.cpf}
          tocado={tocados.cpf}
          icone={
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <rect x="3" y="4" width="18" height="16" rx="2" />
              <line x1="7" y1="9" x2="17" y2="9" />
              <line x1="7" y1="13" x2="13" y2="13" />
            </svg>
          }
        />

        <CampoFormulario
          rotulo="Senha"
          id="senha"
          name="senha"
          type="password"
          value={valores.senha}
          onChange={aoAlterar}
          onBlur={aoSair}
          placeholder="Mínimo 6 caracteres"
          autoComplete="new-password"
          erro={erros.senha}
          tocado={tocados.senha}
          icone={
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
              <path d="M7 11V7a5 5 0 0 1 10 0v4" />
            </svg>
          }
        />

        <CampoFormulario
          rotulo="Confirmar senha"
          id="confirmacao"
          name="confirmacao"
          type="password"
          value={valores.confirmacao}
          onChange={aoAlterar}
          onBlur={aoSair}
          placeholder="Repita a senha"
          autoComplete="new-password"
          erro={erros.confirmacao}
          tocado={tocados.confirmacao}
          icone={
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <polyline points="20 6 9 17 4 12" />
            </svg>
          }
        />

        <BotaoCarregando
          type="submit"
          carregando={carregando}
          textoCarregando="Criando conta..."
          className="w-100 py-2 fw-semibold mt-2"
          style={{
            background: 'var(--sb-gradient)',
            border: 'none',
            borderRadius: '8px',
            fontSize: '1rem',
          }}
        >
          Criar conta
        </BotaoCarregando>

        <p className="text-center text-muted small mt-4 mb-0">
          Já tem uma conta?{' '}
          <Link to="/login" className="fw-semibold text-decoration-none" style={{ color: 'var(--sb-primary)' }}>
            Entrar
          </Link>
        </p>
      </form>
    </LayoutAutenticacao>
  );
};

export default Cadastro;
