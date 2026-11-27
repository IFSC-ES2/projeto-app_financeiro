import { useCallback, useState } from 'react';

export type Tema = 'claro' | 'escuro';

export const CHAVE_TEMA = 'smartbudget.tema';

const COR_BARRA_NAVEGADOR: Record<Tema, string> = {
  claro: '#F7F8FA',
  escuro: '#0B1118',
};

// O script inline do index.html já aplicou o tema antes do React montar (evita piscar o tema errado).
const lerTemaAplicado = (): Tema =>
  document.documentElement.dataset.tema === 'escuro' ? 'escuro' : 'claro';

export const aplicarTema = (tema: Tema) => {
  const raiz = document.documentElement;
  raiz.dataset.tema = tema;
  raiz.dataset.bsTheme = tema === 'escuro' ? 'dark' : 'light';
  document.querySelector('meta[name="theme-color"]')?.setAttribute('content', COR_BARRA_NAVEGADOR[tema]);
};

export const useTema = () => {
  const [tema, setTema] = useState<Tema>(lerTemaAplicado);

  const alternarTema = useCallback(() => {
    setTema((atual) => {
      const proximo: Tema = atual === 'escuro' ? 'claro' : 'escuro';
      aplicarTema(proximo);
      try {
        localStorage.setItem(CHAVE_TEMA, proximo);
      } catch {
        // Sem localStorage (modo privado restrito) o tema vale só para esta visita.
      }
      return proximo;
    });
  }, []);

  return { tema, alternarTema };
};
