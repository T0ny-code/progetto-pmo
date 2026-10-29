package gdr.battaglia;

import gdr.nucleo.Casuale;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * La macchina a turni del combattimento.
 *
 * E' sincrona e istantanea: riceve l'azione del giocatore, decide quella
 * dell'avversario, ordina per velocita', applica tutto e restituisce la lista
 * ordinata degli eventi. Non conosce millisecondi, non attende, non produce
 * frasi da mostrare e non chiama mai un ridisegno: e' la vista che prende
 * quella lista e la mette in scena con i suoi tempi.
 *
 * Il giocatore ha una creatura sola, quindi non esiste sostituzione: se
 * viene stesa, la battaglia e' persa.
 */
public final class Battaglia {

    /** Probabilita' di fuga a parita' di velocita'. */
    private static final double FUGA_BASE = 0.5;

    private static final double FUGA_MASSIMA = 0.95;

    private final Creatura compagno;
    private final Creatura selvatica;
    private final Casuale casuale;

    private Esito esito;

    public Battaglia(final Creatura compagno, final Creatura selvatica, final Casuale casuale) {
        this.compagno = Objects.requireNonNull(compagno, "Il compagno non puo' essere nullo");
        this.selvatica = Objects.requireNonNull(selvatica, "L'avversario non puo' essere nullo");
        this.casuale = Objects.requireNonNull(casuale, "La sorgente casuale non puo' essere nulla");

        if (compagno.esausta()) {
            throw new IllegalArgumentException(
                    "Non si puo' iniziare una battaglia con il compagno gia' steso");
        }
        if (selvatica.esausta()) {
            throw new IllegalArgumentException(
                    "Non si puo' iniziare una battaglia contro una creatura esausta");
        }
    }

    public Creatura selvatica() {
        return selvatica;
    }

    /** La creatura del giocatore che sta combattendo. */
    public Creatura attiva() {
        return compagno;
    }

    public boolean finita() {
        return esito != null;
    }

    /** Come e' finita; solo a battaglia conclusa. */
    public Esito esito() {
        if (esito == null) {
            throw new IllegalStateException("La battaglia non e' ancora finita");
        }
        return esito;
    }

    /**
     * Annuncia l'avversario. Va chiamato una volta, prima del primo turno:
     * chiamarlo due volte annuncerebbe due volte la stessa comparsa.
     */
    public List<EventoBattaglia> inizia() {
        return List.of(new EventoBattaglia.Comparsa(selvatica.nome(), selvatica.livello()));
    }

    /**
     * Risolve un turno e restituisce cosa e' accaduto.
     *
     * Al ritorno lo stato e' gia' quello finale: PS aggiornati, creature esauste
     * marcate, esito noto se la battaglia e' chiusa.
     */
    public List<EventoBattaglia> esegui(final AzioneBattaglia azione) {
        Objects.requireNonNull(azione, "L'azione non puo' essere nulla");
        if (finita()) {
            throw new IllegalStateException("La battaglia e' finita: nessuna azione possibile");
        }

        final List<EventoBattaglia> eventi = new ArrayList<>();

        switch (azione) {
            case AzioneBattaglia.Fuggi ignorato -> {
                if (tentaFuga()) {
                    eventi.add(new EventoBattaglia.FugaRiuscita());
                    concludi(eventi, Esito.FUGA);
                    return eventi;
                }
                eventi.add(new EventoBattaglia.FugaFallita());
                turnoAvversario(eventi);
            }
            case AzioneBattaglia.UsaMossa usaMossa -> eseguiScambioDiColpi(usaMossa, eventi);
        }

        return eventi;
    }

    private void eseguiScambioDiColpi(final AzioneBattaglia.UsaMossa scelta,
                                      final List<EventoBattaglia> eventi) {
        final List<Mossa> repertorio = attiva().mosse();
        if (scelta.indiceMossa() < 0 || scelta.indiceMossa() >= repertorio.size()) {
            throw new IndexOutOfBoundsException(
                    "Mossa inesistente: " + scelta.indiceMossa());
        }
        final Mossa mossa = repertorio.get(scelta.indiceMossa());

        // A parita' di velocita' tocca al giocatore: e' la convenzione che
        // sorprende meno chi gioca.
        if (attiva().velocita() >= selvatica.velocita()) {
            attacca(attiva(), selvatica, mossa, true, eventi);
            turnoAvversario(eventi);
        } else {
            turnoAvversario(eventi);
            if (!finita()) {
                attacca(attiva(), selvatica, mossa, true, eventi);
            }
        }
    }

    private void turnoAvversario(final List<EventoBattaglia> eventi) {
        if (finita()) {
            return;
        }
        attacca(selvatica, attiva(), mossaDellAvversario(), false, eventi);
    }

    /**
     * L'avversario sceglie una mossa a caso fra quelle che conosce.
     *
     * Volutamente non e' un'intelligenza: una scelta casuale
     * non fa sembrare il gioco truccato, ed e' anche l'unico punto da
     * toccare se un giorno servisse della strategia.
     */
    private Mossa mossaDellAvversario() {
        final List<Mossa> repertorio = selvatica.mosse();
        return repertorio.get(casuale.intero(repertorio.size()));
    }

    private void attacca(final Creatura attaccante,
                         final Creatura difensore,
                         final Mossa mossa,
                         final boolean delGiocatore,
                         final List<EventoBattaglia> eventi) {
        if (!casuale.accade(mossa.probabilitaDiColpire())) {
            eventi.add(new EventoBattaglia.MossaMancata(delGiocatore, attaccante.nome()));
            return;
        }

        eventi.add(new EventoBattaglia.MossaUsata(
                delGiocatore, attaccante.nome(), mossa.nome()));

        final CalcolatoreDanno.Risultato risultato =
                CalcolatoreDanno.calcola(attaccante, difensore, mossa, casuale);
        difensore.subisci(risultato.danno());

        if (risultato.efficacia() != Elemento.NEUTRO) {
            eventi.add(new EventoBattaglia.EfficaciaRilevata(risultato.efficacia()));
        }
        eventi.add(new EventoBattaglia.DannoSubito(!delGiocatore, difensore.psAttuali()));

        if (difensore.esausta()) {
            eventi.add(new EventoBattaglia.CreaturaEsausta(!delGiocatore, difensore.nome()));
            concludi(eventi, delGiocatore ? Esito.VITTORIA : Esito.SCONFITTA);
        }
    }

    private void concludi(final List<EventoBattaglia> eventi, final Esito esitoFinale) {
        this.esito = esitoFinale;
        eventi.add(new EventoBattaglia.FineBattaglia(esitoFinale));
    }

    /**
     * Piu' si e' veloci dell'avversario, piu' e' facile scappare, con un
     * massimo perche' la fuga non sia mai garantita. Non serve un minimo: con
     * FUGA_BASE a 0,5 e le velocita' del bestiario la probabilita' non scende
     * sotto il 47%.
     */
    private boolean tentaFuga() {
        final double rapporto = (double) attiva().velocita() / selvatica.velocita();
        final double probabilita = Math.min(FUGA_MASSIMA, FUGA_BASE * rapporto);
        return casuale.accade(probabilita);
    }
}
