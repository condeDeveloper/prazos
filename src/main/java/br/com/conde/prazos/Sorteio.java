package br.com.conde.prazos;

/**
 * O SORTEIO DA CASA, escrito aqui em vez de usar o da biblioteca.
 *
 * <p>E um gerador congruente linear com as constantes de Knuth. A razao de ser proprio e
 * medida: o teste roda com semente fixa em tres sistemas operacionais e precisa dar o mesmo
 * numero nos tres, e a implementacao da biblioteca nao e contrato entre versoes.
 *
 * <h2>O deslocamento de 32</h2>
 *
 * <p>O estado tem 64 bits e o que sai sao os 32 DE CIMA. Nos bits de baixo de um gerador
 * assim, os k ultimos bits repetem com periodo 2 elevado a k: o bit zero alterna a cada
 * passo, e {@code ate(16)} devolveria os dezesseis valores em ordem, para sempre.
 *
 * <p>Aqui isso cairia bem no lugar que mais importa. O periodo de cada tarefa e escolhido de
 * uma lista, e com os bits de baixo a escolha vira um contador: os conjuntos sorteados
 * saiem todos com a mesma forma de periodos, repetida. A tabela da utilizacao estaria
 * medindo dezesseis conjuntos disfarcados de dois mil, e conjunto de periodos parecido e
 * exatamente o que torna o problema facil ou dificil.
 */
public final class Sorteio {

    private long estado;

    public Sorteio(long semente) {
        this.estado = semente;
    }

    /** Um inteiro qualquer, dos 32 bits de cima do estado. */
    public int proximo() {
        estado = estado * 6364136223846793005L + 1442695040888963407L;

        return (int) (estado >>> 32);
    }

    /** Um valor de 0 ate quantos menos um. */
    public int ate(int quantos) {
        if (quantos < 1) throw new IllegalArgumentException("quantos precisa ser positivo");

        return Math.floorMod(proximo(), quantos);
    }

    /** Um numero entre 0 inclusive e 1 exclusive. */
    public double fracao() {
        return (proximo() & 0x7fffffff) / (double) 0x80000000L;
    }
}
