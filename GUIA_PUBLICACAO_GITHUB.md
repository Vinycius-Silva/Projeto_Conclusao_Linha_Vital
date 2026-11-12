# Publicação da versão consolidada no GitHub

Esta entrega **não contém a pasta `.git`**. Preserve o histórico do repositório
existente; não use `git init` nem substitua a pasta `.git` do clone.

## Antes de atualizar

1. Use uma cópia/clone local do repositório atual e confirme `git status` sem
   modificações pendentes (ou salve-as em um commit/branch anterior).
2. Considere criar a branch `docs/consolidacao-ifsp-sao-paulo` a partir da branch
   desejada antes de copiar os arquivos.
3. Extraia o ZIP; dentro da pasta `Linha_Vital` estão os **conteúdos da raiz** a
   versionar (`README.md`, `.gitignore`, `Source/`, `Projeto/`, `Documentacao/`, etc.).
4. Faça a cópia desses conteúdos para a raiz do clone, **não criando outra pasta
   `Linha_Vital/` dentro do repositório**.
5. Remova, pelo gerenciador de arquivos ou no Git, as árvores LaTeX antigas do
   template IFPI e outros arquivos redundantes que ainda existam no clone. A
   simples cópia dos arquivos novos **não remove** os antigos.
6. Não inclua `.env` reais, senhas, chaves SSH, certificados privados ou pastas
   de build. Revise eventuais números de telefone/e-mails nas capturas do
   relatório de testes **antes de tornar o repositório público**.
7. Leia o resultado de `git status --short` e `git diff --stat`. Confirme que as
   remoções previstas são apenas arquivos antigos/duplicados, e que o código
   em `Source/` não foi alterado pela atualização documental.

## Exemplo de comandos (na raiz do clone, após copiar e revisar)

```bash
git status --short
git diff --stat
git diff --check
git add -A
git diff --cached --stat
git diff --cached --check
git commit -m "docs: consolidar TCC Linha Vital para IFSP Sao Paulo"
git push -u origin docs/consolidacao-ifsp-sao-paulo
```

O `git push` acima pressupõe que você tenha criado a branch com o nome indicado,
por exemplo:

```bash
git switch -c docs/consolidacao-ifsp-sao-paulo
```

**Avisos de espaçamento:** `git diff --cached --check` pode listar espaços
finais em arquivos LaTeX legados (`.cls`, `.sty`, `.tex`) ou em trechos do
código já existentes. Não modifique esses arquivos automaticamente apenas para
eliminar avisos; avalie primeiro se a alteração afetaria a compilação ou a
integridade do projeto. Esses avisos não impedem criar um commit.

Se preferir alterar diretamente a branch existente, **não use** o comando de
criação de branch e ajuste o `git push` para o nome correto. Recomenda-se uma
pull request para revisão antes da integração. Nunca use `--force` para
substituir o histórico remoto nesta atualização.

**Observação:** GitHub Desktop também pode ser utilizado. Ao colar os arquivos
novos na pasta do repositório local, confira os arquivos deletados, modificados
e adicionados na aba Changes antes de preencher a mensagem e clicar em Commit.

## Arquivos oficiais do TCC

- `Documentacao/TCC_LaTeX/documento.tex`: fonte principal editável.
- `Documentacao/TCC_LaTeX/documento.pdf`: versão compilada para consulta.
- `Documentacao/Evidencias/Relatorio_Bateria_Testes_2026-10-14.pdf`: relatório
  de validação funcional pré-deploy.
- `Documentacao/GUIA_TECNICO.md`: execução, APIs, configuração e testes.

A documentação acadêmica é um MVP, não uma certificação de serviço de emergência.
O depósito institucional deve ocorrer após a revisão da banca e os procedimentos
obrigatórios do IFSP.
