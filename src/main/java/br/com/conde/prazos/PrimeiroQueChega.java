package br.com.conde.prazos;

/**
 * PRIMEIRO QUE CHEGA: a politica que nao olha prazo nenhum.
 *
 * <p>Ela esta aqui para medir o tamanho do assunto. E a politica que qualquer fila usa, e a
 * unica informacao que ela usa e a ordem de chegada.
 *
 * <p>A pergunta que ela responde e honesta: quanto se perde por nao olhar prazo? A tabela da
 * utilizacao responde, e a resposta e grande ja em carga baixa.
 */
public final class PrimeiroQueChega implements Politica {

    @Override
    public String nome() {
        return "primeiro que chega";
    }

    @Override
    public long chave(Trabalho trabalho, int instante) {
        return trabalho.chegada();
    }

    @Override
    public boolean prioridadeFixa() {
        return false;
    }
}
