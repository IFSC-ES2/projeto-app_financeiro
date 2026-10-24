import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAutenticacao } from '../hooks/useAutenticacao';
import LayoutAcesso from '../components/layout/LayoutAcesso';
import CampoFormulario from '../components/ui/CampoFormulario';
import BotaoCarregando from '../components/ui/BotaoCarregando';
import MensagemAlerta from '../components/ui/MensagemAlerta';
import { obterMensagemErroApi, obterStatusHttp } from '../services/apiError';
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
      navigate('/contas/primeira', { replace: true });
    } catch (err: unknown) {
      const status = obterStatusHttp(err);
      if (status === 409) setErro(obterMensagemErroApi(err, 'E-mail ou CPF já cadastrado.'));
      else if (status === 400) setErro(obterMensagemErroApi(err, 'Dados inválidos. Verifique os campos.'));
      else setErro(obterMensagemErroApi(err, 'Não foi possível criar a conta. Tente novamente.'));
    } finally {
      setCarregando(false);
    }
  };

  return (
    <LayoutAcesso pergunta="Já tem uma conta?" rotuloLink="Entrar" destinoLink="/login" classeCartao="acesso-cartao-largo">
      <h1 className="acesso-titulo">Criar conta</h1>
      <p className="acesso-subtitulo">Preencha seus dados para começar a organizar suas finanças.</p>

      <MensagemAlerta mensagem={erro} tipo="danger" />

      <form onSubmit={aoEnviar} noValidate className="acesso-formulario">
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
        />

        <div className="acesso-campos-duplos">
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
          />
        </div>

        <div className="acesso-campos-duplos">
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
          />
        </div>

        <BotaoCarregando
          type="submit"
          carregando={carregando}
          textoCarregando="Criando conta..."
          className="acesso-botao w-100"
        >
          Criar conta
        </BotaoCarregando>
      </form>
    </LayoutAcesso>
  );
};

export default Cadastro;
