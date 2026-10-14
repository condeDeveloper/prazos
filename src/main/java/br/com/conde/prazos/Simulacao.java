package br.com.conde.prazos;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * O JUIZ: rodar o relogio e ver o que acontece.
 *
 * <p>Ela e o juiz certo e nao so o conveniente porque nao e uma analise. Nao ha formula, nao
 * ha limite, nao ha teorema: ha o tempo passando de uma em uma unidade, os trabalhos
 * chegando quando a tarefa manda, a politica escolhendo um, e a conta de quem passou do
 * prazo.
 *
 * <p>Nenhuma formula substitui isso, porque toda formula vale sob hipotese. O limite de Liu
 * e Layland so vale com prazo igual ao periodo; a analise de tempo de resposta so vale com
 * prioridade fixa; o criterio do prazo mais cedo so vale sem sobrecarga. A simulacao nao
 * supoe nada.
 *
 * <h2>Por que um hiperperiodo basta</h2>
 *
 * <p>Porque todas as tarefas chegam juntas no instante zero e voltam a chegar juntas no
 * minimo multiplo comum dos periodos. O estado do sistema no fim do hiperperiodo e igual ao
 * do comeco, entao o segundo hiperperiodo repete o primeiro, e assim para sempre.
 *
 * <p>Comecar com todas chegando juntas e o INSTANTE CRITICO, o pior caso. Qualquer outro
 * comeco mede uma situacao mais facil.
 *
 * <h2>O que fazer com quem ja perdeu o prazo</h2>
 *
 * <p>Este simulador NAO descarta o trabalho atrasado: ele continua executando ate terminar.
 * Essa escolha e o que deixa o efeito domino aparecer na tabela da sobrecarga, e ela e a
 * escolha honesta, porque descartar e uma politica de emergencia que nem toda implementacao
 * tem.
 */
public final class Simulacao {

    /** O que aconteceu com uma tarefa ao longo do hiperperiodo. */
    public record PorTarefa(Tarefa tarefa, int execucoes, int perdeuOPrazo, int piorResposta,
            int piorAtraso) {

        public boolean cumpriuTudo() {
            return perdeuOPrazo == 0;
        }
    }

    /** O que aconteceu no conjunto inteiro. */
    public record Resultado(Conjunto conjunto, String politica, long duracao, int trabalhos,
            int perdeuOPrazo, int preempcoes, long ocioso, List<PorTarefa> porTarefa) {

        public boolean escalonou() {
            return perdeuOPrazo == 0;
        }

        public PorTarefa de(String nome) {
            return porTarefa.stream()
                    .filter(linha -> linha.tarefa().nome().equals(nome))
                    .findFirst().orElseThrow();
        }

        /** A primeira tarefa que perdeu prazo, na ordem do conjunto. */
        public String primeiraQuePerdeu() {
            return porTarefa.stream()
                    .filter(linha -> linha.perdeuOPrazo() > 0)
                    .map(linha -> linha.tarefa().nome())
                    .findFirst().orElse("");
        }
    }

    private Simulacao() {}

    /** Roda um hiperperiodo inteiro, que responde sobre o tempo infinito. */
    public static Resultado rodar(Conjunto conjunto, Politica politica) {
        return rodar(conjunto, politica, conjunto.hiperperiodo());
    }

    public static Resultado rodar(Conjunto conjunto, Politica politica, long duracao) {
        if (duracao > Conjunto.MAIOR_HIPERPERIODO)
            throw new IllegalArgumentException("hiperperiodo grande demais: " + duracao);

        List<Tarefa> tarefas = conjunto.tarefas();

        List<Trabalho> prontos = new ArrayList<>();

        Map<Tarefa, Integer> ordemDaTarefa = new LinkedHashMap<>();

        for (int i = 0; i < tarefas.size(); i++) ordemDaTarefa.put(tarefas.get(i), i);

        Map<Tarefa, int[]> contas = new LinkedHashMap<>();

        for (Tarefa tarefa : tarefas) contas.put(tarefa, new int[] {0, 0, 0, 0});

        int[] proximaChegada = new int[tarefas.size()];

        int preempcoes = 0;
        long ocioso = 0;
        int trabalhos = 0;
        int perdeuOPrazo = 0;

        Trabalho rodandoAntes = null;

        for (int instante = 0; instante < duracao; instante++) {
            for (int i = 0; i < tarefas.size(); i++)
                if (instante == proximaChegada[i]) {
                    prontos.add(new Trabalho(tarefas.get(i), instante / tarefas.get(i).periodo()));

                    proximaChegada[i] += tarefas.get(i).periodo();
                    trabalhos++;
                }

            /* Quem passou do prazo e contado UMA vez, no instante em que passou. */
            for (Trabalho trabalho : prontos)
                if (instante == trabalho.prazoAbsoluto()) {
                    int[] conta = contas.get(trabalho.tarefa());

                    conta[1]++;
                    perdeuOPrazo++;
                }

            Trabalho escolhido = escolher(prontos, politica, instante, ordemDaTarefa);

            if (escolhido == null) {
                ocioso++;
                rodandoAntes = null;
                continue;
            }

            if (rodandoAntes != null && rodandoAntes != escolhido && !rodandoAntes.pronto())
                preempcoes++;

            escolhido.gastarUmaUnidade(instante);

            rodandoAntes = escolhido;

            if (!escolhido.pronto()) continue;

            int[] conta = contas.get(escolhido.tarefa());

            conta[0]++;
            conta[2] = Math.max(conta[2], escolhido.tempoDeResposta());
            conta[3] = Math.max(conta[3],
                    escolhido.terminou() - escolhido.prazoAbsoluto());

            prontos.remove(escolhido);
        }

        /* O PRAZO QUE CAIA FORA DO LACO.
         *
         * O ultimo trabalho de cada tarefa tem prazo exatamente no fim do hiperperiodo, e o
         * laco para um instante antes disso. Sem esta conferencia final, o trabalho que nao
         * terminou simplesmente sumia da conta, e a simulacao aprovava conjunto com
         * utilizacao acima de 1: a tarefa de periodo grande nunca rodava, nunca terminava e
         * nunca era acusada.
         *
         * E a conferencia e so no prazo que cai EXATAMENTE no fim: os prazos anteriores ja
         * passaram pelo laco e ja foram contados. Contar de novo aqui dava mais prazos
         * perdidos do que trabalhos existentes, que foi como o erro apareceu. */
        for (Trabalho trabalho : prontos)
            if (trabalho.prazoAbsoluto() == duracao && !trabalho.pronto()) {
                contas.get(trabalho.tarefa())[1]++;
                perdeuOPrazo++;
            }

        List<PorTarefa> porTarefa = new ArrayList<>();

        for (Tarefa tarefa : tarefas) {
            int[] conta = contas.get(tarefa);

            porTarefa.add(new PorTarefa(tarefa, conta[0], conta[1], conta[2],
                    Math.max(conta[3], 0)));
        }

        return new Resultado(conjunto, politica.nome(), duracao, trabalhos, perdeuOPrazo,
                preempcoes, ocioso, List.copyOf(porTarefa));
    }

    /**
     * O trabalho de menor chave, com empate resolvido pela ordem no conjunto.
     *
     * <p>O desempate precisa ser deterministico, e nao por gosto: dois trabalhos com o mesmo
     * prazo acontecem o tempo todo, e um desempate que dependesse da ordem em que a lista
     * ficou faria a medida mudar de maquina para maquina.
     */
    private static Trabalho escolher(List<Trabalho> prontos, Politica politica, int instante,
            Map<Tarefa, Integer> ordemDaTarefa) {

        Trabalho melhor = null;

        long melhorChave = Long.MAX_VALUE;
        int melhorOrdem = Integer.MAX_VALUE;
        int melhorQual = Integer.MAX_VALUE;

        for (Trabalho trabalho : prontos) {
            if (trabalho.pronto()) continue;

            long chave = politica.chave(trabalho, instante);

            if (chave > melhorChave) continue;

            int ordem = ordemDaTarefa.get(trabalho.tarefa());

            if (chave == melhorChave) {
                if (ordem > melhorOrdem) continue;

                if (ordem == melhorOrdem && trabalho.qual() >= melhorQual) continue;
            }

            melhor = trabalho;
            melhorChave = chave;
            melhorOrdem = ordem;
            melhorQual = trabalho.qual();
        }

        return melhor;
    }
}
