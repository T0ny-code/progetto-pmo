package gdr.grafica;

import gdr.nucleo.Direzione;

import java.awt.Graphics2D;

/**
 * Disegna il personaggio del giocatore prendendolo dal foglio di sprite.
 *
 * Il foglio e' src/risorse/personaggio.png, 48x64 pixel divisi in riquadri da
 * 16x16: tre colonne per le fasi di camminata e quattro righe per le
 * direzioni.
 *
 *            colonna 0     colonna 1       colonna 2
 *            fermo         passo sinistro  passo destro
 *   riga 0   NORD          NORD            NORD
 *   riga 1   SUD           SUD             SUD
 *   riga 2   OVEST         OVEST           OVEST
 *   riga 3   EST           EST             EST
 *
 * L'ordine delle righe non e' scelto a caso: e' quello in cui le direzioni
 * sono dichiarate in Direzione, quindi la riga si ricava da sguardo.ordinal()
 * senza bisogno di una tabella di conversione. Se un giorno si aggiunge una
 * direzione, va aggiunta una riga al foglio nella stessa posizione.
 *
 * Le immagini attuali sono segnaposto generati dal vecchio codice di disegno:
 * hanno l'aspetto giusto e le dimensioni giuste, e vanno sostituite con i
 * disegni definitivi senza toccare questa classe.
 */
public final class DisegnatorePersonaggio {

    /** Lato del riquadro di un personaggio, in pixel logici. */
    public static final int LATO_FOGLIO = 16;

    /** Personaggio fermo. */
    public static final int FASE_FERMO = 0;

    /** Primo passo: avanza il piede sinistro. */
    public static final int FASE_PASSO_SINISTRO = 1;

    /** Secondo passo: avanza il piede destro. */
    public static final int FASE_PASSO_DESTRO = 2;

    /** Quante fasi di camminata ha il foglio. */
    private static final int FASI = 3;

    private DisegnatorePersonaggio() {
    }

    /**
     * @param x    angolo sinistro del riquadro 16x16 in pixel logici.
     * @param y    angolo superiore del riquadro 16x16 in pixel logici.
     * @param fase una delle costanti FASE_*.
     */
    public static void disegna(final Graphics2D g,
                               final int x,
                               final int y,
                               final Direzione sguardo,
                               final int fase) {
        if (fase < 0 || fase >= FASI) {
            throw new IllegalArgumentException(
                    "Fase di camminata inesistente: " + fase
                    + ". Usa una delle costanti FASE_*.");
        }
        FoglioSprite.carica("personaggio.png", LATO_FOGLIO)
                .disegna(g, fase, sguardo.ordinal(), x, y);
    }
}
