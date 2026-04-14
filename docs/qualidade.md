# Atributos de Qualidade

## 1. Confiabilidade
- Justificativa: Como o sistema trabalha com movimentações financeiras, precisa apresentar confiabilidade para o usuário, devemos evitar inconsistências nos extratos devido a falhas, inconsistência nos calculos de custos e previsibilidade de gastos devido a lentidão ou falhas de rede.
- Relação com riscos:
- Métricas:
    * Taxa de erro
        * Critério de aceitação: <= 10% de requisições com erro
        * Como medir: análise de respostas durante testes
    * Disponibilidade
        * Critério de aceitação: >= 95% durante o período de testes
        * Como medir: registro do tempo em que o sistema permanece acessível
    * Taxa de falhas não tratadas
        * Critério de aceitação: <= 5% das falhas não tratadas
        * Como medir: execução de testes e análise de logs buscando exceções não capturadas.

## 2. Segurança
- Justificativas: Devido o dinheiro ser uma informação sensível do usuário, nosso sistema deve garantir segurança máxima para o usuário, evitando possíveis invasões em bancos de dados e vazamentos de dados.
- Relação com os riscos:
- Métrica:
    * Proteção de dados sensíveis
        * Critério de aceitação: >= 80% dos dados sensíveis protegidos
        * Como medir: Inspeção de código e verificação das rotas que usam criptografia
    * Tempo médio de correção de vulnerabilidades
        * Critério de aceitação: <= 5 dias
        * Como medir: média de tempo entre abertura e fechamento de issues relacionadas a segurança
    * Número de vulnerabilidades críticas
        * Critério de aceitação: <= 2 vunerabilidades criticas identificadas
        * Como medir: execução de ferramentas de análise estática e contagem das vulnerabilidades reportadas

## 3. Desempenho
- Justificativas: Nosso sistema trabalha com calculos complexos e leitura de arquivos/extratos, devemos garantir que esses procedimentos ocorram com o melhor desempenho para evitar frustação dos usuários.
- Relação com os riscos:
- Métrica:
     * Tempo de resposta
        * Critério de aceitação: <= 3 segundos
        * Como medir: Execução de requisições e cáculo da média dos tempos obtidos.
    * Uso de CPU
        * Critério de aceitação: <= 80%
        * Como medir: monitoramento durante restes locais
    * Desempenho sob carga
        * Critério de aceitação: <= 100%
        * Como medir: Comparação entre tempo médio com 1 usuário e múltiplos usuários

## 4. Manutenibilidade
- Justificativas: Como se trata de um MVP o projeto tende a evoluir rapidamente com feedback de usuários e mudanças nos requisitos, com um código bem organizado o trabalho em equipe é facilitado no desenvolvimento e a implementação de novas funcionalidades tem uma chance menor de ser acompanhada de erros, a manutembilidade ainda favorece a correção desses possíveis erros.
- Relação com os riscos:
- Métricas:
    * Cobertura de testes
        * Critério de aceitação: >= 60%
        * Como medir: Ferramentas de análise de cobertura (ex: relatórios de teste, professor Adriano, etc)
    * Tempo médio para correção de bugs
        * Critério de aceitação: <= 5 dias
        * Como medir: Acompanhamento de issues (criação vs resolução)
    * Duplicação de código
        * Critério de aceitação: <= 10%
        * Como medir: Análise automatizada ou inspeção manual

## Influência dos Atributos nas Decisões

#### 1. Confiabilidade
 A confiabilidade guiará decisões relacionadas ao tratamento de falhas, validação de operações e garantia de consistência dos dados, especialmente em operações críticas do sistema.

#### 2. Segurança
 A segurança orientará decisões relacionadas á autenticação, autorização e proteção de dados, garantindo que apenas usuários autorizados acessem informações sensíveis e que as operações sejam realizadas de forma segura.

#### 3. Desempenho
 O desempenho influenciará decisões de arquitetura e implementação, como otimização de consultas, usp eficiente de recursos e estratrégias para garantir tempos de resposta adequados mesmo com aumento de carga.

#### 4. Manutenibilidade
 A manutenibilidade orientará a adoção de boas práticas de desenvolvimento, como separação de responsabilidades, padronização de código e uso de testes automatizados, facilitando a evolução do sistema.