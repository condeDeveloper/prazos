package br.com.conde.prazos;

/**
 * OS LIMITES DE UTILIZACAO: resposta em uma linha, e so para um lado.
 *
 * <p>Um limite de utilizacao olha so a soma das fracoes de processador e responde sim ou
 * "nao sei". Ele e barato de um jeito que nenhuma analise e: nao olha periodo, nao olha
 * ordem, nao simula nada.
 *
 * <h2>SUFICIENTE nao e NECESSARIO</h2>
 *
 * <p>Esta e a confusao que a tabela do limite existe para desfazer. O limite de Liu e
 * Layland diz: se a utilizacao couber embaixo dele, a taxa monotonica escalona. Ele NAO diz
 * o contrario. Um conjunto que passa do limite pode ser perfeitamente escalonavel, e a
 * medida mostra quantos sao.
 *
 * <p>Com muitas tarefas o limite tende a ln 2, uns 69,3%, e e dai que sai a frase de que
 * "sistemas de tempo real so usam 70% do processador". A frase esta errada e a tabela diz
 * de quanto.
 *
 * <p>Todas as contas usam {@link StrictMath}, nao {@link Math}: o resultado precisa ser bit a
 * bit igual nos tres sistemas operacionais em que o teste roda, e so o StrictMath promete
 * isso.
 */
public final class Limites {

    private Limites() {}

    /**
     * O limite de Liu e Layland de 1973: n vezes a raiz n de 2, menos 1.
     *
     * <p>Com prazo igual ao periodo, qualquer conjunto de n tarefas com utilizacao ate aqui
     * e escalonado pela taxa monotonica, seja qual for o periodo de cada uma.
     */
    public static double liuLayland(int quantas) {
        return quantas * (StrictMath.pow(2.0, 1.0 / quantas) - 1);
    }

    /** O limite para o qual ele tende: o logaritmo natural de 2. */
    public static double limiteNoInfinito() {
        return StrictMath.log(2);
    }

    public static boolean passaNoLiuLayland(Conjunto conjunto) {
        return conjunto.utilizacao() <= liuLayland(conjunto.quantas()) + FOLGA;
    }

    /** Folga para comparacao de ponto flutuante, que nao e tolerancia de prazo. */
    public static final double FOLGA = 1e-12;

    /**
     * O limite hiperbolico de Bini, Buttazzo e Buttazzo, de 2003: o produto de (Ui + 1).
     *
     * <p>Ele aprova tudo o que o de Liu e Layland aprova e mais um tanto, e custa o mesmo.
     * A tabela do limite mede o tamanho desse "mais um tanto", que e maior do que eu
     * esperava.
     */
    public static double hiperbolico(Conjunto conjunto) {
        double produto = 1;

        for (Tarefa tarefa : conjunto.tarefas()) produto *= tarefa.utilizacao() + 1;

        return produto;
    }

    public static boolean passaNoHiperbolico(Conjunto conjunto) {
        return hiperbolico(conjunto) <= 2 + FOLGA;
    }

    /**
     * O criterio do prazo mais cedo: utilizacao ate 1, e e exato.
     *
     * <p>Com prazo igual ao periodo, este criterio nao e um limite suficiente: ele e
     * necessario E suficiente. Se a utilizacao passa de 1, nenhuma politica escalona, porque
     * nao existe processador para o trabalho pedido.
     */
    public static boolean passaNoCriterioDoPrazo(Conjunto conjunto) {
        return conjunto.utilizacao() <= 1 + FOLGA;
    }
}
