package gdr.grafica;

import gdr.mondo.Zona;
import gdr.nucleo.Posizione;

/**
 * Che pezzo di edificio e' una singola casella: tetto, muro, finestra, porta.
 *
 * Gli edifici nella mappa testuale sono solo blocchi di E con una P: l'aspetto
 * di ogni casella si ricava contando i vicini. Cosi' non c'e' una seconda
 * struttura dati da tenere allineata alla mappa, e allargare una casa e' solo
 * questione di scrivere piu' E.
 *
 * Due vincoli che le mappe del gioco rispettano e che qui non sono controllati,
 * perche' le mappe sono dati nostri e non arrivano da fuori: un edificio deve
 * essere alto almeno tre caselle, perche' le prime due sono tetto e una porta
 * messa lassu' verrebbe disegnata come tegole; e un ospedale va largo quattro,
 * che e' dove {@link DisegnatoreEdificio} mette la croce.
 *
 * Nonostante stia in gdr.grafica non usa AWT: e' conteggio di vicini, quindi
 * si puo' seguire leggendola.
 */
public record ParteEdificio(int righeSopra,
                            int colonneASinistra,
                            boolean bordoDestro,
                            boolean ultimaRiga,
                            boolean porta,
                            boolean ospedale) {

    /** Quante file in alto sono tetto; sotto comincia il muro. */
    private static final int RIGHE_TETTO = 2;

    /** Vero se questa casella fa parte della falda del tetto. */
    public boolean tetto() {
        return righeSopra < RIGHE_TETTO;
    }

    /** Vero se e' la fila piu' alta: li' va il colmo. */
    public boolean colmo() {
        return righeSopra == 0;
    }

    /** Vero se e' la colonna piu' a sinistra dell'edificio. */
    public boolean bordoSinistro() {
        return colonneASinistra == 0;
    }

    /**
     * Vero se su questo muro va una finestra: colonne alterne, mai sulla
     * porta. Senza questa regola le pareti sembrerebbero muri ciechi.
     */
    public boolean finestra() {
        return !tetto() && !porta && colonneASinistra % 2 == 0;
    }

    /** Ricava la parte di edificio dalla casella e dai suoi vicini. */
    public static ParteEdificio di(final Zona zona, final Posizione posizione) {
        if (!parteDiEdificio(zona, posizione)) {
            throw new IllegalArgumentException(
                    "La casella " + posizione + " non fa parte di un edificio");
        }

        int righeSopra = 0;
        Posizione sopra = new Posizione(posizione.colonna(), posizione.riga() - 1);
        while (parteDiEdificio(zona, sopra)) {
            righeSopra++;
            sopra = new Posizione(sopra.colonna(), sopra.riga() - 1);
        }

        int colonneASinistra = 0;
        Posizione sinistra = new Posizione(posizione.colonna() - 1, posizione.riga());
        while (parteDiEdificio(zona, sinistra)) {
            colonneASinistra++;
            sinistra = new Posizione(sinistra.colonna() - 1, sinistra.riga());
        }

        final boolean bordoDestro = !parteDiEdificio(zona,
                new Posizione(posizione.colonna() + 1, posizione.riga()));
        final boolean ultimaRiga = !parteDiEdificio(zona,
                new Posizione(posizione.colonna(), posizione.riga() + 1));

        return new ParteEdificio(righeSopra, colonneASinistra, bordoDestro, ultimaRiga,
                zona.tipo(posizione).porta(), zona.tipo(posizione).ospedale());
    }

    /** Vero se la casella esiste ed e' muro o porta di un edificio. */
    public static boolean parteDiEdificio(final Zona zona, final Posizione posizione) {
        if (!zona.dentro(posizione)) {
            return false;
        }
        return zona.tipo(posizione).parteDiEdificio();
    }
}
