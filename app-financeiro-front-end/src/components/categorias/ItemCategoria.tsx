import type { ResumoCategoriaMensal } from '../../utils/categorias';
import { formatarMoeda } from '../../utils/formatacao';
import ListaTransacoesCategoria from './ListaTransacoesCategoria';

interface PropsItemCategoria {
  categoria: ResumoCategoriaMensal;
  expandida: boolean;
  contasPorId: Map<string, string>;
  onAlternar: () => void;
}

const ItemCategoria = ({ categoria, expandida, contasPorId, onAlternar }: PropsItemCategoria) => {
  const corSaldo = categoria.saldo > 0 ? '#2FA98F' : categoria.saldo < 0 ? '#FF6470' : categoria.cor;
  const classeSaldo = categoria.saldo > 0 ? 'amount-positive' : categoria.saldo < 0 ? 'amount-negative' : '';

  return (
  <article className="category-expense-card">
    <button
      type="button"
      className="category-expense-toggle"
      aria-expanded={expandida}
      aria-controls={`categoria-${categoria.id}`}
      onClick={onAlternar}
    >
      <span className="category-expense-chevron" aria-hidden="true">{expandida ? '⌃' : '⌄'}</span>
      <span className="category-expense-count" style={{ backgroundColor: corSaldo }} aria-label={`${categoria.quantidade} transações`}>
        {categoria.quantidade}
      </span>
      <span className="category-expense-name">{categoria.nome}</span>
      <span className={`category-expense-value ${classeSaldo}`}>{formatarMoeda(categoria.saldo)}</span>
      <span className="category-expense-progress" aria-hidden="true">
        {categoria.gastos > 0 && (
          <span className="category-expense-progress-expense" style={{ width: `${(categoria.gastos / categoria.total) * categoria.percentual}%` }} />
        )}
        {categoria.entradas > 0 && (
          <span className="category-expense-progress-income" style={{ width: `${(categoria.entradas / categoria.total) * categoria.percentual}%` }} />
        )}
      </span>
    </button>

    {expandida && (
      <div id={`categoria-${categoria.id}`} className="category-expense-transactions">
        <ListaTransacoesCategoria contasPorId={contasPorId} transacoes={categoria.transacoes} />
      </div>
    )}
  </article>
  );
};

export default ItemCategoria;
