package gdr.nucleo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Prove su posizioni e direzioni: sono le fondamenta su cui poggia tutto il
 * resto, e un errore di segno qui si propagherebbe ovunque.
 */
class PosizioneTest {

    @Test
    @DisplayName("l'asse delle righe cresce verso il basso")
    void asseRigheVersoIlBasso() {
        // E' la trappola numero uno del progetto: NORD diminuisce la riga.
        assertEquals(-1, Direzione.NORD.deltaRiga());
        assertEquals(1, Direzione.SUD.deltaRiga());
        assertEquals(0, Direzione.NORD.deltaColonna());
        assertEquals(0, Direzione.SUD.deltaColonna());

        assertEquals(-1, Direzione.OVEST.deltaColonna());
        assertEquals(1, Direzione.EST.deltaColonna());
        assertEquals(0, Direzione.OVEST.deltaRiga());
        assertEquals(0, Direzione.EST.deltaRiga());
    }

    @Test
    @DisplayName("spostata produce la casella adiacente nelle quattro direzioni")
    void spostataNelleQuattroDirezioni() {
        final Posizione centro = new Posizione(5, 5);

        assertEquals(new Posizione(5, 4), centro.spostata(Direzione.NORD));
        assertEquals(new Posizione(5, 6), centro.spostata(Direzione.SUD));
        assertEquals(new Posizione(4, 5), centro.spostata(Direzione.OVEST));
        assertEquals(new Posizione(6, 5), centro.spostata(Direzione.EST));
    }

    @Test
    @DisplayName("spostarsi e tornare indietro riporta alla casella di partenza")
    void andataERitorno() {
        final Posizione partenza = new Posizione(3, 7);

        for (final Direzione direzione : Direzione.values()) {
            final Direzione opposta = switch (direzione) {
                case NORD -> Direzione.SUD;
                case SUD -> Direzione.NORD;
                case OVEST -> Direzione.EST;
                case EST -> Direzione.OVEST;
            };
            assertEquals(partenza, partenza.spostata(direzione).spostata(opposta));
        }
    }

    @Test
    @DisplayName("spostata non modifica la posizione di partenza")
    void spostataNonModificaLOriginale() {
        final Posizione partenza = new Posizione(1, 1);
        partenza.spostata(Direzione.EST);

        assertEquals(new Posizione(1, 1), partenza);
    }

    @Test
    @DisplayName("colonna e riga non si confondono fra loro")
    void colonnaERigaNonSiConfondono() {
        // Se i due campi fossero scambiati da qualche parte, queste due
        // posizioni risulterebbero uguali.
        assertNotEquals(new Posizione(2, 5), new Posizione(5, 2));
        assertEquals(2, new Posizione(2, 5).colonna());
        assertEquals(5, new Posizione(2, 5).riga());
    }
}
