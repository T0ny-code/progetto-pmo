package gdr.battaglia;

import java.util.Arrays;

/**
 * I quattro elementi del gioco e la loro tabella di efficacia.
 *
 * Il triangolo classico e' fuoco batte erba, erba batte acqua, acqua batte
 * fuoco. L'elettro batte l'acqua e per il resto e' neutro.
 *
 * L'elettro non ha debolezze, e la scelta e' voluta anche se la tabella ne
 * esce asimmetrica. Prima l'erba batteva l'elettro ed era resistente ai suoi
 * attacchi, cosi' l'elemento aveva il suo contro. Il problema e' che l'unica
 * creatura del giocatore e' d'elettro e attacca solo con quell'elemento,
 * mentre nessuna creatura selvatica e' d'elettro: quella debolezza non
 * bilanciava niente, puniva solo il giocatore. Contro Verzino, d'erba,
 * sommava meta' danno inflitto e danno doppio subito, e le battaglie simulate
 * davano zero vittorie su ventimila: non uno scontro difficile, uno scontro
 * impossibile.
 *
 * Togliendo le due regole, e riportando le statistiche di Verzino in linea con
 * le altre specie, la stessa misura da' il 75% di vittorie in sette turni e
 * mezzo sui livelli con cui il bosco lo fa comparire: resta lo scontro piu'
 * lungo del gioco, ma si puo' vincere. Il livello pesa molto — al 4 si vince
 * sempre, al 5 nel 73% dei casi, al 6 e' una monetina — e questo e' voluto:
 * un Verzino di livello 6 e' il momento in cui scappare diventa la scelta
 * giusta.
 *
 * Se un giorno si aggiunge un quinto elemento, la regola da rispettare e' che
 * abbia almeno un bersaglio; la debolezza serve solo se quell'elemento puo'
 * finire sia al giocatore sia agli avversari.
 */
public enum Elemento {

    FUOCO,
    ACQUA,
    ERBA,
    ELETTRO;

    /** Moltiplicatore per un attacco super efficace. */
    private static final double SUPER_EFFICACE = 2.0;

    /** Moltiplicatore per un attacco poco efficace. */
    private static final double POCO_EFFICACE = 0.5;

    /** Moltiplicatore neutro. */
    public static final double NEUTRO = 1.0;

    private static final double[][] TABELLA = new double[values().length][values().length];

    static {
        for (final double[] riga : TABELLA) {
            Arrays.fill(riga, NEUTRO);
        }

        forte(FUOCO, ERBA);
        debole(FUOCO, ACQUA);

        forte(ACQUA, FUOCO);
        debole(ACQUA, ERBA);

        forte(ERBA, ACQUA);
        debole(ERBA, FUOCO);

        forte(ELETTRO, ACQUA);
    }

    /** Il moltiplicatore di danno di questo elemento contro un difensore. */
    public double efficaciaContro(final Elemento difensore) {
        return TABELLA[this.ordinal()][difensore.ordinal()];
    }

    private static void forte(final Elemento attaccante, final Elemento difensore) {
        TABELLA[attaccante.ordinal()][difensore.ordinal()] = SUPER_EFFICACE;
    }

    private static void debole(final Elemento attaccante, final Elemento difensore) {
        TABELLA[attaccante.ordinal()][difensore.ordinal()] = POCO_EFFICACE;
    }
}
