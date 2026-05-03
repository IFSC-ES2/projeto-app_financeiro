import axios from 'axios';

const api = axios.create({
  // TODO: Mover para variável de ambiente (.env) no futuro
  baseURL: 'http://localhost:8080', 
  headers: {
    'Content-Type': 'application/json',
  },
});

export default api;