package gdr.mondo;

import gdr.nucleo.Direzione;
import gdr.nucleo.Posizione;

import java.util.Objects;

/**
 * Il collegamento fra due zone.
 *
 * L'innesco e' la casella che, una volta raggiunta a fine passo, fa cambiare
 * zona. L'arrivo e' dove il giocatore viene posato nella zona di destinazione:
 * per costruzione non e' mai esso stesso un passaggio, altrimenti si
 * rimbalzerebbe avanti e indietro all'infinito.
 */
public record Passaggio(Posizione innesco,
                        String zonaDestinazione,
                        Posizione arrivo,
                        Direzione direzioneUscita) {

    public Passaggio {
        Objects.requireNonNull(innesco, "L'innesco non puo' essere nullo");
        Objects.requireNonNull(arrivo, "L'arrivo non puo' essere nullo");
        Objects.requireNonNull(direzioneUscita, "La direzione d'uscita non puo' essere nulla");
        if (zonaDestinazione == null || zonaDestinazione.isBlank()) {
            throw new IllegalArgumentException("La zona di destinazione non puo' essere vuota");
        }
    }
}
