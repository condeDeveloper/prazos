package br.com.conde.prazos;

import java.util.ArrayList;
import java.util.List;

/**
 * O SEGUNDO JUIZ: o tempo de resposta no pior caso, por ponto fixo.
 *
 * <p>Ele responde a mesma pergunta da simulacao sem rodar o relogio. Para prioridade fixa, o
 * pior tempo de resposta de uma tarefa e o menor R que satisfaz
 *
 * <pre>R = C + soma sobre as tarefas de prioridade maior de teto(R / Tj) * Cj</pre>
 *
 * <p>A leitura e direta: para terminar, a tarefa precisa do proprio custo mais o tempo que as
 * mais prioritarias roubam dela, e quantas vezes elas chegam depende de quanto ela demora,
 * que e o que esta sendo calculado. Dai o ponto fixo.
 *
 * <p>Ele e um juiz de verdade e nao uma segunda opiniao porque e INDEPENDENTE: nao ha
 * relogio, nao ha fila, nao ha trabalho, nao ha preempcao. Se a formula e a simulacao
 * discordam em qualquer conjunto, uma das duas esta errada, e a tabela da correcao existe
 * exatamente para procurar isso.
 *
 * <p>E ele so vale com prioridade fixa. Para o prazo mais cedo e a menor folga, a prioridade
 * muda com o tempo e nao ha hierarquia fixa para somar; ali so a simulacao responde.
 */
public final class Resposta {

    /** Teto de iteracoes do ponto fixo, para o caso de utilizacao acima de 1. */
    public static final int ORCAMENTO = 100_000;

    private Resposta() {}

    public record Linha(Tarefa tarefa, int tempoDeResposta, boolean cumpre, int iteracoes) {}

    /**
     * Calcula o pior tempo de resposta de cada tarefa, na ordem de prioridade da politica.
     *
     * <p>Quando a tarefa nao cumpre o prazo, o tempo devolvido e o primeiro valor que passou
     * do prazo: o ponto fixo pode nem existir, e insistir nele seria um laco infinito com
     * utilizacao acima de 1.
     */
    public static List<Linha> calcular(Conjunto conjunto, Politica politica) {
        if (!politica.prioridadeFixa())
            throw new IllegalArgumentException(
                    "a analise de resposta so vale com prioridade fixa: " + politica.nome());

        List<Tarefa> ordenadas = porPrioridade(conjunto, politica);

        List<Linha> saida = new ArrayList<>();

        for (int i = 0; i < ordenadas.size(); i++) {
            Tarefa tarefa = ordenadas.get(i);

            List<Tarefa> maisPrioritarias = ordenadas.subList(0, i);

            long atual = tarefa.custo();

            int iteracoes = 0;

            while (iteracoes < ORCAMENTO) {
                iteracoes++;

                long proximo = tarefa.custo();

                for (Tarefa outra : maisPrioritarias)
                    proximo += (long) Math.ceil(atual / (double) outra.periodo())
                            * outra.custo();

                if (proximo > tarefa.prazo()) {
                    atual = proximo;
                    break;
                }

                if (proximo == atual) break;

                atual = proximo;
            }

            saida.add(new Linha(tarefa, (int) Math.min(atual, Integer.MAX_VALUE),
                    atual <= tarefa.prazo(), iteracoes));
        }

        return saida;
    }

    /** Verdadeiro quando toda tarefa cumpre o prazo no pior caso. */
    public static boolean escalonavel(Conjunto conjunto, Politica politica) {
        return calcular(conjunto, politica).stream().allMatch(Linha::cumpre);
    }

    /**
     * As tarefas da mais prioritaria para a menos, segundo a politica.
     *
     * <p>O desempate e a ordem no conjunto, a mesma regra da simulacao. Se as duas
     * desempatassem diferente, a comparacao entre elas mediria o desempate e nao a teoria.
     */
    public static List<Tarefa> porPrioridade(Conjunto conjunto, Politica politica) {
        List<Tarefa> tarefas = new ArrayList<>(conjunto.tarefas());

        List<Tarefa> originais = conjunto.tarefas();

        tarefas.sort((uma, outra) -> {
            long chaveDeUma = politica.chave(new Trabalho(uma, 0), 0);
            long chaveDeOutra = politica.chave(new Trabalho(outra, 0), 0);

            if (chaveDeUma != chaveDeOutra) return Long.compare(chaveDeUma, chaveDeOutra);

            return Integer.compare(originais.indexOf(uma), originais.indexOf(outra));
        });

        return tarefas;
    }
}
