package gdr.grafica;

import java.awt.Color;

/**
 * I colori del gioco, raccolti in un posto solo.
 *
 * Tenerli qui evita di spargere costanti esadecimali nei disegnatori e rende
 * banale ritoccare l'aspetto senza rovistare nel codice.
 *
 * Qui non ci sono piu' i colori del terreno, degli edifici e dei personaggi:
 * quelli vivono dentro i tre PNG di src/risorse, e per cambiarli si apre
 * l'immagine invece del codice. Restano i colori di quello che e' ancora
 * dipinto con Graphics2D: la schermata di battaglia, i riquadri di testo e il
 * velo della dissolvenza.
 */
public final class Tavolozza {

    // Schermata di battaglia
    public static final Color CIELO = new Color(0x9ED0E8);
    public static final Color CIELO_BASSO = new Color(0xCDE7F2);
    public static final Color TERRENO_BATTAGLIA = new Color(0x8FBF63);
    public static final Color TERRENO_BATTAGLIA_SCURO = new Color(0x6E9B49);
    public static final Color PIATTAFORMA = new Color(0xB9C97E);
    public static final Color PIATTAFORMA_OMBRA = new Color(0x91A85F);

    // Riquadri di testo e barre dei punti salute
    public static final Color RIQUADRO = new Color(0xF8F4E6);
    public static final Color RIQUADRO_BORDO = new Color(0x4A3F35);
    public static final Color RIQUADRO_OMBRA = new Color(0xD8D0BA);
    public static final Color TESTO_SCURO = new Color(0x2A2420);
    public static final Color BARRA_FONDO = new Color(0x53483E);
    public static final Color BARRA_ALTA = new Color(0x4FC24F);
    public static final Color BARRA_MEDIA = new Color(0xE8C33A);
    public static final Color BARRA_BASSA = new Color(0xD84A3A);

    /** Il quadratino che segna la voce scelta nel menu di battaglia. */
    public static final Color SELEZIONE = new Color(0xD84A3A);

    /** Il nero della dissolvenza fra una zona e l'altra. */
    public static final Color VELO = new Color(0x000000);

    private Tavolozza() {
    }
}
