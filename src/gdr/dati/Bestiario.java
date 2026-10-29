package gdr.dati;

import gdr.battaglia.Creatura;
import gdr.battaglia.Elemento;
import gdr.battaglia.Specie;

import java.util.List;

/**
 * Le creature del gioco.
 *
 * Sono invenzioni nostre, nate come forme geometriche semplici: una sfera con
 * un arco elettrico, una bolla con un nucleo, un'onda, un baccello con una
 * foglia, una goccia di cenere. Ora i disegni stanno in src/risorse/creature.png
 * e li ritaglia {@link gdr.grafica.DisegnatoreCreatura}, una colonna per specie
 * nell'ordine in cui quella classe le elenca.
 *
 * Le statistiche base sono quelle al livello 1: ogni specie ha un profilo
 * riconoscibile, cosi' gli scontri non sono tutti uguali.
 */
public final class Bestiario {

    /** Il compagno del giocatore: veloce e fragile. */
    private static final Specie SCINTILLO = new Specie(
            "Scintillo", Elemento.ELETTRO,
            24, 12, 9, 14,
            List.of(Repertorio.SCINTILLA, Repertorio.SAETTA));

    /** Equilibrata, un po' lenta. */
    public static final Specie RUGIADO = new Specie(
            "Rugiado", Elemento.ACQUA,
            26, 10, 11, 9,
            List.of(Repertorio.SPRUZZO, Repertorio.ONDATA));

    /**
     * Seconda creatura d'acqua, dal profilo opposto a Rugiado: veloce e
     * fragile invece che lenta e solida.
     *
     * Esiste perche' con due sole specie nella radura si incontrava quasi
     * sempre la stessa, e la zona di partenza sembrava vuota. Due creature
     * dello stesso elemento non sono un problema: gli scontri si distinguono
     * per statistiche e per sagoma, non solo per la tabella di efficacia.
     */
    public static final Specie MAREZZO = new Specie(
            "Marezzo", Elemento.ACQUA,
            24, 12, 9, 13,
            List.of(Repertorio.SPRUZZO, Repertorio.ONDATA));

    /**
     * La piu' lenta del bestiario, e per questo lo scontro piu' lungo.
     *
     * Le sue mosse non sono le piu' potenti: Radici, la sua piu' forte, fa 60
     * contro i 65 di Ondata e i 70 di Vampata. La battaglia si allunga perche'
     * con velocita' 7 subisce il colpo prima di darlo quasi sempre, non perche'
     * picchi piu' forte: misurata, dura sette turni e mezzo contro i quattro e
     * poco piu' delle creature d'acqua.
     *
     * Aveva 28 PS e 12 di difesa, i valori piu' alti del gruppo. Sommati al
     * vantaggio che l'erba aveva sull'elettro ne facevano una creatura
     * imbattibile invece che difficile, quindi sono scesi in linea con le
     * altre specie.
     */
    public static final Specie VERZINO = new Specie(
            "Verzino", Elemento.ERBA,
            24, 10, 9, 7,
            List.of(Repertorio.FOGLIALAMA, Repertorio.RADICI));

    /** Fragile ma con l'attacco piu' alto. */
    public static final Specie FULIGNO = new Specie(
            "Fuligno", Elemento.FUOCO,
            22, 14, 8, 11,
            List.of(Repertorio.FAVILLA, Repertorio.VAMPATA));

    /** Livello di partenza del compagno del giocatore. */
    private static final int LIVELLO_INIZIALE = 5;

    private Bestiario() {
    }

    /** La creatura con cui si comincia la partita. */
    public static Creatura compagnoIniziale() {
        return new Creatura(SCINTILLO, LIVELLO_INIZIALE);
    }
}
