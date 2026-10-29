package gdr.dati;

import gdr.battaglia.Elemento;
import gdr.battaglia.Mossa;

/**
 * Le mosse del gioco.
 *
 * Ogni elemento ha due mosse: una debole ma sicura e una potente ma rischiosa.
 * Non esiste un elemento "normale", quindi anche una creatura con un solo
 * elemento ha una scelta tattica vera da fare a ogni turno.
 *
 * Non ci sono punti uso: le mosse si usano quante volte si vuole.
 *
 * Nomi e numeri sono nostri.
 */
public final class Repertorio {

    // Elettro
    public static final Mossa SCINTILLA = new Mossa("Scintilla", Elemento.ELETTRO, 40, 100);
    public static final Mossa SAETTA = new Mossa("Saetta", Elemento.ELETTRO, 65, 80);

    // Acqua
    public static final Mossa SPRUZZO = new Mossa("Spruzzo", Elemento.ACQUA, 40, 100);
    public static final Mossa ONDATA = new Mossa("Ondata", Elemento.ACQUA, 65, 80);

    // Erba
    public static final Mossa FOGLIALAMA = new Mossa("Foglialama", Elemento.ERBA, 45, 95);
    public static final Mossa RADICI = new Mossa("Radici", Elemento.ERBA, 60, 85);

    // Fuoco
    public static final Mossa FAVILLA = new Mossa("Favilla", Elemento.FUOCO, 40, 100);
    public static final Mossa VAMPATA = new Mossa("Vampata", Elemento.FUOCO, 70, 75);

    private Repertorio() {
    }
}
