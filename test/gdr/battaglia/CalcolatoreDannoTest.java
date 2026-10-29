package gdr.battaglia;

import gdr.prova.CasualeDiProva;
import gdr.prova.MappeDiProva;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prove sulla formula del danno.
 *
 * I numeri delle specie usate qui sono inventati e tondi, cosi' il risultato
 * atteso si ricava a mano dalla formula invece di essere copiato da
 * un'esecuzione. Con attaccante di livello 1, attacco 20, difesa 10 e una mossa
 * da 50 di potenza, la base viene 6: la variazione fra l'85% e il 100% e
 * l'efficacia elementale fanno il resto.
 */
class CalcolatoreDannoTest {

    /** Il sorteggio che produce la variazione minima, l'85%. */
    private static final int VARIAZIONE_MINIMA = 0;

    /** Il sorteggio che produce la variazione massima, il 100%. */
    private static final int VARIAZIONE_MASSIMA = 15;

    /** Difensore d'erba: l'acqua le e' poco efficace contro. */
    private static final Specie ERBOSA = new Specie(
            "Erbosa", Elemento.ERBA,
            20, 20, 10, 10,
            List.of(new Mossa("Fogliolina", Elemento.ERBA, 50, 100),
                    new Mossa("Radicina", Elemento.ERBA, 80, 50)));

    /** Difensore di fuoco: l'elettro gli e' neutro contro. */
    private static final Specie INFUOCATA = new Specie(
            "Infuocata", Elemento.FUOCO,
            20, 20, 10, 10,
            List.of(new Mossa("Favillina", Elemento.FUOCO, 50, 100),
                    new Mossa("Vampatina", Elemento.FUOCO, 80, 50)));

    private static int danno(final Creatura attaccante, final Creatura difensore,
                             final Mossa mossa, final int sorteggio) {
        return CalcolatoreDanno.calcola(attaccante, difensore, mossa,
                CasualeDiProva.conInteri(sorteggio)).danno();
    }

    @Test
    @DisplayName("un colpo neutro fa fra il 85% e il 100% della base")
    void colpoNeutro() {
        final Creatura attaccante = new Creatura(MappeDiProva.VELOCE, 1);
        final Creatura difensore = new Creatura(INFUOCATA, 1);

        // base 6, efficacia 1: da 6*85/100 = 5 a 6*100/100 = 6.
        assertEquals(5, danno(attaccante, difensore,
                MappeDiProva.COLPO_SICURO, VARIAZIONE_MINIMA));
        assertEquals(6, danno(attaccante, difensore,
                MappeDiProva.COLPO_SICURO, VARIAZIONE_MASSIMA));
    }

    @Test
    @DisplayName("un colpo superefficace raddoppia prima della variazione")
    void colpoSuperefficace() {
        final Creatura attaccante = new Creatura(MappeDiProva.VELOCE, 1);
        final Creatura difensore = new Creatura(MappeDiProva.LENTA, 1);

        // base 6, efficacia 2 -> 12: da 12*85/100 = 10 a 12.
        assertEquals(10, danno(attaccante, difensore,
                MappeDiProva.COLPO_SICURO, VARIAZIONE_MINIMA));
        assertEquals(12, danno(attaccante, difensore,
                MappeDiProva.COLPO_SICURO, VARIAZIONE_MASSIMA));
    }

    @Test
    @DisplayName("un colpo poco efficace dimezza prima della variazione")
    void colpoPocoEfficace() {
        final Creatura attaccante = new Creatura(MappeDiProva.LENTA, 1);
        final Creatura difensore = new Creatura(ERBOSA, 1);
        final Mossa mossaDAcqua = MappeDiProva.LENTA.mosseApprese().get(0);

        // base 6, efficacia 0,5 -> 3: da 3*85/100 = 2 a 3.
        assertEquals(2, danno(attaccante, difensore, mossaDAcqua, VARIAZIONE_MINIMA));
        assertEquals(3, danno(attaccante, difensore, mossaDAcqua, VARIAZIONE_MASSIMA));
    }

    @Test
    @DisplayName("il risultato riporta l'efficacia che ha applicato")
    void efficaciaRiportata() {
        final Creatura attaccante = new Creatura(MappeDiProva.VELOCE, 1);

        assertEquals(2.0, CalcolatoreDanno.calcola(attaccante,
                new Creatura(MappeDiProva.LENTA, 1), MappeDiProva.COLPO_SICURO,
                CasualeDiProva.sempre()).efficacia(), 0.0);

        assertEquals(Elemento.NEUTRO, CalcolatoreDanno.calcola(attaccante,
                new Creatura(INFUOCATA, 1), MappeDiProva.COLPO_SICURO,
                CasualeDiProva.sempre()).efficacia(), 0.0);
    }

    @Test
    @DisplayName("un colpo che va a segno fa sempre almeno un punto")
    void almenoUnPunto() {
        // Attacco ridicolo contro una corazza assurda: senza il minimo a 1 il
        // danno sarebbe zero e la battaglia non finirebbe mai.
        final Specie fragile = new Specie("Fragile", Elemento.ACQUA,
                20, 1, 10, 10,
                List.of(new Mossa("Sputo", Elemento.ACQUA, 1, 100),
                        new Mossa("Sputone", Elemento.ACQUA, 2, 100)));
        final Specie corazzata = new Specie("Corazzata", Elemento.ERBA,
                20, 10, 1000, 10,
                List.of(new Mossa("Fogliolina", Elemento.ERBA, 50, 100),
                        new Mossa("Radicina", Elemento.ERBA, 80, 50)));

        final int inflitto = danno(new Creatura(fragile, 1), new Creatura(corazzata, 1),
                fragile.mosseApprese().get(0), VARIAZIONE_MINIMA);

        assertEquals(1, inflitto);
    }

    @Test
    @DisplayName("la variazione copre tutti e sedici i valori fra 85% e 100%")
    void sediciVariazioni() {
        final Creatura attaccante = new Creatura(MappeDiProva.VELOCE, 1);
        final Creatura difensore = new Creatura(MappeDiProva.LENTA, 1);

        int minimo = Integer.MAX_VALUE;
        int massimo = Integer.MIN_VALUE;
        for (int sorteggio = 0; sorteggio < 16; sorteggio++) {
            final int inflitto =
                    danno(attaccante, difensore, MappeDiProva.COLPO_SICURO, sorteggio);
            minimo = Math.min(minimo, inflitto);
            massimo = Math.max(massimo, inflitto);
        }

        assertEquals(10, minimo);
        assertEquals(12, massimo);
    }

    @Test
    @DisplayName("a parita' di sorteggio il danno e' sempre lo stesso")
    void calcoloDeterministico() {
        final Creatura attaccante = new Creatura(MappeDiProva.VELOCE, 3);
        final Creatura difensore = new Creatura(MappeDiProva.LENTA, 4);

        final int primo = danno(attaccante, difensore, MappeDiProva.COLPO_RISCHIOSO, 7);
        for (int ripetizione = 0; ripetizione < 20; ripetizione++) {
            assertEquals(primo,
                    danno(attaccante, difensore, MappeDiProva.COLPO_RISCHIOSO, 7));
        }
        assertTrue(primo > 0);
    }
}
