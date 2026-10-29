package gdr.grafica;

import gdr.battaglia.Specie;

import java.awt.Graphics2D;
import java.util.List;

/**
 * Disegna una creatura prendendola dal foglio di sprite.
 *
 * Il foglio e' src/risorse/creature.png, 160x32 pixel divisi in cinque
 * riquadri da 32x32, uno per specie, nell'ordine dichiarato in NOMI qui
 * sotto. Aggiungere una specie vuol dire aggiungere un riquadro in coda al
 * foglio e il suo nome in fondo alla lista: le due cose vanno tenute
 * allineate, ed e' il solo punto in cui serve ricordarsene.
 *
 * L'abbinamento e' per nome e non per posizione nel Bestiario, cosi'
 * riordinare le costanti del Bestiario non cambia quale sprite compare.
 *
 * Il parametro tempo serve a far ondeggiare la creatura di un pixel: con uno
 * sprite fermo la schermata di battaglia sembra un'immagine morta. Il vecchio
 * codice di disegno animava i singoli dettagli (pinne, nucleo, scintille);
 * con le immagini quell'animazione si otterrebbe solo aggiungendo altri
 * riquadri al foglio, e un'ondeggiata intera costa zero.
 *
 * Le immagini attuali sono segnaposto generati dal vecchio codice di disegno
 * e vanno sostituite con i disegni definitivi senza toccare questa classe.
 */
public final class DisegnatoreCreatura {

    /** Lato del riquadro di una creatura, in pixel logici. */
    public static final int LATO = 32;

    /** L'ordine dei riquadri nel foglio: la posizione qui e' la colonna la'. */
    private static final List<String> NOMI =
            List.of("Scintillo", "Rugiado", "Marezzo", "Verzino", "Fuligno");

    /** Quanto ondeggia la creatura, in pixel. */
    private static final double AMPIEZZA_ONDEGGIO = 1.0;

    /** Quanto e' veloce l'ondeggio, in radianti al secondo. */
    private static final double VELOCITA_ONDEGGIO = 1.8;

    private DisegnatoreCreatura() {
    }

    /**
     * @param x     angolo sinistro del riquadro 32x32 in pixel logici.
     * @param y     angolo superiore del riquadro 32x32 in pixel logici.
     * @param tempo secondi trascorsi, per l'ondeggio.
     */
    public static void disegna(final Graphics2D g, final Specie specie,
                               final int x, final int y, final double tempo) {
        final int colonna = NOMI.indexOf(specie.nome());
        if (colonna < 0) {
            throw new IllegalArgumentException(
                    "Nessuno sprite per la specie " + specie.nome()
                    + ". Aggiungi un riquadro a risorse/creature.png e il nome"
                    + " in fondo a DisegnatoreCreatura.NOMI.");
        }
        final int scostamento =
                (int) Math.round(Math.sin(tempo * VELOCITA_ONDEGGIO) * AMPIEZZA_ONDEGGIO);
        FoglioSprite.carica("creature.png", LATO)
                .disegna(g, colonna, 0, x, y + scostamento);
    }
}
