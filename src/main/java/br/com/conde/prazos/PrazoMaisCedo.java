package br.com.conde.prazos;

/**
 * PRAZO MAIS CEDO PRIMEIRO: quem vence antes roda antes.
 *
 * <p>A prioridade muda o tempo todo, porque o prazo comparado e o ABSOLUTO: a tarefa de
 * periodo grande passa na frente da de periodo pequeno quando o prazo dela esta chegando.
 *
 * <p>Ela e otima entre TODAS as politicas: se existe qualquer ordem que cumpre os prazos,
 * esta cumpre. Com prazo igual ao periodo, isso vira um criterio de uma linha so, utilizacao
 * ate 1, e e por isso que ela parece resolver o assunto.
 *
 * <p>A tabela da sobrecarga mostra o que esse "otima" nao promete.
 */
public final class PrazoMaisCedo implements Politica {

    @Override
    public String nome() {
        return "prazo mais cedo";
    }

    @Override
    public long chave(Trabalho trabalho, int instante) {
        return trabalho.prazoAbsoluto();
    }

    @Override
    public boolean prioridadeFixa() {
        return false;
    }
}
