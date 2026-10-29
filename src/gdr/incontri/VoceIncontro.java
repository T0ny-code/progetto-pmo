package gdr.incontri;

import gdr.battaglia.Specie;

import java.util.Objects;

/**
 * Una riga della tabella incontri di una zona: quale specie e in che
 * intervallo di livelli.
 *
 * Tutte le voci di una zona hanno la stessa probabilita' di uscire: e' cosi'
 * che sono i dati del gioco, e un peso per voce sarebbe un numero da spiegare
 * senza niente da esprimere.
 */
public record VoceIncontro(Specie specie,
                           int livelloMinimo,
                           int livelloMassimo) {

    public VoceIncontro {
        Objects.requireNonNull(specie, "La specie non puo' essere nulla");
        if (livelloMinimo <= 0) {
            throw new IllegalArgumentException(
                    "Il livello minimo deve essere positivo: " + livelloMinimo);
        }
        if (livelloMassimo < livelloMinimo) {
            throw new IllegalArgumentException("Intervallo di livelli rovesciato per "
                    + specie.nome() + ": da " + livelloMinimo + " a " + livelloMassimo);
        }
    }

    /** Quanti livelli diversi puo' assumere questa voce. */
    public int livelliPossibili() {
        return livelloMassimo - livelloMinimo + 1;
    }
}
