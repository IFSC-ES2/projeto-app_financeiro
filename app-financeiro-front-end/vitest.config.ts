import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react'; //se usarmos SWC, mudamos para '@vitejs/plugin-react-swc'

export default defineConfig({
  plugins: [react()],
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: './src/setupTests.ts',
    coverage: {
      provider: 'v8',
      reporter: ['text', 'json-summary', 'html'],
      reportsDirectory: './coverage',
      include: ['src/**/*.{ts,tsx}'],
      exclude: [
        'src/main.tsx',
        'src/App.tsx',
        'src/setupTests.ts',
        'src/**/*.test.{ts,tsx}',
        'src/**/*.d.ts',
      ],
    },
  },
});