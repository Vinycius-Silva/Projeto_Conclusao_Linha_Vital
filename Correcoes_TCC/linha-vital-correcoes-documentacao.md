# Linha Vital — correções da documentação conforme parecer

Esta revisão foi aplicada somente à documentação acadêmica e aos recursos visuais usados por ela. O código-fonte em `Source/` permaneceu inalterado.

## Correções realizadas

1. **Análise da concorrência**
   - incluídos critérios explícitos de seleção dos concorrentes;
   - registrada a data e a forma de verificação das funcionalidades;
   - tabela comparativa revisada para distinguir funcionalidades verificadas, parciais e previstas;
   - adicionadas fontes oficiais para Demumu/Are You Dead? e Ok Alone.

2. **Revisão da literatura**
   - reforçada com literatura recente de 2023 e 2024;
   - adicionadas referências sobre mobile integrated health, smart-home health e monitoramento de atividades de vida diária em pessoas idosas;
   - reforçadas as citações nas seções de atualidade e outros contextos.

3. **Gestão do projeto**
   - redação ajustada para descrever a execução no passado;
   - incluída evidência rastreável do histórico do repositório;
   - incluído roadmap por macroetapas/marcos;
   - datas de sprints não foram inventadas porque não havia registro formal suficiente nos artefatos analisados.

4. **Rastreabilidade e histórias de usuário**
   - adicionada matriz RN x RF;
   - adicionados critérios de aceitação às 10 histórias de usuário, com contexto, ação e resultado esperado.

5. **Testes e manutenibilidade**
   - separação explícita entre Plano de Testes e Resultados de Testes Executados;
   - documentados somente os testes automatizados que existem no repositório;
   - cobertura, bateria, disponibilidade e desempenho permanecem identificados como métricas pendentes quando não há evidência versionada.

6. **Diagramas**
   - MER e DER receberam recorte e ampliação para melhorar a leitura em A4.

7. **Protótipos da aplicação**
   - adicionadas quatro figuras: tela principal/monitoramento, confirmação de bem-estar, critérios de emergência e contatos.

8. **Viabilidade financeira**
   - removido o valor escalado de R$ 320,00/mês como cifra fechada sem memória de cálculo;
   - incluída uma memória parametrizada para cenário ampliado;
   - custo de SMS foi retirado do custo fixo do MVP porque a versão implementada utiliza e-mail/SMTP para o acionamento automático;
   - incluídos dois modelos de sustentabilidade e fórmula de ponto de equilíbrio, sem inventar preço comercial ainda não validado.

9. **Considerações finais**
   - mantida e complementada a conclusão já atualizada anteriormente;
   - trabalhos futuros incluem expansão para iOS e possibilidade de gamificação não crítica, além das integrações e validações técnicas já registradas.

## Validação

- LaTeX compilado com `pdflatex` + BibTeX;
- documento final: **64 páginas**;
- nenhuma referência ou citação indefinida no passe final;
- páginas modificadas renderizadas e inspecionadas visualmente;
- MER/DER, tabela de concorrentes, protótipos e viabilidade financeira conferidos em renderização;
- patch testado com `git apply --check` e reprodução exata dos 26 arquivos alterados/adicionados;
- diretório `Source/` comparado com a versão-base: **sem alterações**.

## Observação

O roadmap foi documentado por macroetapas verificáveis. Datas específicas de sprints, percentuais de cobertura e métricas de desempenho não foram adicionados porque os materiais disponíveis não fornecem evidência suficiente para esses valores.
