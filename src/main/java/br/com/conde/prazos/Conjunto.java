package br.com.conde.prazos;

import java.util.ArrayList;
import java.util.List;

/**
 * UM CONJUNTO DE TAREFAS, que e o que se pergunta se cabe ou nao cabe.
 *
 * <p>A pergunta inteira deste repositorio e sobre o conjunto, nunca sobre uma tarefa: cada
 * tarefa sozinha sempre cabe, e o que pode nao caber e a soma delas disputando um
 * processador so.
 *
 * <h2>O hiperperiodo</h2>
 *
 * <p>E o minimo multiplo comum dos periodos, e e o que torna a pergunta decidivel. Depois de
 * um hiperperiodo, todas as tarefas chegam juntas de novo e a historia se repete igual, entao
 * simular um hiperperiodo a partir do instante em que todas chegam juntas responde sobre o
 * tempo infinito.
 *
 * <p>Esse "a partir do instante em que todas chegam juntas" nao e detalhe: e o INSTANTE
 * CRITICO, o pior momento possivel para uma tarefa chegar, porque ela encontra todas as
 * outras tambem chegando. Comecar a simulacao em qualquer outro ponto mede um caso mais
 * facil e responde a pergunta errada.
 */
public final class Conjunto {

    /** Teto do hiperperiodo. Acima disso o conjunto nao entra nas medidas. */
    public static final long MAIOR_HIPERPERIODO = 2_000_000L;

    private final String nome;
    private final List<Tarefa> tarefas;

    public Conjunto(String nome, List<Tarefa> tarefas) {
        if (tarefas.isEmpty()) throw new IllegalArgumentException("conjunto vazio");

        this.nome = nome;
        this.tarefas = List.copyOf(tarefas);
    }

    public String nome() {
        return nome;
    }

    public List<Tarefa> tarefas() {
        return tarefas;
    }

    public int quantas() {
        return tarefas.size();
    }

    /** A soma das fracoes de processador. Acima de 1 nenhuma politica salva. */
    public double utilizacao() {
        double soma = 0;

        for (Tarefa tarefa : tarefas) soma += tarefa.utilizacao();

        return soma;
    }

    /** O minimo multiplo comum dos periodos: depois dele a historia se repete igual. */
    public long hiperperiodo() {
        long atual = 1;

        for (Tarefa tarefa : tarefas) atual = minimoMultiplo(atual, tarefa.periodo());

        return atual;
    }

    private static long minimoMultiplo(long um, long outro) {
        return um / maiorDivisor(um, outro) * outro;
    }

    private static long maiorDivisor(long um, long outro) {
        while (outro != 0) {
            long resto = um % outro;

            um = outro;
            outro = resto;
        }

        return um;
    }

    /**
     * Verdadeiro quando os periodos sao multiplos uns dos outros.
     *
     * <p>Conjunto harmonico e o caso facil escondido: nele a taxa monotonica aguenta
     * utilizacao 1, e qualquer medida feita so com conjuntos harmonicos diz que o limite
     * classico e inutil. Ha um teste so sobre isso, porque um sorteio descuidado produz
     * harmonicos sem parar.
     */
    public boolean harmonico() {
        List<Integer> periodos = new ArrayList<>();

        for (Tarefa tarefa : tarefas) periodos.add(tarefa.periodo());

        periodos.sort(null);

        for (int i = 1; i < periodos.size(); i++)
            if (periodos.get(i) % periodos.get(i - 1) != 0) return false;

        return true;
    }

    /** Verdadeiro quando todo prazo e igual ao periodo. */
    public boolean prazoNoPeriodo() {
        return tarefas.stream().allMatch(tarefa -> tarefa.prazo() == tarefa.periodo());
    }

    @Override
    public String toString() {
        return nome + " " + tarefas;
    }
}
