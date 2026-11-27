import { useEffect, useRef, useState } from "react";
import type { ReactNode } from "react";
import { NavLink, useNavigate } from "react-router-dom";
import { useAutenticacao } from "../../hooks/useAutenticacao";
import { useTema } from "../../hooks/useTema";
import { obterPrimeiroNome } from "../../utils/authStorage";

interface PropsLayoutPrivado {
  titulo: string;
  subtitulo?: string;
  children: ReactNode;
  acaoPrimaria?: ReactNode;
}

// rotuloCurto: rótulo da barra de abas do celular, onde cada aba tem ~70px de largura
const itensNavegacao = [
  { para: "/dashboard", rotulo: "Dashboard", rotuloCurto: "Início", icone: "dashboard" },
  { para: "/transacoes", rotulo: "Transações", rotuloCurto: "Transações", icone: "transacoes" },
  { para: "/categorias", rotulo: "Categorias", rotuloCurto: "Categorias", icone: "categorias" },
  { para: "/parcelamentos", rotulo: "Extrato futuro", rotuloCurto: "Futuro", icone: "parcelamentos" },
  { para: "/contas", rotulo: "Contas", rotuloCurto: "Contas", icone: "contas" },
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

  if (nome === "contas") {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true">
      <path d="M3 7h18v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
      <path d="M3 7l9-4 9 4" />
      <path d="M7 10v6M12 10v6M17 10v6" />
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
  const { sair, token } = useAutenticacao();
  const primeiroNome = obterPrimeiroNome(token);
  const { tema, alternarTema } = useTema();
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
                <div className="profile-dropdown">
                  <p className="profile-greeting">
                    {primeiroNome ? `Olá, ${primeiroNome}!` : "Olá!"}
                  </p>
                  <div role="menu" aria-label="Menu do perfil">
                    <button
                      type="button"
                      role="menuitemcheckbox"
                      aria-checked={tema === "escuro"}
                      className="profile-theme-toggle"
                      onClick={alternarTema}
                    >
                      <svg viewBox="0 0 24 24" aria-hidden="true">
                        <path d="M21 12.8A9 9 0 1 1 11.2 3a7 7 0 0 0 9.8 9.8z" />
                      </svg>
                      Modo escuro
                      <span className="profile-switch" aria-hidden="true" />
                    </button>
                    <button type="button" role="menuitem" onClick={sairDaConta}>
                      <svg viewBox="0 0 24 24" aria-hidden="true">
                        <path d="M9 4H6a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h3M16 17l5-5-5-5M21 12H9" />
                      </svg>
                      Sair
                    </button>
                  </div>
                </div>
              )}
            </div>
          </div>
        </header>

        {children}
      </section>

      {/* No celular a navegação vira barra de abas inferior, ao alcance do polegar, no lugar do menu escondido */}
      <nav className="app-tabbar" aria-label="Navegação por abas">
        {itensNavegacao.map((item) => (
          <NavLink key={item.para} to={item.para} className="app-tabbar-link">
            <IconeNavegacao nome={item.icone} />
            <span>{item.rotuloCurto}</span>
          </NavLink>
        ))}
      </nav>
    </main>
  );
};

export default LayoutPrivado;
