package gdr.mondo;

import gdr.nucleo.Posizione;
import gdr.prova.MappeDiProva;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prove sulla lettura delle mappe testuali e sulla zona che ne esce.
 *
 * Una mappa sbagliata deve far fallire la costruzione con un messaggio che dice
 * dove: scoprirlo giocando costerebbe molto di piu'.
 */
class LettoreZonaTest {

    private static Zona zona(final String... righe) {
        return LettoreZona.leggi("collaudo", List.of(righe),
                List.of(), MappeDiProva.faunaVuota());
    }

    @Test
    @DisplayName("la griglia si legge riga per riga, con le righe che crescono verso il basso")
    void grigliaLettaNelVersoGiusto() {
        final Zona z = zona(
                "..,,",
                "AAAA",
                "~~**");

        assertEquals(4, z.larghezza());
        assertEquals(3, z.altezza());

        assertEquals(TipoTile.SENTIERO, z.tipo(new Posizione(0, 0)));
        assertEquals(TipoTile.ERBA, z.tipo(new Posizione(3, 0)));
        assertEquals(TipoTile.ALBERO, z.tipo(new Posizione(0, 1)));
        assertEquals(TipoTile.ACQUA, z.tipo(new Posizione(0, 2)));
        assertEquals(TipoTile.ERBA_ALTA, z.tipo(new Posizione(3, 2)));
    }

    @Test
    @DisplayName("ogni simbolo della legenda si traduce nel proprio tipo")
    void tuttiISimboliDellaLegenda() {
        final Zona z = zona(".,*A~>EPOH");

        assertEquals(TipoTile.SENTIERO, z.tipo(new Posizione(0, 0)));
        assertEquals(TipoTile.ERBA, z.tipo(new Posizione(1, 0)));
        assertEquals(TipoTile.ERBA_ALTA, z.tipo(new Posizione(2, 0)));
        assertEquals(TipoTile.ALBERO, z.tipo(new Posizione(3, 0)));
        assertEquals(TipoTile.ACQUA, z.tipo(new Posizione(4, 0)));
        assertEquals(TipoTile.PASSAGGIO, z.tipo(new Posizione(5, 0)));
        assertEquals(TipoTile.EDIFICIO, z.tipo(new Posizione(6, 0)));
        assertEquals(TipoTile.PORTA, z.tipo(new Posizione(7, 0)));
        assertEquals(TipoTile.OSPEDALE, z.tipo(new Posizione(8, 0)));
        assertEquals(TipoTile.PORTA_OSPEDALE, z.tipo(new Posizione(9, 0)));
    }

    @Test
    @DisplayName("si cammina su sentiero, erba, erba alta e passaggio, e su nient'altro")
    void calpestabilita() {
        final Zona z = zona(".,*A~>EPOH");

        assertTrue(z.calpestabile(new Posizione(0, 0)));
        assertTrue(z.calpestabile(new Posizione(1, 0)));
        assertTrue(z.calpestabile(new Posizione(2, 0)));
        assertTrue(z.calpestabile(new Posizione(5, 0)));

        assertFalse(z.calpestabile(new Posizione(3, 0)));
        assertFalse(z.calpestabile(new Posizione(4, 0)));
        assertFalse(z.calpestabile(new Posizione(6, 0)));
        assertFalse(z.calpestabile(new Posizione(7, 0)));
        assertFalse(z.calpestabile(new Posizione(8, 0)));
        assertFalse(z.calpestabile(new Posizione(9, 0)));
    }

    @Test
    @DisplayName("solo l'erba alta fa scattare incontri")
    void soloLErbaAltaGeneraIncontri() {
        final Zona z = zona(".,*A~>");

        assertTrue(z.generaIncontri(new Posizione(2, 0)));
        assertFalse(z.generaIncontri(new Posizione(0, 0)));
        assertFalse(z.generaIncontri(new Posizione(1, 0)));
        assertFalse(z.generaIncontri(new Posizione(3, 0)));
        assertFalse(z.generaIncontri(new Posizione(5, 0)));
    }

    @Test
    @DisplayName("fuori dai confini non si cammina e non si incontra niente")
    void fuoriDaiConfini() {
        final Zona z = zona("...", "...");

        assertFalse(z.dentro(new Posizione(-1, 0)));
        assertFalse(z.dentro(new Posizione(0, -1)));
        assertFalse(z.dentro(new Posizione(3, 0)));
        assertFalse(z.dentro(new Posizione(0, 2)));
        assertTrue(z.dentro(new Posizione(2, 1)));

        // Fuori rispondono falso invece di esplodere: e' il contratto.
        assertFalse(z.calpestabile(new Posizione(-1, 0)));
        assertFalse(z.generaIncontri(new Posizione(99, 99)));

        // Chiedere il tipo di una casella fuori e' invece un errore del chiamante.
        assertThrows(IndexOutOfBoundsException.class, () -> z.tipo(new Posizione(-1, 0)));
    }

    @Test
    @DisplayName("un simbolo sconosciuto dice in quale mappa e a quali coordinate")
    void simboloSconosciuto() {
        final IllegalArgumentException errore = assertThrows(IllegalArgumentException.class,
                () -> zona("...",
                           ".?.",
                           "..."));

        assertTrue(errore.getMessage().contains("collaudo"));
        assertTrue(errore.getMessage().contains("(1, 1)"));
        assertTrue(errore.getMessage().contains("?"));
    }

    @Test
    @DisplayName("una mappa storta viene rifiutata dicendo quale riga")
    void mappaStorta() {
        final IllegalArgumentException errore = assertThrows(IllegalArgumentException.class,
                () -> zona("....",
                           "..",
                           "...."));

        assertTrue(errore.getMessage().contains("riga 1"));
    }

    @Test
    @DisplayName("una mappa vuota viene rifiutata")
    void mappaVuota() {
        assertThrows(IllegalArgumentException.class,
                () -> LettoreZona.leggi("collaudo", List.of(),
                        List.of(), MappeDiProva.faunaVuota()));
        assertThrows(IllegalArgumentException.class, () -> zona(""));
    }

    @Test
    @DisplayName("la zona copia la griglia e non si lascia modificare da fuori")
    void laGrigliaVieneCopiata() {
        final TipoTile[][] griglia = {
            {TipoTile.SENTIERO, TipoTile.SENTIERO},
            {TipoTile.SENTIERO, TipoTile.SENTIERO},
        };
        final Zona z = new Zona("collaudo", griglia, List.of(), MappeDiProva.faunaVuota());

        griglia[0][0] = TipoTile.ALBERO;
        griglia[1][1] = TipoTile.ACQUA;

        assertEquals(TipoTile.SENTIERO, z.tipo(new Posizione(0, 0)));
        assertEquals(TipoTile.SENTIERO, z.tipo(new Posizione(1, 1)));
    }

    @Test
    @DisplayName("la lista dei passaggi non si lascia modificare da fuori")
    void iPassaggiNonSiModificano() {
        final Zona z = MappeDiProva.mondo().zonaCorrente();

        assertThrows(UnsupportedOperationException.class, () -> z.passaggi().clear());
    }
}
