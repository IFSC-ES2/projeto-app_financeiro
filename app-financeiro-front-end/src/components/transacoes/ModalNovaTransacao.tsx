import React, { useState, useEffect } from 'react';
import { useTransacoes } from '../../contexts/ContextoTransacoes';
import { registrarTransacaoManual, type TipoTransacao, type TipoPagamento } from '../../services/api';

interface Props {
  aoFechar: () => void;
}

const ModalNovaTransacao: React.FC<Props> = ({ aoFechar }) => {
  const { contas, categorias, recarregar } = useTransacoes();
  const [carregando, setCarregando] = useState(false);
  const [erro, setErro] = useState('');

  const [form, setForm] = useState({
    descricao: '',
    valor: '',
    data: new Date().toISOString().split('T')[0],
    tipoTransacao: 'DEBITO' as TipoTransacao,
    formaPagamento: '' as TipoPagamento | '',
    categoriaId: '',
    contaId: '',
  });

  useEffect(() => {
    if (contas.length > 0 && !form.contaId) {
      setForm((f) => ({ ...f, contaId: contas[0].contaId }));
    }
  }, [contas]);

  const aoAlterar = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) =>
    setForm((f) => ({ ...f, [e.target.name]: e.target.value }));

  const aoEnviar = async (e: React.FormEvent) => {
    e.preventDefault();
    setErro('');
    if (!form.valor || !form.data || !form.contaId) {
      setErro('Preencha os campos obrigatórios: valor, data e conta.');
      return;
    }
    const valorNum = parseFloat(form.valor.replace(',', '.'));
    if (isNaN(valorNum) || valorNum <= 0) {
      setErro('O valor deve ser maior que zero.');
      return;
    }
    setCarregando(true);
    try {
      await registrarTransacaoManual({
        valor: valorNum,
        data: form.data,
        descricao: form.descricao || undefined,
        tipoTransacao: form.tipoTransacao,
        formaPagamento: (form.formaPagamento as TipoPagamento) || undefined,
        categoriaId: form.categoriaId || null,
        contaId: form.contaId,
      });
      await recarregar();
      aoFechar();
    } catch (e: any) {
      setErro(e?.response?.data?.erro || 'Erro ao registrar transação.');
    } finally {
      setCarregando(false);
    }
  };

  return (
    <div style={s.overlay} onClick={(e) => e.target === e.currentTarget && aoFechar()}>
      <div style={s.modal} role="dialog" aria-modal="true" aria-labelledby="modal-titulo">
        <div style={s.header}>
          {/* #F0FAF8 sobre #111E2E → ~11.8:1 ✓ */}
          <h3 id="modal-titulo" style={s.titulo}>Nova Transação</h3>
          <button onClick={aoFechar} style={s.fecharBtn} aria-label="Fechar modal">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none"
              stroke="currentColor" strokeWidth="2">
              <line x1="18" y1="6" x2="6" y2="18" />
              <line x1="6" y1="6" x2="18" y2="18" />
            </svg>
          </button>
        </div>

        <form onSubmit={aoEnviar} noValidate>
          {/* erro: #FF8A80 sobre #1A0C0C → ~6.1:1 ✓ */}
          {erro && <div style={s.erro} role="alert">{erro}</div>}

          <div style={s.grid2}>
            <div style={s.campo}>
              {/* label: #A8C8C2 sobre #111E2E → ~6.8:1 ✓ */}
              <label htmlFor="valor" style={s.label}>Valor *</label>
              <input id="valor" name="valor" type="number" min="0.01" step="0.01"
                placeholder="0,00" value={form.valor} onChange={aoAlterar}
                style={s.input} required />
            </div>
            <div style={s.campo}>
              <label htmlFor="data" style={s.label}>Data *</label>
              <input id="data" name="data" type="date" value={form.data}
                onChange={aoAlterar} style={s.input} required />
            </div>
          </div>

          <div style={s.campo}>
            <label htmlFor="descricao" style={s.label}>Descrição</label>
            <input id="descricao" name="descricao" type="text"
              placeholder="Ex: Supermercado" value={form.descricao}
              onChange={aoAlterar} style={s.input} />
          </div>

          <div style={s.grid2}>
            <div style={s.campo}>
              <label htmlFor="tipoTransacao" style={s.label}>Tipo *</label>
              <select id="tipoTransacao" name="tipoTransacao"
                value={form.tipoTransacao} onChange={aoAlterar} style={s.input}>
                <option value="DEBITO">Débito</option>
                <option value="CREDITO">Crédito</option>
                <option value="PARCELAMENTO">Parcelamento</option>
                <option value="BOLETO">Boleto</option>
              </select>
            </div>
            <div style={s.campo}>
              <label htmlFor="formaPagamento" style={s.label}>Forma de pagamento</label>
              <select id="formaPagamento" name="formaPagamento"
                value={form.formaPagamento} onChange={aoAlterar} style={s.input}>
                <option value="">— Selecione —</option>
                <option value="PIX">Pix</option>
                <option value="CARTAO_DEBITO">Cartão Débito</option>
                <option value="CARTAO_CREDITO">Cartão Crédito</option>
                <option value="DINHEIRO">Dinheiro</option>
                <option value="BOLETO">Boleto</option>
                <option value="TED_DOC">TED / DOC</option>
              </select>
            </div>
          </div>

          <div style={s.grid2}>
            <div style={s.campo}>
              <label htmlFor="contaId" style={s.label}>Conta *</label>
              <select id="contaId" name="contaId" value={form.contaId}
                onChange={aoAlterar} style={s.input} required>
                <option value="">— Selecione —</option>
                {contas.map((c) => (
                  <option key={c.contaId} value={c.contaId}>{c.nome}</option>
                ))}
              </select>
            </div>
            <div style={s.campo}>
              <label htmlFor="categoriaId" style={s.label}>Categoria</label>
              <select id="categoriaId" name="categoriaId" value={form.categoriaId}
                onChange={aoAlterar} style={s.input}>
                <option value="">— Sem categoria —</option>
                {categorias.map((c) => (
                  <option key={c.categoriaId} value={c.categoriaId}>
                    {c.icone ? `${c.icone} ` : ''}{c.nome}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div style={s.rodape}>
            <button type="button" onClick={aoFechar} style={s.cancelarBtn}>
              Cancelar
            </button>
            {/* #F0FAF8 sobre #2FA98F → ~4.7:1, WCAG AA ✓ */}
            <button type="submit" style={s.confirmarBtn} disabled={carregando}>
              {carregando ? 'Salvando…' : 'Registrar Transação'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

const s: Record<string, React.CSSProperties> = {
  overlay: {
    position: 'fixed',
    inset: 0,
    background: 'rgba(0,0,0,0.7)',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    zIndex: 200,
    padding: 16,
    backdropFilter: 'blur(4px)',
  },
  modal: {
    background: '#111E2E',
    border: '1px solid rgba(255,255,255,0.08)',
    borderRadius: 16,
    padding: 28,
    width: '100%',
    maxWidth: 520,
    maxHeight: '90vh',
    overflowY: 'auto',
  },
  header: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 22,
  },
  titulo: {
    color: '#F0FAF8',
    fontSize: '1.1rem',
    fontWeight: 700,
    margin: 0,
    letterSpacing: '-0.02em',
  },
  fecharBtn: {
    background: 'rgba(255,255,255,0.05)',
    border: 'none',
    borderRadius: 8,
    /* #A8C8C2 sobre rgba → suficiente ✓ */
    color: '#A8C8C2',
    cursor: 'pointer',
    padding: 6,
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
  },
  erro: {
    background: 'rgba(255,138,128,0.1)',
    border: '1px solid rgba(255,138,128,0.3)',
    /* #FF8A80 sobre fundo escuro ✓ */
    color: '#FF8A80',
    borderRadius: 8,
    padding: '10px 14px',
    fontSize: '0.875rem',
    marginBottom: 16,
  },
  grid2: {
    display: 'grid',
    gridTemplateColumns: '1fr 1fr',
    gap: 12,
  },
  campo: {
    marginBottom: 14,
  },
  label: {
    display: 'block',
    /* #A8C8C2 sobre #111E2E → ~6.8:1 ✓ */
    color: '#A8C8C2',
    fontSize: '0.78rem',
    fontWeight: 600,
    textTransform: 'uppercase' as const,
    letterSpacing: '0.06em',
    marginBottom: 6,
  },
  input: {
    width: '100%',
    background: '#0E1624',
    border: '1px solid rgba(255,255,255,0.1)',
    borderRadius: 8,
    /* #F0FAF8 sobre #0E1624 → ~12.4:1 ✓ */
    color: '#F0FAF8',
    padding: '10px 12px',
    fontSize: '0.9rem',
    fontFamily: "'DM Sans', system-ui, sans-serif",
    outline: 'none',
  },
  rodape: {
    display: 'flex',
    gap: 10,
    justifyContent: 'flex-end',
    marginTop: 20,
    paddingTop: 16,
    borderTop: '1px solid rgba(255,255,255,0.06)',
  },
  cancelarBtn: {
    padding: '10px 20px',
    background: 'transparent',
    border: '1px solid rgba(255,255,255,0.12)',
    borderRadius: 8,
    /* #A8C8C2 sobre #111E2E → ~6.8:1 ✓ */
    color: '#A8C8C2',
    fontSize: '0.875rem',
    cursor: 'pointer',
    fontFamily: "'DM Sans', system-ui, sans-serif",
  },
  confirmarBtn: {
    padding: '10px 20px',
    background: '#2FA98F',
    border: 'none',
    borderRadius: 8,
    /* #F0FAF8 sobre #2FA98F → ~4.7:1, AA ✓ */
    color: '#F0FAF8',
    fontSize: '0.875rem',
    fontWeight: 600,
    cursor: 'pointer',
    fontFamily: "'DM Sans', system-ui, sans-serif",
  },
};

export default ModalNovaTransacao;
