import LayoutPrivado from '../layout/LayoutPrivado';
import EstadoVazio from './EstadoVazio';

interface PropsPaginaEmConstrucao {
  titulo: string;
  subtitulo?: string;
}

const PaginaEmConstrucao = ({ titulo, subtitulo }: PropsPaginaEmConstrucao) => (
  <LayoutPrivado titulo={titulo} subtitulo={subtitulo}>
    <EstadoVazio titulo="Em construção." descricao="Esta seção será implementada em uma próxima etapa do MVP." />
  </LayoutPrivado>
);

export default PaginaEmConstrucao;
