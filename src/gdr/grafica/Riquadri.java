package gdr.grafica;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

/**
 * Il testo del gioco: il font, la misura, la scrittura con capo automatico e
 * la cornice dei riquadri.
 *
 * Sta in una classe a parte perche' la usano sia la schermata di battaglia sia
 * i messaggi che compaiono sulla mappa, e non ha senso duplicarla.
 *
 * Il font e' uno dei font *logici* di Java, non uno installato sulla macchina.
 * La differenza conta: "Arial" o "DejaVu Sans" potrebbero non esistere sul
 * computer di chi esegue il gioco, e Java ripiegherebbe in silenzio su
 * qualcos'altro, cambiando l'aspetto del gioco da una macchina all'altra.
 * MONOSPACED, SANS_SERIF, SERIF e DIALOG esistono ovunque per contratto.
 *
 * Il corpo e' 9 e non meno: a 8 pixel il buco interno della "a" si chiude e
 * la lettera diventa una macchia. E niente antialiasing: la tela viene
 * ingrandita di un fattore intero a pixel netti, quindi le sfumature
 * diventerebbero blocchi grigi invece di ammorbidire i contorni.
 */
public final class Riquadri {

    /** Altezza del riquadro di testo in basso. */
    private static final int ALTEZZA_TESTO = 41;

    /** Corpo del font in pixel logici, prima dell'ingrandimento della finestra. */
    private static final int CORPO = 9;

    /** Distanza fra la cima di una riga e la cima della successiva. */
    public static final int INTERLINEA = 11;

    /** Il font di tutto il gioco. */
    private static final Font FONT = new Font(Font.SANS_SERIF, Font.PLAIN, CORPO);

    private Riquadri() {
    }

    /** Cornice chiara con bordo scuro e un filo d'ombra interno. */
    public static void cornice(final Graphics2D g, final int x, final int y,
                               final int larghezza, final int altezza) {
        g.setColor(Tavolozza.RIQUADRO_BORDO);
        g.fillRect(x, y, larghezza, altezza);
        g.setColor(Tavolozza.RIQUADRO_OMBRA);
        g.fillRect(x + 1, y + 1, larghezza - 2, altezza - 2);
        g.setColor(Tavolozza.RIQUADRO);
        g.fillRect(x + 1, y + 1, larghezza - 3, altezza - 3);
    }

    /** Margine del riquadro di testo dai bordi della tela. */
    private static final int MARGINE = 4;

    /**
     * Disegna la cornice del riquadro di testo in basso, uguale sulla mappa e
     * in battaglia, e restituisce dove si puo' scrivere dentro.
     *
     * Sta qui e non nei due renderizzatori perche' altrimenti margine, altezza
     * e larghezza andrebbero tenuti allineati a mano in due file.
     */
    public static Punto corniceInBasso(final Graphics2D g,
                                       final int larghezzaTela, final int altezzaTela) {
        final int y = altezzaTela - ALTEZZA_TESTO - MARGINE;
        final int larghezzaRiquadro = larghezzaTela - 2 * MARGINE;
        cornice(g, MARGINE, y, larghezzaRiquadro, ALTEZZA_TESTO);
        return new Punto(MARGINE, y, larghezzaRiquadro);
    }

    /** Dove e quanto largo si puo' scrivere dentro il riquadro in basso. */
    public record Punto(int x, int y, int larghezza) {

        /** Il primo pixel utile del testo, sei pixel dentro il bordo. */
        public int xTesto() {
            return x + 6;
        }

        /** Larghezza utile per il testo, sei pixel di margine per lato. */
        public int larghezzaTesto() {
            return larghezza - 12;
        }
    }

    /** Quanti pixel occupa un testo scritto su una riga sola. */
    public static int larghezza(final Graphics2D g, final String testo) {
        return metriche(g).stringWidth(testo);
    }

    /**
     * Scrive una riga sola, col colore del testo dei riquadri.
     *
     * Attenzione alla convenzione: y e' il bordo SUPERIORE del testo, non la
     * linea di base come vuole drawString. E' la convenzione piu' comoda quando
     * si ragiona per riquadri, ed e' quella che usano tutti i chiamanti.
     */
    public static void scrivi(final Graphics2D g, final String testo,
                              final int x, final int y) {
        g.setColor(Tavolozza.TESTO_SCURO);
        g.setFont(FONT);
        g.drawString(testo, x, y + metriche(g).getAscent());
    }

    /**
     * Scrive un testo mandandolo a capo quando eccede la larghezza concessa,
     * col colore del testo dei riquadri.
     *
     * Va a capo solo sugli spazi: una parola piu' larga del riquadro sborda
     * invece di essere spezzata a meta'.
     */
    public static void scrivi(final Graphics2D g, final String testo,
                              final int x, final int y, final int larghezzaUtile) {
        final FontMetrics metriche = metriche(g);
        final StringBuilder riga = new StringBuilder();
        int righe = 0;

        for (final String parola : testo.split(" ")) {
            final String candidata = riga.isEmpty() ? parola : riga + " " + parola;
            if (metriche.stringWidth(candidata) > larghezzaUtile && !riga.isEmpty()) {
                scrivi(g, riga.toString(), x, y + righe * INTERLINEA);
                righe++;
                riga.setLength(0);
                riga.append(parola);
            } else {
                riga.setLength(0);
                riga.append(candidata);
            }
        }
        if (!riga.isEmpty()) {
            scrivi(g, riga.toString(), x, y + righe * INTERLINEA);
        }
    }

    private static FontMetrics metriche(final Graphics2D g) {
        return g.getFontMetrics(FONT);
    }
}
