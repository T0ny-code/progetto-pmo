package gdr.partita;

/** In che stato si trova la partita. */
public enum FaseGioco {

    /** Si cammina sulla mappa. */
    ESPLORAZIONE,

    /**
     * Il personaggio e' entrato nell'ospedale e per qualche secondo non si
     * vede. L'interno non esiste: si vede solo la mappa senza il personaggio.
     */
    DENTRO_OSPEDALE,

    /** E' in corso una battaglia. */
    BATTAGLIA
}
