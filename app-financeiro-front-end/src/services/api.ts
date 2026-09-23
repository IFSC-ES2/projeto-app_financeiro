// Compatibilidade temporária para consumidores externos ainda não migrados.
// Novos imports devem apontar para o serviço ou tipo do domínio correspondente.
export { default } from './httpClient';
export * from './apiError';
export * from './authService';
export * from './categoriaService';
export * from './contaService';
export * from './importacaoService';
export * from './resumoService';
export * from './transacaoService';
export type * from '../types/auth';
export type * from '../types/categoria';
export type * from '../types/conta';
export type * from '../types/importacao';
export type * from '../types/resumo';
export type * from '../types/transacao';
