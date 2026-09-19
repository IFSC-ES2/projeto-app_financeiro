import type { ResumoCategoriaMensal } from '../../utils/categorias';
import ItemCategoria from './ItemCategoria';

interface PropsListaCategorias {
  categorias: ResumoCategoriaMensal[];
  categoriasAbertas: Set<string>;
  contasPorId: Map<string, string>;
  rotuloMes: string;
  quantidadeTransacoes: number;
  onAlternarCategoria: (categoriaId: string) => void;
}

const ListaCategorias = ({
  categorias,
  categoriasAbertas,
  contasPorId,
  rotuloMes,
  quantidadeTransacoes,
  onAlternarCategoria,
}: PropsListaCategorias) => (
  <section className="categories-list-panel" aria-label="Lista de categorias">
    <div className="categories-list-header">
      <div>
        <span>Categorias</span>
        <h2>Gastos de {rotuloMes}</h2>
      </div>
      <p>{quantidadeTransacoes} {quantidadeTransacoes === 1 ? 'transação' : 'transações'}</p>
    </div>

    <div className="categories-list">
      {categorias.map((categoria) => (
        <ItemCategoria
          key={categoria.id}
          categoria={categoria}
          expandida={categoriasAbertas.has(categoria.id)}
          contasPorId={contasPorId}
          onAlternar={() => onAlternarCategoria(categoria.id)}
        />
      ))}
    </div>
  </section>
);

export default ListaCategorias;
