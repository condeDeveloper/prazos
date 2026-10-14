package br.com.conde.prazos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * TODO NUMERO DO README ESTA AQUI.
 *
 * <p>Texto nao quebra quando deixa de ser verdade. Teste quebra. Se alguma medida mudar,
 * este arquivo e a tabela do README caem juntos, e nenhum numero fica no texto sozinho.
 *
 * <p>As medidas ficam em campos estaticos porque o lote sao dois mil conjuntos simulados por
 * cinco politicas, e nada disso precisa acontecer de novo a cada teste.
 */
class MedidasTest {

    private static final List<Medidas.Correcao> CORRECAO = Medidas.correcao();
    private static final Medidas.CriterioDoPrazo CRITERIO = Medidas.criterioDoPrazo();
    private static final Medidas.Limite LIMITE = Medidas.limite();
    private static final List<Medidas.Faixa> FAIXAS = Medidas.faixas();
    private static final List<Medidas.Sobrecarga> SOBRECARGA = Medidas.sobrecarga();
    private static final List<Medidas.Custo> CUSTOS = Medidas.custos();

    private static Medidas.Correcao correcao(String politica) {
        return CORRECAO.stream().filter(linha -> linha.politica().equals(politica))
                .findFirst().orElseThrow();
    }

    private static Medidas.Faixa faixa(double de) {
        return FAIXAS.stream().filter(linha -> Math.abs(linha.de() - de) < 1e-9)
                .findFirst().orElseThrow();
    }

    private static Medidas.Sobrecarga sobrecarga(String politica) {
        return SOBRECARGA.stream().filter(linha -> linha.politica().equals(politica))
                .findFirst().orElseThrow();
    }

    private static Medidas.Custo custo(String politica) {
        return CUSTOS.stream().filter(linha -> linha.politica().equals(politica))
                .findFirst().orElseThrow();
    }

    // ───────────────────── 1. correcao ─────────────────────

    @Nested
    @DisplayName("tabela 1: a simulacao e a analise de resposta concordam")
    class ACorrecao {

        @Test
        @DisplayName("nenhuma discordancia no sim ou nao, em 2.000 conjuntos")
        void nenhumaDiscordancia() {
            for (Medidas.Correcao linha : CORRECAO) {
                assertThat(linha.conjuntos()).isEqualTo(2000);
                assertThat(linha.discordaramNoSimNao()).as(linha.politica()).isZero();
            }
        }

        @Test
        @DisplayName("e nenhuma no pior tempo de resposta, em 6.665 tarefas")
        void nenhumaDiscordanciaNaResposta() {
            for (Medidas.Correcao linha : CORRECAO) {
                assertThat(linha.tarefas()).isEqualTo(6665);
                assertThat(linha.discordaramNaResposta()).as(linha.politica()).isZero();
                assertThat(linha.primeiroProblema()).isEmpty();
            }
        }

        @Test
        @DisplayName("sao as duas politicas de prioridade fixa, e so elas")
        void saoAsDuasDePrioridadeFixa() {
            assertThat(CORRECAO).extracting(Medidas.Correcao::politica)
                    .containsExactly("taxa monotonica", "prazo monotonico");
        }

        /**
         * O CRITERIO DO PRAZO MAIS CEDO E EXATO, E A MEDIDA MOSTRA ISSO NOS DOIS SENTIDOS.
         *
         * <p>Utilizacao ate 1 aprova 1.979 conjuntos e a simulacao escalona exatamente
         * 1.979. Nao e um limite conservador como o de Liu e Layland: e necessario e
         * suficiente, e as duas contagens batem na casa.
         */
        @Test
        @DisplayName("o criterio do prazo mais cedo acerta os 2.000, nos dois sentidos")
        void oCriterioDoPrazoEhExato() {
            assertThat(CRITERIO.conjuntos()).isEqualTo(2000);
            assertThat(CRITERIO.criterioAprovou()).isEqualTo(1979);
            assertThat(CRITERIO.simulacaoEscalonou()).isEqualTo(1979);
            assertThat(CRITERIO.discordaram()).isZero();
        }

        @Test
        @DisplayName("os conjuntos escritos a mao separam as politicas como o texto diz")
        void osConjuntosEscritosAMao() {
            assertThat(Simulacao.rodar(Conjuntos.quaseCheio(), new TaxaMonotonica())
                    .escalonou()).isFalse();

            assertThat(Simulacao.rodar(Conjuntos.quaseCheio(), new PrazoMaisCedo())
                    .escalonou()).isTrue();

            assertThat(Simulacao.rodar(Conjuntos.prazoCurto(), new TaxaMonotonica())
                    .escalonou()).isFalse();

            assertThat(Simulacao.rodar(Conjuntos.prazoCurto(), new PrazoMonotonico())
                    .escalonou()).isTrue();

            assertThat(Simulacao.rodar(Conjuntos.classico(), new PrimeiroQueChega())
                    .escalonou())
                    .as("com 70% de carga, quem nao olha prazo ja perde")
                    .isFalse();
        }
    }

    // ───────────────────── 2. o limite ─────────────────────

    @Nested
    @DisplayName("tabela 2: o limite classico recusa o que a simulacao aprova")
    class OLimite {

        @Test
        @DisplayName("1.023 passam no limite classico e 1.735 escalonam de verdade")
        void oTamanhoDaDiferenca() {
            assertThat(LIMITE.conjuntos()).isEqualTo(2000);
            assertThat(LIMITE.passaNoClassico()).isEqualTo(1023);
            assertThat(LIMITE.passaNoHiperbolico()).isEqualTo(1172);
            assertThat(LIMITE.escalonaDeVerdade()).isEqualTo(1735);
        }

        /**
         * SUFICIENTE, E SO ISSO.
         *
         * <p>Nenhum conjunto aprovado pelo limite perde prazo: o teorema vale nos 1.023. O
         * que ele nao promete e o outro lado, e e la que estao 712 conjuntos.
         */
        @Test
        @DisplayName("nenhum aprovado pelo limite perde prazo, nos dois limites")
        void oLimiteNuncaAprovaErrado() {
            assertThat(LIMITE.aprovadoPeloClassicoQueFalha()).isZero();
            assertThat(LIMITE.aprovadoPeloHiperbolicoQueFalha()).isZero();
        }

        @Test
        @DisplayName("mas 712 dos 977 reprovados escalonam: 72,9% do que ele recusa")
        void oQueOLimiteJogaFora() {
            assertThat(LIMITE.reprovadosPeloClassico()).isEqualTo(977);
            assertThat(LIMITE.reprovadoPeloClassicoQueEscalona()).isEqualTo(712);
            assertThat(LIMITE.fracaoDesperdicada()).isCloseTo(0.729, within(0.001));
        }

        @Test
        @DisplayName("o limite hiperbolico aprova 149 a mais, e todos escalonam")
        void oHiperbolicoAprovaMais() {
            assertThat(LIMITE.hiperbolicoAprovaEClassicoNao()).isEqualTo(149);

            assertThat(LIMITE.passaNoHiperbolico() - LIMITE.passaNoClassico())
                    .isEqualTo(LIMITE.hiperbolicoAprovaEClassicoNao());
        }

        @Test
        @DisplayName("os valores do limite: 0,7798 para 3 tarefas e 0,6931 no infinito")
        void osValoresDoLimite() {
            assertThat(Limites.liuLayland(3)).isCloseTo(0.7798, within(0.0001));
            assertThat(Limites.limiteNoInfinito()).isCloseTo(0.6931, within(0.0001));
        }
    }

    // ───────────────────── 3. utilizacao ─────────────────────

    @Nested
    @DisplayName("tabela 3: quanta carga cada politica aguenta")
    class AUtilizacao {

        @Test
        @DisplayName("ate 80% de carga, as quatro que olham prazo escalonam tudo")
        void ateOitentaPorCentoNinguemPerde() {
            for (double de : new double[] {0.5, 0.6, 0.7}) {
                Medidas.Faixa faixa = faixa(de);

                for (String politica : Medidas.nomes()) {
                    if (politica.equals("primeiro que chega")) continue;

                    assertThat(faixa.de(politica))
                            .as(politica + " na faixa " + de)
                            .isEqualTo(faixa.conjuntos());
                }
            }
        }

        @Test
        @DisplayName("na faixa de 90% a taxa monotonica cai para 196 de 416")
        void naFaixaDeNoventaATaxaCai() {
            Medidas.Faixa faixa = faixa(0.9);

            assertThat(faixa.conjuntos()).isEqualTo(416);
            assertThat(faixa.de("taxa monotonica")).isEqualTo(196);
            assertThat(faixa.de("prazo mais cedo")).isEqualTo(416);
            assertThat(faixa.de("primeiro que chega")).isEqualTo(52);
        }

        @Test
        @DisplayName("na faixa de 80% ela ja perde 24 conjuntos")
        void naFaixaDeOitentaElaJaPerde() {
            Medidas.Faixa faixa = faixa(0.8);

            assertThat(faixa.conjuntos()).isEqualTo(410);
            assertThat(faixa.de("taxa monotonica")).isEqualTo(386);
            assertThat(faixa.de("prazo mais cedo")).isEqualTo(410);
        }

        @Test
        @DisplayName("a taxa monotonica e o prazo monotonico empatam no lote inteiro")
        void asDuasFixasEmpatam() {
            for (Medidas.Faixa faixa : FAIXAS)
                assertThat(faixa.de("taxa monotonica"))
                        .as("faixa " + faixa.de())
                        .isEqualTo(faixa.de("prazo monotonico"));
        }

        @Test
        @DisplayName("quem nao olha prazo perde mesmo com meio processador livre")
        void quemNaoOlhaPrazoPerdeCedo() {
            Medidas.Faixa faixa = faixa(0.5);

            assertThat(faixa.de("primeiro que chega")).isEqualTo(166);
            assertThat(faixa.conjuntos()).isEqualTo(353);
        }

        /**
         * O CASO FACIL ESCONDIDO.
         *
         * <p>Num conjunto harmonico, o limite da taxa monotonica sobe para 1: os 187
         * harmonicos do lote que cabem no processador sao todos escalonados, e nenhum limite
         * de utilizacao classico enxerga isso.
         */
        @Test
        @DisplayName("nos 187 harmonicos que cabem, a taxa monotonica nao perde nenhum")
        void osHarmonicos() {
            assertThat(Medidas.harmonicosNoLote()).isEqualTo(193);
            assertThat(Medidas.harmonicosAteUm()).isEqualTo(187);
            assertThat(Medidas.harmonicosAcimaDeUm()).isEqualTo(6);
            assertThat(Medidas.harmonicosQueATaxaMonotonicaPerde()).isZero();
        }
    }

    // ───────────────────── 4. sobrecarga ─────────────────────

    @Nested
    @DisplayName("tabela 4: o que cada politica faz quando a carga passa de um")
    class ASobrecarga {

        @Test
        @DisplayName("sao 500 conjuntos e 82.767 trabalhos, iguais para todas")
        void oLoteSobrecarregado() {
            for (Medidas.Sobrecarga linha : SOBRECARGA) {
                assertThat(linha.conjuntos()).isEqualTo(500);
                assertThat(linha.trabalhos()).isEqualTo(82767);
            }
        }

        /**
         * A MEDIDA QUE DESFAZ A PALAVRA "OTIMA".
         *
         * <p>O prazo mais cedo e otimo quando da para cumprir tudo. Quando nao da, ele perde
         * 90,2% dos prazos contra 18,1% da taxa monotonica, porque persegue o trabalho que ja
         * vai atrasar e deixa o seguinte atrasar junto.
         */
        @Test
        @DisplayName("a taxa monotonica perde 18,1% e o prazo mais cedo perde 90,2%")
        void oEfeitoDomino() {
            assertThat(sobrecarga("taxa monotonica").perdidos()).isEqualTo(15000);
            assertThat(sobrecarga("taxa monotonica").fracaoPerdida())
                    .isCloseTo(0.181, within(0.001));

            assertThat(sobrecarga("prazo mais cedo").perdidos()).isEqualTo(74677);
            assertThat(sobrecarga("prazo mais cedo").fracaoPerdida())
                    .isCloseTo(0.902, within(0.001));

            assertThat(sobrecarga("menor folga").fracaoPerdida())
                    .isCloseTo(0.946, within(0.001));
        }

        @Test
        @DisplayName("a prioridade fixa concentra o estrago em 41% das tarefas")
        void aPrioridadeFixaConcentra() {
            assertThat(sobrecarga("taxa monotonica").tarefasAfetadas()).isEqualTo(833);
            assertThat(sobrecarga("taxa monotonica").fracaoDasTarefas())
                    .isCloseTo(0.410, within(0.001));

            assertThat(sobrecarga("prazo mais cedo").tarefasAfetadas()).isEqualTo(1948);
            assertThat(sobrecarga("prazo mais cedo").fracaoDasTarefas())
                    .isCloseTo(0.958, within(0.001));

            assertThat(sobrecarga("menor folga").fracaoDasTarefas())
                    .as("a menor folga atinge quase toda tarefa que existe")
                    .isGreaterThan(0.99);
        }

        /**
         * E O OUTRO LADO DA MESMA MOEDA.
         *
         * <p>A taxa monotonica perde menos prazos, e quem ela sacrifica fica muito pior: o
         * pior atraso dela e de 25.037 unidades contra 9.840 do prazo mais cedo. Ela protege
         * as tarefas de periodo curto e abandona a de periodo longo, que simplesmente nao
         * roda.
         */
        @Test
        @DisplayName("mas o pior atraso dela e quase tres vezes maior")
        void oPiorAtraso() {
            assertThat(sobrecarga("taxa monotonica").piorAtraso()).isEqualTo(25037);
            assertThat(sobrecarga("prazo mais cedo").piorAtraso()).isEqualTo(9840);

            assertThat(sobrecarga("taxa monotonica").piorAtraso())
                    .isGreaterThan(2 * sobrecarga("prazo mais cedo").piorAtraso());
        }
    }

    // ───────────────────── 5. custo ─────────────────────

    @Nested
    @DisplayName("tabela 5: quantas preempcoes cada politica custa")
    class OCusto {

        @Test
        @DisplayName("sao os mesmos 1.735 conjuntos e 266.792 trabalhos para todas")
        void aMesmaPopulacao() {
            for (Medidas.Custo linha : CUSTOS) {
                assertThat(linha.conjuntos()).isEqualTo(1735);
                assertThat(linha.trabalhos()).isEqualTo(266792);
            }
        }

        /**
         * A DIFERENCA QUE EU ESPERAVA GRANDE.
         *
         * <p>A frase pronta e que o prazo mais cedo preempta menos que a taxa monotonica. E
         * verdade: 0,2505 contra 0,2731 por trabalho. So que isso e 8%, e eu esperava um
         * fator, nao uma margem.
         */
        @Test
        @DisplayName("o prazo mais cedo preempta 8% menos que a taxa monotonica")
        void oPrazoMaisCedoPreemptaPoucoMenos() {
            assertThat(custo("taxa monotonica").porTrabalho())
                    .isCloseTo(0.2731, within(0.0001));

            assertThat(custo("prazo mais cedo").porTrabalho())
                    .isCloseTo(0.2505, within(0.0001));

            assertThat(1 - custo("prazo mais cedo").porTrabalho()
                    / custo("taxa monotonica").porTrabalho())
                    .isCloseTo(0.083, within(0.005));
        }

        @Test
        @DisplayName("a menor folga preempta 7 vezes mais, e escalona o mesmo")
        void aMenorFolgaCustaSete() {
            assertThat(custo("menor folga").porTrabalho())
                    .isCloseTo(1.9595, within(0.0001));

            assertThat(custo("menor folga").porTrabalho()
                    / custo("prazo mais cedo").porTrabalho())
                    .isCloseTo(7.8, within(0.1));

            assertThat(Medidas.escalonamOsMesmos(new MenorFolga(), new PrazoMaisCedo()))
                    .as("e nao escalona um conjunto a mais por isso")
                    .isTrue();
        }

        @Test
        @DisplayName("quem nao preempta nunca nao preempta nenhuma vez")
        void oPrimeiroQueChegaNaoPreempta() {
            assertThat(custo("primeiro que chega").preempcoes()).isZero();
        }
    }
}
