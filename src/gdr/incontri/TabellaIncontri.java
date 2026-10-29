package gdr.incontri;

import gdr.battaglia.Creatura;
import gdr.nucleo.Casuale;

import java.util.List;
import java.util.Objects;

/**
 * La fauna di una zona: quali creature vi si incontrano e quanto spesso.
 *
 * La probabilita' e' per passo compiuto nell'erba alta, non al secondo: cosi'
 * non dipende da quanto e' veloce la camminata ne' da quanti fotogrammi al
 * secondo gira il gioco.
 */
public record TabellaIncontri(List<VoceIncontro> voci, double probabilitaPerPasso) {

    public TabellaIncontri {
        Objects.requireNonNull(voci, "Le voci non possono essere nulle");
        if (probabilitaPerPasso < 0.0 || probabilitaPerPasso > 1.0) {
            throw new IllegalArgumentException(
                    "La probabilita' va da 0 a 1, ricevuta " + probabilitaPerPasso);
        }
        voci = List.copyOf(voci);
    }

    /** Vero se non c'e' nessuna creatura da incontrare. */
    public boolean vuota() {
        return voci.isEmpty() || probabilitaPerPasso <= 0.0;
    }

    /**
     * Estrae una creatura selvatica: prima la specie, con la stessa
     * probabilita' per tutte le voci, poi il livello nell'intervallo di quella
     * uscita.
     *
     * Va chiamata solo su una tabella non vuota: chi decide se ci sara' un
     * incontro e' GeneratoreIncontri, e la vuota l'ha gia' scartata.
     */
    public Creatura sorteggia(final Casuale casuale) {
        final VoceIncontro voce = voci.get(casuale.intero(voci.size()));
        final int livello = voce.livelloMinimo() + casuale.intero(voce.livelliPossibili());
        return new Creatura(voce.specie(), livello);
    }
}
