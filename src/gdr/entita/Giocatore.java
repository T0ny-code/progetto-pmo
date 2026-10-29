package gdr.entita;

import gdr.nucleo.Direzione;
import gdr.nucleo.Posizione;

/**
 * Il personaggio comandato dall'utente.
 *
 * Possiede soltanto il proprio movimento: la creatura che combatte la tiene
 * {@link gdr.partita.Partita}, perche' e' della partita e non del personaggio.
 */
public final class Giocatore {

    private final MovimentoGriglia movimento;

    public Giocatore(final Posizione posizioneIniziale,
                     final Direzione sguardoIniziale,
                     final double velocita) {
        this.movimento = new MovimentoGriglia(posizioneIniziale, sguardoIniziale, velocita);
    }

    /** Lo stato del passo in corso. */
    public MovimentoGriglia movimento() {
        return movimento;
    }

    /** La casella occupata. */
    public Posizione posizione() {
        return movimento.casella();
    }
}
