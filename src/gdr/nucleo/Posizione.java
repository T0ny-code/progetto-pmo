package gdr.nucleo;

/**
 * Una casella della griglia, in coordinate di tile e non di pixel.
 *
 * E' immutabile: ogni spostamento produce una nuova posizione. La conversione
 * in pixel avviene soltanto nel livello grafico.
 */
public record Posizione(int colonna, int riga) {

    /** La casella adiacente nella direzione indicata. */
    public Posizione spostata(final Direzione direzione) {
        return new Posizione(colonna + direzione.deltaColonna(), riga + direzione.deltaRiga());
    }

    @Override
    public String toString() {
        return "(" + colonna + ", " + riga + ")";
    }
}
