# ADR-0004 — Estratégia de arquitetura e camadas

## Status
Em espera

## Contexto
O projeto precisa de uma estratégia de arquitetura para organizar o desenvolvimento do projeto, visando agilidade e organização entre os membros.

## Decisão
A equipe decidiu utilizar o MVC(Model-View-Controller)
como estratégia de arquitetura e camadas.

## Alternativas consideradas
- Microserviços
- Orientada a Eventos (Event-Driven)

## Consequências
### Positivas
- Padrão do frameword Spring.   
- Separação de responsabilidades clara
- Manutenção e evolução mais simples

### Negativas / trade-offs
- Curva de aprendizagem maior para alguns membros
- Acoplamento com o framework, devido a isso pode difilcutar teste unitarios e eventual troca de tecnologia.

## Revisão futura
A decisão poderá ser revista caso ocorra o crescimento da complexidade de negócio, a necessidade de maior testabilidade ou uma mudança de framework ou linguagem.