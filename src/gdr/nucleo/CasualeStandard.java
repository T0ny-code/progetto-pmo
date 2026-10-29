package gdr.nucleo;

import java.util.Random;

/** Sorgente casuale del gioco, basata su java.util.Random. */
public final class CasualeStandard implements Casuale {

    private final Random sorgente;

    /** La sorgente del gioco. */
    public CasualeStandard() {
        this.sorgente = new Random();
    }

    @Override
    public int intero(final int limiteEsclusivo) {
        if (limiteEsclusivo <= 0) {
            throw new IllegalArgumentException(
                    "Il limite deve essere positivo, ricevuto " + limiteEsclusivo);
        }
        return sorgente.nextInt(limiteEsclusivo);
    }

    @Override
    public double frazione() {
        return sorgente.nextDouble();
    }
}
