package gdr.grafica;

import gdr.mondo.TipoTile;

import java.awt.Graphics2D;

/**
 * Disegna una casella di terreno prendendola dal foglio di tile.
 *
 * Il foglio e' src/risorse/terreno.png, 256x64 pixel divisi in riquadri da
 * 16x16. Le prime due righe sono il terreno, le altre due i pezzi degli
 * edifici, che usa DisegnatoreEdificio.
 *
 *   riga 0   sentiero, erba, erba alta della radura, erba alta del bosco,
 *            albero, passaggio, ciuffi davanti al personaggio (radura e bosco)
 *   riga 1   i sedici fotogrammi dell'acqua
 *
 * Tutte le caselle dello stesso tipo sono identiche, tranne l'acqua, che
 * scorre. Prima ognuna aveva i propri sassolini e ciuffi, ricavati dalle sue
 * coordinate; adesso il disegno sta nel PNG e la varieta' si otterrebbe
 * aggiungendo piu' riquadri per terreno, non piu' codice.
 */
public final class DisegnatoreTile {

    /** Lato di una casella, in pixel logici. */
    public static final int LATO = 16;

    /** Il foglio dei terreni e degli edifici. */
    static final String FOGLIO = "terreno.png";

    /** Riga del foglio con i terreni fissi. */
    static final int RIGA_TERRENO = 0;

    private static final int COLONNA_SENTIERO = 0;
    private static final int COLONNA_ERBA = 1;
    private static final int COLONNA_ERBA_ALTA_RADURA = 2;
    private static final int COLONNA_ERBA_ALTA_BOSCO = 3;
    private static final int COLONNA_ALBERO = 4;
    private static final int COLONNA_PASSAGGIO = 5;
    private static final int COLONNA_CIUFFI_RADURA = 6;
    private static final int COLONNA_CIUFFI_BOSCO = 7;

    /** Riga del foglio con i fotogrammi dell'acqua. */
    private static final int RIGA_ACQUA = 1;

    private static final int FOTOGRAMMI_ACQUA = 16;

    /** Di quanti fotogrammi al secondo avanza l'onda: un giro in 16/7 di secondo. */
    private static final double VELOCITA_ONDE = 7.0;

    /** Di quanti fotogrammi e' sfasata una riga d'acqua rispetto a quella sopra. */
    private static final int SFASAMENTO_PER_RIGA = 3;

    private DisegnatoreTile() {
    }

    /**
     * @param riga   riga della casella nella zona: serve a sfasare l'acqua.
     * @param tempo  secondi trascorsi, per l'animazione dell'acqua.
     * @param bosco  vero nella seconda zona, dove l'erba alta e' piu' cupa.
     */
    public static void disegna(final Graphics2D g, final TipoTile tipo, final int riga,
                               final int x, final int y, final double tempo,
                               final boolean bosco) {
        switch (tipo) {
            case SENTIERO -> poni(g, COLONNA_SENTIERO, RIGA_TERRENO, x, y);
            case ERBA -> poni(g, COLONNA_ERBA, RIGA_TERRENO, x, y);
            case ERBA_ALTA -> poni(g, bosco ? COLONNA_ERBA_ALTA_BOSCO : COLONNA_ERBA_ALTA_RADURA,
                    RIGA_TERRENO, x, y);
            case ALBERO -> poni(g, COLONNA_ALBERO, RIGA_TERRENO, x, y);
            case PASSAGGIO -> poni(g, COLONNA_PASSAGGIO, RIGA_TERRENO, x, y);
            case ACQUA -> poni(g, fotogrammaAcqua(riga, tempo), RIGA_ACQUA, x, y);

            // Le caselle di edificio le disegna DisegnatoreEdificio, che sa
            // anche che pezzo di casa sono.
            case EDIFICIO, PORTA, OSPEDALE, PORTA_OSPEDALE -> { }
        }
    }

    /**
     * I ciuffi che passano davanti al personaggio quando sta nell'erba alta:
     * si capisce a vista che li' possono comparire creature.
     */
    public static void ciuffiDavanti(final Graphics2D g, final int x, final int y,
                                     final boolean bosco) {
        poni(g, bosco ? COLONNA_CIUFFI_BOSCO : COLONNA_CIUFFI_RADURA, RIGA_TERRENO, x, y);
    }

    /**
     * Le onde scorrono, e righe diverse sono sfasate fra loro: senza, tutto il
     * laghetto pulserebbe insieme. Un giro completo dura poco piu' di due
     * secondi.
     */
    private static int fotogrammaAcqua(final int riga, final double tempo) {
        return Math.floorMod((long) (tempo * VELOCITA_ONDE)
                + (long) riga * SFASAMENTO_PER_RIGA, FOTOGRAMMI_ACQUA);
    }

    /** Mette un riquadro del foglio con l'angolo in alto a sinistra in (x, y). */
    static void poni(final Graphics2D g, final int colonna, final int riga,
                     final int x, final int y) {
        FoglioSprite.carica(FOGLIO, LATO).disegna(g, colonna, riga, x, y);
    }
}
