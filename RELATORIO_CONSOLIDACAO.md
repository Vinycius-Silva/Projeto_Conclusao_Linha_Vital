# Relatório de consolidação documental — Linha Vital

**Data:** 12/11/2026  
**Instituição de referência:** IFSP — Campus São Paulo (Canindé)  
**Escopo:** documentação acadêmica e empacotamento, sem alterações na programação

## Mudanças aplicadas

1. Unificadas **três árvores LaTeX legadas** em uma única fonte `Documentacao/TCC_LaTeX/`. Os arquivos `.tex`, `.bib`, diagramas e imagens necessários foram preservados.
2. A estrutura foi identificada para o **Campus São Paulo**, com referência à Biblioteca Francisco Montojos e às Portarias 782/2023 e 1674/2023. Não foram aplicadas exigências do ADS/Pirituba.
3. Incluída seção de **três personas hipotéticas** (P01--P03) como instrumentos de design, sem alegar entrevistas nem funções inexistentes.
4. Incluído quadro de **critérios de aceitação** confrontados com código e relatório funcional; escopo documental não é confundido com escopo implementado.
5. Incluída distinção formal de **36 casos planejados** (`PL-CTxx`) e **19 cenários executados** (`EX-CTxx`) em 14/10/2026, mantendo a distinção entre planejamento e resultado observado.
6. Acrescentada nota de rastreabilidade do roadmap, evitando apresentar FastAPI/JWT como tecnologias implementadas.
7. Preservado um único exemplar do relatório de testes com **111 evidências**, e fontes de projeto pertinentes, sem repetir a versão DOCX do mesmo relatório.
8. Mantida uma única cópia das portarias e do Guia da Biblioteca Francisco Montojos, com alerta de que o guia é de 2011 e precisa ser acompanhado das normas ABNT atuais.

## Duplicatas/arquivos excluídos

- Duas cópias adicionais da monografia LaTeX (anteriormente uma raiz paralela e outra aninhada).
- Pastas antigas do template com nome IFPI, PDFs de revisão histórica, relatórios anteriores, caches e arquivos auxiliares de compilação.
- Modelos de avaliação específicos do **Campus São Paulo Pirituba** (não aplicáveis a esta entrega).
- Diagramas antigos que ilustravam AWS, Twilio/SMS e serviços de emergência que **não representam** o MVP entregue.
- APK de debug, exemplos genéricos e fontes multimídia sem vínculo claro com a documentação acadêmica.
- **Chave privada SSH e chave pública presentes no ZIP do Grupo 4** (NÃO copiadas). Se a chave privada foi compartilhada, avaliar sua revogação/rotação no servidor.

## Verificações técnicas

- **218 arquivos da pasta `Source/` comparados por SHA-256:** todos idênticos aos originais da base documental revisada.
- A monografia foi alterada somente em arquivos LaTeX; a versão compilada será gerada a partir dessa única fonte.
- A existência de cenários e classes de teste não implica execução adicional nesta etapa.

## Pendências que dependem da equipe/biblioteca

- Confirmar data, banca examinadora, metadados e eventuais correções de orientador/banca.
- Solicitar **ficha catalográfica após as correções da banca**, conforme Portaria 782/2023; não foi inventada.
- Preencher/assinar termo de divulgação conforme redação retificada pela Portaria 1674/2023.
- Realizar checagem final de citações e formatação com normas ABNT em vigor e regras do curso; a adaptação abnTeX2 é legada, **não template oficial do Campus São Paulo**.
- Revisar prints e documentos originais quanto a dados pessoais antes de distribuir publicamente.
- Validar esquema real da base PostgreSQL e smoke test do backend pós-deploy, caso exigidos pela banca.


## Resultado de compilação e empacotamento

- `latexmk -pdf -interaction=nonstopmode -halt-on-error documento.tex`: **compilado com sucesso** em 12/11/2026.
- PDF final: **78 páginas**, tamanho A4, gerado da única árvore LaTeX incluída.
- `Source/`: **218 arquivos**, idênticos bit a bit à versão de entrada.
- **33 arquivos `.tex`** preservados, além dos arquivos de bibliografia, figuras e estilo necessários à recompilação.
- `299 arquivos` no projeto completo antes de finalizar este relatório.
- `0 arquivos auxiliares de build/temporários` removidos durante a preparação do pacote.
- A compilação produziu avisos de composição tipográfica pontuais (`overfull/underfull`), **sem falha fatal e sem referências indefinidas**. Revisão visual amostral realizada sobre capa, tabelas novas, seção de testes e bibliografia.


## Correção adicional para publicação no GitHub — 12/11/2026

A comparação do consolidado com o ZIP original apontou duas perdas documentais,
que **foram corrigidas neste pacote**:

- `.gitignore` da raiz recuperado e ampliado com exclusões específicas para
  chaves privadas, credenciais e arquivos transitórios;
- README técnico original recuperado em `Documentacao/GUIA_TECNICO.md`, com
  links atualizados para a monografia e a bateria de testes. O `README.md`
  principal permanece como índice, sem duplicação do conteúdo integral.

O novo arquivo `GUIA_PUBLICACAO_GITHUB.md` orienta como inserir os conteúdos na
raiz de um clone já existente, preservar o histórico Git e eliminar as pastas
redundantes antigas. Os arquivos `Source/` e `Projeto/` continuam inalterados.

Os relatórios históricos de correção e PDFs de versões antigas **não foram
reintroduzidos**, para preservar a árvore sem duplicatas. A inspeção de nomes
sensíveis e marcadores comuns de chave privada não substitui uma revisão de
dados pessoais nos documentos de testes; conferir antes de um repositório público.

Os Gradle Wrappers `Source/backend/gradlew` e `Source/frontend/gradlew` tiveram a permissão executável restaurada para ambientes Unix/Linux.
