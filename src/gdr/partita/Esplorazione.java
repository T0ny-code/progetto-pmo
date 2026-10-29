package gdr.partita;

import gdr.entita.Giocatore;
import gdr.entita.MovimentoGriglia;
import gdr.mondo.Mondo;
import gdr.mondo.Passaggio;
import gdr.mondo.Zona;
import gdr.nucleo.Direzione;
import gdr.nucleo.Posizione;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Lega il movimento del giocatore alla mappa: decide se un passo e' lecito e
 * controlla cosa c'e' sulla casella raggiunta.
 *
 * Non conosce ne' Swing ne' i millisecondi dell'animazione: riceve un passo
 * temporale in secondi e il comando premuto, e restituisce la lista di cio'
 * che e' successo.
 */
public final class Esplorazione {

    private final Mondo mondo;
    private final Giocatore giocatore;

    /**
     * La porta contro cui si sta premendo, per non riannunciarla a ogni
     * aggiornamento. Nulla quando non si sta toccando nessuna porta.
     */
    private Posizione portaInContatto;

    public Esplorazione(final Mondo mondo, final Giocatore giocatore) {
        this.mondo = Objects.requireNonNull(mondo, "Il mondo non puo' essere nullo");
        this.giocatore = Objects.requireNonNull(giocatore, "Il giocatore non puo' essere nullo");
    }

    public Mondo mondo() {
        return mondo;
    }

    public Giocatore giocatore() {
        return giocatore;
    }

    /**
     * Fa avanzare l'esplorazione di un passo temporale.
     *
     * Un aggiornamento conclude al massimo un passo: il tempo che resta dopo
     * la fine di un passo si scarta invece di riversarlo in quello successivo.
     * Costa niente e fa guadagnare due cose. La prima e' che la lista di
     * eventi riguarda sempre una sola casella e una sola zona: riversando
     * l'eccedenza, un aggiornamento abbastanza lungo poteva concludere un
     * passo nella radura e subito dopo attraversare il passaggio, e chi
     * leggeva la lista finiva per cercare un incontro nel bosco usando la
     * casella della radura. La seconda e' che un passo non comincia nello
     * stesso aggiornamento in cui ne finisce un altro: quel passo appena
     * avviato sopravviveva alla battaglia che l'incontro faceva partire, e a
     * battaglia chiusa il personaggio camminava di una casella da solo, senza
     * che nessuno toccasse i tasti.
     *
     * Non si perde movimento: con il passo fisso di 1/60 di secondo e quattro
     * caselle al secondo una casella dura esattamente quindici aggiornamenti.
     *
     * @param secondi durata del passo, non negativa.
     * @param comando la direzione tenuta premuta, vuoto se nessuna.
     * @return gli eventi accaduti, in ordine cronologico.
     */
    public List<EventoEsplorazione> aggiorna(final double secondi,
                                             final Optional<Direzione> comando) {
        final List<EventoEsplorazione> eventi = new ArrayList<>();
        final MovimentoGriglia movimento = giocatore.movimento();

        if (!movimento.inTransito() && !avviaPasso(comando, eventi)) {
            return eventi;
        }

        movimento.avanza(secondi);
        if (movimento.inTransito()) {
            return eventi;
        }

        // Il passaggio va guardato PRIMA di annunciare il passo: la casella
        // che innesca un cambio di zona non e' una casella su cui si cammina,
        // e annunciarla farebbe cercare un incontro nella zona in cui si e'
        // appena arrivati, usando la casella di quella vecchia.
        if (!esaminaCasellaRaggiunta(eventi)) {
            eventi.add(new EventoEsplorazione.PassoCompletato(movimento.casella()));
        }

        return eventi;
    }

    /**
     * Guarda il comando senza muovere nessuno, solo per tenere aggiornato il
     * contatto con la porta.
     *
     * Serve a chi sospende l'esplorazione, cioe' alla visita in ospedale:
     * mentre la visita e' in corso {@link #aggiorna} non viene chiamato, e un
     * tasto lasciato la' dentro non passerebbe da nessuna parte. Senza questo,
     * chi lasciava il tasto durante la visita non riusciva piu' a rientrare
     * finche' non lo lasciava una seconda volta, a visita finita.
     */
    void osservaSenzaMuovere(final Optional<Direzione> comando) {
        Objects.requireNonNull(comando, "Il comando non puo' essere nullo");
        if (comando.isEmpty()
                || !giocatore.posizione().spostata(comando.get()).equals(portaInContatto)) {
            portaInContatto = null;
        }
    }

    /**
     * Prova a far partire un nuovo passo.
     *
     * @return vero se il passo e' partito; falso se non c'era comando o se la
     *         casella davanti non e' calpestabile.
     */
    private boolean avviaPasso(final Optional<Direzione> comando,
                               final List<EventoEsplorazione> eventi) {
        if (comando.isEmpty()) {
            portaInContatto = null;
            return false;
        }

        final Direzione direzione = comando.get();
        final MovimentoGriglia movimento = giocatore.movimento();

        // Anche quando il passo e' impossibile il personaggio si gira: e' il
        // modo classico di guardare un ostacolo senza salirci sopra.
        movimento.orienta(direzione);

        final Posizione bersaglio = movimento.casella().spostata(direzione);
        final Zona zona = mondo.zonaCorrente();

        if (zona.dentro(bersaglio) && zona.tipo(bersaglio).porta()) {
            // Una porta si annuncia al primo contatto e non piu' finche' si
            // resta premuti contro di essa. Altrimenti l'evento uscirebbe
            // sessanta volte al secondo e, nel caso dell'ospedale, si
            // rientrerebbe a farsi curare ogni tre secondi senza mai lasciare
            // il tasto. Basta girarsi o lasciare il tasto per poter rientrare.
            if (!bersaglio.equals(portaInContatto)) {
                portaInContatto = bersaglio;
                eventi.add(new EventoEsplorazione.PortaToccata(zona.tipo(bersaglio).ospedale()));
            }
            return false;
        }

        portaInContatto = null;
        if (!zona.calpestabile(bersaglio)) {
            return false;
        }

        movimento.inizia(direzione);
        return true;
    }

    /**
     * Controlla cosa c'e' sulla casella appena raggiunta.
     *
     * @return vero se e' avvenuto un cambio di zona, nel qual caso
     *         l'aggiornamento si ferma qui.
     */
    private boolean esaminaCasellaRaggiunta(final List<EventoEsplorazione> eventi) {
        final Zona zona = mondo.zonaCorrente();
        final Posizione casella = giocatore.posizione();

        final Optional<Passaggio> passaggio = zona.passaggioIn(casella);
        if (passaggio.isPresent()) {
            mondo.vaiA(passaggio.get().zonaDestinazione());
            giocatore.movimento().teletrasporta(
                    passaggio.get().arrivo(), passaggio.get().direzioneUscita());
            eventi.add(new EventoEsplorazione.ZonaCambiata());
            return true;
        }

        return false;
    }
}
