package br.com.conde.prazos;

/**
 * UMA EXECUCAO DE UMA TAREFA: a tarefa volta, o trabalho acontece uma vez.
 *
 * <p>A tarefa diz "a cada 20 unidades, gaste 5". O trabalho e uma dessas vezes: chegou no
 * instante 40, ainda faltam 3 unidades, e o prazo dele acaba em 60.
 *
 * <p>A diferenca entre os dois e onde mora quase todo engano neste assunto. Prazo RELATIVO
 * pertence a tarefa e nunca muda; prazo ABSOLUTO pertence ao trabalho e e o que as politicas
 * dinamicas comparam.
 */
public final class Trabalho {

    private final Tarefa tarefa;
    private final int qual;
    private final int chegada;

    private int restante;
    private int terminou = -1;

    public Trabalho(Tarefa tarefa, int qual) {
        this.tarefa = tarefa;
        this.qual = qual;
        this.chegada = qual * tarefa.periodo();
        this.restante = tarefa.custo();
    }

    public Tarefa tarefa() {
        return tarefa;
    }

    /** A quantidade de vezes que a tarefa ja tinha chegado antes desta. */
    public int qual() {
        return qual;
    }

    public int chegada() {
        return chegada;
    }

    /** O instante em que este trabalho precisa estar pronto. */
    public int prazoAbsoluto() {
        return chegada + tarefa.prazo();
    }

    public int restante() {
        return restante;
    }

    public boolean pronto() {
        return restante == 0;
    }

    public void gastarUmaUnidade(int instante) {
        if (restante == 0) throw new IllegalStateException("trabalho ja terminado");

        restante--;

        if (restante == 0) terminou = instante + 1;
    }

    public int terminou() {
        return terminou;
    }

    /** Do momento em que chegou ao momento em que ficou pronto. */
    public int tempoDeResposta() {
        if (terminou < 0) throw new IllegalStateException("trabalho ainda nao terminou");

        return terminou - chegada;
    }

    /** Quanto tempo ainda sobra alem do que falta executar. */
    public int folga(int instante) {
        return prazoAbsoluto() - instante - restante;
    }

    public boolean perdeuOPrazo(int instante) {
        return instante >= prazoAbsoluto() && restante > 0;
    }

    @Override
    public String toString() {
        return tarefa.nome() + "#" + qual + "[chegou " + chegada + " prazo " + prazoAbsoluto()
                + " falta " + restante + "]";
    }
}
