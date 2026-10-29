package gdr.incontri;

import gdr.battaglia.Creatura;
import gdr.nucleo.Casuale;

import java.util.Objects;
import java.util.Optional;

/**
 * Decide se un passo nell'erba alta fa scattare un incontro.
 *
 * Tiene il conto dei passi dall'ultimo incontro e ne impone alcuni di tregua:
 * senza questa regola, uscendo da una battaglia il passo successivo puo' farne
 * partire subito un'altra, e il gioco sembra rotto.
 *
 * Va interpellato una volta per ogni passo concluso, anche fuori dall'erba:
 * anche camminare sul sentiero consuma la tregua. L'unico passo che non conta
 * e' quello che finisce su un passaggio fra due zone, perche' quella casella
 * non viene mai annunciata come passo: si attraversa, non ci si cammina.
 */
public final class GeneratoreIncontri {

    /**
     * Dopo un incontro servono almeno tanti passi perche' possa scattarne un
     * altro: i due successivi sono sicuri, il terzo puo' gia' farne partire uno.
     */
    private static final int PASSI_DI_TREGUA = 3;

    private int passiDallUltimoIncontro;

    public GeneratoreIncontri() {
        // Si parte gia' fuori dalla tregua: il primo passo nell'erba alta puo'
        // far scattare un incontro.
        this.passiDallUltimoIncontro = PASSI_DI_TREGUA;
    }

    /**
     * Valuta un passo appena concluso.
     *
     * @param erbaAlta se la casella raggiunta genera incontri.
     * @return la creatura selvatica comparsa, se l'incontro e' scattato.
     */
    public Optional<Creatura> valuta(final boolean erbaAlta,
                                     final TabellaIncontri tabella,
                                     final Casuale casuale) {
        Objects.requireNonNull(tabella, "La tabella non puo' essere nulla");
        Objects.requireNonNull(casuale, "La sorgente casuale non puo' essere nulla");

        passiDallUltimoIncontro++;

        if (!erbaAlta || tabella.vuota()) {
            return Optional.empty();
        }
        if (passiDallUltimoIncontro < PASSI_DI_TREGUA) {
            return Optional.empty();
        }
        if (!casuale.accade(tabella.probabilitaPerPasso())) {
            return Optional.empty();
        }

        passiDallUltimoIncontro = 0;
        return Optional.of(tabella.sorteggia(casuale));
    }
}
