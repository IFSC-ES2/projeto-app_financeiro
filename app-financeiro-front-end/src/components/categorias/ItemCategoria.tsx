import type { ResumoCategoriaMensal } from '../../utils/categorias';
import { formatarMoeda } from '../../utils/formatacao';
import ListaTransacoesCategoria from './ListaTransacoesCategoria';

interface PropsItemCategoria {
  categoria: ResumoCategoriaMensal;
  expandida: boolean;
  contasPorId: Map<string, string>;
  onAlternar: () => void;
}

const ItemCategoria = ({ categoria, expandida, contasPorId, onAlternar }: PropsItemCategoria) => (
  <article className="category-expense-card">
    <button
      type="button"
      className="category-expense-toggle"
      aria-expanded={expandida}
      aria-controls={`categoria-${categoria.id}`}
      onClick={onAlternar}
    >
      <span className="category-expense-chevron" aria-hidden="true">{expandida ? '⌃' : '⌄'}</span>
      <span className="category-expense-count" style={{ backgroundColor: categoria.cor }} aria-label={`${categoria.quantidade} transações`}>
        {categoria.quantidade}
      </span>
      <span className="category-expense-name">{categoria.nome}</span>
      <span className="category-expense-value">{formatarMoeda(categoria.total)}</span>
      <span className="category-expense-progress" aria-hidden="true">
        <span style={{ width: `${Math.min(categoria.percentual, 100)}%`, backgroundColor: categoria.cor }} />
      </span>
    </button>

    {expandida && (
      <div id={`categoria-${categoria.id}`} className="category-expense-transactions">
        <ListaTransacoesCategoria contasPorId={contasPorId} transacoes={categoria.transacoes} />
      </div>
    )}
  </article>
);

export default ItemCategoria;
