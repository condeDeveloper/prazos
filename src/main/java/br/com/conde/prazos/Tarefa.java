package br.com.conde.prazos;

/**
 * UMA TAREFA PERIODICA: ela volta, e sempre tem um prazo.
 *
 * <p>Este e o modelo de Liu e Layland, de 1973, e ele e proposital: a tarefa chega a cada
 * {@code periodo} unidades de tempo, gasta no maximo {@code custo} unidades de processador e
 * tem {@code prazo} unidades para terminar depois de chegar.
 *
 * <p>O que torna o assunto diferente de escalonar uma fila e que aqui nao existe "rapido o
 * bastante na media". Ou toda execucao de toda tarefa termina dentro do prazo, para sempre,
 * ou o conjunto nao serve. A media nao e consolo nenhum para o freio que atua tarde.
 *
 * <p>O custo e o PIOR caso de execucao, nao o tipico. Medir isso de verdade e um assunto
 * inteiro e fica de fora daqui: aqui o pior caso e dado.
 */
public record Tarefa(String nome, int periodo, int custo, int prazo) {

    public Tarefa {
        if (periodo < 1) throw new IllegalArgumentException("periodo precisa ser positivo");
        if (custo < 1) throw new IllegalArgumentException("custo precisa ser positivo");
        if (prazo < 1) throw new IllegalArgumentException("prazo precisa ser positivo");
        if (prazo > periodo) throw new IllegalArgumentException("prazo maior que o periodo");
    }

    /** Uma tarefa com prazo igual ao periodo, que e o caso classico. */
    public static Tarefa de(String nome, int periodo, int custo) {
        return new Tarefa(nome, periodo, custo, periodo);
    }

    /** A fracao de processador que esta tarefa consome, custo dividido por periodo. */
    public double utilizacao() {
        return custo / (double) periodo;
    }

    /** Quantas vezes esta tarefa chega dentro de uma janela. */
    public int chegadasAte(int instante) {
        return instante / periodo;
    }

    @Override
    public String toString() {
        return nome + "(T=" + periodo + " C=" + custo
                + (prazo == periodo ? "" : " D=" + prazo) + ")";
    }
}
