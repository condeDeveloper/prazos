package br.com.conde.prazos;

import java.util.List;

/**
 * OS CONJUNTOS ESCRITOS A MAO, cada um por um motivo.
 *
 * <p>Nenhum deles esta aqui para ser tipico. Cada um existe porque separa duas coisas que o
 * anterior nao separava, e o que eles medem e sempre a mesma pergunta: o que o limite diz, o
 * que a formula diz, e o que acontece quando o relogio roda.
 */
public final class Conjuntos {

    private Conjuntos() {}

    /** O exemplo do artigo de 1973, com utilizacao folgada. */
    public static Conjunto classico() {
        return new Conjunto("classico", List.of(
                Tarefa.de("controle", 20, 5),
                Tarefa.de("leitura", 50, 10),
                Tarefa.de("registro", 100, 25)));
    }

    /**
     * Periodos multiplos uns dos outros, com utilizacao exatamente 1.
     *
     * <p>Ele passa LONGE do limite de Liu e Layland e mesmo assim a taxa monotonica cumpre
     * todo prazo, porque num conjunto harmonico a tarefa de periodo grande nunca e
     * interrompida no meio por uma chegada que nao caiba.
     */
    public static Conjunto harmonico() {
        return new Conjunto("harmonico", List.of(
                Tarefa.de("rapida", 4, 1),
                Tarefa.de("media", 8, 2),
                Tarefa.de("lenta", 16, 8)));
    }

    /**
     * Utilizacao bem acima do limite de Liu e Layland, e a taxa monotonica cumpre tudo.
     *
     * <p>Ele e o contraexemplo da frase "nao passa de 69% do processador": 95,24% de
     * utilizacao, reprovado pelo limite classico e pelo hiperbolico, e nenhum prazo perdido
     * no hiperperiodo inteiro. O limite nao erra, ele so responde "nao sei" dizendo "nao".
     */
    public static Conjunto noLimite() {
        return new Conjunto("no limite", List.of(
                Tarefa.de("a", 10, 4),
                Tarefa.de("b", 15, 4),
                Tarefa.de("c", 35, 10)));
    }

    /**
     * Utilizacao alta com periodos sem divisor comum: o prazo mais cedo aguenta e a taxa
     * monotonica nao.
     *
     * <p>Ele e o par do conjunto anterior. Os dois passam do limite, e um escalona e o outro
     * nao: e exatamente por nao conseguir separar estes dois que o limite precisa ser
     * conservador.
     */
    public static Conjunto quaseCheio() {
        return new Conjunto("quase cheio", List.of(
                Tarefa.de("a", 7, 2),
                Tarefa.de("b", 11, 4),
                Tarefa.de("c", 13, 4)));
    }

    /**
     * Prazo menor que o periodo, que e o caso em que a taxa monotonica erra a ordem.
     *
     * <p>A tarefa que volta devagar precisa responder rapido. A taxa monotonica olha o
     * periodo e da prioridade a errada; o prazo monotonico olha o prazo e acerta.
     */
    public static Conjunto prazoCurto() {
        return new Conjunto("prazo curto", List.of(
                new Tarefa("alarme", 50, 8, 10),
                new Tarefa("rotina", 12, 4, 12)));
    }

    /** Utilizacao acima de 1: nenhuma politica escalona, e o que muda e QUEM perde. */
    public static Conjunto sobrecarregado() {
        return new Conjunto("sobrecarregado", List.of(
                Tarefa.de("a", 6, 3),
                Tarefa.de("b", 10, 5),
                Tarefa.de("c", 15, 6)));
    }

    /** Duas tarefas e nada mais, para a conta caber na cabeca. */
    public static Conjunto duasTarefas() {
        return new Conjunto("duas tarefas", List.of(
                Tarefa.de("curta", 5, 2),
                Tarefa.de("longa", 7, 2)));
    }

    /** Os sete, na ordem das tabelas. */
    public static List<Conjunto> todos() {
        return List.of(duasTarefas(), classico(), harmonico(), noLimite(), quaseCheio(),
                prazoCurto(), sobrecarregado());
    }

    /** Os que tem prazo igual ao periodo, que e onde os limites valem. */
    public static List<Conjunto> comPrazoNoPeriodo() {
        return todos().stream().filter(Conjunto::prazoNoPeriodo).toList();
    }
}
