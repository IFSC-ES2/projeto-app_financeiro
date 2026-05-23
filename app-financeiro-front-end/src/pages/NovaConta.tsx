import { useState } from 'react';
import type { FormEvent } from 'react';
import { registrarConta } from '../services/api';
import type { ContaRequest, TipoConta } from '../services/api';

const tiposConta: { label: string; value: TipoConta }[] = [
  { label: 'Conta corrente', value: 'CORRENTE' },
  { label: 'Conta poupança', value: 'POUPANCA' },
  { label: 'Cartão de crédito', value: 'CARTAO_CREDITO' },
  { label: 'Outro / Carteira', value: 'CARTEIRA' },
];

const bancos = [
  'Nubank',
  'Banco do Brasil',
  'Caixa',
  'Itaú',
  'Bradesco',
  'Santander',
  'Outro',
];

export function NovaConta() {
  const [nome, setNome] = useState('');
  const [tipoConta, setTipoConta] = useState<TipoConta>('CORRENTE');
  const [banco, setBanco] = useState('Nubank');
  const [descricao, setDescricao] = useState('');

  const [carregando, setCarregando] = useState(false);
  const [mensagemSucesso, setMensagemSucesso] = useState('');
  const [mensagemErro, setMensagemErro] = useState('');

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    setMensagemSucesso('');
    setMensagemErro('');

    if (!nome.trim()) {
      setMensagemErro('Informe o nome da conta.');
      return;
    }

    if (!tipoConta) {
      setMensagemErro('Selecione o tipo da conta.');
      return;
    }

    if (!banco.trim()) {
      setMensagemErro('Informe o banco.');
      return;
    }

    const novaConta: ContaRequest = {
      nome,
      tipoConta,
      banco,
      descricao,
    };

    try {
      setCarregando(true);

      await registrarConta(novaConta);

      setMensagemSucesso('Conta bancária cadastrada com sucesso!');
      setNome('');
      setTipoConta('CORRENTE');
      setBanco('Nubank');
      setDescricao('');
    } catch (error) {
      console.error(error);
      setMensagemErro('Não foi possível cadastrar a conta bancária.');
    } finally {
      setCarregando(false);
    }
  }

  return (
    <main className="min-h-screen bg-slate-100 px-4 py-8">
      <section className="mx-auto max-w-2xl rounded-lg bg-white p-6 shadow">
        <h1 className="mb-2 text-2xl font-bold text-slate-800">
          Associar conta bancária
        </h1>

        <p className="mb-6 text-sm text-slate-600">
          Cadastre uma conta bancária para utilizar nas funcionalidades do sistema,
          como transações manuais.
        </p>

        {mensagemSucesso && (
          <div className="mb-4 rounded-md bg-green-100 p-3 text-sm text-green-700">
            {mensagemSucesso}
          </div>
        )}

        {mensagemErro && (
          <div className="mb-4 rounded-md bg-red-100 p-3 text-sm text-red-700">
            {mensagemErro}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label
              htmlFor="nome"
              className="mb-1 block text-sm font-medium text-slate-700"
            >
              Nome da conta
            </label>
            <input
              id="nome"
              type="text"
              value={nome}
              onChange={(event) => setNome(event.target.value)}
              placeholder="Ex: Conta Principal"
              className="w-full rounded-md border border-slate-300 px-3 py-2 outline-none focus:border-blue-500"
            />
          </div>

          <div>
            <label
              htmlFor="tipoConta"
              className="mb-1 block text-sm font-medium text-slate-700"
            >
              Tipo de conta
            </label>
            <select
              id="tipoConta"
              value={tipoConta}
              onChange={(event) => setTipoConta(event.target.value as TipoConta)}
              className="w-full rounded-md border border-slate-300 px-3 py-2 outline-none focus:border-blue-500"
            >
              {tiposConta.map((tipo) => (
                <option key={tipo.value} value={tipo.value}>
                  {tipo.label}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label
              htmlFor="banco"
              className="mb-1 block text-sm font-medium text-slate-700"
            >
              Banco
            </label>
            <select
              id="banco"
              value={banco}
              onChange={(event) => setBanco(event.target.value)}
              className="w-full rounded-md border border-slate-300 px-3 py-2 outline-none focus:border-blue-500"
            >
              {bancos.map((nomeBanco) => (
                <option key={nomeBanco} value={nomeBanco}>
                  {nomeBanco}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label
              htmlFor="descricao"
              className="mb-1 block text-sm font-medium text-slate-700"
            >
              Descrição
            </label>
            <textarea
              id="descricao"
              value={descricao}
              onChange={(event) => setDescricao(event.target.value)}
              placeholder="Ex: Conta para transações manuais"
              className="min-h-24 w-full rounded-md border border-slate-300 px-3 py-2 outline-none focus:border-blue-500"
            />
          </div>

          <button
            type="submit"
            disabled={carregando}
            className="w-full rounded-md bg-blue-600 px-4 py-2 font-medium text-white hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-70"
          >
            {carregando ? 'Cadastrando...' : 'Cadastrar conta'}
          </button>
        </form>
      </section>
    </main>
  );
}