import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import CampoFormulario from '../components/ui/CampoFormulario';
import BotaoCarregando from '../components/ui/BotaoCarregando';
import MensagemAlerta from '../components/ui/MensagemAlerta';
import { obterMensagemErroApi } from '../services/apiError';
import { useAutenticacao } from '../contexts/ContextoAutenticacao';
import { useFormulario } from '../hooks/useFormulario';

const validar = (valores: { email: string; senha: string }) => {
  const erros: Partial<typeof valores> = {};
  if (!valores.email) erros.email = 'E-mail é obrigatório.';
  else if (!/\S+@\S+\.\S+/.test(valores.email)) erros.email = 'E-mail inválido.';
  if (!valores.senha) erros.senha = 'Senha é obrigatória.';
  return erros;
};

const Login: React.FC = () => {
  const navigate = useNavigate();
  const [erro, setErro] = useState('');
  const [carregando, setCarregando] = useState(false);
  const { login } = useAutenticacao();

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
      navigate('/dashboard');
    } catch (err: unknown) {
      setErro(obterMensagemErroApi(err, 'Credenciais inválidas. Verifique seu e-mail e senha.'));
    } finally {
      setCarregando(false);
    }
  };

  return (
    <div className="acesso-page">
      <aside className="acesso-vitrine" aria-hidden="true">
        <div className="acesso-marca">
          <img src="/smartbudget-logo.png" alt="" width="36" height="36" />
          <span>SmartBudget</span>
        </div>

        <div className="acesso-vitrine-conteudo">
          <p className="acesso-manchete">
            Seu dinheiro,
            <br />
            <em>em ordem.</em>
          </p>
          <p className="acesso-vitrine-texto">
            Contas, cartões e categorias num só lugar. Você vê para onde vai cada real antes do fim do mês.
          </p>

          {/* Ilustração estática da carteira da logo, sem dados reais */}
          <div className="acesso-carteira">
            <div className="acesso-carteira-cartao">
              <span className="acesso-carteira-prompt">&gt;_</span>
              <span className="acesso-carteira-rotulo">Saldo do mês</span>
              <strong className="acesso-carteira-saldo">R$ 2.418,90</strong>
              <svg className="acesso-carteira-grafico" viewBox="0 0 240 56" preserveAspectRatio="none">
                <path d="M0 44 L30 38 L60 42 L90 26 L120 30 L150 18 L180 22 L210 10 L240 14" />
              </svg>
            </div>
            <div className="acesso-carteira-faixa acesso-carteira-faixa-amarela" />
            <div className="acesso-carteira-faixa acesso-carteira-faixa-verde" />
            <ul className="acesso-carteira-bolso">
              <li><span>Salário</span><span className="acesso-valor-entrada">+ R$ 4.200,00</span></li>
              <li><span>Mercado</span><span>− R$ 612,40</span></li>
              <li><span>Aluguel</span><span>− R$ 1.168,70</span></li>
            </ul>
          </div>
        </div>

        <p className="acesso-vitrine-rodape">Projeto acadêmico · IFSC</p>
      </aside>

      <main className="acesso-area">
        <div className="acesso-area-topo">
          <div className="acesso-marca acesso-marca-mobile">
            <img src="/smartbudget-logo.png" alt="" width="32" height="32" />
            <span>SmartBudget</span>
          </div>
          <p className="acesso-cadastro">
            <span className="acesso-cadastro-pergunta">Novo aqui?</span> <Link to="/cadastro">Criar uma conta</Link>
          </p>
        </div>

        <div className="acesso-cartao">
          <h1 className="acesso-titulo">Entrar</h1>
          <p className="acesso-subtitulo">Bem-vindo de volta. Acesse com seu e-mail e senha.</p>

          <MensagemAlerta mensagem={erro} tipo="danger" />

          <form onSubmit={aoEnviar} noValidate className="acesso-formulario">
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
            />

            <BotaoCarregando
              type="submit"
              carregando={carregando}
              textoCarregando="Entrando..."
              className="acesso-botao w-100"
            >
              Entrar
            </BotaoCarregando>
          </form>
        </div>

        <p className="acesso-area-rodape">© 2026 SmartBudget</p>
      </main>
    </div>
  );
};

export default Login;
