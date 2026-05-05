import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../services/api';
import './Login.css';

const Login: React.FC = () => {
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const navigate = useNavigate();

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (!email || !senha) {
      setError('Por favor, preencha todos os campos.');
      return;
    }

    setLoading(true);
    try {
      const { data } = await api.post('/auth/login', { email, senha });

      if (data?.accessToken) {
        localStorage.setItem('token', data.accessToken);
        navigate('/dashboard');
      } else {
        setError('Resposta inesperada do servidor.');
      }
    } catch (err: any) {
      const msg = err?.response?.data?.erro;
      setError(msg || 'E-mail ou senha inválidos. Tente novamente.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <form className="login-form" onSubmit={handleLogin}>
        <h2>SmartBudget</h2>
        <p>Faça login para continuar</p>

        {error && <div className="error-message">{error}</div>}

        <div className="input-group">
          <label htmlFor="email">E-mail</label>
          <input
            type="email"
            id="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="Digite seu e-mail"
            autoComplete="email"
          />
        </div>

        <div className="input-group">
          <label htmlFor="senha">Senha</label>
          <input
            type="password"
            id="senha"
            value={senha}
            onChange={(e) => setSenha(e.target.value)}
            placeholder="Digite sua senha"
            autoComplete="current-password"
          />
        </div>

        <button type="submit" className="btn-login" disabled={loading}>
          {loading ? 'Entrando…' : 'Entrar'}
        </button>

        <p className="form-footer">
          Ainda não tem conta? <Link to="/register">Criar conta</Link>
        </p>
      </form>
    </div>
  );
};

export default Login;
