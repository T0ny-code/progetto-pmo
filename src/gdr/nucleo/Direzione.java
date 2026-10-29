package gdr.nucleo;

/**
 * Le quattro direzioni cardinali, con lo spostamento in caselle che comportano.
 *
 * L'asse delle righe cresce verso il basso, come nelle mappe testuali.
 */
public enum Direzione {

    NORD(0, -1),
    SUD(0, 1),
    OVEST(-1, 0),
    EST(1, 0);

    private final int deltaColonna;
    private final int deltaRiga;

    Direzione(final int deltaColonna, final int deltaRiga) {
        this.deltaColonna = deltaColonna;
        this.deltaRiga = deltaRiga;
    }

    /** Spostamento orizzontale in caselle. */
    public int deltaColonna() {
        return deltaColonna;
    }

    /** Spostamento verticale in caselle. */
    public int deltaRiga() {
        return deltaRiga;
    }
}
