export const formatarMoeda = (valor: number | string) =>
  Number(valor || 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });

export const formatarData = (data: string) => {
  if (!data) return 'Data não informada';

  const [ano, mes, dia] = data.split('-');
  if (!ano || !mes || !dia) return data;

  return `${dia}/${mes}/${ano}`;
};

export const formatarPercentual = (valor: number | string, casasDecimais = 1) =>
  `${Number(valor || 0).toLocaleString('pt-BR', {
    minimumFractionDigits: casasDecimais,
    maximumFractionDigits: casasDecimais,
  })}%`;

export const formatarVariacaoPercentual = (valor: number | string) => {
  const numero = Number(valor || 0);
  const sinal = numero > 0 ? '+' : '';
  return `${sinal}${formatarPercentual(numero)}`;
};
