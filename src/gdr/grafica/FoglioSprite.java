package gdr.grafica;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * Un foglio di sprite: una sola immagine divisa in riquadri tutti uguali.
 *
 * I fogli stanno in src/risorse/ e vengono caricati dal classpath, non dal
 * disco: cosi' funzionano allo stesso modo lanciati da Eclipse, da uno script
 * o da dentro un jar, senza dipendere da quale sia la cartella di lavoro.
 *
 * Ogni foglio viene letto una volta sola e tenuto in memoria: i riquadri si
 * chiedono per riga e colonna, e disegnarli e' una copia di pixel.
 *
 * Se un foglio manca il gioco non parte, e il messaggio dice quale file
 * cercava. E' voluto: un segnaposto silenzioso al posto di uno sprite
 * mancante si nota solo quando qualcuno guarda lo schermo, un'eccezione
 * all'avvio si nota subito.
 */
public final class FoglioSprite {

    /** I fogli gia' caricati, per non rileggere lo stesso file due volte. */
    private static final Map<String, FoglioSprite> CARICATI = new HashMap<>();

    private final String nome;
    private final BufferedImage immagine;
    private final int lato;
    private final int colonne;
    private final int righe;

    private FoglioSprite(final String nome, final BufferedImage immagine, final int lato) {
        this.nome = nome;
        this.immagine = immagine;
        this.lato = lato;
        this.colonne = immagine.getWidth() / lato;
        this.righe = immagine.getHeight() / lato;
    }

    /**
     * Carica un foglio da src/risorse/, o lo restituisce se gia' letto.
     *
     * @param nome nome del file, per esempio "personaggio.png".
     * @param lato lato in pixel di un singolo riquadro; deve essere lo stesso
     *             a ogni chiamata per lo stesso file.
     */
    public static FoglioSprite carica(final String nome, final int lato) {
        if (lato <= 0) {
            throw new IllegalArgumentException(
                    "Il lato del riquadro deve essere positivo, ricevuto " + lato);
        }

        final FoglioSprite foglio = CARICATI.computeIfAbsent(nome, chiave -> leggi(chiave, lato));

        // La cache e' per nome di file, non per nome e lato: chiedere lo stesso
        // foglio con un lato diverso restituirebbe quello di prima e
        // ritaglierebbe i riquadri sbagliati, in silenzio. Meglio fermarsi.
        if (foglio.lato != lato) {
            throw new IllegalArgumentException("Il foglio " + nome + " e' gia' stato letto"
                    + " a riquadri da " + foglio.lato + " pixel, non da " + lato);
        }
        return foglio;
    }

    private static FoglioSprite leggi(final String nome, final int lato) {
        final String percorso = "/risorse/" + nome;
        try (InputStream flusso = FoglioSprite.class.getResourceAsStream(percorso)) {
            if (flusso == null) {
                throw new IllegalStateException(
                        "Foglio di sprite non trovato: " + percorso
                        + ". Deve stare in src/risorse/ ed essere copiato accanto"
                        + " alle classi compilate.");
            }
            final BufferedImage immagine = ImageIO.read(flusso);
            if (immagine == null) {
                throw new IllegalStateException(
                        "Il file " + percorso + " non e' un'immagine leggibile.");
            }
            if (immagine.getWidth() % lato != 0 || immagine.getHeight() % lato != 0) {
                throw new IllegalStateException(
                        "Il foglio " + percorso + " e' " + immagine.getWidth() + "x"
                        + immagine.getHeight() + ", che non e' divisibile in riquadri da "
                        + lato + " pixel.");
            }
            return new FoglioSprite(nome, immagine, lato);
        } catch (final IOException errore) {
            throw new IllegalStateException(
                    "Non riesco a leggere " + percorso + ": " + errore.getMessage(), errore);
        }
    }

    /**
     * Disegna un riquadro del foglio con l'angolo in alto a sinistra in (x, y).
     *
     * @param colonna indice orizzontale del riquadro, da 0.
     * @param riga    indice verticale del riquadro, da 0.
     */
    public void disegna(final Graphics2D g, final int colonna, final int riga,
                        final int x, final int y) {
        if (colonna < 0 || colonna >= colonne || riga < 0 || riga >= righe) {
            throw new IndexOutOfBoundsException(
                    "Riquadro (" + colonna + ", " + riga + ") fuori dal foglio " + nome
                    + ", che e' " + colonne + "x" + righe + " riquadri.");
        }
        final int sorgenteX = colonna * lato;
        final int sorgenteY = riga * lato;
        g.drawImage(immagine,
                x, y, x + lato, y + lato,
                sorgenteX, sorgenteY, sorgenteX + lato, sorgenteY + lato,
                null);
    }
}
