# Linha Vital — correções aplicadas a partir do parecer

## Escopo

A documentação foi revisada tomando como referência os apontamentos do parecer anexado. A atualização preserva o conteúdo tecnicamente sustentado pelo projeto e evita preencher lacunas com resultados ou datas não comprovadas.

## Correções aplicadas

| Apontamento | Correção realizada |
|---|---|
| Critérios de seleção do benchmarking ausentes | Foram definidos critérios explícitos de inclusão e exclusão e registrada a data de verificação. |
| Comparativo sem fonte/data verificável | A comparação passou a usar fontes primárias dos fornecedores e ganhou tabela de fonte, escopo e data de verificação. |
| Revisão histórica pouco diversificada | Foram adicionados estudos recentes de 2024, incluindo trabalhos com pessoas idosas e aplicação móvel no contexto brasileiro. |
| Afirmações pouco fundamentadas nas seções 2.2 e 2.3 | Os parágrafos factuais foram reescritos e ancorados em referências acadêmicas. |
| Gestão descrita como planejamento futuro | A seção foi reescrita no passado, descrevendo o processo efetivamente realizado. |
| Falta de cronograma/roadmap | Foram incluídos marcos executados e um roadmap M1–M7. Datas exatas de sprints não foram inventadas porque não há evidência auditável suficiente nos artefatos fornecidos. |
| Rastreabilidade RN x RF incompleta | Foi adicionada uma matriz complementar RN x RF com os relacionamentos cruzados. |
| Histórias sem critérios de aceitação | Cada história passou a ter pelo menos dois critérios no formato Dado/Quando/Então, sem caracterizar adoção de BDD como metodologia. |
| Testes misturando plano e resultado | A seção foi dividida em Plano de Testes e Resultados no Estado Atual, distinguindo o que existe no repositório do que permanece planejado. |
| Métricas de teste não comprovadas | Não foram inventados percentuais de cobertura, tempos de resposta ou resultados de carga; essas métricas foram explicitadas como pendentes. |
| MER/DER pouco legíveis | As figuras foram ampliadas para ocupar melhor a página A4. |
| Ausência de telas/protótipos | Foram incluídas quatro telas do protótipo: check-in, Home/monitoramento, critérios de emergência e contatos. |
| Cenário de R$ 320 sem memória de cálculo | Foi adicionada decomposição explícita de referência, identificada como reserva de planejamento e não como cotação comercial. |
| Sustentabilidade sem modelo de receita | Foram incluídos cenários institucional e freemium e uma simulação ilustrativa de ponto de equilíbrio. |
| Considerações finais incompletas | Foi acrescentada avaliação dos objetivos, além das seções de resultados, dificuldades, limitações e trabalhos futuros. |

## Validação do documento

- LaTeX compilado sem referências ou citações indefinidas.
- PDF final com 63 páginas.
- Protótipos divididos em duas figuras para evitar corte/overflow na página.
- MER e DER inspecionados em página inteira.
- As duas cópias da árvore LaTeX presentes no repositório foram mantidas sincronizadas.

## Limitações mantidas explicitamente

- Não há datas exatas de todas as sprints preservadas de forma auditável nos materiais analisados.
- A suíte automatizada do projeto ainda é reduzida; resultados de integração, E2E, carga, bateria e cobertura permanecem como validações futuras.
- Valores financeiros de expansão são cenários de planejamento e devem ser substituídos por cotações quando fornecedores e volume de usuários forem definidos.
