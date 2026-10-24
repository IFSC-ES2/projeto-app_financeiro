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
