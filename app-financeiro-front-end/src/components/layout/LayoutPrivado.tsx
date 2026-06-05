import { useEffect, useRef, useState } from "react";
import type { ReactNode } from "react";
import { NavLink, useNavigate } from "react-router-dom";
import { useAutenticacao } from "../../hooks/useAutenticacao";

interface PropsLayoutPrivado {
  titulo: string;
  subtitulo?: string;
  children: ReactNode;
  acaoPrimaria?: ReactNode;
}

const itensNavegacao = [
  { para: "/dashboard", rotulo: "Dashboard", icone: "dashboard" },
  { para: "/transacoes", rotulo: "Transações", icone: "transacoes" },
  { para: "/categorias", rotulo: "Categorias", icone: "categorias" },
  { para: "/parcelamentos", rotulo: "Parcelamentos", icone: "parcelamentos" },
  { para: "/contas", rotulo: "Contas", icone: "contas" },
];

const IconeNavegacao = ({ nome }: { nome: string }) => {
  if (nome === "transacoes") {
    return (
      <svg viewBox="0 0 24 24" aria-hidden="true">
        <path d="M7 7h11m0 0-3-3m3 3-3 3M17 17H6m0 0 3 3m-3-3 3-3" />
      </svg>
    );
  }

  if (nome === "categorias") {
    return (
      <svg viewBox="0 0 24 24" aria-hidden="true">
        <path d="M7 7h.01M3 5.5A2.5 2.5 0 0 1 5.5 3h5.4a3 3 0 0 1 2.12.88l7.1 7.1a3 3 0 0 1 0 4.24l-4.9 4.9a3 3 0 0 1-4.24 0l-7.1-7.1A3 3 0 0 1 3 10.9z" />
      </svg>
    );
  }

  if (nome === "parcelamentos") {
    return (
      <svg viewBox="0 0 24 24" aria-hidden="true">
        <path d="M4 7h16M7 11h10M7 15h6M6 3h12a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2z" />
      </svg>
    );
  }

  return (
    <svg viewBox="0 0 24 24" aria-hidden="true">
      <path d="M4 13h6V4H4zM14 20h6V4h-6zM4 20h6v-4H4z" />
    </svg>
  );
};

const LayoutPrivado = ({
  titulo,
  subtitulo,
  children,
  acaoPrimaria,
}: PropsLayoutPrivado) => {
  const { sair } = useAutenticacao();
  const navigate = useNavigate();
  const [menuAberto, setMenuAberto] = useState(false);
  const [menuLateralRecolhido, setMenuLateralRecolhido] = useState(false);
  const [menuLateralAberto, setMenuLateralAberto] = useState(false);
  const menuRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    const fecharAoClicarFora = (evento: MouseEvent) => {
      if (!menuRef.current?.contains(evento.target as Node)) {
        setMenuAberto(false);
      }
    };

    document.addEventListener("mousedown", fecharAoClicarFora);
    return () => document.removeEventListener("mousedown", fecharAoClicarFora);
  }, []);

  const sairDaConta = () => {
    sair();
    navigate("/login", { replace: true });
  };

  const alternarMenuLateral = () => {
    setMenuLateralRecolhido((atual) => !atual);
  };

  const fecharMenuLateralMobile = () => {
    setMenuLateralAberto(false);
  };

  const classesShell = [
    "app-shell",
    menuLateralRecolhido ? "sidebar-collapsed" : "",
    menuLateralAberto ? "sidebar-mobile-open" : "",
  ]
    .filter(Boolean)
    .join(" ");

  return (
    <main className={classesShell}>
      <button
        type="button"
        className="sidebar-backdrop"
        aria-label="Fechar menu lateral"
        onClick={fecharMenuLateralMobile}
      />

      <aside className="app-sidebar" aria-label="Navegação principal">
        <button
          type="button"
          className="app-brand"
          aria-label={
            menuLateralRecolhido
              ? "Expandir menu lateral"
              : "Recolher menu lateral"
          }
          aria-expanded={!menuLateralRecolhido}
          data-sidebar-tooltip={
            menuLateralRecolhido
              ? "Abrir barra lateral"
              : "Recolher barra lateral"
          }
          onClick={alternarMenuLateral}
        >
          <img
            src="/smartbudget-logo.png"
            alt=""
            className="app-brand-logo"
          />
          <span className="app-brand-text">
            <strong>SmartBudget</strong>
            <span>Área privada</span>
          </span>
        </button>

        <nav className="app-nav">
          {itensNavegacao.map((item) => (
            <NavLink
              key={item.para}
              to={item.para}
              className="app-nav-link"
              aria-label={item.rotulo}
              title={menuLateralRecolhido ? item.rotulo : undefined}
              onClick={fecharMenuLateralMobile}
            >
              <IconeNavegacao nome={item.icone} />
              <span>{item.rotulo}</span>
            </NavLink>
          ))}
        </nav>
      </aside>

      <section className="app-content">
        <header className="app-topbar">
          <div className="app-title-group">
            <button
              type="button"
              className="mobile-sidebar-button"
              aria-label="Abrir menu lateral"
              aria-expanded={menuLateralAberto}
              onClick={() => setMenuLateralAberto(true)}
            >
              <svg viewBox="0 0 24 24" aria-hidden="true">
                <path d="M4 6h16M4 12h16M4 18h16" />
              </svg>
            </button>

            <div>
              <p className="app-eyebrow">Painel financeiro</p>
              <h1>{titulo}</h1>
              {subtitulo && <p>{subtitulo}</p>}
            </div>
          </div>

          <div className="app-topbar-actions">
            {acaoPrimaria}

            <div className="profile-menu" ref={menuRef}>
              <button
                type="button"
                className="profile-button"
                aria-label="Abrir menu do perfil"
                aria-expanded={menuAberto}
                onClick={() => setMenuAberto((atual) => !atual)}
              >
                <svg viewBox="0 0 24 24" aria-hidden="true">
                  <path d="M20 21a8 8 0 0 0-16 0" />
                  <circle cx="12" cy="8" r="4" />
                </svg>
              </button>

              {menuAberto && (
                <div className="profile-dropdown" role="menu">
                  <button type="button" role="menuitem" onClick={sairDaConta}>
                    Sair
                  </button>
                </div>
              )}
            </div>
          </div>
        </header>

        {children}
      </section>
    </main>
  );
};

export default LayoutPrivado;
