package gdr.battaglia;

/**
 * Cosa e' accaduto durante un turno di battaglia.
 *
 * E' il canale che va dalla logica alla vista. La battaglia risolve il turno
 * tutto insieme e restituisce questi eventi in ordine cronologico: al ritorno
 * lo stato e' gia' quello finale, PS aggiornati e esito noto. Sta poi alla
 * vista consumarli uno alla volta con le sue durate, tenendo dei valori
 * "mostrati" distinti da quelli reali, cosi' la barra dei PS scende
 * gradualmente verso un numero che la logica ha gia' fissato.
 *
 * Gli eventi portano nomi e numeri, non frasi: la lingua sta nella vista.
 */
public sealed interface EventoBattaglia {

    /** Un avversario selvatico e' comparso. */
    record Comparsa(String nomeCreatura, int livello) implements EventoBattaglia { }

    /** Qualcuno ha usato una mossa. */
    record MossaUsata(boolean delGiocatore, String nomeCreatura, String nomeMossa)
            implements EventoBattaglia { }

    /** La mossa e' andata a vuoto. */
    record MossaMancata(boolean delGiocatore, String nomeCreatura)
            implements EventoBattaglia { }

    /** L'elemento della mossa era vantaggioso o svantaggioso. */
    record EfficaciaRilevata(double moltiplicatore) implements EventoBattaglia { }

    /** Una creatura ha subito danno. */
    record DannoSubito(boolean alGiocatore, int psRimanenti) implements EventoBattaglia { }

    /** Una creatura e' andata esausta. */
    record CreaturaEsausta(boolean delGiocatore, String nomeCreatura)
            implements EventoBattaglia { }

    /** La fuga e' riuscita. */
    record FugaRiuscita() implements EventoBattaglia { }

    /** La fuga e' fallita: il turno passa all'avversario. */
    record FugaFallita() implements EventoBattaglia { }

    /** La battaglia e' chiusa. */
    record FineBattaglia(Esito esito) implements EventoBattaglia { }
}
