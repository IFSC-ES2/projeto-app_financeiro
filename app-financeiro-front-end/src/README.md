# SmartBudget – Front-end (src/)

## Estrutura de pastas

```
src/
├── App.tsx                         # Rotas + AuthProvider
├── main.tsx                        # Entrypoint com Bootstrap
├── contexts/
│   └── AuthContext.tsx             # Contexto global de autenticação
├── hooks/
│   └── useForm.ts                  # Hook reutilizável de formulário com validação
├── services/
│   └── api.ts                      # Axios configurado + interceptor de token
├── utils/
│   └── cpf.ts                      # formatCpf() e isCpfValido()
├── components/
│   ├── layout/
│   │   ├── AuthLayout.tsx          # Wrapper split-panel para Login/Register
│   │   └── AuthPanel.tsx           # Painel esquerdo decorativo (reutilizável)
│   └── ui/
│       ├── FormInput.tsx           # Input com label, ícone e validação inline
│       ├── LoadingButton.tsx       # Botão com spinner de carregamento
│       └── AlertMessage.tsx        # Alerta de erro/sucesso
├── pages/
│   ├── Login.tsx                   # Tela de login
│   └── Register.tsx                # Tela de cadastro
└── styles/
    └── global.css                  # Variáveis CSS e overrides do Bootstrap
```

## Instalação

```bash
# Instale as dependências existentes + Bootstrap
npm install
npm install bootstrap
```

## Como executar

```bash
npm run dev
```

## Conexão com o backend

O arquivo `src/services/api.ts` já aponta para `http://localhost:8080`.
O interceptor injeta automaticamente o token JWT em todas as requisições autenticadas.

## Reutilizando components em novas páginas

- **AuthLayout**: use em qualquer página de autenticação passando `panelTitle` e `panelSubtitle`
- **FormInput**: campo de formulário com suporte a ícone, validação e feedback visual
- **LoadingButton**: botão com estado de carregamento
- **AlertMessage**: mensagem de erro/sucesso com ícone
- **useForm**: hook com gerenciamento de estado, erros e validação
- **useAuth**: acessa `login`, `register`, `logout` e `isAuthenticated` em qualquer componente
