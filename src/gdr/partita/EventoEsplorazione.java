package gdr.partita;

import gdr.nucleo.Posizione;

/**
 * Cosa e' successo durante un aggiornamento dell'esplorazione.
 *
 * E' il canale che va dalla logica alla vista: l'esplorazione non emette suoni,
 * non scrive testo e non disegna, si limita a dire cosa e' accaduto.
 */
public sealed interface EventoEsplorazione {

    /** Il giocatore ha finito di entrare in una nuova casella. */
    record PassoCompletato(Posizione casella) implements EventoEsplorazione { }

    /**
     * Il giocatore ha sbattuto contro una porta.
     *
     * Gli interni sono fuori scope, quindi le porte non si attraversano: e'
     * questo evento che permette a una porta di fare qualcosa comunque, come
     * la cura all'ospedale.
     */
    record PortaToccata(boolean ospedale) implements EventoEsplorazione { }

    /** Il giocatore ha attraversato un passaggio ed e' in un'altra zona. */
    record ZonaCambiata() implements EventoEsplorazione { }
}
