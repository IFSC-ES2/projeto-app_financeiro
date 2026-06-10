import LayoutPrivado from '../components/layout/LayoutPrivado';
import ResumoFormaPagamentoPizza from '../components/resumo/ResumoFormaPagamentoPizza';
import EstadoVazio from '../components/ui/EstadoVazio';

const Dashboard = () => (
  <LayoutPrivado
    titulo="Dashboard"
    subtitulo="Veja aqui o seu resumo financeiro."
  >
    <ResumoFormaPagamentoPizza />

    <EstadoVazio
      titulo="Em construção."
      descricao="Esta seção será implementada em uma próxima etapa do MVP."
    />
  </LayoutPrivado>
);

export default Dashboard;