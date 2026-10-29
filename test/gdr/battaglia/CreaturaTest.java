package gdr.battaglia;

import gdr.dati.Bestiario;
import gdr.prova.MappeDiProva;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prove sulla creatura viva: la formula di crescita e la gestione dei punti
 * salute.
 *
 * La formula e' volutamente semplice — valore = base + base * (livello - 1) / 5,
 * a divisione intera — proprio perche' si possa controllare a mente, ed e' qui
 * che la si fissa.
 */
class CreaturaTest {

    /** Una specie dai numeri tondi: dieci di tutto, cosi' i conti si fanno a mente. */
    private static final Specie TONDA = new Specie(
            "Tonda", Elemento.FUOCO,
            10, 10, 10, 10,
            List.of(MappeDiProva.COLPO_SICURO, MappeDiProva.COLPO_RISCHIOSO));

    @Test
    @DisplayName("al livello 1 le statistiche sono quelle base")
    void alPrimoLivelloValgonoLeBase() {
        final Creatura c = new Creatura(TONDA, 1);

        assertEquals(10, c.psMassimi());
        assertEquals(10, c.attacco());
        assertEquals(10, c.difesa());
        assertEquals(10, c.velocita());
    }

    @Test
    @DisplayName("ogni cinque livelli le statistiche raddoppiano rispetto alla base")
    void crescitaDiUnQuintoPerLivello() {
        // base + base * (livello - 1) / 5, a divisione intera.
        assertEquals(10, new Creatura(TONDA, 1).psMassimi());
        assertEquals(12, new Creatura(TONDA, 2).psMassimi());
        assertEquals(14, new Creatura(TONDA, 3).psMassimi());
        assertEquals(16, new Creatura(TONDA, 4).psMassimi());
        assertEquals(18, new Creatura(TONDA, 5).psMassimi());
        assertEquals(20, new Creatura(TONDA, 6).psMassimi());
        assertEquals(30, new Creatura(TONDA, 11).psMassimi());
    }

    @Test
    @DisplayName("il compagno iniziale ha i 43 punti salute che mostra la schermata")
    void compagnoInizialeComeAschermo() {
        // Unico aggancio al contenuto vero del gioco: se qualcuno ritocca
        // Scintillo o la formula, questa prova lo dice.
        final Creatura compagno = Bestiario.compagnoIniziale();

        assertEquals(5, compagno.livello());
        assertEquals(43, compagno.psMassimi());
        assertEquals(43, compagno.psAttuali());
        assertEquals(Elemento.ELETTRO, compagno.elemento());
        assertEquals(Specie.MOSSE_PER_SPECIE, compagno.mosse().size());
    }

    @Test
    @DisplayName("appena creata ha tutti i punti salute e non e' esausta")
    void appenaCreataEInSalute() {
        final Creatura c = new Creatura(TONDA, 5);

        assertEquals(c.psMassimi(), c.psAttuali());
        assertFalse(c.esausta());
    }

    @Test
    @DisplayName("il danno toglie punti salute e non scende sotto zero")
    void ilDannoNonScendeSottoZero() {
        final Creatura c = new Creatura(TONDA, 5);
        final int massimi = c.psMassimi();

        c.subisci(3);
        assertEquals(massimi - 3, c.psAttuali());
        assertFalse(c.esausta());

        c.subisci(10_000);
        assertEquals(0, c.psAttuali());
        assertTrue(c.esausta());

        // E restare colpita da esausta non la manda in negativo.
        c.subisci(5);
        assertEquals(0, c.psAttuali());
    }

    @Test
    @DisplayName("un danno di zero e' lecito, uno negativo no")
    void dannoNegativoRifiutato() {
        final Creatura c = new Creatura(TONDA, 5);
        final int prima = c.psAttuali();

        c.subisci(0);
        assertEquals(prima, c.psAttuali());

        assertThrows(IllegalArgumentException.class, () -> c.subisci(-1));
        assertEquals(prima, c.psAttuali());
    }

    @Test
    @DisplayName("la cura riporta al massimo anche una creatura stesa")
    void curaCompleta() {
        final Creatura c = new Creatura(TONDA, 5);
        c.subisci(10_000);
        assertTrue(c.esausta());

        c.curaCompletamente();

        assertEquals(c.psMassimi(), c.psAttuali());
        assertFalse(c.esausta());
    }

    @Test
    @DisplayName("un livello non positivo viene rifiutato")
    void livelloNonPositivoRifiutato() {
        assertThrows(IllegalArgumentException.class, () -> new Creatura(TONDA, 0));
        assertThrows(IllegalArgumentException.class, () -> new Creatura(TONDA, -3));
    }

    @Test
    @DisplayName("una specie deve avere nome, statistiche positive ed esattamente due mosse")
    void specieMalFormataRifiutata() {
        final List<Mossa> dueMosse =
                List.of(MappeDiProva.COLPO_SICURO, MappeDiProva.COLPO_RISCHIOSO);

        assertThrows(IllegalArgumentException.class,
                () -> new Specie(" ", Elemento.FUOCO, 10, 10, 10, 10, dueMosse));
        assertThrows(IllegalArgumentException.class,
                () -> new Specie("X", Elemento.FUOCO, 0, 10, 10, 10, dueMosse));
        assertThrows(IllegalArgumentException.class,
                () -> new Specie("X", Elemento.FUOCO, 10, 10, 10, 10,
                        List.of(MappeDiProva.COLPO_SICURO)));
    }

    @Test
    @DisplayName("la precisione di una mossa diventa una probabilita' fra 0 e 1")
    void precisioneComeProbabilita() {
        assertEquals(1.0, new Mossa("A", Elemento.FUOCO, 10, 100).probabilitaDiColpire(), 0.0);
        assertEquals(0.5, new Mossa("B", Elemento.FUOCO, 10, 50).probabilitaDiColpire(), 0.0);

        assertThrows(IllegalArgumentException.class,
                () -> new Mossa("C", Elemento.FUOCO, 10, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new Mossa("D", Elemento.FUOCO, 10, 101));
        assertThrows(IllegalArgumentException.class,
                () -> new Mossa("E", Elemento.FUOCO, 0, 50));
    }
}
