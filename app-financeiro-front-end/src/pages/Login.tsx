import React, { useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import './Login.css';

const Login: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  
  // hook pra navegar entre as telas
  const navigate = useNavigate();

  const handleLogin = async (e: React.FormEvent) => {
    // segura o reload da pagina no submit
    e.preventDefault(); 
    // zera os erros antes de tentar de novo
    setError(''); 

    // validacao basica pra nao mandar form vazio
    if (!email || !password) {
      setError('Por favor, preencha todos os campos.');
      return;
    }

    try {
      // bate na api do backend pra logar
      const response = await axios.post('http://localhost:8080/auth/login', {
        email,
        password
      });

      // pegando o token que a api devolve
      const token = response.data.token;

      if (token) {
        // joga o token no localstorage pra manter logado
        localStorage.setItem('token', token);
        
        // manda o usuario pro dashboard
        navigate('/dashboard'); 
      }
    } catch (err) {
      // deu ruim na senha ou email
      setError('E-mail ou senha inválidos. Tente novamente.');
      console.error('erro na api de login:', err);
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
          />
        </div>

        <div className="input-group">
          <label htmlFor="password">Senha</label>
          <input 
            type="password" 
            id="password" 
            value={password} 
            onChange={(e) => setPassword(e.target.value)} 
            placeholder="Digite sua senha"
          />
        </div>

        <button type="submit" className="btn-login">Entrar</button>
      </form>
    </div>
  );
};

export default Login;