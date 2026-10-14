package br.com.conde.prazos;

import java.util.List;

/**
 * UMA POLITICA: dados os trabalhos prontos, qual roda agora.
 *
 * <p>Todas as politicas daqui sao preemptivas e decidem a cada unidade de tempo. Isso e uma
 * escolha e tem preco, que aparece na tabela das preempcoes.
 *
 * <h2>Prioridade fixa contra prioridade dinamica</h2>
 *
 * <p>Na prioridade FIXA, a ordem entre duas tarefas nunca muda: taxa monotonica e prazo
 * monotonico decidem tudo antes de o sistema ligar. Isso e o que permite a analise de tempo
 * de resposta, que e o segundo juiz deste repositorio, e e o que a maioria dos sistemas de
 * verdade usa.
 *
 * <p>Na prioridade DINAMICA, a ordem muda conforme o tempo passa: o prazo mais cedo depende
 * de quando cada trabalho chegou. Ela aguenta mais carga e cobra o preco na tabela da
 * sobrecarga.
 */
public interface Politica {

    String nome();

    /**
     * Quanto menor o valor, mais cedo roda. Empate e resolvido pela ordem da tarefa no
     * conjunto, para que a simulacao seja a mesma em qualquer maquina.
     */
    long chave(Trabalho trabalho, int instante);

    /** Verdadeiro quando a ordem entre duas tarefas nunca muda com o tempo. */
    boolean prioridadeFixa();

    /** Todas as politicas medidas, na ordem das tabelas. */
    static List<Politica> todas() {
        return List.of(new TaxaMonotonica(), new PrazoMonotonico(), new PrazoMaisCedo(),
                new MenorFolga(), new PrimeiroQueChega());
    }

    /** As de prioridade fixa, que sao as que a analise de resposta sabe julgar. */
    static List<Politica> fixas() {
        return todas().stream().filter(Politica::prioridadeFixa).toList();
    }
}
