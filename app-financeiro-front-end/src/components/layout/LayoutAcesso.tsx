import React from 'react';
import { Link } from 'react-router-dom';
import CarteiraIlustrativa, { type PropsCarteiraIlustrativa } from './CarteiraIlustrativa';

interface PropsLayoutAcesso {
  manchete: string;
  mancheteDestaque: string;
  textoApresentacao: string;
  carteira: PropsCarteiraIlustrativa;
  pergunta: string;
  rotuloLink: string;
  destinoLink: string;
  classeCartao?: string;
  children: React.ReactNode;
}

// Valores fictícios, só para compor o fundo
const LANCAMENTOS_FUNDO = [
  '+ 4.200,00', '- 612,40', '- 1.168,70', '- 89,90', '+ 350,00', '- 45,90', '- 230,15', '- 19,99',
  '+ 1.120,00', '- 74,30', '- 310,00', '- 12,50', '+ 80,00', '- 158,42', '- 999,00', '- 27,80',
  '+ 640,00', '- 63,10', '- 420,75', '- 8,90', '+ 2.050,00', '- 132,60', '- 55,00', '- 301,20',
];

const COLUNAS_FUNDO = [0, 8, 16].map((inicio) =>
  [...LANCAMENTOS_FUNDO.slice(inicio), ...LANCAMENTOS_FUNDO.slice(0, inicio)],
);

const LayoutAcesso: React.FC<PropsLayoutAcesso> = ({
  manchete,
  mancheteDestaque,
  textoApresentacao,
  carteira,
  pergunta,
  rotuloLink,
  destinoLink,
  classeCartao = '',
  children,
}) => {
  return (
    <div className="acesso-page">
      <div className="acesso-fundo-extrato" aria-hidden="true">
        {COLUNAS_FUNDO.map((coluna, indice) => (
          <ul key={indice}>
            {coluna.map((valor, linha) => (
              <li key={linha} className={valor.startsWith('+') ? 'acesso-fundo-extrato-entrada' : undefined}>
                {valor}
              </li>
            ))}
          </ul>
        ))}
      </div>

      <aside className="acesso-vitrine" aria-hidden="true">
        <div className="acesso-marca">
          <img src="/smartbudget-logo.png" alt="" width="36" height="36" />
          <span>SmartBudget</span>
        </div>

        <div className="acesso-vitrine-conteudo">
          <p className="acesso-manchete">
            {manchete}
            <br />
            <em>{mancheteDestaque}</em>
          </p>
          <p className="acesso-vitrine-texto">{textoApresentacao}</p>

          <CarteiraIlustrativa {...carteira} />
        </div>

        <p className="acesso-vitrine-rodape">Projeto acadêmico · IFSC</p>
      </aside>

      <main className="acesso-area">
        <div className="acesso-area-topo">
          <div className="acesso-marca acesso-marca-mobile">
            <img src="/smartbudget-logo.png" alt="" width="32" height="32" />
            <span>SmartBudget</span>
          </div>
          <p className="acesso-alternativa">
            <span className="acesso-alternativa-pergunta">{pergunta}</span> <Link to={destinoLink}>{rotuloLink}</Link>
          </p>
        </div>

        <div className={`acesso-cartao ${classeCartao}`}>
          {children}
        </div>

        <p className="acesso-area-rodape">© 2026 SmartBudget</p>
      </main>
    </div>
  );
};

export default LayoutAcesso;
