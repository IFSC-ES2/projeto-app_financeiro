export const formatarMoeda = (valor: number | string) =>
  Number(valor || 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });

export const formatarData = (data: string) => {
  if (!data) return 'Data não informada';

  const [ano, mes, dia] = data.split('-');
  if (!ano || !mes || !dia) return data;

  return `${dia}/${mes}/${ano}`;
};
