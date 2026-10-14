package br.com.conde.prazos;

import java.util.ArrayList;
import java.util.List;

/**
 * CONJUNTOS QUE NINGUEM ESCOLHEU.
 *
 * <p>Os conjuntos escritos a mao tem um defeito que nao da para consertar escolhendo melhor:
 * eles foram escolhidos. Cada um esta no repositorio porque mostra alguma coisa, entao medir
 * "quantos a taxa monotonica aguenta" entre eles mede a minha selecao.
 *
 * <h2>Como a utilizacao e repartida</h2>
 *
 * <p>Pelo algoritmo UUniFast, de Bini e Buttazzo. Repartir a utilizacao de um jeito
 * ingenuo, sorteando cada fracao e normalizando no fim, concentra as fracoes perto da media
 * e some com os casos interessantes, que sao justamente os conjuntos com uma tarefa pesada e
 * varias leves.
 *
 * <p>O UUniFast sorteia o corte da soma restante elevado a 1/i, o que distribui os pontos
 * uniformemente sobre o simplexo. Em uma linha: ele nao favorece conjunto equilibrado.
 *
 * <h2>Por que os periodos saem de uma lista</h2>
 *
 * <p>Porque o hiperperiodo e o minimo multiplo comum, e com periodos quaisquer ele explode:
 * tres primos perto de vinte ja dao milhares, cinco dao milhoes, e a simulacao, que e o
 * juiz, para de caber. A lista mistura numeros com divisores comuns e sem, e os conjuntos
 * cujo hiperperiodo passa do teto saem contados, nao escondidos.
 */
public final class Sorteador {

    /**
     * De onde cada periodo e tirado.
     *
     * <p>Sao os mesmos dezesseis numeros de 2 a 30, multiplicados por dez. A escala existe
     * porque o custo e inteiro: com periodo 4, uma tarefa de utilizacao 0,1 pede custo 0,4,
     * que nao existe, e o conjunto inteiro ia fora. Multiplicar por dez nao muda nenhuma
     * razao entre periodos, nao muda o hiperperiodo relativo, e faz caber tarefa leve.
     */
    public static final int[] PERIODOS =
            {20, 30, 40, 50, 60, 80, 90, 100, 120, 150, 160, 180, 200, 240, 250, 300};

    public static final int MENOS_TAREFAS = 2;
    public static final int MAIS_TAREFAS = 6;

    private final Sorteio sorteio;

    private int descartadosPorHiperperiodo;
    private int descartadosPorCustoZero;

    public Sorteador(long semente) {
        this.sorteio = new Sorteio(semente);
    }

    /**
     * Um conjunto com esta utilizacao pedida, ou nulo quando ele nao serve.
     *
     * <p>O custo e inteiro, entao a utilizacao que sai nao e exatamente a pedida. Quem usa
     * precisa olhar a utilizacao do conjunto, nunca a que foi pedida, e as tabelas fazem
     * isso.
     */
    public Conjunto um(int numero, double utilizacaoPedida) {
        int quantas = MENOS_TAREFAS + sorteio.ate(MAIS_TAREFAS - MENOS_TAREFAS + 1);

        double[] fracoes = repartir(quantas, utilizacaoPedida);

        List<Tarefa> tarefas = new ArrayList<>();

        for (int i = 0; i < quantas; i++) {
            int periodo = PERIODOS[sorteio.ate(PERIODOS.length)];

            int custo = (int) StrictMath.round(fracoes[i] * periodo);

            if (custo < 1 || custo > periodo) {
                descartadosPorCustoZero++;

                return null;
            }

            tarefas.add(Tarefa.de("t" + i, periodo, custo));
        }

        Conjunto conjunto = new Conjunto("sorteado " + numero, tarefas);

        if (conjunto.hiperperiodo() > Conjunto.MAIOR_HIPERPERIODO) {
            descartadosPorHiperperiodo++;

            return null;
        }

        return conjunto;
    }

    /**
     * UUniFast: reparte a utilizacao entre n tarefas sem favorecer o equilibrio.
     *
     * <p>Usa {@link StrictMath#pow}, nao {@link Math#pow}, porque o lote inteiro precisa
     * sair igual nos tres sistemas operacionais do teste, e so o StrictMath promete o mesmo
     * bit em todos.
     */
    private double[] repartir(int quantas, double total) {
        double[] fracoes = new double[quantas];

        double sobra = total;

        for (int i = 1; i < quantas; i++) {
            double proxima = sobra * StrictMath.pow(sorteio.fracao(), 1.0 / (quantas - i));

            fracoes[i - 1] = sobra - proxima;
            sobra = proxima;
        }

        fracoes[quantas - 1] = sobra;

        return fracoes;
    }

    /** Tantos conjuntos utilizaveis, com a utilizacao pedida sorteada na faixa. */
    public List<Conjunto> quantosDerem(int quantos, double menorUtilizacao,
            double maiorUtilizacao) {

        List<Conjunto> saida = new ArrayList<>();

        int numero = 0;

        while (saida.size() < quantos) {
            double pedida = menorUtilizacao
                    + sorteio.fracao() * (maiorUtilizacao - menorUtilizacao);

            Conjunto conjunto = um(numero++, pedida);

            if (conjunto != null) saida.add(conjunto);
        }

        return saida;
    }

    public int descartadosPorHiperperiodo() {
        return descartadosPorHiperperiodo;
    }

    public int descartadosPorCustoZero() {
        return descartadosPorCustoZero;
    }
}
