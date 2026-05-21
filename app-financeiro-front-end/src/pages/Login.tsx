import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAutenticacao } from '../contexts/ContextoAutenticacao';
import LayoutAutenticacao from '../components/layout/LayoutAutenticacao';
import CampoFormulario from '../components/ui/CampoFormulario';
import BotaoCarregando from '../components/ui/BotaoCarregando';
import MensagemAlerta from '../components/ui/MensagemAlerta';
import { useFormulario } from '../hooks/useFormulario';

const validar = (valores: { email: string; senha: string }) => {
  const erros: Partial<typeof valores> = {};
  if (!valores.email) erros.email = 'E-mail é obrigatório.';
  else if (!/\S+@\S+\.\S+/.test(valores.email)) erros.email = 'E-mail inválido.';
  if (!valores.senha) erros.senha = 'Senha é obrigatória.';
  return erros;
};

const Login: React.FC = () => {
  const { login } = useAutenticacao();
  const navigate = useNavigate();
  const [erro, setErro] = useState('');
  const [carregando, setCarregando] = useState(false);

  const { valores, erros, tocados, aoAlterar, aoSair, eValido } = useFormulario({
    valoresIniciais: { email: '', senha: '' },
    validar,
  });

  const aoEnviar = async (e: React.FormEvent) => {
    e.preventDefault();
    setErro('');
    if (!eValido()) return;

    setCarregando(true);
    try {
      await login(valores.email, valores.senha);
      navigate('/painel');
    } catch (err: any) {
      const msg = err?.response?.data?.erro;
      setErro(msg || 'E-mail ou senha inválidos. Tente novamente.');
    } finally {
      setCarregando(false);
    }
  };

  return (
    <LayoutAutenticacao
      tituloPainel="Bem-vindo de volta!"
      subtituloPainel="Faça login para acessar seu painel financeiro e continuar no controle do seu orçamento."
    >
      <div className="mb-4">

        <h2 className="fw-bold mb-1" style={{ color: 'var(--sb-text)', fontSize: '1.8rem' }}>
          Entrar
        </h2>
        <p className="text-muted small mb-0">Acesse sua conta com suas credenciais</p>
      </div>

      <MensagemAlerta mensagem={erro} tipo="danger" />

      <form onSubmit={aoEnviar} noValidate>
        <CampoFormulario
          rotulo="Usuário ou e-mail"
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
          rotulo="Senha"
          id="senha"
          name="senha"
          type="password"
          value={valores.senha}
          onChange={aoAlterar}
          onBlur={aoSair}
          placeholder="Sua senha"
          autoComplete="current-password"
          erro={erros.senha}
          tocado={tocados.senha}
          icone={
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
              <path d="M7 11V7a5 5 0 0 1 10 0v4" />
            </svg>
          }
        />

        <div className="d-flex justify-content-between align-items-center mb-4">
          <div className="form-check mb-0">
            <input
              className="form-check-input"
              type="checkbox"
              id="lembrarMe"
              style={{ accentColor: 'var(--sb-primary)' }}
            />
            <label className="form-check-label small text-muted" htmlFor="lembrarMe">
              Lembrar-me
            </label>
          </div>
          <a href="#" className="small text-decoration-none" style={{ color: 'var(--sb-primary)' }}>
            Esqueceu a senha?
          </a>
        </div>

        <BotaoCarregando
          type="submit"
          carregando={carregando}
          textoCarregando="Entrando..."
          className="w-100 py-2 fw-semibold"
          style={{
            background: 'var(--sb-gradient)',
            border: 'none',
            borderRadius: '8px',
            fontSize: '1rem',
          }}
        >
          Entrar
        </BotaoCarregando>

        <p className="text-center text-muted small mt-4 mb-0">
          Novo aqui?{' '}
          <Link to="/cadastro" className="fw-semibold text-decoration-none" style={{ color: 'var(--sb-primary)' }}>
            Criar uma conta
          </Link>
        </p>
      </form>
    </LayoutAutenticacao>
  );
};

export default Login;
