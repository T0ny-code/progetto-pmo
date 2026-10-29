package gdr.entita;

import gdr.nucleo.Direzione;
import gdr.nucleo.Posizione;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prove sul movimento a griglia: e' il cuore dell'esplorazione e ha
 * l'invariante piu' delicato del progetto, cioe' che la posizione logica resti
 * sempre una casella intera mentre il disegno si muove fra due caselle.
 */
class MovimentoGrigliaTest {

    /** Le stesse di Avvio: quattro caselle al secondo. */
    private static final double VELOCITA = 4.0;

    /** Lo stesso di CicloDiGioco: sessanta aggiornamenti al secondo. */
    private static final double PASSO = 1.0 / 60.0;

    /** Quanti aggiornamenti deve durare una casella con quei due numeri. */
    private static final int AGGIORNAMENTI_PER_CASELLA = 15;

    private static MovimentoGriglia movimento() {
        return new MovimentoGriglia(new Posizione(2, 3), Direzione.SUD, VELOCITA);
    }

    @Test
    @DisplayName("appena creato non e' in transito e sta dove gli si e' detto")
    void statoIniziale() {
        final MovimentoGriglia m = movimento();

        assertEquals(new Posizione(2, 3), m.casella());
        assertEquals(Direzione.SUD, m.sguardo());
        assertFalse(m.inTransito());
        assertEquals(0.0, m.progresso(), 0.0);
        assertEquals(0, m.passiCompletati());
    }

    @Test
    @DisplayName("una casella dura esattamente quindici aggiornamenti")
    void quindiciAggiornamentiPerCasella() {
        final MovimentoGriglia m = movimento();
        m.inizia(Direzione.EST);

        for (int i = 1; i < AGGIORNAMENTI_PER_CASELLA; i++) {
            m.avanza(PASSO);
            assertTrue(m.inTransito());
        }

        m.avanza(PASSO);
        assertFalse(m.inTransito());
        assertEquals(new Posizione(3, 3), m.casella());
        assertEquals(1, m.passiCompletati());
    }

    @Test
    @DisplayName("dieci caselle di fila costano centocinquanta aggiornamenti")
    void dieciCaselleNonRallentano() {
        // Senza la tolleranza sul confronto con 1 ne servirebbero 160: la
        // somma di quindici quindicesimi in virgola mobile non fa esattamente 1.
        final MovimentoGriglia m = movimento();
        int aggiornamenti = 0;

        while (m.passiCompletati() < 10) {
            if (!m.inTransito()) {
                m.inizia(Direzione.SUD);
            }
            m.avanza(PASSO);
            aggiornamenti++;
        }

        assertEquals(10 * AGGIORNAMENTI_PER_CASELLA, aggiornamenti);
    }

    @Test
    @DisplayName("durante il passo la casella resta quella di partenza")
    void laCasellaRestaQuellaDiPartenzaFinoAllaFine() {
        final MovimentoGriglia m = movimento();
        m.inizia(Direzione.EST);

        for (int i = 1; i < AGGIORNAMENTI_PER_CASELLA; i++) {
            m.avanza(PASSO);
            assertEquals(new Posizione(2, 3), m.casella());
        }
    }

    @Test
    @DisplayName("il progresso resta sempre fra 0 compreso e 1 escluso")
    void progressoSempreNelSuoIntervallo() {
        final MovimentoGriglia m = movimento();

        for (int casella = 0; casella < 5; casella++) {
            if (!m.inTransito()) {
                m.inizia(Direzione.EST);
            }
            for (int i = 0; i < AGGIORNAMENTI_PER_CASELLA; i++) {
                m.avanza(PASSO);
                assertTrue(m.progresso() >= 0.0);
                assertTrue(m.progresso() < 1.0);
            }
        }
    }

    @Test
    @DisplayName("la posizione interpolata va dalla casella di partenza a quella d'arrivo")
    void interpolazioneFraLeDueCaselle() {
        final MovimentoGriglia m = movimento();

        assertEquals(2.0, m.colonnaInterpolata(), 0.0);
        assertEquals(3.0, m.rigaInterpolata(), 0.0);

        m.inizia(Direzione.EST);
        m.avanza(PASSO * 5);

        // A un terzo del passo verso est: la riga non si muove.
        assertTrue(m.colonnaInterpolata() > 2.0);
        assertTrue(m.colonnaInterpolata() < 3.0);
        assertEquals(3.0, m.rigaInterpolata(), 0.0);

        while (m.inTransito()) {
            m.avanza(PASSO);
        }
        assertEquals(3.0, m.colonnaInterpolata(), 0.0);
        assertEquals(3.0, m.rigaInterpolata(), 0.0);
    }

    @Test
    @DisplayName("orienta gira il personaggio senza muoverlo")
    void orientaNonMuove() {
        final MovimentoGriglia m = movimento();
        m.orienta(Direzione.OVEST);

        assertEquals(Direzione.OVEST, m.sguardo());
        assertEquals(new Posizione(2, 3), m.casella());
        assertFalse(m.inTransito());
    }

    @Test
    @DisplayName("teletrasporta interrompe il passo in corso")
    void teletrasportaInterrompeIlPasso() {
        final MovimentoGriglia m = movimento();
        m.inizia(Direzione.EST);
        m.avanza(PASSO);
        assertTrue(m.inTransito());

        m.teletrasporta(new Posizione(9, 9), Direzione.NORD);

        assertEquals(new Posizione(9, 9), m.casella());
        assertEquals(Direzione.NORD, m.sguardo());
        assertFalse(m.inTransito());
        assertEquals(0.0, m.progresso(), 0.0);
        assertEquals(9.0, m.colonnaInterpolata(), 0.0);
    }

    @Test
    @DisplayName("avviare un passo mentre un altro e' in corso e' un errore")
    void nonSiAvvianoDuePassiInsieme() {
        final MovimentoGriglia m = movimento();
        m.inizia(Direzione.EST);

        assertThrows(IllegalStateException.class, () -> m.inizia(Direzione.OVEST));
    }

    @Test
    @DisplayName("avanzare senza un passo in corso non fa niente")
    void avanzareFermoNonFaNiente() {
        final MovimentoGriglia m = movimento();
        m.avanza(PASSO * 100);

        assertEquals(new Posizione(2, 3), m.casella());
        assertEquals(0, m.passiCompletati());
        assertFalse(m.inTransito());
    }

    @Test
    @DisplayName("un aggiornamento conclude al massimo un passo")
    void unPassoPerAggiornamento() {
        // Anche con un tempo enorme: l'eccedenza si scarta invece di riversarla
        // nel passo successivo. E' cio' che impedisce a un aggiornamento di
        // attraversare due caselle, e con esse due zone diverse.
        final MovimentoGriglia m = movimento();
        m.inizia(Direzione.EST);
        m.avanza(100.0);

        assertEquals(1, m.passiCompletati());
        assertEquals(new Posizione(3, 3), m.casella());
        assertFalse(m.inTransito());
    }

    @Test
    @DisplayName("una velocita' non positiva viene rifiutata")
    void velocitaNonPositivaRifiutata() {
        assertThrows(IllegalArgumentException.class,
                () -> new MovimentoGriglia(new Posizione(0, 0), Direzione.SUD, 0.0));
        assertThrows(IllegalArgumentException.class,
                () -> new MovimentoGriglia(new Posizione(0, 0), Direzione.SUD, -1.0));
    }

    @Test
    @DisplayName("il giocatore espone la casella del proprio movimento")
    void giocatoreEspoineLaCasella() {
        final Giocatore giocatore = new Giocatore(new Posizione(4, 1), Direzione.EST, VELOCITA);

        assertEquals(new Posizione(4, 1), giocatore.posizione());
        assertEquals(giocatore.movimento().casella(), giocatore.posizione());
    }
}
