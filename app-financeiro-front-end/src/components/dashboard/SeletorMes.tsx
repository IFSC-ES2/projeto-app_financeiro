import { formatarRotuloMes } from '../../utils/categorias';

interface PropsSeletorMes {
  mesSelecionado: string;
  desabilitarProximo?: boolean;
  onAlterarMes: (deslocamento: number) => void;
  onSelecionarMes: (mes: string) => void;
}

const SeletorMes = ({
  mesSelecionado,
  desabilitarProximo = false,
  onAlterarMes,
  onSelecionarMes,
}: PropsSeletorMes) => (
  <div className="dashboard-month-selector" aria-label="Selecionar mês do resumo">
    <button
      type="button"
      className="categories-month-button"
      aria-label="Mês anterior"
      onClick={() => onAlterarMes(-1)}
    >
      ‹
    </button>

    <label>
      <span>{formatarRotuloMes(mesSelecionado)}</span>
      <input
        type="month"
        value={mesSelecionado}
        aria-label="Mês selecionado"
        onChange={(evento) => {
          if (evento.target.value) onSelecionarMes(evento.target.value);
        }}
      />
    </label>

    <button
      type="button"
      className="categories-month-button"
      aria-label="Próximo mês"
      disabled={desabilitarProximo}
      onClick={() => onAlterarMes(1)}
    >
      ›
    </button>
  </div>
);

export default SeletorMes;
