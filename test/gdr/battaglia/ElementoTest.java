package gdr.battaglia;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prove sulla tabella di efficacia elementale.
 *
 * E' una tabella piccola ma asimmetrica, e l'asimmetria e' voluta: verificarla
 * casella per casella impedisce che una modifica distratta la renda simmetrica
 * "per ordine" rompendo l'equilibrio del gioco.
 */
class ElementoTest {

    private static final double SUPER = 2.0;
    private static final double POCO = 0.5;

    @Test
    @DisplayName("il triangolo classico: fuoco batte erba, erba batte acqua, acqua batte fuoco")
    void triangoloClassico() {
        assertEquals(SUPER, Elemento.FUOCO.efficaciaContro(Elemento.ERBA), 0.0);
        assertEquals(SUPER, Elemento.ERBA.efficaciaContro(Elemento.ACQUA), 0.0);
        assertEquals(SUPER, Elemento.ACQUA.efficaciaContro(Elemento.FUOCO), 0.0);
    }

    @Test
    @DisplayName("e il verso opposto del triangolo e' poco efficace")
    void versoOppostoDelTriangolo() {
        assertEquals(POCO, Elemento.ERBA.efficaciaContro(Elemento.FUOCO), 0.0);
        assertEquals(POCO, Elemento.ACQUA.efficaciaContro(Elemento.ERBA), 0.0);
        assertEquals(POCO, Elemento.FUOCO.efficaciaContro(Elemento.ACQUA), 0.0);
    }

    @Test
    @DisplayName("l'elettro batte l'acqua e per il resto e' neutro")
    void elettroBatteSoloLAcqua() {
        assertEquals(SUPER, Elemento.ELETTRO.efficaciaContro(Elemento.ACQUA), 0.0);
        assertEquals(Elemento.NEUTRO, Elemento.ELETTRO.efficaciaContro(Elemento.FUOCO), 0.0);
        assertEquals(Elemento.NEUTRO, Elemento.ELETTRO.efficaciaContro(Elemento.ERBA), 0.0);
        assertEquals(Elemento.NEUTRO, Elemento.ELETTRO.efficaciaContro(Elemento.ELETTRO), 0.0);
    }

    @Test
    @DisplayName("l'elettro non ha debolezze: e' una scelta, non una dimenticanza")
    void elettroSenzaDebolezze() {
        // La creatura del giocatore e' d'elettro e nessuna selvatica lo e':
        // una debolezza dell'elettro punirebbe solo il giocatore.
        for (final Elemento attaccante : Elemento.values()) {
            assertTrue(attaccante.efficaciaContro(Elemento.ELETTRO) <= Elemento.NEUTRO);
            assertEquals(Elemento.NEUTRO, attaccante.efficaciaContro(Elemento.ELETTRO), 0.0);
        }
    }

    @Test
    @DisplayName("nessun elemento e' forte o debole contro se stesso")
    void neutroControSeStesso() {
        for (final Elemento elemento : Elemento.values()) {
            assertEquals(Elemento.NEUTRO, elemento.efficaciaContro(elemento), 0.0);
        }
    }

    @Test
    @DisplayName("ogni elemento ha almeno un bersaglio contro cui e' forte")
    void ogniElementoHaUnBersaglio() {
        // E' la regola che il progetto si impone per un eventuale quinto elemento.
        for (final Elemento attaccante : Elemento.values()) {
            boolean trovato = false;
            for (final Elemento difensore : Elemento.values()) {
                if (attaccante.efficaciaContro(difensore) > Elemento.NEUTRO) {
                    trovato = true;
                }
            }
            assertTrue(trovato);
        }
    }
}
