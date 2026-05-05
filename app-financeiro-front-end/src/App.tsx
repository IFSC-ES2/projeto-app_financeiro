import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import Register from './pages/Register';

// criando um dashboard provisorio direto aqui pra nao dar erro de import faltando
const DashboardFake = () => (
  <div style={{ textAlign: 'center', marginTop: '50px', fontFamily: 'Arial' }}>
    <h2>Dashboard</h2>
    <p>Login realizado com sucesso! (Tela oficial em construção)</p>
  </div>
);

function App() {
  // router em volta de tudo pra navegacao funcionar
  return (
    <Router>
      <Routes>
        {/* redireciona a raiz pro login logo de cara */}
        <Route path="/" element={<Navigate to="/login" />} />
        
        {/* rota da nossa tela de login oficial */}
        <Route path="/login" element={<Login />} />

        {/* rota da tela de cadastro */}
        <Route path="/register" element={<Register />} />

        {/* joga pro dashboard provisorio quando o login der certo */}
        <Route path="/dashboard" element={<DashboardFake />} />
      </Routes>
    </Router>
  );
}

export default App;