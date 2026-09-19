import EstadoVazio from '../ui/EstadoVazio';
import type { TransacaoResponse } from '../../types/transacao';
import { formatarData, formatarMoeda } from '../../utils/formatacao';

interface PropsListaTransacoesCategoria {
  contasPorId: Map<string, string>;
  transacoes: TransacaoResponse[];
}

const obterRotuloTipo = (tipo: TransacaoResponse['tipoTransacao']) =>
  tipo === 'CREDITO' ? 'Receita' : 'Despesa';

const ListaTransacoesCategoria = ({ contasPorId, transacoes }: PropsListaTransacoesCategoria) => {
  if (transacoes.length === 0) {
    return <EstadoVazio titulo="Sem transações neste mês" descricao="Essa categoria não possui gastos no mês selecionado." />;
  }

  return (
    <div className="category-transactions-table-wrapper">
      <table className="category-transactions-table">
        <thead>
          <tr>
            <th>Descrição</th>
            <th>Data</th>
            <th>Tipo</th>
            <th>Conta</th>
            <th className="text-end">Valor</th>
          </tr>
        </thead>
        <tbody>
          {transacoes.map((transacao) => (
            <tr key={transacao.transacaoId}>
              <td><strong>{transacao.descricao || 'Transação sem descrição'}</strong></td>
              <td>{formatarData(transacao.data)}</td>
              <td>{obterRotuloTipo(transacao.tipoTransacao)}</td>
              <td>{(transacao.contaId && contasPorId.get(transacao.contaId)) || 'Conta não informada'}</td>
              <td className="text-end amount-negative">{formatarMoeda(Math.abs(Number(transacao.valor || 0)))}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default ListaTransacoesCategoria;
