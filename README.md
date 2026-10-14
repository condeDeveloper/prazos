# prazos

Escalonamento de tempo real do zero em Java 21: **72,9% dos conjuntos que o limite clássico
recusa cumprem todos os prazos.**

Aqui não existe "rápido o bastante na média". Ou toda execução de toda tarefa termina dentro do
prazo, para sempre, ou o conjunto não serve. A média não é consolo nenhum para o freio que atua
tarde.

São cinco políticas, taxa monotônica, prazo monotônico, prazo mais cedo, menor folga e primeiro
que chega, medidas sobre 2.000 conjuntos de tarefas periódicas sorteados.

## Dois juízes que não têm nada em comum

O primeiro é a **simulação**: o relógio anda de uma em uma unidade, as tarefas chegam quando o
período manda, a política escolhe uma, e conta-se quem passou do prazo. Ela não é uma análise e
não supõe nada.

Um **hiperperíodo** basta porque todas as tarefas chegam juntas no instante zero e voltam a
chegar juntas no mínimo múltiplo comum dos períodos: o estado no fim é igual ao do começo, e o
segundo hiperperíodo repete o primeiro. E começar com todas chegando juntas é o **instante
crítico**, o pior caso possível; qualquer outro começo mede uma situação mais fácil.

O segundo é a **análise de tempo de resposta**, que responde a mesma pergunta sem relógio nenhum:

```
R = C + soma sobre as mais prioritárias de teto(R / Tj) * Cj
```

Para terminar, a tarefa precisa do próprio custo mais o tempo que as mais prioritárias roubam
dela, e quantas vezes elas chegam depende de quanto ela demora, que é o que está sendo
calculado. Daí o ponto fixo.

E a comparação entre os dois não é só o sim ou não: é o **pior tempo de resposta de cada tarefa**.

```
$ mvn exec:java -Dexec.args="correcao"

  politica              conjuntos    discordaram    tarefas  resposta errada
  taxa monotonica            2000              0       6665                0
  prazo monotonico           2000              0       6665                0

E O CRITERIO DO PRAZO MAIS CEDO, que e exato e nao so suficiente:
  utilizacao ate 1 em 1979 conjuntos, simulacao escalonou 1979, discordaram em 0.
```

A última linha é o terceiro juiz, e esse é exato: com prazo igual ao período, o prazo mais cedo
escalona se, e somente se, a utilização couber em 1. Aprovou 1.979 conjuntos e a simulação
escalonou exatamente os mesmos 1.979.

## Os conjuntos escritos à mão

```
$ mvn exec:java -Dexec.args="correcao"

  conjunto              U    hiper     limite     taxa  prazo D prazo abs    folga  chegada
  duas tarefas      0.686       35     aprova   cumpre   cumpre   cumpre   cumpre   cumpre
  classico          0.700      100     aprova   cumpre   cumpre   cumpre   cumpre    PERDE
  harmonico         1.000       16    reprova   cumpre   cumpre   cumpre   cumpre    PERDE
  no limite         0.952      210    reprova   cumpre   cumpre   cumpre   cumpre    PERDE
  quase cheio       0.957     1001    reprova    PERDE    PERDE   cumpre   cumpre    PERDE
  prazo curto       0.493      300     aprova    PERDE   cumpre   cumpre   cumpre   cumpre
  sobrecarregado    1.400       30    reprova    PERDE    PERDE    PERDE    PERDE    PERDE
```

Cada linha está aqui porque separa duas coisas que a anterior não separava. Olhe as duas do
meio: `no limite` e `quase cheio` têm quase a mesma utilização, 95,2% e 95,7%, e o limite
reprova as duas. Uma cumpre todos os prazos e a outra não. É por não conseguir separar esses
dois casos que o limite precisa ser conservador.

E olhe `prazo curto`: **49,3% de utilização e a taxa monotônica perde prazo**. A tarefa que volta
devagar precisa responder rápido, a taxa monotônica olha o período e dá prioridade à errada.
Quando o prazo é menor que o período, utilização não diz nada.

## O limite recusa o que a simulação aprova

```
$ mvn exec:java -Dexec.args="limite"

  conjuntos no lote                                2000
  aprovados por Liu e Layland                      1023
  aprovados pelo limite hiperbolico                1172
  escalonados de verdade pela taxa monotonica      1735

  aprovados pelo classico que perdem prazo            0
  aprovados pelo hiperbolico que perdem prazo         0
  reprovados pelo classico que escalonam            712
  o hiperbolico aprova e o classico nao             149

  Dos 977 reprovados pelo limite classico, 712 escalonam: 72.9%.
  O limite para 3 tarefas e 0.7798, e tende a 0.6931.
```

O limite de Liu e Layland, `n * (2^(1/n) - 1)`, é **suficiente e não necessário**, e essa é a
frase que a tabela existe para desfazer. Nenhum dos 1.023 aprovados perde prazo: o teorema vale.
Só que entre os 977 reprovados, 712 cumprem tudo.

O limite tende a `ln 2`, uns 69,3%, e é daí que sai a frase de que sistemas de tempo real só
usam 70% do processador. A taxa monotônica escalonou **1.735 de 2.000**, com utilização que vai
até 1.

O limite hiperbólico, o produto de `(Ui + 1) <= 2`, aprova **149 conjuntos a mais** e custa a
mesma conta. Nenhum deles perde prazo.

## Quanta carga cada política aguenta

```
$ mvn exec:java -Dexec.args="utilizacao"

  faixa           conjuntos         taxa      prazo D    prazo abs        folga      chegada
  0.5 a 0.6           353          353          353          353          353          166
  0.6 a 0.7           388          388          388          388          388          161
  0.7 a 0.8           401          401          401          401          401          117
  0.8 a 0.9           410          386          386          410          410           76
  0.9 a 1.0           416          196          196          416          416           52

  Harmonicos no lote: 193, dos quais 187 cabem no processador.
  Nesses, a taxa monotonica perde prazo em 0: num conjunto harmonico o limite dela e 1.
```

Até 80% de carga, qualquer política que olhe prazo cumpre tudo. A taxa monotônica só começa a
cair na faixa dos 80%, e na dos 90% ela escalona menos da metade.

Quem **não** olha prazo perde cedo: com o processador meio vazio, o primeiro que chega já falha
em 187 dos 353 conjuntos.

A última linha é o caso fácil escondido. Em conjunto harmônico, com períodos múltiplos uns dos
outros, o limite da taxa monotônica sobe para 1, e nenhum limite de utilização clássico enxerga
isso. São 187 conjuntos do lote, todos escalonados, muitos deles reprovados pelo limite.

## O que acontece quando não cabe

```
$ mvn exec:java -Dexec.args="sobrecarga"

  politica                trabalhos     perderam     do total tarefas afetadas  pior atraso
  taxa monotonica             82767        15000        18.1%            41.0%        25037
  prazo monotonico            82767        15000        18.1%            41.0%        25037
  prazo mais cedo             82767        74677        90.2%            95.8%         9840
  menor folga                 82767        78282        94.6%            99.9%         9889
  primeiro que chega          82767        78862        95.3%            91.1%         9984
```

Quinhentos conjuntos com utilização entre 1,05 e 1,4. Nenhuma política escalona, e dizer isso
não mede nada; a pergunta que importa é **quem** perde o prazo.

O prazo mais cedo é ótimo quando dá para cumprir tudo. Quando não dá, ele persegue o trabalho que
já vai atrasar, deixa o seguinte atrasar junto, persegue esse, e assim por diante: **90,2% dos
prazos perdidos, contra 18,1% da prioridade fixa**.

E a outra metade da mesma moeda está na última coluna. A taxa monotônica perde menos prazos e
quem ela sacrifica fica muito pior: pior atraso de 25.037 unidades contra 9.840. Ela protege as
tarefas de período curto e simplesmente abandona a de período longo.

## O preço das preempções

```
$ mvn exec:java -Dexec.args="custo"

  politica                conjuntos    trabalhos preempcoes por trabalho
  taxa monotonica              1735       266792               0.2731
  prazo monotonico             1735       266792               0.2731
  prazo mais cedo              1735       266792               0.2505
  menor folga                  1735       266792               1.9595
  primeiro que chega           1735       266792               0.0000
```

Nos **mesmos conjuntos** para todas, que são os que a taxa monotônica escalona. Comparar a média
de cada política sobre os conjuntos que ela própria escalona mediria populações diferentes, e a
que aguenta mais ficaria com os casos mais apertados.

## Como rodar

```
mvn test
mvn exec:java -Dexec.args="tudo"
```

Os comandos do medidor são `correcao`, `limite`, `utilizacao`, `sobrecarga` e `custo`.

## O que as medidas me corrigiram

**O primeiro erro foi do meu juiz, e quem me contou foi o outro juiz.** A simulação parava um
instante antes do fim do hiperperíodo, e o último trabalho de cada tarefa tem prazo exatamente
ali. Resultado: o trabalho que não terminava sumia da conta, e a simulação aprovava conjunto de
utilização 1,04, com a tarefa de período grande nunca rodando, nunca terminando e nunca sendo
acusada. A análise de tempo de resposta discordou dela em dezenas de conjuntos, e toda discordância era
esse caso. É o motivo de haver dois juízes, e não um.

**A frase dos 69% está errada, e a medida diz de quanto.** O limite de Liu e Layland recusa 977
dos 2.000 conjuntos, e **712 deles cumprem todos os prazos**. Suficiente não é necessário. Quem
dimensiona um sistema pelo limite joga fora quase três quartos do que estava reprovando.

**"Ótima" vale só enquanto dá.** O prazo mais cedo escalona tudo o que é escalonável, e é
exatamente ele que entra em efeito dominó quando a carga passa de 1: 90,2% dos prazos perdidos
contra 18,1% da taxa monotônica. Eu ia escrever que a prioridade fixa é a escolha conservadora;
ela é a escolha que decide **antes** quem vai ser sacrificado.

**E essa decisão tem um custo que eu não tinha medido.** O pior atraso da taxa monotônica é
25.037 unidades contra 9.840 do prazo mais cedo. Ela não degrada melhor: ela concentra o estrago
em 41% das tarefas em vez de espalhar por 96%.

**A conta das preempções era para ser um fator, e é uma margem.** O prazo mais cedo preempta
0,2505 vezes por trabalho contra 0,2731 da taxa monotônica: **8% menos**, não um fator. A menor
folga, que é tão ótima quanto o prazo mais cedo, preempta **7,8 vezes mais** e não escalona um
único conjunto a mais, porque a folga de quem está esperando diminui a cada unidade de tempo e a
ordem muda no meio da execução.

**Utilização não diz nada quando o prazo é menor que o período.** O conjunto `prazo curto` usa
49,3% do processador e a taxa monotônica perde prazo nele. O prazo monotônico, que é a mesma
política olhando a coluna certa, cumpre tudo.

## O que não tem aqui

**Recursos compartilhados.** Nenhuma tarefa daqui trava um semáforo. É o que falta para a
inversão de prioridade existir, que é o problema que derrubou o Mars Pathfinder em 1997, e com
ele vêm a herança de prioridade e o protocolo do teto. Isso muda a fórmula do tempo de resposta,
que ganha um termo de bloqueio, e é outro repositório.

**Mais de um processador.** Tudo aqui disputa um processador só. Em vários, a taxa monotônica
deixa de ser ótima, aparecem os efeitos de anomalia de escalonamento, e o limite de utilização
cai para perto de metade. É um assunto diferente, não uma extensão.

**Tarefas esporádicas e servidores.** Aqui toda tarefa é periódica e chega na hora. Tratar
evento que chega quando quer, sem perder a garantia das periódicas, é o que servidor de banda
e servidor esporádico fazem.

**Medir o pior caso de execução.** O custo de cada tarefa é dado. Descobrir o pior caso de um
código de verdade, com cache, previsão de desvio e tudo o mais, é uma área inteira, e é a
hipótese sobre a qual todo este repositório se apoia.

**Políticas não preemptivas de verdade.** O primeiro que chega daqui decide a cada unidade de
tempo como as outras; ele nunca interrompe ninguém porque quem chegou antes continua sendo o
mais antigo. Escalonamento não preemptivo tem teoria própria e é mais difícil, não mais fácil.

## Licença

MIT.
