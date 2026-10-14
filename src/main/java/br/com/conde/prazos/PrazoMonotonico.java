package br.com.conde.prazos;

/**
 * PRAZO MONOTONICO: quem tem menos tempo para terminar tem mais prioridade.
 *
 * <p>Igual a taxa monotonica quando todo prazo e o periodo. A diferenca aparece quando uma
 * tarefa precisa terminar bem antes de voltar, que e o caso de quem responde a um evento e
 * depois espera muito.
 *
 * <p>Ela e otima entre as de prioridade fixa para prazo menor ou igual ao periodo, que e um
 * caso mais largo do que a taxa monotonica cobre.
 */
public final class PrazoMonotonico implements Politica {

    @Override
    public String nome() {
        return "prazo monotonico";
    }

    @Override
    public long chave(Trabalho trabalho, int instante) {
        return trabalho.tarefa().prazo();
    }

    @Override
    public boolean prioridadeFixa() {
        return true;
    }
}
