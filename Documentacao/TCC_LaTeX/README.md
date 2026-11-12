# Monografia Linha Vital — fonte única em LaTeX

**Instituição: IFSP — Campus São Paulo (Canindé).**

Este diretório é a única cópia editável da monografia no pacote. O PDF `documento.pdf` é o resultado compilado dos mesmos arquivos e não outra versão do texto.

## Edição
- `documento.tex`: arquivo de entrada;
- `capitulos/*.tex`: capítulos da monografia;
- `pre-textual/*.tex`: resumo, abstract, elementos pré-textuais;
- `bibliografia.bib`: referências;
- `imagens/`: figuras e arquivos-fonte `.dot` dos diagramas;
- `configuracoes/`: metadados institucionais, tipografia e configurações;
- `estilo-adaptado/`: customização **legada** do abnTeX2, mantida como dependência técnica. **Não é um template oficial do Campus São Paulo.**

## Como compilar
Na pasta `Documentacao/TCC_LaTeX`, com distribuição TeX instalada, execute:

```bash
latexmk -pdf -interaction=nonstopmode -halt-on-error documento.tex
```

Se necessário, a cadeia equivalente é `pdflatex`, `bibtex`, `pdflatex`, `pdflatex` (as referências deste projeto usam BibTeX). A primeira compilação pode gerar arquivos temporários de build; eles não foram incluídos no ZIP.

## Critérios acadêmicos
Referência institucional: Biblioteca Francisco Montojos (Campus São Paulo); Portarias IFSP 782/2023 e 1674/2023; ABNT aplicável. O guia da biblioteca disponibilizado é de 2011 e deve ser confrontado com as edições recentes da ABNT. **A conformidade integral não foi certificada pela biblioteca nem pelo orientador.**

Antes da entrega à Biblioteca: confirmar dados da banca e data de defesa, fazer correções solicitadas pela banca, solicitar ficha catalográfica e coletar assinaturas do termo de divulgação; conferir tratamento de dados pessoais em evidências. Os arquivos administrativos estão em `../Normas_IFSP/`. A ficha catalográfica NÃO foi simulada.

## Fundamentação das evidências
O relatório original de 14/10/2026 com 111 capturas está armazenado somente em `../Evidencias/`. O planejamento de 36 casos é diferenciado da execução de 19 cenários; não foi feita nova execução automatizada no processo de revisão documental.
