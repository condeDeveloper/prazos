package br.com.conde.prazos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** As pecas, uma a uma, antes de qualquer medida. */
class PrazosTest {

    // ───────────────────── o sorteio ─────────────────────

    @Nested
    @DisplayName("o sorteio da casa")
    class OSorteio {

        private long estado;

        private int proximoSemDeslocamento() {
            estado = estado * 6364136223846793005L + 1442695040888963407L;

            return (int) estado;
        }

        private int ateSemDeslocamento(int quantos) {
            return Math.floorMod(proximoSemDeslocamento(), quantos);
        }

        private double fracaoSemDeslocamento() {
            return (proximoSemDeslocamento() & 0x7fffffff) / (double) 0x80000000L;
        }

        @Test
        @DisplayName("o sorteio da os mesmos numeros em qualquer maquina")
        void oSorteioEhFixo() {
            Sorteio sorteio = new Sorteio(1);

            assertThat(List.of(sorteio.proximo(), sorteio.proximo(), sorteio.proximo()))
                    .containsExactly(1817669548, -2107078989, -1510284903);
        }

        /**
         * OS BITS DE BAIXO SAO UM CONTADOR, E A LISTA DE PERIODOS TEM 16 ENTRADAS.
         *
         * <p>Dezesseis e uma potencia de dois, entao {@code ate(16)} sobre os bits de baixo
         * devolve os quatro ultimos bits do estado, que percorrem os dezesseis valores e
         * voltam ao comeco. Nao e um sorteio ruim: e um ciclo.
         */
        @Test
        @DisplayName("sem o deslocamento, a escolha do periodo e um ciclo de 16")
        void semODeslocamentoAEscolhaEhUmCiclo() {
            estado = Medidas.SEMENTE;

            List<Integer> primeiros = new ArrayList<>();

            for (int i = 0; i < 16; i++) primeiros.add(ateSemDeslocamento(16));

            assertThat(new LinkedHashSet<>(primeiros))
                    .as("os dezesseis valores, cada um exatamente uma vez")
                    .hasSize(16);

            List<Integer> seguintes = new ArrayList<>();

            for (int i = 0; i < 16; i++) seguintes.add(ateSemDeslocamento(16));

            assertThat(seguintes)
                    .as("e o ciclo recomeca igual")
                    .isEqualTo(primeiros);
        }

        /**
         * E O QUE ISSO FARIA COM A MEDIDA.
         *
         * <p>O conjunto de periodos e o que decide se o problema e facil ou dificil: periodos
         * que se dividem fazem a taxa monotonica aguentar utilizacao 1, e periodos primos
         * entre si a derrubam bem antes. Com os bits de baixo, duas mil rodadas do sorteador
         * produzem oitenta formas de periodo, repetidas; a tabela da utilizacao estaria
         * medindo oitenta conjuntos disfarcados de dois mil.
         */
        @Test
        @DisplayName("sem o deslocamento, 2.000 sorteios dao 80 formas de periodo")
        void semODeslocamentoOLoteSeRepete() {
            estado = Medidas.SEMENTE;

            Set<String> semDeslocamento = new LinkedHashSet<>();

            for (int i = 0; i < 2000; i++) semDeslocamento.add(periodosSemDeslocamento());

            assertThat(semDeslocamento).hasSize(80);

            Set<String> comDeslocamento = new LinkedHashSet<>();

            for (Conjunto conjunto : Medidas.lote()) {
                List<Integer> periodos = new ArrayList<>();

                for (Tarefa tarefa : conjunto.tarefas()) periodos.add(tarefa.periodo());

                comDeslocamento.add(periodos.toString());
            }

            assertThat(comDeslocamento)
                    .as("com os bits de cima, 1.775 formas distintas em 2.000 conjuntos")
                    .hasSize(1775);
        }

        /** O mesmo caminho de sorteios do Sorteador, com os bits de baixo. */
        private String periodosSemDeslocamento() {
            int quantas = Sorteador.MENOS_TAREFAS
                    + ateSemDeslocamento(Sorteador.MAIS_TAREFAS - Sorteador.MENOS_TAREFAS + 1);

            double sobra = 0.5 + fracaoSemDeslocamento() * 0.5;

            for (int i = 1; i < quantas; i++)
                sobra *= StrictMath.pow(fracaoSemDeslocamento(), 1.0 / (quantas - i));

            List<Integer> periodos = new ArrayList<>();

            for (int i = 0; i < quantas; i++)
                periodos.add(Sorteador.PERIODOS[
                        ateSemDeslocamento(Sorteador.PERIODOS.length)]);

            return periodos.toString();
        }
    }

    // ───────────────────── a tarefa e o conjunto ─────────────────────

    @Nested
    @DisplayName("a tarefa e o conjunto")
    class ATarefa {

        @Test
        @DisplayName("prazo maior que o periodo nao existe neste modelo")
        void prazoMaiorQueOPeriodo() {
            assertThatThrownBy(() -> new Tarefa("t", 10, 2, 20))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> Tarefa.de("t", 10, 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("a utilizacao e a soma das fracoes, e nada mais")
        void aUtilizacao() {
            assertThat(Conjuntos.classico().utilizacao()).isCloseTo(0.70, within(1e-12));
            assertThat(Conjuntos.harmonico().utilizacao()).isCloseTo(1.0, within(1e-12));
        }

        @Test
        @DisplayName("o hiperperiodo e o minimo multiplo comum dos periodos")
        void oHiperperiodo() {
            assertThat(Conjuntos.classico().hiperperiodo()).isEqualTo(100);
            assertThat(Conjuntos.harmonico().hiperperiodo()).isEqualTo(16);
            assertThat(Conjuntos.quaseCheio().hiperperiodo()).isEqualTo(7 * 11 * 13);
        }

        @Test
        @DisplayName("harmonico e quando um periodo divide o outro")
        void oHarmonico() {
            assertThat(Conjuntos.harmonico().harmonico()).isTrue();
            assertThat(Conjuntos.quaseCheio().harmonico()).isFalse();
            assertThat(Conjuntos.classico().harmonico())
                    .as("50 nao divide 20, entao nao e harmonico")
                    .isFalse();
        }
    }

    // ───────────────────── a simulacao ─────────────────────

    @Nested
    @DisplayName("a simulacao, que e o juiz")
    class ASimulacao {

        @Test
        @DisplayName("ela conta um trabalho por chegada, no hiperperiodo inteiro")
        void contaUmTrabalhoPorChegada() {
            Simulacao.Resultado resultado =
                    Simulacao.rodar(Conjuntos.classico(), new TaxaMonotonica());

            assertThat(resultado.trabalhos())
                    .as("100/20 + 100/50 + 100/100")
                    .isEqualTo(5 + 2 + 1);

            assertThat(resultado.de("controle").execucoes()).isEqualTo(5);
            assertThat(resultado.ocioso()).isEqualTo(30);
        }

        /**
         * O PRAZO QUE CAIA FORA DO LACO.
         *
         * <p>O ultimo trabalho de cada tarefa tem prazo exatamente no fim do hiperperiodo, e
         * o laco para um instante antes. Antes da correcao, esse trabalho sumia da conta: a
         * tarefa de periodo grande nunca rodava, nunca terminava, e a simulacao dizia que um
         * conjunto de utilizacao 1,04 cumpria todos os prazos.
         */
        @Test
        @DisplayName("o prazo que cai no fim do hiperperiodo e conferido")
        void oPrazoNoFimDoHiperperiodo() {
            Conjunto conjunto = new Conjunto("do erro", List.of(
                    Tarefa.de("longa", 24, 7),
                    Tarefa.de("curta", 4, 3)));

            assertThat(conjunto.utilizacao()).isGreaterThan(1);
            assertThat(conjunto.hiperperiodo()).isEqualTo(24);

            Simulacao.Resultado resultado = Simulacao.rodar(conjunto, new TaxaMonotonica());

            assertThat(resultado.escalonou())
                    .as("com utilizacao acima de 1 nao existe escalonamento")
                    .isFalse();

            assertThat(resultado.de("longa").perdeuOPrazo()).isEqualTo(1);
        }

        @Test
        @DisplayName("prazo perdido e contado uma vez por trabalho, nunca mais que isso")
        void oPrazoEhContadoUmaVez() {
            for (Conjunto conjunto : Medidas.loteSobrecarregado().subList(0, 50))
                for (Politica politica : Politica.todas()) {
                    Simulacao.Resultado resultado = Simulacao.rodar(conjunto, politica);

                    assertThat(resultado.perdeuOPrazo())
                            .as(conjunto.nome() + " com " + politica.nome())
                            .isLessThanOrEqualTo(resultado.trabalhos());
                }
        }

        @Test
        @DisplayName("a soma do tempo ocupado mais o ocioso e o hiperperiodo")
        void oTempoFecha() {
            Conjunto conjunto = Conjuntos.classico();

            Simulacao.Resultado resultado = Simulacao.rodar(conjunto, new PrazoMaisCedo());

            long ocupado = 0;

            for (Tarefa tarefa : conjunto.tarefas())
                ocupado += (long) tarefa.custo()
                        * (conjunto.hiperperiodo() / tarefa.periodo());

            assertThat(ocupado + resultado.ocioso()).isEqualTo(conjunto.hiperperiodo());
        }

        @Test
        @DisplayName("quem nunca e interrompido nao gera preempcao")
        void semPreempcaoNoPrimeiroQueChega() {
            for (Conjunto conjunto : Conjuntos.todos())
                assertThat(Simulacao.rodar(conjunto, new PrimeiroQueChega()).preempcoes())
                        .as("em " + conjunto.nome())
                        .isZero();
        }

        @Test
        @DisplayName("hiperperiodo grande demais e recusado em vez de travar a maquina")
        void oHiperperiodoTemTeto() {
            Conjunto grande = new Conjunto("enorme", List.of(
                    Tarefa.de("a", 999_983, 1),
                    Tarefa.de("b", 999_979, 1)));

            assertThatThrownBy(() -> Simulacao.rodar(grande, new TaxaMonotonica()))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ───────────────────── a analise de resposta ─────────────────────

    @Nested
    @DisplayName("a analise de tempo de resposta")
    class AResposta {

        @Test
        @DisplayName("no exemplo classico ela da 5, 15 e 50, na conta da mao")
        void oExemploClassico() {
            List<Resposta.Linha> linhas =
                    Resposta.calcular(Conjuntos.classico(), new TaxaMonotonica());

            assertThat(linhas).extracting(Resposta.Linha::tempoDeResposta)
                    .containsExactly(5, 15, 50);

            assertThat(linhas).allMatch(Resposta.Linha::cumpre);
        }

        @Test
        @DisplayName("e o relogio concorda com ela, tarefa por tarefa")
        void oRelogioConcorda() {
            Simulacao.Resultado simulado =
                    Simulacao.rodar(Conjuntos.classico(), new TaxaMonotonica());

            for (Resposta.Linha linha
                    : Resposta.calcular(Conjuntos.classico(), new TaxaMonotonica()))
                assertThat(simulado.de(linha.tarefa().nome()).piorResposta())
                        .as("em " + linha.tarefa())
                        .isEqualTo(linha.tempoDeResposta());
        }

        @Test
        @DisplayName("ela nao vale para prioridade dinamica, e diz isso")
        void soValeParaPrioridadeFixa() {
            assertThatThrownBy(
                    () -> Resposta.calcular(Conjuntos.classico(), new PrazoMaisCedo()))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("a prioridade da taxa monotonica e a ordem dos periodos")
        void aOrdemDasPrioridades() {
            assertThat(Resposta.porPrioridade(Conjuntos.classico(), new TaxaMonotonica()))
                    .extracting(Tarefa::periodo)
                    .containsExactly(20, 50, 100);

            assertThat(Resposta.porPrioridade(Conjuntos.prazoCurto(), new PrazoMonotonico()))
                    .extracting(Tarefa::nome)
                    .as("o alarme tem prazo 10 e periodo 50: o prazo manda")
                    .containsExactly("alarme", "rotina");
        }

        @Test
        @DisplayName("com utilizacao acima de 1 ela para, em vez de iterar para sempre")
        void elaParaComSobrecarga() {
            List<Resposta.Linha> linhas =
                    Resposta.calcular(Conjuntos.sobrecarregado(), new TaxaMonotonica());

            assertThat(linhas).anyMatch(linha -> !linha.cumpre());

            for (Resposta.Linha linha : linhas)
                assertThat(linha.iteracoes()).isLessThan(Resposta.ORCAMENTO);
        }
    }

    // ───────────────────── as politicas ─────────────────────

    @Nested
    @DisplayName("as politicas")
    class AsPoliticas {

        @Test
        @DisplayName("com periodos primos entre si, o prazo mais cedo aguenta e a taxa nao")
        void oPrazoMaisCedoAguentaMais() {
            Conjunto conjunto = Conjuntos.quaseCheio();

            assertThat(conjunto.utilizacao()).isLessThan(1);

            assertThat(Simulacao.rodar(conjunto, new TaxaMonotonica()).escalonou()).isFalse();
            assertThat(Simulacao.rodar(conjunto, new PrazoMaisCedo()).escalonou()).isTrue();
        }

        @Test
        @DisplayName("com prazo menor que o periodo, a taxa monotonica erra a ordem")
        void oPrazoMonotonicoAcertaAOrdem() {
            Conjunto conjunto = Conjuntos.prazoCurto();

            assertThat(conjunto.utilizacao())
                    .as("metade do processador, e mesmo assim a taxa monotonica perde")
                    .isLessThan(0.5);

            assertThat(Simulacao.rodar(conjunto, new TaxaMonotonica()).escalonou()).isFalse();
            assertThat(Simulacao.rodar(conjunto, new PrazoMonotonico()).escalonou()).isTrue();
        }

        @Test
        @DisplayName("num conjunto harmonico a taxa monotonica aguenta utilizacao 1")
        void oHarmonicoAguentaTudo() {
            Conjunto conjunto = Conjuntos.harmonico();

            assertThat(conjunto.utilizacao()).isCloseTo(1.0, within(1e-12));

            assertThat(Limites.passaNoLiuLayland(conjunto))
                    .as("e o limite classico reprova esse conjunto")
                    .isFalse();

            assertThat(Simulacao.rodar(conjunto, new TaxaMonotonica()).escalonou()).isTrue();
        }

        @Test
        @DisplayName("as duas politicas otimas escalonam exatamente os mesmos conjuntos")
        void asDuasOtimasEmpatam() {
            assertThat(Medidas.escalonamOsMesmos(new PrazoMaisCedo(), new MenorFolga()))
                    .isTrue();
        }
    }

    // ───────────────────── os limites ─────────────────────

    @Nested
    @DisplayName("os limites de utilizacao")
    class OsLimites {

        @Test
        @DisplayName("o limite de Liu e Layland cai de 1 para o logaritmo de 2")
        void oLimiteCai() {
            assertThat(Limites.liuLayland(1)).isCloseTo(1.0, within(1e-12));
            assertThat(Limites.liuLayland(2)).isCloseTo(0.8284271247, within(1e-9));
            assertThat(Limites.liuLayland(3)).isCloseTo(0.7797631497, within(1e-9));
            assertThat(Limites.liuLayland(1000)).isCloseTo(0.6933874626, within(1e-9));

            assertThat(Limites.limiteNoInfinito()).isCloseTo(0.6931471806, within(1e-9));
        }

        @Test
        @DisplayName("ele e decrescente, sempre")
        void oLimiteEhDecrescente() {
            for (int quantas = 1; quantas < 50; quantas++)
                assertThat(Limites.liuLayland(quantas + 1))
                        .as("de " + quantas + " para " + (quantas + 1))
                        .isLessThan(Limites.liuLayland(quantas));
        }

        /**
         * O LIMITE HIPERBOLICO APROVA TUDO O QUE O CLASSICO APROVA.
         *
         * <p>Isso nao e uma coincidencia do lote: e o teorema de Bini, Buttazzo e Buttazzo.
         * O teste varre os dois mil conjuntos sorteados procurando um contraexemplo, que
         * seria um conjunto aprovado pelo classico e reprovado pelo hiperbolico.
         */
        @Test
        @DisplayName("o hiperbolico nunca reprova o que o classico aprova")
        void oHiperbolicoDomina() {
            for (Conjunto conjunto : Medidas.lote()) {
                if (!Limites.passaNoLiuLayland(conjunto)) continue;

                assertThat(Limites.passaNoHiperbolico(conjunto))
                        .as("em " + conjunto)
                        .isTrue();
            }
        }
    }

    // ───────────────────── o sorteador ─────────────────────

    @Nested
    @DisplayName("o sorteador de conjuntos")
    class OSorteador {

        @Test
        @DisplayName("a mesma semente da o mesmo lote")
        void aMesmaSementeDaOMesmoLote() {
            List<Conjunto> um = new Sorteador(7).quantosDerem(30, 0.5, 1.0);
            List<Conjunto> outro = new Sorteador(7).quantosDerem(30, 0.5, 1.0);

            for (int i = 0; i < um.size(); i++)
                assertThat(um.get(i).tarefas()).isEqualTo(outro.get(i).tarefas());
        }

        @Test
        @DisplayName("todo conjunto sorteado cabe no teto do hiperperiodo")
        void todosCabemNoTeto() {
            for (Conjunto conjunto : Medidas.lote())
                assertThat(conjunto.hiperperiodo())
                        .isLessThanOrEqualTo(Conjunto.MAIOR_HIPERPERIODO);
        }

        @Test
        @DisplayName("todo custo e pelo menos 1, porque meio ciclo nao existe")
        void todoCustoEhInteiroPositivo() {
            for (Conjunto conjunto : Medidas.lote())
                for (Tarefa tarefa : conjunto.tarefas()) {
                    assertThat(tarefa.custo()).isPositive();
                    assertThat(tarefa.custo()).isLessThanOrEqualTo(tarefa.periodo());
                }
        }

        @Test
        @DisplayName("o numero de tarefas fica na faixa combinada")
        void aQuantidadeDeTarefas() {
            for (Conjunto conjunto : Medidas.lote())
                assertThat(conjunto.quantas())
                        .isBetween(Sorteador.MENOS_TAREFAS, Sorteador.MAIS_TAREFAS);
        }
    }
}
