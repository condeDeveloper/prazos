package br.com.conde.prazos;

import java.util.ArrayList;
import java.util.List;

/**
 * AS MEDIDAS: tudo o que o repositorio afirma, calculado aqui e em nenhum outro lugar.
 *
 * <p>O Medidor imprime o que sai daqui e o teste confere o que sai daqui. Os dois leem a
 * MESMA funcao, entao nao existe a situacao de o texto dizer uma coisa e o teste conferir
 * outra: se um numero mudar, a tabela muda e o teste quebra junto.
 *
 * <p>O lote e simulado uma vez e guardado. Sao dois mil conjuntos vezes cinco politicas
 * vezes um hiperperiodo de tres mil unidades em media, e nada disso precisa acontecer tres
 * vezes.
 */
public final class Medidas {

    /** Quantos conjuntos sorteados entram no lote principal. */
    public static final int LOTE = 2000;

    /** Quantos conjuntos sobrecarregados entram na tabela da sobrecarga. */
    public static final int LOTE_SOBRECARREGADO = 500;

    /** A semente, fixa para o numero sair igual nos tres sistemas operacionais. */
    public static final long SEMENTE = 1;

    /** A semente do lote sobrecarregado, separada para um lote nao mexer no outro. */
    public static final long SEMENTE_DA_SOBRECARGA = 2;

    public static final double MENOR_UTILIZACAO = 0.5;
    public static final double MAIOR_UTILIZACAO = 1.0;

    private Medidas() {}

    /* ===================== o lote ===================== */

    private static List<Conjunto> lote;
    private static Sorteador sorteadorDoLote;

    public static List<Conjunto> lote() {
        if (lote != null) return lote;

        sorteadorDoLote = new Sorteador(SEMENTE);

        return lote = sorteadorDoLote.quantosDerem(LOTE, MENOR_UTILIZACAO, MAIOR_UTILIZACAO);
    }

    public static Sorteador sorteadorDoLote() {
        lote();

        return sorteadorDoLote;
    }

    /* ===================== 1. correcao ===================== */

    public record Correcao(String politica, int conjuntos, int discordaramNoSimNao,
            int tarefas, int discordaramNaResposta, String primeiroProblema) {}

    private static List<Correcao> correcao;

    /**
     * A SIMULACAO CONTRA A ANALISE DE RESPOSTA, nos dois sentidos.
     *
     * <p>Nao basta concordarem no sim ou nao: a analise calcula o PIOR tempo de resposta de
     * cada tarefa, e a simulacao mede o pior tempo que de fato aconteceu no hiperperiodo.
     * Com todas as tarefas chegando juntas no instante zero, que e o instante critico, os
     * dois numeros tem de ser iguais, tarefa por tarefa.
     *
     * <p>Comparar so o sim ou nao esconderia uma analise otimista que por sorte acerta a
     * conclusao, e a conclusao e o que menos custa acertar.
     */
    public static List<Correcao> correcao() {
        if (correcao != null) return correcao;

        List<Correcao> saida = new ArrayList<>();

        for (Politica politica : Politica.fixas()) {
            int discordaramNoSimNao = 0;
            int discordaramNaResposta = 0;
            int tarefas = 0;

            String primeiro = "";

            for (Conjunto conjunto : lote()) {
                Simulacao.Resultado simulado = Simulacao.rodar(conjunto, politica);

                List<Resposta.Linha> calculado = Resposta.calcular(conjunto, politica);

                boolean cumpreTudo = calculado.stream().allMatch(Resposta.Linha::cumpre);

                if (cumpreTudo != simulado.escalonou()) {
                    discordaramNoSimNao++;

                    if (primeiro.isEmpty())
                        primeiro = conjunto.nome() + ": formula diz " + cumpreTudo;
                }

                if (!simulado.escalonou()) continue;

                for (Resposta.Linha linha : calculado) {
                    tarefas++;

                    int medido = simulado.de(linha.tarefa().nome()).piorResposta();

                    if (medido == linha.tempoDeResposta()) continue;

                    discordaramNaResposta++;

                    if (primeiro.isEmpty())
                        primeiro = conjunto.nome() + " " + linha.tarefa().nome()
                                + ": formula " + linha.tempoDeResposta() + ", relogio "
                                + medido;
                }
            }

            saida.add(new Correcao(politica.nome(), lote().size(), discordaramNoSimNao,
                    tarefas, discordaramNaResposta, primeiro));
        }

        return correcao = List.copyOf(saida);
    }

    /** Quantos conjuntos do lote o criterio do prazo mais cedo acerta, que deve ser todos. */
    public record CriterioDoPrazo(int conjuntos, int criterioAprovou, int simulacaoEscalonou,
            int discordaram) {}

    private static CriterioDoPrazo criterioDoPrazo;

    public static CriterioDoPrazo criterioDoPrazo() {
        if (criterioDoPrazo != null) return criterioDoPrazo;

        int aprovou = 0;
        int escalonou = 0;
        int discordaram = 0;

        for (Conjunto conjunto : lote()) {
            boolean criterio = Limites.passaNoCriterioDoPrazo(conjunto);
            boolean simulou = Simulacao.rodar(conjunto, new PrazoMaisCedo()).escalonou();

            if (criterio) aprovou++;
            if (simulou) escalonou++;
            if (criterio != simulou) discordaram++;
        }

        return criterioDoPrazo =
                new CriterioDoPrazo(lote().size(), aprovou, escalonou, discordaram);
    }

    /* ===================== 2. o limite ===================== */

    public record Limite(int conjuntos, int passaNoClassico, int passaNoHiperbolico,
            int escalonaDeVerdade, int aprovadoPeloClassicoQueFalha,
            int reprovadoPeloClassicoQueEscalona, int hiperbolicoAprovaEClassicoNao,
            int aprovadoPeloHiperbolicoQueFalha) {

        public int reprovadosPeloClassico() {
            return conjuntos - passaNoClassico;
        }

        /** Que fracao dos reprovados pelo limite classico escalona mesmo assim. */
        public double fracaoDesperdicada() {
            return reprovadoPeloClassicoQueEscalona / (double) reprovadosPeloClassico();
        }
    }

    private static Limite limite;

    public static Limite limite() {
        if (limite != null) return limite;

        int passaNoClassico = 0;
        int passaNoHiperbolico = 0;
        int escalona = 0;
        int aprovadoQueFalha = 0;
        int reprovadoQueEscalona = 0;
        int hiperbolicoAMais = 0;
        int hiperbolicoQueFalha = 0;

        for (Conjunto conjunto : lote()) {
            boolean classico = Limites.passaNoLiuLayland(conjunto);
            boolean hiperbolico = Limites.passaNoHiperbolico(conjunto);
            boolean escalonou = Simulacao.rodar(conjunto, new TaxaMonotonica()).escalonou();

            if (classico) passaNoClassico++;
            if (hiperbolico) passaNoHiperbolico++;
            if (escalonou) escalona++;

            if (classico && !escalonou) aprovadoQueFalha++;
            if (!classico && escalonou) reprovadoQueEscalona++;
            if (hiperbolico && !classico) hiperbolicoAMais++;
            if (hiperbolico && !escalonou) hiperbolicoQueFalha++;
        }

        return limite = new Limite(lote().size(), passaNoClassico, passaNoHiperbolico,
                escalona, aprovadoQueFalha, reprovadoQueEscalona, hiperbolicoAMais,
                hiperbolicoQueFalha);
    }

    /* ===================== 3. utilizacao ===================== */

    public record Faixa(double de, double ate, int conjuntos, int[] escalonaram) {

        public int de(String politica) {
            return escalonaram[nomes().indexOf(politica)];
        }
    }

    public static List<String> nomes() {
        return Politica.todas().stream().map(Politica::nome).toList();
    }

    private static List<Faixa> faixas;

    public static List<Faixa> faixas() {
        if (faixas != null) return faixas;

        List<Politica> politicas = Politica.todas();

        List<Faixa> saida = new ArrayList<>();

        for (int passo = 5; passo < 10; passo++) {
            double de = passo / 10.0;
            double ate = (passo + 1) / 10.0;

            int[] escalonaram = new int[politicas.size()];
            int quantos = 0;

            for (Conjunto conjunto : lote()) {
                double utilizacao = conjunto.utilizacao();

                if (utilizacao < de || utilizacao >= ate) continue;

                quantos++;

                for (int i = 0; i < politicas.size(); i++)
                    if (Simulacao.rodar(conjunto, politicas.get(i)).escalonou())
                        escalonaram[i]++;
            }

            saida.add(new Faixa(de, ate, quantos, escalonaram));
        }

        return faixas = List.copyOf(saida);
    }

    /** Quantos conjuntos do lote sao harmonicos, que e o caso facil escondido. */
    public static int harmonicosNoLote() {
        return (int) lote().stream().filter(Conjunto::harmonico).count();
    }

    /** Os harmonicos que cabem no processador, que e onde o teorema vale. */
    public static int harmonicosAteUm() {
        return (int) lote().stream()
                .filter(Conjunto::harmonico)
                .filter(Limites::passaNoCriterioDoPrazo)
                .count();
    }

    /**
     * NUM CONJUNTO HARMONICO QUE CABE, A TAXA MONOTONICA NAO PERDE NENHUM PRAZO.
     *
     * <p>Com periodos multiplos uns dos outros o limite de utilizacao sobe para 1: a tarefa
     * de periodo maior so e interrompida por chegadas que cabem inteiras dentro do periodo
     * dela, e sobra nenhuma se perde.
     *
     * <p>A condicao "que cabe" nao e detalhe. Entre os harmonicos do lote ha alguns com
     * utilizacao acima de 1, porque o custo e arredondado para inteiro, e esses a taxa
     * monotonica perde como qualquer outra politica perderia. Medir sem separar os dois
     * casos daria a impressao de que o teorema falha.
     */
    public static int harmonicosQueATaxaMonotonicaPerde() {
        return (int) lote().stream()
                .filter(Conjunto::harmonico)
                .filter(Limites::passaNoCriterioDoPrazo)
                .filter(conjunto -> !Simulacao.rodar(conjunto, new TaxaMonotonica()).escalonou())
                .count();
    }

    /** Harmonicos acima de 1, que nao cabem e por isso nao dizem nada sobre o teorema. */
    public static int harmonicosAcimaDeUm() {
        return harmonicosNoLote() - harmonicosAteUm();
    }

    /* ===================== 4. sobrecarga ===================== */

    public record Sobrecarga(String politica, int conjuntos, int trabalhos, int perdidos,
            int tarefas, int tarefasAfetadas, int piorAtraso) {

        public double fracaoPerdida() {
            return perdidos / (double) trabalhos;
        }

        public double fracaoDasTarefas() {
            return tarefasAfetadas / (double) tarefas;
        }
    }

    private static List<Sobrecarga> sobrecarga;
    private static List<Conjunto> loteSobrecarregado;

    public static List<Conjunto> loteSobrecarregado() {
        if (loteSobrecarregado != null) return loteSobrecarregado;

        return loteSobrecarregado = new Sorteador(SEMENTE_DA_SOBRECARGA)
                .quantosDerem(LOTE_SOBRECARREGADO, 1.05, 1.4);
    }

    /**
     * O QUE CADA POLITICA FAZ QUANDO NAO CABE.
     *
     * <p>Acima de utilizacao 1 nenhuma politica escalona, e dizer isso nao e medir nada. A
     * pergunta que importa e outra: QUEM perde o prazo, e quantos.
     *
     * <p>E aqui a palavra "otima" muda de significado. O prazo mais cedo e otimo quando da
     * para cumprir tudo; quando nao da, ele persegue o trabalho que ja vai perder, deixa o
     * seguinte atrasar, persegue esse, e por ai vai.
     */
    public static List<Sobrecarga> sobrecarga() {
        if (sobrecarga != null) return sobrecarga;

        List<Sobrecarga> saida = new ArrayList<>();

        for (Politica politica : Politica.todas()) {
            int trabalhos = 0;
            int perdidos = 0;
            int tarefas = 0;
            int afetadas = 0;
            int piorAtraso = 0;

            for (Conjunto conjunto : loteSobrecarregado()) {
                Simulacao.Resultado resultado = Simulacao.rodar(conjunto, politica);

                trabalhos += resultado.trabalhos();
                perdidos += resultado.perdeuOPrazo();
                tarefas += conjunto.quantas();

                for (Simulacao.PorTarefa linha : resultado.porTarefa()) {
                    if (linha.perdeuOPrazo() > 0) afetadas++;

                    piorAtraso = Math.max(piorAtraso, linha.piorAtraso());
                }
            }

            saida.add(new Sobrecarga(politica.nome(), loteSobrecarregado().size(), trabalhos,
                    perdidos, tarefas, afetadas, piorAtraso));
        }

        return sobrecarga = List.copyOf(saida);
    }

    /* ===================== 5. custo ===================== */

    public record Custo(String politica, int conjuntos, int trabalhos, int preempcoes) {

        public double porTrabalho() {
            return preempcoes / (double) trabalhos;
        }
    }

    private static List<Custo> custos;

    /**
     * QUANTAS PREEMPCOES CADA POLITICA COBRA.
     *
     * <p>So entram os conjuntos que a taxa monotonica escalona, e isso e proposital: comparar
     * a media de cada politica sobre o conjunto que ELA escalona mediria populacoes
     * diferentes, e a politica que aguenta mais ficaria com os casos mais apertados.
     */
    public static List<Custo> custos() {
        if (custos != null) return custos;

        List<Conjunto> comuns = lote().stream()
                .filter(conjunto -> Simulacao.rodar(conjunto, new TaxaMonotonica()).escalonou())
                .toList();

        List<Custo> saida = new ArrayList<>();

        for (Politica politica : Politica.todas()) {
            int trabalhos = 0;
            int preempcoes = 0;

            for (Conjunto conjunto : comuns) {
                Simulacao.Resultado resultado = Simulacao.rodar(conjunto, politica);

                trabalhos += resultado.trabalhos();
                preempcoes += resultado.preempcoes();
            }

            saida.add(new Custo(politica.nome(), comuns.size(), trabalhos, preempcoes));
        }

        return custos = List.copyOf(saida);
    }

    /** As politicas que escalonam exatamente os mesmos conjuntos do lote. */
    public static boolean escalonamOsMesmos(Politica uma, Politica outra) {
        for (Conjunto conjunto : lote())
            if (Simulacao.rodar(conjunto, uma).escalonou()
                    != Simulacao.rodar(conjunto, outra).escalonou()) return false;

        return true;
    }
}
