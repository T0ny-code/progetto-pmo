package gdr.nucleo;

/**
 * Sorgente di casualita' del gioco.
 *
 * E' un'interfaccia e non un riferimento diretto a java.util.Random perche'
 * cosi' chi la usa non dipende da Math.random: la sorgente arriva dall'esterno,
 * e chi vuole un comportamento prevedibile puo' fornirne una propria al posto
 * di quella del gioco.
 */
public interface Casuale {

    /** Intero nell'intervallo [0, limiteEsclusivo). */
    int intero(int limiteEsclusivo);

    /** Numero reale nell'intervallo [0, 1). */
    double frazione();

    /** Vero con la probabilita' indicata, espressa tra 0 e 1. */
    default boolean accade(final double probabilita) {
        if (probabilita <= 0.0) {
            return false;
        }
        if (probabilita >= 1.0) {
            return true;
        }
        return frazione() < probabilita;
    }
}
