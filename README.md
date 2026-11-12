# Linha Vital — projeto completo consolidado

Trabalho de Conclusão de Curso de Tecnologia em Análise e Desenvolvimento de Sistemas — **IFSP, Campus São Paulo (Canindé)**. Versão de consolidação documental: 12/11/2026.

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
| `RELATORIO_CONSOLIDACAO.md` | Alterações, itens retirados, verificação e pendências |

## Execução e manutenção

O [guia técnico](Documentacao/GUIA_TECNICO.md) descreve a preparação do PostgreSQL,
variáveis de ambiente, execução do backend e do Android, endpoints REST,
autenticação, SOS e comandos para testes automatizados.

Para publicar alterações no GitHub, consulte o [guia de commit](GUIA_PUBLICACAO_GITHUB.md).
Não faça upload do próprio ZIP para dentro do repositório: **extraia e versione os arquivos**.

## Compilação da monografia
Abra `Documentacao/TCC_LaTeX/README.md` para o comando e as dependências. O estilo local veio de uma adaptação anterior baseada no abnTeX2; não é um template oficial do IFSP. Arquivos de outros campi e cópias legadas foram eliminados.

## Interpretação dos resultados
O relatório de 14/10/2026 registra 19 cenários funcionais (17 aprovados e 2 aprovados com observações). A execução refere-se à branch e ao ambiente registrados naquele documento. O código-fonte de `Source/` não foi alterado nesta consolidação; não houve novo teste em produção.

## Antes da publicação
Revisar dados pessoais nas evidências e configurar segredos operacionais via variáveis de ambiente; não distribuir chaves privadas SSH. A versão final a ser depositada depende de correções da banca, ficha catalográfica solicitada à biblioteca e termo de autorização preenchido/assinado. O Linha Vital é um MVP demonstrativo, não um serviço garantido de resposta a emergências.
