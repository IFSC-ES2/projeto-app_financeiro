import React from 'react';
import { Link } from 'react-router-dom';
import CarteiraIlustrativa from './CarteiraIlustrativa';

interface PropsLayoutAcesso {
  pergunta: string;
  rotuloLink: string;
  destinoLink: string;
  classeCartao?: string;
  children: React.ReactNode;
}

const LayoutAcesso: React.FC<PropsLayoutAcesso> = ({
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
            Seu dinheiro,
            <br />
            <em>em ordem.</em>
          </p>
          <p className="acesso-vitrine-texto">
            Contas, cartões e categorias num só lugar. Você vê para onde vai cada real antes do fim do mês.
          </p>

          <CarteiraIlustrativa
            rotulo="Saldo do mês"
            saldo="R$ 2.418,90"
            trajetoGrafico="M0 44 L30 38 L60 42 L90 26 L120 30 L150 18 L180 22 L210 10 L240 14"
            itens={[
              { texto: 'Salário', valor: '+ R$ 4.200,00', destaque: true },
              { texto: 'Mercado', valor: '− R$ 612,40' },
              { texto: 'Aluguel', valor: '− R$ 1.168,70' },
            ]}
          />
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
