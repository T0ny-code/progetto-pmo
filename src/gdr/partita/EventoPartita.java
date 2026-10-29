package gdr.partita;

/**
 * Cosa e' cambiato nella partita in questo aggiornamento.
 *
 * Sono i soli fatti che interessano alla vista per cambiare schermata,
 * accendere una dissolvenza o scrivere un messaggio. Gli eventi di dettaglio
 * del movimento restano in {@link EventoEsplorazione}.
 */
public sealed interface EventoPartita {

    /** Si e' passati in un'altra zona. */
    record ZonaCambiata() implements EventoPartita { }

    /** Un incontro casuale ha aperto una battaglia. */
    record BattagliaIniziata() implements EventoPartita { }

    /** Il personaggio e' entrato nell'ospedale. */
    record IngressoOspedale() implements EventoPartita { }

    /** Il personaggio e' uscito dall'ospedale con la creatura curata. */
    record UscitaOspedale() implements EventoPartita { }

    /** Creatura stesa: il giocatore e' tornato al punto di partenza. */
    record SvenutoRiportatoACasa() implements EventoPartita { }
}
