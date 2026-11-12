# Monografia Linha Vital — fonte única em LaTeX

**Instituição: Instituto Federal de Educação, Ciência e Tecnologia de São Paulo (IFSP) — Campus São Paulo.**

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

Antes do depósito institucional, verificar com a coordenação e a biblioteca os procedimentos aplicáveis, incluindo eventuais correções solicitadas pela banca, ficha catalográfica e autorizações de divulgação. Conferir também a proteção de dados pessoais nas evidências. As referências institucionais disponíveis estão em `../Normas_IFSP/`. Não foi criada ficha catalográfica fictícia.

## Fundamentação das evidências
A bateria funcional executada pela equipe foi concluída em 14/10/2026 e está documentada em `../Evidencias/`, com 19 cenários (17 aprovados e 2 aprovados com observações) e 111 capturas de tela. Os 36 casos do plano original são apresentados separadamente, pois sua execução individual integral não está demonstrada no relatório. A revisão documental de novembro não corresponde a uma nova execução de testes automatizados.
