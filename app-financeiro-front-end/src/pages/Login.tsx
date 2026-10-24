import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import LayoutAcesso from '../components/layout/LayoutAcesso';
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
    <LayoutAcesso
      manchete="Seu dinheiro,"
      mancheteDestaque="em ordem."
      textoApresentacao="Contas, cartões e categorias num só lugar. Você vê para onde vai cada real antes do fim do mês."
      carteira={{
        rotulo: 'Saldo do mês',
        saldo: 'R$ 2.418,90',
        trajetoGrafico: 'M0 44 L30 38 L60 42 L90 26 L120 30 L150 18 L180 22 L210 10 L240 14',
        itens: [
          { texto: 'Salário', valor: '+ R$ 4.200,00', destaque: true },
          { texto: 'Mercado', valor: '− R$ 612,40' },
          { texto: 'Aluguel', valor: '− R$ 1.168,70' },
        ],
      }}
      pergunta="Novo aqui?"
      rotuloLink="Criar uma conta"
      destinoLink="/cadastro"
    >
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
    </LayoutAcesso>
  );
};

export default Login;
