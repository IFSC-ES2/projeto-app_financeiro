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
      <svg className="acesso-fundo-grafico" viewBox="0 0 1440 900" preserveAspectRatio="none" aria-hidden="true">
        <defs>
          <linearGradient id="acesso-fundo-grafico-area" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0" className="acesso-fundo-grafico-area-topo" />
            <stop offset="1" className="acesso-fundo-grafico-area-base" />
          </linearGradient>
        </defs>
        <path
          fill="url(#acesso-fundo-grafico-area)"
          d="M0 720 L120 690 L240 705 L360 640 L480 655 L600 580 L720 600 L840 520 L960 540 L1080 450 L1200 470 L1320 380 L1440 400 L1440 900 L0 900 Z"
        />
        <path
          className="acesso-fundo-grafico-linha"
          d="M0 720 L120 690 L240 705 L360 640 L480 655 L600 580 L720 600 L840 520 L960 540 L1080 450 L1200 470 L1320 380 L1440 400"
        />
      </svg>

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
