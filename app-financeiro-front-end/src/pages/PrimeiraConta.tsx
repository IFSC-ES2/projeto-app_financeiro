import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import LayoutAcesso from '../components/layout/LayoutAcesso';
import BotaoCarregando from '../components/ui/BotaoCarregando';
import CampoFormulario from '../components/ui/CampoFormulario';
import MensagemAlerta from '../components/ui/MensagemAlerta';
import { useFormulario } from '../hooks/useFormulario';
import { obterMensagemErroApi } from '../services/apiError';
import { registrarConta } from '../services/contaService';
import type { ContaRequest, TipoConta } from '../types/conta';

type CamposConta = {
  nome: string;
  banco: string;
  tipoConta: string;
  descricao: string;
};

const tiposConta: Array<{ valor: TipoConta; rotulo: string }> = [
  { valor: 'CORRENTE', rotulo: 'Conta corrente' },
  { valor: 'POUPANCA', rotulo: 'Conta poupança' },
  { valor: 'CARTAO_CREDITO', rotulo: 'Cartão de crédito' },
  { valor: 'CARTEIRA', rotulo: 'Carteira' },
];

const validar = (valores: CamposConta) => {
  const erros: Partial<CamposConta> = {};
  if (!valores.nome.trim()) erros.nome = 'Nome da conta é obrigatório.';
  if (!valores.banco.trim()) erros.banco = 'Banco é obrigatório.';
  if (!valores.tipoConta) erros.tipoConta = 'Tipo de conta é obrigatório.';
  return erros;
};

const PrimeiraConta: React.FC = () => {
  const navigate = useNavigate();
  const [erroGeral, setErroGeral] = useState('');
  const [sucesso, setSucesso] = useState('');
  const [salvando, setSalvando] = useState(false);

  const { valores, erros, tocados, aoAlterar, aoSair, eValido } = useFormulario<CamposConta>({
    valoresIniciais: { nome: '', banco: 'Nubank', tipoConta: 'CORRENTE', descricao: '' },
    validar,
  });

  const erroTipoConta = tocados.tipoConta ? erros.tipoConta : undefined;

  const enviar = async (evento: React.FormEvent) => {
    evento.preventDefault();
    setErroGeral('');
    setSucesso('');

    if (!eValido()) {
      const primeiroInvalido = Object.keys(validar(valores))[0];
      document.getElementById(primeiroInvalido)?.focus();
      return;
    }

    setSalvando(true);

    try {
      const novaConta: ContaRequest = {
        nome: valores.nome.trim(),
        banco: valores.banco.trim(),
        tipoConta: valores.tipoConta as TipoConta,
        descricao: valores.descricao.trim() || undefined,
      };

      await registrarConta(novaConta);

      setSucesso('Conta bancária cadastrada com sucesso. Redirecionando para o Dashboard...');

      setTimeout(() => {
        navigate('/dashboard', { replace: true });
      }, 900);
    } catch (erro) {
      setErroGeral(obterMensagemErroApi(erro, 'Não foi possível cadastrar a conta bancária.'));
    } finally {
      setSalvando(false);
    }
  };

  return (
    <LayoutAcesso
      manchete="Falta só"
      mancheteDestaque="um passo."
      textoApresentacao="Cadastre a conta onde seu dinheiro está hoje. É a partir dela que o SmartBudget organiza suas primeiras transações."
      carteira={{
        rotulo: 'Seu cadastro',
        saldo: '2 de 3',
        itens: [
          { texto: 'Criar sua conta', valor: 'concluído' },
          { texto: 'Cadastrar a primeira conta', valor: 'agora', destaque: true },
          { texto: 'Importar um extrato', valor: 'quando quiser' },
        ],
      }}
      classeCartao="acesso-cartao-largo"
    >
      <h1 className="acesso-titulo">Cadastre sua primeira conta</h1>
      <p className="acesso-subtitulo">Você poderá adicionar outras contas depois.</p>

      <MensagemAlerta mensagem={erroGeral} tipo="danger" />
      <MensagemAlerta mensagem={sucesso} tipo="success" />

      <form onSubmit={enviar} noValidate className="acesso-formulario">
        <div className="acesso-campos-duplos">
          <CampoFormulario
            rotulo="Nome da conta"
            id="nome"
            name="nome"
            type="text"
            value={valores.nome}
            onChange={aoAlterar}
            onBlur={aoSair}
            placeholder="Ex.: Conta principal"
            erro={erros.nome}
            tocado={tocados.nome}
          />
          <CampoFormulario
            rotulo="Banco"
            id="banco"
            name="banco"
            type="text"
            value={valores.banco}
            onChange={aoAlterar}
            onBlur={aoSair}
            placeholder="Ex.: Nubank"
            erro={erros.banco}
            tocado={tocados.banco}
          />
        </div>

        {/* CampoFormulario só renderiza <input>; select e textarea seguem a mesma marcação e os mesmos atributos de acessibilidade */}
        <div className="mb-3">
          <label htmlFor="tipoConta" className="form-label fw-semibold text-secondary small mb-1">
            Tipo de conta
          </label>
          <select
            id="tipoConta"
            name="tipoConta"
            className={`form-select ${erroTipoConta ? 'is-invalid' : ''}`}
            value={valores.tipoConta}
            onChange={aoAlterar}
            onBlur={aoSair}
            aria-invalid={erroTipoConta ? true : undefined}
            aria-describedby={erroTipoConta ? 'tipoConta-erro' : undefined}
          >
            {tiposConta.map((tipo) => (
              <option key={tipo.valor} value={tipo.valor}>
                {tipo.rotulo}
              </option>
            ))}
          </select>
          {erroTipoConta && (
            <div id="tipoConta-erro" className="invalid-feedback">{erroTipoConta}</div>
          )}
        </div>

        <div className="mb-3">
          <label htmlFor="descricao" className="form-label fw-semibold text-secondary small mb-1">
            Descrição <span className="acesso-opcional">(opcional)</span>
          </label>
          <textarea
            id="descricao"
            name="descricao"
            className="form-control"
            rows={2}
            value={valores.descricao}
            onChange={aoAlterar}
            placeholder="Ex.: Conta usada para despesas do dia a dia"
          />
        </div>

        <BotaoCarregando
          type="submit"
          carregando={salvando}
          textoCarregando="Cadastrando..."
          className="acesso-botao w-100"
        >
          Finalizar cadastro
        </BotaoCarregando>
      </form>
    </LayoutAcesso>
  );
};

export default PrimeiraConta;
