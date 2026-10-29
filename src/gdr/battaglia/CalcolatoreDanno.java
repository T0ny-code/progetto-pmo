package gdr.battaglia;

import gdr.nucleo.Casuale;

/**
 * Calcola il danno di una mossa. Non ha stato: date le stesse creature, la
 * stessa mossa e la stessa sorgente casuale, restituisce lo stesso risultato.
 *
 * E' una variante della formula classica dei GDR a turni: cresce col livello e
 * con la potenza, si divide per la difesa, e viene moltiplicata per
 * l'efficacia elementale e per una variazione casuale fra l'85% e il 100%,
 * cosi' due colpi identici non fanno sempre lo stesso numero.
 */
public final class CalcolatoreDanno {

    /** Variazione minima applicata, in percentuale. */
    private static final int VARIAZIONE_MINIMA = 85;

    /** Quanti valori diversi puo' assumere la variazione: da 85 a 100. */
    private static final int VARIAZIONI_POSSIBILI = 16;

    private CalcolatoreDanno() {
    }

    /** Il danno calcolato e l'efficacia elementale che lo ha prodotto. */
    public record Risultato(int danno, double efficacia) {
    }

    public static Risultato calcola(final Creatura attaccante,
                                    final Creatura difensore,
                                    final Mossa mossa,
                                    final Casuale casuale) {
        final double efficacia =
                mossa.elemento().efficaciaContro(difensore.elemento());

        final int base = ((2 * attaccante.livello() / 5 + 2)
                * mossa.potenza() * attaccante.attacco() / difensore.difesa()) / 50 + 2;

        final int conEfficacia = (int) Math.round(base * efficacia);
        final int variazione = VARIAZIONE_MINIMA + casuale.intero(VARIAZIONI_POSSIBILI);

        // Almeno un punto: un colpo che va a segno deve sempre fare qualcosa,
        // altrimenti contro un difensore molto corazzato la battaglia si blocca.
        final int danno = Math.max(1, conEfficacia * variazione / 100);

        return new Risultato(danno, efficacia);
    }
}
