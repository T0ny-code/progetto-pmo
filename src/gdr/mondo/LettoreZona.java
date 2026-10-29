package gdr.mondo;

import gdr.incontri.TabellaIncontri;

import java.util.List;
import java.util.Objects;

/**
 * Costruisce una Zona da una mappa testuale, un carattere per casella.
 *
 * Tenere le mappe come testo le rende leggibili e correggibili a occhio, e
 * permette di versionarle senza allegare file binari al progetto.
 */
public final class LettoreZona {

    private LettoreZona() {
    }

    /**
     * @param righe una stringa per riga della mappa, tutte della stessa lunghezza.
     */
    public static Zona leggi(final String identificatore,
                             final List<String> righe,
                             final List<Passaggio> passaggi,
                             final TabellaIncontri tabellaIncontri) {
        Objects.requireNonNull(righe, "Le righe della mappa non possono essere nulle");

        if (righe.isEmpty()) {
            throw new IllegalArgumentException("La mappa di " + identificatore + " e' vuota");
        }

        final int larghezza = righe.get(0).length();
        if (larghezza == 0) {
            throw new IllegalArgumentException(
                    "La mappa di " + identificatore + " ha la prima riga vuota");
        }

        final TipoTile[][] griglia = new TipoTile[righe.size()][larghezza];
        for (int riga = 0; riga < righe.size(); riga++) {
            final String testo = righe.get(riga);
            if (testo.length() != larghezza) {
                throw new IllegalArgumentException("La mappa di " + identificatore
                        + " ha la riga " + riga + " lunga " + testo.length()
                        + " invece di " + larghezza);
            }
            for (int colonna = 0; colonna < larghezza; colonna++) {
                final char simbolo = testo.charAt(colonna);
                try {
                    griglia[riga][colonna] = TipoTile.daSimbolo(simbolo);
                } catch (final IllegalArgumentException errore) {
                    throw new IllegalArgumentException("Nella mappa di " + identificatore
                            + ", a (" + colonna + ", " + riga + "): " + errore.getMessage(),
                            errore);
                }
            }
        }

        return new Zona(identificatore, griglia, passaggi, tabellaIncontri);
    }
}
