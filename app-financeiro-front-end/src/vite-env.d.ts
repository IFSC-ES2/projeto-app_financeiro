/// <reference types="vite/client" />

interface ImportMetaEnv {
  /** URL base da API consumida pelo frontend. Definida no build (Vite). */
  readonly VITE_API_URL?: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
