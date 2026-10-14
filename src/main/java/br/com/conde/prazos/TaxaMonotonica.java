package br.com.conde.prazos;

/**
 * TAXA MONOTONICA: quem volta mais vezes tem mais prioridade.
 *
 * <p>A prioridade e o periodo, e so. Quem tem periodo menor ganha, para sempre, sem olhar
 * prazo, sem olhar carga, sem olhar quanto ja esperou.
 *
 * <p>Liu e Layland provaram em 1973 que ela e OTIMA entre as de prioridade fixa, com prazo
 * igual ao periodo: se existe alguma atribuicao fixa de prioridades que escalona o conjunto,
 * a taxa monotonica escalona. Isso nao quer dizer que ela escalona tudo, e a tabela da
 * utilizacao mostra onde ela para.
 */
public final class TaxaMonotonica implements Politica {

    @Override
    public String nome() {
        return "taxa monotonica";
    }

    @Override
    public long chave(Trabalho trabalho, int instante) {
        return trabalho.tarefa().periodo();
    }

    @Override
    public boolean prioridadeFixa() {
        return true;
    }
}
