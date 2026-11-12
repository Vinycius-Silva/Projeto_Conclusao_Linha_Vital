# Linha Vital — Trabalho de Conclusão de Curso

Trabalho de Conclusão de Curso de Tecnologia em Análise e Desenvolvimento de Sistemas — **Instituto Federal de Educação, Ciência e Tecnologia de São Paulo (IFSP), Campus São Paulo**. Apresentação prevista para 13/11/2026.

## Onde está cada material?

| Caminho | Conteúdo |
|---|---|
| `Source/backend/` | Backend Kotlin / Spring Boot; código preservado |
| `Documentacao/GUIA_TECNICO.md` | **Guia operacional recuperado**: execução, configurações, endpoints, segurança e testes |
| `Source/frontend/` | Aplicativo Android Kotlin; código preservado |
| `Projeto/` | Arquivo original Figma e ativos de identidade visual |
| `Documentacao/TCC_LaTeX/documento.tex` | **Fonte única da monografia em LaTeX** |
| `Documentacao/TCC_LaTeX/documento.pdf` | Monografia compilada a partir do fonte acima |
| `Documentacao/Evidencias/` | Relatório único dos testes funcionais e 111 evidências |
| `Documentacao/Fontes_de_Projeto/` | Documentos históricos/fontes editáveis dos requisitos, personas, testes planejados e roadmap |
| `Documentacao/Normas_IFSP/` | Portarias de entrega e guia da Biblioteca Francisco Montojos |

## Execução e manutenção

O [guia técnico](Documentacao/GUIA_TECNICO.md) descreve a preparação do PostgreSQL,
variáveis de ambiente, execução do backend e do Android, endpoints REST,
autenticação, SOS e comandos para testes automatizados.

As alterações são versionadas no GitHub; os documentos acadêmicos estão concentrados em `Documentacao/`.

## Compilação da monografia
Abra `Documentacao/TCC_LaTeX/README.md` para o comando e as dependências. O estilo local veio de uma adaptação anterior baseada no abnTeX2; não é um template oficial do IFSP. Arquivos de outros campi e cópias legadas foram eliminados.

## Interpretação dos resultados
A bateria de validação funcional executada pela equipe foi concluída em 14/10/2026, com 19 cenários documentados: 17 aprovados e 2 aprovados com observações não bloqueantes. O planejamento original incluía 36 casos, mas o relatório não demonstra execução individual de todos eles. Os resultados correspondem ao ambiente e à revisão de código identificados no relatório, sem representar certificação para operação em emergências reais.

## Antes da publicação
Revisar dados pessoais nas evidências e configurar segredos operacionais via variáveis de ambiente; não distribuir chaves privadas SSH. Após a apresentação, verificar com a coordenação e com a biblioteca as exigências de depósito, ficha catalográfica e documentos de autorização aplicáveis. O Linha Vital é um MVP demonstrativo, não um serviço garantido de resposta a emergências.
