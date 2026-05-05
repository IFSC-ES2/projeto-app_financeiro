import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../services/api';
import './Login.css';

const formatCpf = (raw: string) => {
  const digits = raw.replace(/\D/g, '').slice(0, 11);
  return digits
    .replace(/(\d{3})(\d)/, '$1.$2')
    .replace(/(\d{3})(\d)/, '$1.$2')
    .replace(/(\d{3})(\d{1,2})$/, '$1-$2');
};

const Register: React.FC = () => {
  const [nome, setNome] = useState('');
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [confirmacao, setConfirmacao] = useState('');
  const [cpf, setCpf] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const navigate = useNavigate();

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (!nome || !email || !senha || !confirmacao || !cpf) {
      setError('Por favor, preencha todos os campos.');
      return;
    }
    if (senha !== confirmacao) {
      setError('As senhas não coincidem.');
      return;
    }
    if (senha.length < 6) {
      setError('A senha deve ter pelo menos 6 caracteres.');
      return;
    }

    const cpfDigits = cpf.replace(/\D/g, '');
    if (cpfDigits.length !== 11) {
      setError('CPF deve conter 11 dígitos.');
      return;
    }

    setLoading(true);
    try {
      const { data } = await api.post('/auth/register', {
        nome,
        email,
        senha,
        cpf: cpfDigits,
      });

      if (data?.accessToken) {
        localStorage.setItem('token', data.accessToken);
        navigate('/dashboard');
      } else {
        setError('Resposta inesperada do servidor.');
      }
    } catch (err: any) {
      const status = err?.response?.status;
      const msg = err?.response?.data?.erro;
      if (status === 409) {
        setError(msg || 'E-mail ou CPF já cadastrado.');
      } else if (status === 400) {
        setError(msg || 'Dados inválidos. Verifique os campos.');
      } else {
        setError('Não foi possível criar a conta. Tente novamente.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <form className="login-form" onSubmit={handleRegister}>
        <h2>SmartBudget</h2>
        <p>Crie sua conta</p>

        {error && <div className="error-message">{error}</div>}

        <div className="input-group">
          <label htmlFor="nome">Nome completo</label>
          <input
            type="text"
            id="nome"
            value={nome}
            onChange={(e) => setNome(e.target.value)}
            placeholder="Digite seu nome"
            autoComplete="name"
          />
        </div>

        <div className="input-group">
          <label htmlFor="email">E-mail</label>
          <input
            type="email"
            id="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="seu@email.com"
            autoComplete="email"
          />
        </div>

        <div className="input-group">
          <label htmlFor="cpf">CPF</label>
          <input
            type="text"
            id="cpf"
            value={cpf}
            onChange={(e) => setCpf(formatCpf(e.target.value))}
            placeholder="000.000.000-00"
            inputMode="numeric"
          />
        </div>

        <div className="input-group">
          <label htmlFor="senha">Senha</label>
          <input
            type="password"
            id="senha"
            value={senha}
            onChange={(e) => setSenha(e.target.value)}
            placeholder="Mínimo 6 caracteres"
            autoComplete="new-password"
          />
        </div>

        <div className="input-group">
          <label htmlFor="confirmacao">Confirmar senha</label>
          <input
            type="password"
            id="confirmacao"
            value={confirmacao}
            onChange={(e) => setConfirmacao(e.target.value)}
            placeholder="Repita a senha"
            autoComplete="new-password"
          />
        </div>

        <button type="submit" className="btn-login" disabled={loading}>
          {loading ? 'Criando conta…' : 'Criar conta'}
        </button>

        <p className="form-footer">
          Já tem uma conta? <Link to="/login">Entrar</Link>
        </p>
      </form>
    </div>
  );
};

export default Register;
