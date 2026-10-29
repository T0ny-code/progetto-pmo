package gdr.prova;

import gdr.nucleo.Casuale;

/**
 * Una sorgente casuale che non e' casuale: restituisce i valori che le si
 * dicono, in ordine.
 *
 * E' il motivo per cui la logica riceve un {@link Casuale} dall'esterno invece
 * di chiamare Math.random: cosi' una battaglia o un incontro si possono far
 * accadere esattamente come serve alla prova, e il risultato e' sempre lo
 * stesso a ogni esecuzione.
 *
 * Quando i valori finiscono, l'ultimo viene ripetuto all'infinito: cosi' una
 * prova deve elencare solo i tiri che le interessano davvero.
 */
public final class CasualeDiProva implements Casuale {

    /**
     * Il valore piu' alto che {@link #frazione()} puo' restituire.
     *
     * Il contratto dice che la frazione sta in [0, 1): restituire 1,0 sarebbe
     * un aiutante che viola l'interfaccia che sta simulando.
     */
    private static final double QUASI_UNO = 0.999_999_999;

    private final double[] frazioni;
    private final int[] interi;

    private int prossimaFrazione;
    private int prossimoIntero;

    public CasualeDiProva(final double[] frazioni, final int[] interi) {
        if (frazioni.length == 0 || interi.length == 0) {
            throw new IllegalArgumentException("Servono almeno un valore per tipo");
        }
        this.frazioni = frazioni.clone();
        this.interi = interi.clone();
    }

    /** Ogni probabilita' si avvera e ogni sorteggio da' il primo elemento. */
    public static CasualeDiProva sempre() {
        return new CasualeDiProva(new double[] {0.0}, new int[] {0});
    }

    /** Nessuna probabilita' si avvera; i sorteggi danno il primo elemento. */
    public static CasualeDiProva mai() {
        return new CasualeDiProva(new double[] {QUASI_UNO}, new int[] {0});
    }

    /** Detta la sequenza delle frazioni; i sorteggi danno il primo elemento. */
    public static CasualeDiProva conFrazioni(final double... frazioni) {
        return new CasualeDiProva(frazioni, new int[] {0});
    }

    /** Detta la sequenza dei sorteggi; ogni probabilita' si avvera. */
    public static CasualeDiProva conInteri(final int... interi) {
        return new CasualeDiProva(new double[] {0.0}, interi);
    }

    @Override
    public double frazione() {
        final double valore = frazioni[prossimaFrazione];
        if (prossimaFrazione < frazioni.length - 1) {
            prossimaFrazione++;
        }
        return valore;
    }

    @Override
    public int intero(final int limiteEsclusivo) {
        if (limiteEsclusivo <= 0) {
            throw new IllegalArgumentException(
                    "Il limite deve essere positivo, ricevuto " + limiteEsclusivo);
        }
        final int valore = interi[prossimoIntero];
        if (prossimoIntero < interi.length - 1) {
            prossimoIntero++;
        }
        if (valore >= limiteEsclusivo) {
            throw new IllegalStateException("La prova ha chiesto il valore " + valore
                    + " ma il limite e' " + limiteEsclusivo
                    + ": la sequenza non corrisponde a cio' che il codice sorteggia");
        }
        return valore;
    }
}
