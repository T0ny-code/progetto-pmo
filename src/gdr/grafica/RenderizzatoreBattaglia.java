package gdr.grafica;

import gdr.battaglia.Battaglia;
import gdr.battaglia.Creatura;
import gdr.battaglia.Mossa;
import gdr.battaglia.Specie;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

/**
 * Disegna la schermata di battaglia a risoluzione logica.
 *
 * Legge la battaglia e l'animatore e produce pixel: non tocca lo stato, non
 * calcola danni, non decide turni. Le barre dei PS usano i valori *mostrati*
 * dell'animatore, non quelli reali, cosi' scendono gradualmente.
 */
public final class RenderizzatoreBattaglia {

    /** Quante voci ha il menu: le mosse della creatura piu' la fuga. */
    public static final int VOCI_MENU = Specie.MOSSE_PER_SPECIE + 1;

    /** La fuga e' sempre l'ultima voce; le precedenti sono le mosse, in ordine. */
    public static final int VOCE_FUGA = VOCI_MENU - 1;

    /**
     * Questa schermata e' disegnata per la tela standard e non si adatta ad
     * altre misure: piattaforme, creature e riquadri stanno a coordinate
     * scelte per 240x176. Prendere le dimensioni dal costruttore prometteva
     * un'indipendenza che non c'era.
     */
    private static final int LARGHEZZA = 240;
    private static final int ALTEZZA = 176;

    /**
     * @param menuAperto       se il giocatore puo' scegliere in questo istante.
     * @param voceSelezionata  quale voce del menu e' evidenziata.
     */
    public static void disegna(final Graphics2D g,
                        final Battaglia battaglia,
                        final AnimatoreBattaglia animatore,
                        final double tempo,
                        final boolean menuAperto,
                        final int voceSelezionata) {
        sfondo(g);

        final Creatura mia = battaglia.attiva();
        final Creatura sua = battaglia.selvatica();

        // Avversario in alto a destra, compagno in basso a sinistra: lo schema
        // classico, che lascia libero il centro per il riquadro di testo.
        creaturaSuPiattaforma(g, sua.specie(), 166, 76, 46, tempo);
        creaturaSuPiattaforma(g, mia.specie(), 60, 126, 50, tempo);

        riquadroInfo(g, 8, 10, 104, sua, animatore.psMostratiSuo(), false);
        riquadroInfo(g, 126, 94, 106, mia, animatore.psMostratiMio(), true);

        riquadroTesto(g, animatore.messaggio(), menuAperto, mia, voceSelezionata);
    }

    private static void sfondo(final Graphics2D g) {
        final int orizzonte = 96;

        g.setColor(Tavolozza.CIELO);
        g.fillRect(0, 0, LARGHEZZA, orizzonte - 14);
        g.setColor(Tavolozza.CIELO_BASSO);
        g.fillRect(0, orizzonte - 14, LARGHEZZA, 14);

        g.setColor(Tavolozza.TERRENO_BATTAGLIA);
        g.fillRect(0, orizzonte, LARGHEZZA, ALTEZZA - orizzonte);
        g.setColor(Tavolozza.TERRENO_BATTAGLIA_SCURO);
        g.fillRect(0, orizzonte, LARGHEZZA, 2);
    }

    /**
     * Una creatura in piedi sul proprio disco di terra.
     *
     * Lo scostamento verticale e' quanto la creatura affonda nel disco: la
     * relazione sta scritta qui una volta invece di essere gia' sottratta nei
     * numeri delle due chiamate.
     */
    private static void creaturaSuPiattaforma(final Graphics2D g, final Specie specie,
                                              final int centroX, final int centroY,
                                              final int diametro, final double tempo) {
        final int affondo = 34;
        piattaforma(g, centroX, centroY, diametro);
        DisegnatoreCreatura.disegna(g, specie,
                centroX - DisegnatoreCreatura.LATO / 2, centroY - affondo, tempo);
    }

    /** Il disco di terra sotto una creatura. */
    private static void piattaforma(final Graphics2D g, final int centroX,
                                    final int centroY, final int diametro) {
        final int altezzaDisco = diametro / 4;
        g.setColor(Tavolozza.PIATTAFORMA_OMBRA);
        g.fillOval(centroX - diametro / 2, centroY - altezzaDisco / 2 + 1,
                diametro, altezzaDisco);
        g.setColor(Tavolozza.PIATTAFORMA);
        g.fillOval(centroX - diametro / 2, centroY - altezzaDisco / 2,
                diametro, altezzaDisco);
    }

    /**
     * Nome, livello e barra dei PS. Per il compagno si mostrano anche i numeri,
     * per l'avversario no: e' la convenzione del genere e ha senso, di una
     * creatura selvatica non si conoscono i punti esatti.
     */
    private static void riquadroInfo(final Graphics2D g, final int x, final int y,
                                     final int larghezzaRiquadro, final Creatura creatura,
                                     final double psMostrati, final boolean conNumeri) {
        final int altezzaRiquadro = conNumeri ? 37 : 26;
        Riquadri.cornice(g, x, y, larghezzaRiquadro, altezzaRiquadro);

        Riquadri.scrivi(g, creatura.nome(), x + 4, y + 4);
        final String livello = "L" + creatura.livello();
        Riquadri.scrivi(g, livello,
                x + larghezzaRiquadro - 4 - Riquadri.larghezza(g, livello), y + 4);

        barraPs(g, x + 4, y + 17, larghezzaRiquadro - 8, psMostrati, creatura.psMassimi());

        if (conNumeri) {
            final int arrotondati = (int) Math.ceil(psMostrati);
            Riquadri.scrivi(g, arrotondati + "/" + creatura.psMassimi(), x + 4, y + 24);
        }
    }

    private static void barraPs(final Graphics2D g, final int x, final int y,
                                final int larghezzaBarra, final double psMostrati,
                                final int psMassimi) {
        g.setColor(Tavolozza.BARRA_FONDO);
        g.fillRect(x, y, larghezzaBarra, 5);

        final double frazione = Math.max(0.0, Math.min(1.0, psMostrati / psMassimi));
        final int pieno = (int) Math.round((larghezzaBarra - 2) * frazione);

        // Il colore segue la salute: si capisce a colpo d'occhio se si sta
        // perdendo, senza leggere i numeri.
        final Color colore;
        if (frazione > 0.5) {
            colore = Tavolozza.BARRA_ALTA;
        } else if (frazione > 0.2) {
            colore = Tavolozza.BARRA_MEDIA;
        } else {
            colore = Tavolozza.BARRA_BASSA;
        }

        g.setColor(colore);
        g.fillRect(x + 1, y + 1, pieno, 3);
    }

    private static void riquadroTesto(final Graphics2D g, final String messaggio,
                               final boolean menuAperto, final Creatura mia,
                               final int voceSelezionata) {
        final Riquadri.Punto riquadro = Riquadri.corniceInBasso(g, LARGHEZZA, ALTEZZA);

        if (!menuAperto) {
            // In battaglia la frase e' una sola: centrata verticalmente sta
            // meglio delle tre righe che puo' occupare sulla mappa.
            Riquadri.scrivi(g, messaggio, riquadro.xTesto(), riquadro.y() + 14,
                    riquadro.larghezzaTesto());
            return;
        }

        // Con il menu aperto il testo si stringe sulla sinistra e le voci
        // stanno a destra.
        Riquadri.scrivi(g, "Cosa fa " + mia.nome() + "?",
                riquadro.xTesto(), riquadro.y() + 6, 100);

        final int xMenu = riquadro.x() + riquadro.larghezza() - 86;
        final List<Mossa> mosse = mia.mosse();

        for (int voce = 0; voce < VOCI_MENU; voce++) {
            final int yVoce = riquadro.y() + 4 + voce * Riquadri.INTERLINEA;

            final String testo =
                    voce == VOCE_FUGA ? "Fuggi" : mosse.get(voce).nome();

            if (voce == voceSelezionata) {
                g.setColor(Tavolozza.SELEZIONE);
                g.fillRect(xMenu - 6, yVoce + 2, 3, 3);
            }
            Riquadri.scrivi(g, testo, xMenu, yVoce);
        }
    }

}
