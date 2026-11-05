import React from 'react';

export interface ItemCarteira {
  texto: string;
  valor: string;
  destaque?: boolean;
}

export interface PropsCarteiraIlustrativa {
  rotulo: string;
  saldo: string;
  trajetoGrafico?: string;
  itens: ItemCarteira[];
}

// Ilustração estática da carteira da logo, sem dados reais
const CarteiraIlustrativa: React.FC<PropsCarteiraIlustrativa> = ({ rotulo, saldo, trajetoGrafico, itens }) => {
  return (
    <div className="acesso-carteira">
      <div className="acesso-carteira-cartao">
        <span className="acesso-carteira-rotulo">{rotulo}</span>
        <strong className="acesso-carteira-saldo">{saldo}</strong>
        {trajetoGrafico && (
          <svg className="acesso-carteira-grafico" viewBox="0 0 240 56" preserveAspectRatio="none">
            <path d={trajetoGrafico} />
          </svg>
        )}
      </div>
      <div className="acesso-carteira-faixa acesso-carteira-faixa-amarela" />
      <div className="acesso-carteira-faixa acesso-carteira-faixa-verde" />
      <ul className="acesso-carteira-bolso">
        {itens.map((item) => (
          <li key={item.texto}>
            <span>{item.texto}</span>
            <span className={item.destaque ? 'acesso-valor-entrada' : undefined}>{item.valor}</span>
          </li>
        ))}
      </ul>
    </div>
  );
};

export default CarteiraIlustrativa;
