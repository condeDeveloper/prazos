package br.com.conde.prazos;

import java.util.Locale;

/**
 * O MEDIDOR: imprime as tabelas que o README mostra e que os testes conferem.
 *
 * <p>Toda tabela daqui tem um teste do lado que afirma os mesmos numeros. Texto nao quebra
 * quando deixa de ser verdade; teste quebra.
 */
public final class Medidor {

    private Medidor() {
    }

    public static void main(String[] args) {
        String comando = args.length > 0 ? args[0] : "tudo";

        if (comando.equals("correcao") || comando.equals("tudo")) correcao();
        if (comando.equals("limite") || comando.equals("tudo")) limite();
        if (comando.equals("utilizacao") || comando.equals("tudo")) utilizacao();
        if (comando.equals("sobrecarga") || comando.equals("tudo")) sobrecarga();
        if (comando.equals("custo") || comando.equals("tudo")) custo();
    }

    private static void correcao() {
        System.out.println();
        System.out.println("A SIMULACAO E A ANALISE DE RESPOSTA CONCORDAM?");
        System.out.println("Dois metodos que nao tem nada em comum: um roda o relogio, o"
                + " outro resolve um ponto fixo.");
        System.out.println("A comparacao nao e so o sim ou nao: e o pior tempo de resposta de"
                + " CADA tarefa.");
        System.out.println();

        System.out.printf("  %-20s %10s %14s %10s %16s %s%n",
                "politica", "conjuntos", "discordaram", "tarefas", "resposta errada",
                "problema");

        for (Medidas.Correcao linha : Medidas.correcao())
            System.out.printf("  %-20s %10d %14d %10d %16d %s%n",
                    linha.politica(), linha.conjuntos(), linha.discordaramNoSimNao(),
                    linha.tarefas(), linha.discordaramNaResposta(), linha.primeiroProblema());

        Medidas.CriterioDoPrazo criterio = Medidas.criterioDoPrazo();

        System.out.println();
        System.out.println("E O CRITERIO DO PRAZO MAIS CEDO, que e exato e nao so suficiente:");
        System.out.printf("  utilizacao ate 1 em %d conjuntos, simulacao escalonou %d,"
                + " discordaram em %d.%n", criterio.criterioAprovou(),
                criterio.simulacaoEscalonou(), criterio.discordaram());
        System.out.println();

        System.out.println("OS CONJUNTOS ESCRITOS A MAO");
        System.out.println();

        System.out.printf("  %-16s %6s %8s %10s", "conjunto", "U", "hiper", "limite");

        for (String nome : Medidas.nomes()) System.out.printf(" %8s", curto(nome));

        System.out.println();

        for (Conjunto conjunto : Conjuntos.todos()) {
            System.out.printf(Locale.ROOT, "  %-16s %6.3f %8d %10s", conjunto.nome(),
                    conjunto.utilizacao(), conjunto.hiperperiodo(),
                    Limites.passaNoLiuLayland(conjunto) ? "aprova" : "reprova");

            for (Politica politica : Politica.todas())
                System.out.printf(" %8s",
                        Simulacao.rodar(conjunto, politica).escalonou() ? "cumpre" : "PERDE");

            System.out.println();
        }

        System.out.println();
    }

    private static String curto(String nome) {
        return switch (nome) {
            case "taxa monotonica" -> "taxa";
            case "prazo monotonico" -> "prazo D";
            case "prazo mais cedo" -> "prazo abs";
            case "menor folga" -> "folga";
            case "primeiro que chega" -> "chegada";
            default -> nome;
        };
    }

    private static void limite() {
        Medidas.Limite medida = Medidas.limite();

        System.out.println();
        System.out.println("O LIMITE CLASSICO RECUSA O QUE A SIMULACAO APROVA");
        System.out.println("Limite de utilizacao e resposta em uma linha, e so para um lado:"
                + " quem passa escalona,");
        System.out.println("quem nao passa nao se sabe. A medida diz o tamanho desse nao se"
                + " sabe.");
        System.out.println();

        System.out.printf("  %-44s %8d%n", "conjuntos no lote", medida.conjuntos());
        System.out.printf("  %-44s %8d%n", "aprovados por Liu e Layland",
                medida.passaNoClassico());
        System.out.printf("  %-44s %8d%n", "aprovados pelo limite hiperbolico",
                medida.passaNoHiperbolico());
        System.out.printf("  %-44s %8d%n", "escalonados de verdade pela taxa monotonica",
                medida.escalonaDeVerdade());

        System.out.println();
        System.out.printf("  %-44s %8d%n", "aprovados pelo classico que perdem prazo",
                medida.aprovadoPeloClassicoQueFalha());
        System.out.printf("  %-44s %8d%n", "aprovados pelo hiperbolico que perdem prazo",
                medida.aprovadoPeloHiperbolicoQueFalha());
        System.out.printf("  %-44s %8d%n", "reprovados pelo classico que escalonam",
                medida.reprovadoPeloClassicoQueEscalona());
        System.out.printf("  %-44s %8d%n", "o hiperbolico aprova e o classico nao",
                medida.hiperbolicoAprovaEClassicoNao());

        System.out.println();
        System.out.printf(Locale.ROOT,
                "  Dos %d reprovados pelo limite classico, %d escalonam: %.1f%%.%n",
                medida.reprovadosPeloClassico(), medida.reprovadoPeloClassicoQueEscalona(),
                100 * medida.fracaoDesperdicada());
        System.out.printf(Locale.ROOT,
                "  O limite para 3 tarefas e %.4f, e tende a %.4f.%n",
                Limites.liuLayland(3), Limites.limiteNoInfinito());
        System.out.println();
    }

    private static void utilizacao() {
        System.out.println();
        System.out.println("QUANTA CARGA CADA POLITICA AGUENTA");
        System.out.println("Conjuntos sorteados por faixa de utilizacao de verdade, nao a"
                + " pedida ao sorteio.");
        System.out.println();

        System.out.printf("  %-14s %10s", "faixa", "conjuntos");

        for (String nome : Medidas.nomes()) System.out.printf(" %12s", curto(nome));

        System.out.println();

        for (Medidas.Faixa faixa : Medidas.faixas()) {
            System.out.printf(Locale.ROOT, "  %.1f a %.1f    %10d", faixa.de(), faixa.ate(),
                    faixa.conjuntos());

            for (String nome : Medidas.nomes()) System.out.printf(" %12d", faixa.de(nome));

            System.out.println();
        }

        System.out.println();
        System.out.printf("  Harmonicos no lote: %d, dos quais %d cabem no processador.%n",
                Medidas.harmonicosNoLote(), Medidas.harmonicosAteUm());
        System.out.printf("  Nesses, a taxa monotonica perde prazo em %d: num conjunto"
                + " harmonico o limite dela e 1.%n",
                Medidas.harmonicosQueATaxaMonotonicaPerde());
        System.out.println();
    }

    private static void sobrecarga() {
        System.out.println();
        System.out.println("O QUE CADA POLITICA FAZ QUANDO A CARGA PASSA DE UM");
        System.out.println(Medidas.LOTE_SOBRECARREGADO + " conjuntos sorteados com utilizacao"
                + " entre 1,05 e 1,4. Nenhuma politica escalona;");
        System.out.println("a pergunta e quem perde o prazo, e quantos.");
        System.out.println();

        System.out.printf("  %-20s %12s %12s %12s %16s %12s%n",
                "politica", "trabalhos", "perderam", "do total", "tarefas afetadas",
                "pior atraso");

        for (Medidas.Sobrecarga linha : Medidas.sobrecarga())
            System.out.printf(Locale.ROOT, "  %-20s %12d %12d %11.1f%% %15.1f%% %12d%n",
                    linha.politica(), linha.trabalhos(), linha.perdidos(),
                    100 * linha.fracaoPerdida(), 100 * linha.fracaoDasTarefas(),
                    linha.piorAtraso());

        System.out.println();
    }

    private static void custo() {
        System.out.println();
        System.out.println("QUANTAS PREEMPCOES CADA POLITICA CUSTA");
        System.out.println("Nos mesmos conjuntos para todas: os que a taxa monotonica"
                + " escalona.");
        System.out.println();

        System.out.printf("  %-20s %12s %12s %20s%n",
                "politica", "conjuntos", "trabalhos", "preempcoes por trabalho");

        for (Medidas.Custo linha : Medidas.custos())
            System.out.printf(Locale.ROOT, "  %-20s %12d %12d %20.4f%n",
                    linha.politica(), linha.conjuntos(), linha.trabalhos(),
                    linha.porTrabalho());

        System.out.println();
    }
}
