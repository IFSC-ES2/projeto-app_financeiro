import { useState } from 'react';

interface OpcoesCampo<T> {
  valoresIniciais: T;
  validar?: (valores: T) => Partial<Record<keyof T, string>>;
}

export function useFormulario<T extends Record<string, string>>({
  valoresIniciais,
  validar,
}: OpcoesCampo<T>) {
  const [valores, setValores] = useState<T>(valoresIniciais);
  const [erros, setErros] = useState<Partial<Record<keyof T, string>>>({});
  const [tocados, setTocados] = useState<Partial<Record<keyof T, boolean>>>({});

  const aoAlterar = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setValores((prev) => ({ ...prev, [name]: value }));
    if (erros[name as keyof T]) {
      setErros((prev) => ({ ...prev, [name]: undefined }));
    }
  };

  const aoSair = (e: React.FocusEvent<HTMLInputElement>) => {
    const { name } = e.target;
    setTocados((prev) => ({ ...prev, [name]: true }));
  };

  const eValido = () => {
    if (!validar) return true;
    const novosErros = validar(valores);
    setErros(novosErros);
    setTocados(Object.keys(valores).reduce((acc, key) => ({ ...acc, [key]: true }), {}));
    return Object.keys(novosErros).length === 0;
  };

  return { valores, erros, tocados, aoAlterar, aoSair, eValido, setValores };
}
