package gdr.mondo;

import gdr.incontri.TabellaIncontri;
import gdr.nucleo.Posizione;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Una mappa esterna: griglia di terreni piu' i passaggi che la collegano ad
 * altre zone.
 *
 * E' immutabile dopo la costruzione e non conosce ne' pixel ne' Swing.
 */
public final class Zona {

    private final String identificatore;
    private final TipoTile[][] griglia;
    private final List<Passaggio> passaggi;
    private final TabellaIncontri tabellaIncontri;

    /**
     * Visibile solo nel package: le zone si costruiscono con
     * {@link LettoreZona#leggi}, che e' quello che valida la mappa. Tenerlo
     * riservato e' cio' che permette di non ricontrollare qui la griglia.
     *
     * @param griglia indicizzata come griglia[riga][colonna]; viene copiata.
     */
    Zona(final String identificatore,
         final TipoTile[][] griglia,
         final List<Passaggio> passaggi,
         final TabellaIncontri tabellaIncontri) {
        if (identificatore == null || identificatore.isBlank()) {
            throw new IllegalArgumentException("L'identificatore della zona non puo' essere vuoto");
        }
        Objects.requireNonNull(griglia, "La griglia non puo' essere nulla");
        Objects.requireNonNull(passaggi, "La lista dei passaggi non puo' essere nulla");

        // La griglia arriva sempre da LettoreZona, che la costruisce
        // rettangolare e piena e segnala da solo mappe storte o simboli
        // sconosciuti: qui non si ricontrolla, si copia soltanto. Riga per
        // riga, perche' clone() su un array bidimensionale copierebbe solo le
        // righe esterne lasciandone condivise le interne.
        this.griglia = new TipoTile[griglia.length][];
        for (int riga = 0; riga < griglia.length; riga++) {
            this.griglia[riga] = griglia[riga].clone();
        }

        this.identificatore = identificatore;
        this.passaggi = List.copyOf(passaggi);
        this.tabellaIncontri =
                Objects.requireNonNull(tabellaIncontri, "La tabella incontri non puo' essere nulla");

        for (final Passaggio passaggio : this.passaggi) {
            if (!dentro(passaggio.innesco())) {
                throw new IllegalArgumentException("Il passaggio di " + identificatore
                        + " e' innescato fuori dalla zona, in " + passaggio.innesco());
            }
            if (tipo(passaggio.innesco()) != TipoTile.PASSAGGIO) {
                throw new IllegalArgumentException("Il passaggio di " + identificatore + " in "
                        + passaggio.innesco() + " non poggia su una casella di passaggio");
            }
        }
    }

    public String identificatore() {
        return identificatore;
    }

    public int larghezza() {
        return griglia[0].length;
    }

    public int altezza() {
        return griglia.length;
    }

    /** Vero se la posizione cade dentro i confini della griglia. */
    public boolean dentro(final Posizione posizione) {
        return posizione.colonna() >= 0 && posizione.colonna() < larghezza()
                && posizione.riga() >= 0 && posizione.riga() < altezza();
    }

    /** Il terreno in una casella; la posizione deve essere dentro la zona. */
    public TipoTile tipo(final Posizione posizione) {
        if (!dentro(posizione)) {
            throw new IndexOutOfBoundsException(
                    "Posizione " + posizione + " fuori dalla zona " + identificatore);
        }
        return griglia[posizione.riga()][posizione.colonna()];
    }

    /** Vero se il giocatore puo' entrare in quella casella (falso se e' fuori). */
    public boolean calpestabile(final Posizione posizione) {
        return dentro(posizione) && tipo(posizione).calpestabile();
    }

    /** Vero se quella casella puo' far scattare un incontro (falso se e' fuori). */
    public boolean generaIncontri(final Posizione posizione) {
        return dentro(posizione) && tipo(posizione).generaIncontri();
    }

    /** Il passaggio innescato da quella casella, se ce n'e' uno. */
    public Optional<Passaggio> passaggioIn(final Posizione posizione) {
        for (final Passaggio passaggio : passaggi) {
            if (passaggio.innesco().equals(posizione)) {
                return Optional.of(passaggio);
            }
        }
        return Optional.empty();
    }

    /** I passaggi della zona, in sola lettura. */
    public List<Passaggio> passaggi() {
        return passaggi;
    }

    /** La fauna che si incontra nell'erba alta di questa zona. */
    public TabellaIncontri tabellaIncontri() {
        return tabellaIncontri;
    }
}
