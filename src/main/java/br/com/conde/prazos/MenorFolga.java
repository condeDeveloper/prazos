package br.com.conde.prazos;

/**
 * MENOR FOLGA PRIMEIRO: quem tem menos tempo sobrando roda antes.
 *
 * <p>Folga e o prazo menos o instante menos o que falta executar. Ela tambem e otima, pelo
 * mesmo argumento de troca do prazo mais cedo, e usa mais informacao: ela sabe quanto ainda
 * falta de cada trabalho.
 *
 * <p>Usar mais informacao nao sai de graca. A folga de quem NAO esta rodando diminui a cada
 * unidade de tempo, entao a ordem entre dois trabalhos pode mudar no meio da execucao de um
 * deles, e isso e exatamente uma preempcao a mais. A tabela do custo mede o tamanho disso.
 */
public final class MenorFolga implements Politica {

    @Override
    public String nome() {
        return "menor folga";
    }

    @Override
    public long chave(Trabalho trabalho, int instante) {
        return trabalho.folga(instante);
    }

    @Override
    public boolean prioridadeFixa() {
        return false;
    }
}
