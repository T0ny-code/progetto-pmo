package gdr.grafica;

import java.awt.Graphics2D;

/**
 * Disegna una casella di edificio componendo i pezzi del foglio di tile.
 *
 * Nel foglio src/risorse/terreno.png:
 *
 *   riga 2   le basi (tetto al colmo o alla gronda, casa o ospedale; muro) e
 *            le sovrapposizioni trasparenti (finestra, porta, zoccolo, le due
 *            meta' della croce)
 *   riga 3   i bordi laterali, in tre colori: tetto di casa, tetto d'ospedale
 *            e muro
 *
 * Si compone invece di avere un riquadro per ogni combinazione. Su un muro
 * possono comparire indipendentemente una finestra oppure una porta, lo
 * zoccolo di pietra e i bordi laterali: tenendoli separati bastano il muro piu'
 * una sovrapposizione per ciascuno, mentre un riquadro per combinazione ne
 * vorrebbe uno per ogni incrocio. Che pezzo sia una casella lo dice
 * {@link ParteEdificio} contando i vicini: qui si sceglie soltanto quale
 * riquadro mettere.
 *
 * L'ordine di sovrapposizione conta: lo zoccolo di pietra va dopo la porta,
 * perche' le passa davanti negli ultimi due pixel.
 *
 * Difetto noto, lasciato com'e': i bordi laterali si mettono per ultimi e sono
 * colonne intere, quindi nelle caselle d'angolo coprono i due pixel dello
 * zoccolo e il pixel di sottogronda. Sono sei pixel per edificio, invisibili
 * mentre si gioca, e per toglierli servirebbero altri sei riquadri di bordo
 * nel foglio, uno per ogni pezzo su cui il bordo puo' cadere.
 */
public final class DisegnatoreEdificio {

    /** Riga del foglio con le basi e le sovrapposizioni. */
    private static final int RIGA_PEZZI = 2;

    /** Riga del foglio con i bordi laterali. */
    private static final int RIGA_BORDI = 3;

    private static final int TETTO_COLMO_CASA = 0;
    private static final int TETTO_GRONDA_CASA = 1;
    private static final int TETTO_COLMO_OSPEDALE = 2;
    private static final int TETTO_GRONDA_OSPEDALE = 3;
    private static final int MURO = 4;
    private static final int FINESTRA = 5;
    private static final int PORTA_CASA = 6;
    private static final int PORTA_OSPEDALE = 7;
    private static final int ZOCCOLO = 8;
    private static final int CROCE_SINISTRA = 9;
    private static final int CROCE_DESTRA = 10;

    private static final int BORDO_SINISTRO_TETTO_CASA = 0;
    private static final int BORDO_DESTRO_TETTO_CASA = 1;
    private static final int BORDO_SINISTRO_TETTO_OSPEDALE = 2;
    private static final int BORDO_DESTRO_TETTO_OSPEDALE = 3;
    private static final int BORDO_SINISTRO_MURO = 4;
    private static final int BORDO_DESTRO_MURO = 5;

    /**
     * Colonna, contata da sinistra, che porta il centro della croce.
     *
     * Vale per l'ospedale del gioco, che e' larga quattro caselle: il valore 2
     * mette la croce fra la seconda e la terza, cioe' in mezzo. Su un ospedale
     * di larghezza diversa la croce resterebbe qui e andrebbe fuori centro;
     * per centrarla sempre servirebbe conoscere anche le colonne a destra, che
     * {@link ParteEdificio} non conta perche' a nessuno serve.
     */
    private static final int COLONNA_CENTRO_CROCE = 2;

    private DisegnatoreEdificio() {
    }

    public static void disegna(final Graphics2D g, final ParteEdificio parte,
                               final int x, final int y) {
        if (parte.tetto()) {
            tetto(g, parte, x, y);
        } else {
            muro(g, parte, x, y);
        }
        bordi(g, parte, x, y);
    }

    private static void tetto(final Graphics2D g, final ParteEdificio parte,
                              final int x, final int y) {
        final int base;
        if (parte.ospedale()) {
            base = parte.colmo() ? TETTO_COLMO_OSPEDALE : TETTO_GRONDA_OSPEDALE;
        } else {
            base = parte.colmo() ? TETTO_COLMO_CASA : TETTO_GRONDA_CASA;
        }
        DisegnatoreTile.poni(g, base, RIGA_PEZZI, x, y);

        // La croce sta sulla falda alta: su una casa larga quattro caselle il
        // centro cade fra la seconda e la terza, quindi e' tagliata a meta'
        // fra due caselle vicine.
        //
        // Le disegna tutte e due questa casella, la terza da sinistra, perche'
        // e' l'unica che sa di stare al centro: la vicina di sinistra non puo'
        // saperlo, e su un edificio largo meno di tre caselle la croce non ci
        // starebbe. Le caselle si disegnano da sinistra a destra, quindi a
        // sinistra il tetto c'e' gia' e la meta' che sborda non ricopre nulla.
        if (parte.ospedale() && parte.colmo()
                && parte.colonneASinistra() == COLONNA_CENTRO_CROCE) {
            DisegnatoreTile.poni(g, CROCE_SINISTRA, RIGA_PEZZI, x - DisegnatoreTile.LATO, y);
            DisegnatoreTile.poni(g, CROCE_DESTRA, RIGA_PEZZI, x, y);
        }
    }

    private static void muro(final Graphics2D g, final ParteEdificio parte,
                             final int x, final int y) {
        DisegnatoreTile.poni(g, MURO, RIGA_PEZZI, x, y);

        if (parte.porta()) {
            DisegnatoreTile.poni(g, parte.ospedale() ? PORTA_OSPEDALE : PORTA_CASA,
                    RIGA_PEZZI, x, y);
        } else if (parte.finestra()) {
            DisegnatoreTile.poni(g, FINESTRA, RIGA_PEZZI, x, y);
        }

        if (parte.ultimaRiga()) {
            // Zoccolo di pietra: appoggia la casa a terra invece di farla
            // sembrare incollata sul prato.
            DisegnatoreTile.poni(g, ZOCCOLO, RIGA_PEZZI, x, y);
        }
    }

    private static void bordi(final Graphics2D g, final ParteEdificio parte,
                              final int x, final int y) {
        if (parte.bordoSinistro()) {
            DisegnatoreTile.poni(g, bordo(parte, true), RIGA_BORDI, x, y);
        }
        if (parte.bordoDestro()) {
            DisegnatoreTile.poni(g, bordo(parte, false), RIGA_BORDI, x, y);
        }
    }

    /**
     * Il bordo ha il colore della base su cui cade: tegole, tetto chiaro o
     * intonaco. Delle sovrapposizioni non sa niente, ed e' per questo che
     * negli angoli copre lo zoccolo (vedi il difetto noto in testa al file).
     */
    private static int bordo(final ParteEdificio parte, final boolean sinistro) {
        if (!parte.tetto()) {
            return sinistro ? BORDO_SINISTRO_MURO : BORDO_DESTRO_MURO;
        }
        if (parte.ospedale()) {
            return sinistro ? BORDO_SINISTRO_TETTO_OSPEDALE : BORDO_DESTRO_TETTO_OSPEDALE;
        }
        return sinistro ? BORDO_SINISTRO_TETTO_CASA : BORDO_DESTRO_TETTO_CASA;
    }
}
