package gdr.mondo;

import gdr.nucleo.Direzione;
import gdr.nucleo.Posizione;
import gdr.prova.MappeDiProva;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prove sul mondo e sui collegamenti fra zone.
 *
 * Il mondo controlla la coerenza dei passaggi in costruzione: un collegamento
 * sbagliato fa fallire l'avvio invece di produrre un vicolo cieco che si scopre
 * solo camminandoci dentro. Queste prove verificano che quei controlli
 * scattino davvero, uno per uno.
 */
class MondoTest {

    private static Zona zonaCon(final String identificatore,
                                final List<Passaggio> passaggi,
                                final String... righe) {
        return LettoreZona.leggi(identificatore, List.of(righe),
                passaggi, MappeDiProva.faunaVuota());
    }

    @Test
    @DisplayName("si parte dalla zona iniziale e vaiA porta nell'altra")
    void cambioZona() {
        final Mondo mondo = MappeDiProva.mondo();

        assertEquals(MappeDiProva.PROVA, mondo.zonaCorrente().identificatore());

        mondo.vaiA(MappeDiProva.ALTRA);
        assertEquals(MappeDiProva.ALTRA, mondo.zonaCorrente().identificatore());

        mondo.vaiA(MappeDiProva.PROVA);
        assertEquals(MappeDiProva.PROVA, mondo.zonaCorrente().identificatore());
    }

    @Test
    @DisplayName("andare in una zona che non esiste e' un errore")
    void zonaInesistente() {
        final Mondo mondo = MappeDiProva.mondo();

        assertThrows(IllegalArgumentException.class, () -> mondo.vaiA("inventata"));
        // E lo stato non e' cambiato.
        assertEquals(MappeDiProva.PROVA, mondo.zonaCorrente().identificatore());
    }

    @Test
    @DisplayName("il passaggio si trova dalla casella che lo innesca")
    void passaggioInnescato() {
        final Zona prova = MappeDiProva.mondo().zonaCorrente();

        final Optional<Passaggio> passaggio = prova.passaggioIn(MappeDiProva.PASSAGGIO);
        assertTrue(passaggio.isPresent());
        assertEquals(MappeDiProva.ALTRA, passaggio.get().zonaDestinazione());
        assertEquals(MappeDiProva.ARRIVO_IN_ALTRA, passaggio.get().arrivo());

        assertTrue(prova.passaggioIn(MappeDiProva.LIBERA).isEmpty());
    }

    @Test
    @DisplayName("una zona iniziale che non esiste viene rifiutata")
    void zonaInizialeInesistente() {
        final Zona sola = zonaCon("sola", List.of(), "...", "...");

        assertThrows(IllegalArgumentException.class,
                () -> new Mondo(List.of(sola), "inventata"));
    }

    @Test
    @DisplayName("due zone con lo stesso nome vengono rifiutate")
    void identificatoriDuplicati() {
        final Zona una = zonaCon("stessa", List.of(), "...", "...");
        final Zona altra = zonaCon("stessa", List.of(), "...", "...");

        assertThrows(IllegalArgumentException.class,
                () -> new Mondo(List.of(una, altra), "stessa"));
    }

    @Test
    @DisplayName("un passaggio verso una zona inesistente viene rifiutato")
    void passaggioVersoIlNulla() {
        final Zona rotta = zonaCon("rotta",
                List.of(new Passaggio(new Posizione(0, 0), "inventata",
                        new Posizione(1, 1), Direzione.EST)),
                ">..",
                "...");

        assertThrows(IllegalStateException.class, () -> new Mondo(List.of(rotta), "rotta"));
    }

    @Test
    @DisplayName("un passaggio che atterra su una casella non calpestabile viene rifiutato")
    void arrivoNonCalpestabile() {
        // L'arrivo (1, 0) nella seconda zona e' un albero.
        final Zona prima = zonaCon("prima",
                List.of(new Passaggio(new Posizione(0, 1), "seconda",
                        new Posizione(1, 0), Direzione.EST)),
                "...",
                ">..");
        final Zona seconda = zonaCon("seconda",
                List.of(new Passaggio(new Posizione(0, 0), "prima",
                        new Posizione(1, 1), Direzione.OVEST)),
                ">A.",
                "...");

        final IllegalStateException errore = assertThrows(IllegalStateException.class,
                () -> new Mondo(List.of(prima, seconda), "prima"));
        assertTrue(errore.getMessage().contains("non calpestabile"));
    }

    @Test
    @DisplayName("un passaggio che atterra sopra un altro passaggio viene rifiutato")
    void arrivoSopraUnPassaggio() {
        // Senza questo controllo si rimbalzerebbe fra le due zone all'infinito.
        final Zona prima = zonaCon("prima",
                List.of(new Passaggio(new Posizione(0, 1), "seconda",
                        new Posizione(0, 0), Direzione.EST)),
                "...",
                ">..");
        final Zona seconda = zonaCon("seconda",
                List.of(new Passaggio(new Posizione(0, 0), "prima",
                        new Posizione(1, 1), Direzione.OVEST)),
                ">..",
                "...");

        final IllegalStateException errore = assertThrows(IllegalStateException.class,
                () -> new Mondo(List.of(prima, seconda), "prima"));
        assertTrue(errore.getMessage().contains("un altro passaggio"));
    }

    @Test
    @DisplayName("un passaggio senza ritorno viene rifiutato")
    void passaggioSenzaRitorno() {
        final Zona prima = zonaCon("prima",
                List.of(new Passaggio(new Posizione(0, 1), "seconda",
                        new Posizione(1, 1), Direzione.EST)),
                "...",
                ">..");
        final Zona seconda = zonaCon("seconda", List.of(),
                "...",
                "...");

        final IllegalStateException errore = assertThrows(IllegalStateException.class,
                () -> new Mondo(List.of(prima, seconda), "prima"));
        assertTrue(errore.getMessage().contains("ritorno"));
    }

    @Test
    @DisplayName("un passaggio che non poggia su una casella di passaggio viene rifiutato")
    void innescoSenzaCasellaDiPassaggio() {
        // L'innesco e' in (1, 1), che nella mappa e' sentiero e non '>'.
        assertThrows(IllegalArgumentException.class,
                () -> zonaCon("storta",
                        List.of(new Passaggio(new Posizione(1, 1), "altrove",
                                new Posizione(0, 0), Direzione.EST)),
                        "...",
                        "..."));
    }
}
