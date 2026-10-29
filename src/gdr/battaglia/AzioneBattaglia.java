package gdr.battaglia;

/**
 * Cosa ha scelto il giocatore in questo turno.
 *
 * E' il solo tipo che va dalla vista alla logica di battaglia: la vista non
 * calcola danni, non decide chi va esausto, non tocca le creature.
 */
public sealed interface AzioneBattaglia {

    /** Usa la mossa in quella posizione del repertorio della creatura attiva. */
    record UsaMossa(int indiceMossa) implements AzioneBattaglia { }

    /** Tenta la fuga. */
    record Fuggi() implements AzioneBattaglia { }
}
