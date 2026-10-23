import EstadoVazio from '../ui/EstadoVazio';
import MensagemAlerta from '../ui/MensagemAlerta';
import type { ResumoCategoriaResponse } from '../../types/resumo';
import { formatarMoeda, formatarPercentual } from '../../utils/formatacao';

interface PropsGastosPorCategoria {
  categorias: ResumoCategoriaResponse[];
  carregando: boolean;
  erro: string;
  onTentarNovamente: () => void;
}

const COR_SEM_CATEGORIA = 'var(--sb-text-muted)';

const obterCorCategoria = (categoria: ResumoCategoriaResponse) => {
  if (categoria.cor?.trim()) return categoria.cor;
  if (!categoria.categoriaID) return COR_SEM_CATEGORIA;

  const hash = Array.from(categoria.nome).reduce(
    (acumulador, caractere) => acumulador + caractere.charCodeAt(0),
    0,
  );
  return `hsl(${hash % 360} 76% 57%)`;
};

const limitarPercentual = (percentual: number) =>
  Math.min(Math.max(Number(percentual) || 0, 0), 100);

const GastosPorCategoria = ({
  categorias,
  carregando,
  erro,
  onTentarNovamente,
}: PropsGastosPorCategoria) => (
  <section className="dashboard-categories-card" aria-label="Gastos por categoria">
    <div className="dashboard-categories-header">
      <h2>Gastos por categoria</h2>
      <p>Quanto foi gasto em cada categoria no mês selecionado.</p>
    </div>

    {carregando ? (
      <div className="loading-inline" aria-live="polite">
        Carregando gastos por categoria...
      </div>
    ) : erro ? (
      <div className="dashboard-categories-content">
        <MensagemAlerta mensagem={erro} tipo="danger" />
        <button
          type="button"
          className="btn sb-btn-outline dashboard-retry-button"
          onClick={onTentarNovamente}
        >
          Tentar novamente
        </button>
      </div>
    ) : categorias.length === 0 ? (
      <EstadoVazio
        titulo="Nenhum gasto no mês"
        descricao="Quando houver gastos neste mês, eles aparecerão agrupados por categoria."
      />
    ) : (
      <ul className="dashboard-categories-list">
        {categorias.map((categoria) => {
          const cor = obterCorCategoria(categoria);

          return (
            <li
              className="dashboard-category-item"
              key={categoria.categoriaID ?? 'sem-categoria'}
            >
              <div className="dashboard-category-item-header">
                <span
                  className="dashboard-category-dot"
                  style={{ backgroundColor: cor }}
                  aria-hidden="true"
                />
                <div className="dashboard-category-info">
                  <strong>{categoria.nome}</strong>
                  <span>
                    {categoria.quantidade}{' '}
                    {categoria.quantidade === 1 ? 'transação' : 'transações'}
                  </span>
                </div>
                <div className="dashboard-category-amount">
                  <strong>{formatarMoeda(categoria.total)}</strong>
                  <span>{formatarPercentual(categoria.percentual)}</span>
                </div>
              </div>

              <div className="dashboard-category-progress" aria-hidden="true">
                <span
                  style={{
                    width: `${limitarPercentual(categoria.percentual)}%`,
                    backgroundColor: cor,
                  }}
                />
              </div>
            </li>
          );
        })}
      </ul>
    )}
  </section>
);

export default GastosPorCategoria;
