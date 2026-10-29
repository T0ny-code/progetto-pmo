package gdr.partita;

import gdr.battaglia.AzioneBattaglia;
import gdr.battaglia.Battaglia;
import gdr.battaglia.Creatura;
import gdr.battaglia.Esito;
import gdr.battaglia.EventoBattaglia;
import gdr.entita.Giocatore;
import gdr.incontri.GeneratoreIncontri;
import gdr.mondo.Mondo;
import gdr.nucleo.Casuale;
import gdr.nucleo.Direzione;
import gdr.nucleo.Posizione;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * La radice dello stato di gioco: tiene insieme mondo, giocatore, compagno,
 * incontri e battaglia, e decide in che fase siamo.
 *
 * E' l'unico punto che conosce tutti i pezzi, ed e' comunque logica pura: non
 * disegna, non legge tasti, non conosce Swing. La vista le passa il tempo
 * trascorso e il comando premuto, e riceve in cambio cosa e' cambiato.
 */
public final class Partita {

    /** Quanto resta dentro l'ospedale il personaggio, in secondi. */
    private static final double DURATA_VISITA_OSPEDALE = 3.0;

    private final Mondo mondo;
    private final Giocatore giocatore;
    private final Creatura compagno;
    private final Esplorazione esplorazione;
    private final GeneratoreIncontri generatore;
    private final Casuale casuale;

    private final String zonaDiPartenza;
    private final Posizione posizioneDiPartenza;

    private FaseGioco fase = FaseGioco.ESPLORAZIONE;
    private Battaglia battaglia;
    private List<EventoBattaglia> aperturaBattaglia = List.of();
    private double cronometroVisita;

    public Partita(final Mondo mondo,
                   final Giocatore giocatore,
                   final Creatura compagno,
                   final Casuale casuale) {
        this.mondo = Objects.requireNonNull(mondo);
        this.giocatore = Objects.requireNonNull(giocatore);
        this.compagno = Objects.requireNonNull(compagno);
        this.casuale = Objects.requireNonNull(casuale);
        this.esplorazione = new Esplorazione(mondo, giocatore);
        this.generatore = new GeneratoreIncontri();

        this.zonaDiPartenza = mondo.zonaCorrente().identificatore();
        this.posizioneDiPartenza = giocatore.posizione();
    }

    public FaseGioco fase() {
        return fase;
    }

    public Esplorazione esplorazione() {
        return esplorazione;
    }

    /** La battaglia in corso, se c'e'. */
    public Optional<Battaglia> battaglia() {
        return Optional.ofNullable(battaglia);
    }

    /**
     * Gli eventi con cui si e' aperta la battaglia in corso.
     *
     * Serve alla vista, che deve metterli in scena: la battaglia si annuncia
     * una volta sola e chi la apre non e' chi la disegna.
     */
    public List<EventoBattaglia> aperturaBattaglia() {
        return aperturaBattaglia;
    }

    /** Falso mentre il personaggio e' dentro l'ospedale: non va disegnato. */
    public boolean giocatoreVisibile() {
        return fase != FaseGioco.DENTRO_OSPEDALE;
    }

    /**
     * Fa avanzare la partita di un passo temporale.
     *
     * In battaglia il tempo del mondo non scorre: l'azione passa da
     * {@link #scegliInBattaglia(AzioneBattaglia)}.
     */
    public List<EventoPartita> aggiorna(final double secondi,
                                        final Optional<Direzione> comando) {
        // Unico punto in cui il tempo entra nella logica: da qui in giu' e'
        // gia' stato controllato, e nessuno lo rifa'. Il controllo e' scritto
        // al contrario perche' "secondi < 0" lascerebbe passare NaN, e un NaN
        // concluderebbe un passo per aggiornamento senza che niente protesti.
        if (!Double.isFinite(secondi) || secondi < 0.0) {
            throw new IllegalArgumentException(
                    "Il tempo deve essere un numero finito e non negativo, ricevuto " + secondi);
        }
        Objects.requireNonNull(comando, "Il comando non puo' essere nullo");

        return switch (fase) {
            case ESPLORAZIONE -> aggiornaEsplorazione(secondi, comando);
            case DENTRO_OSPEDALE -> aggiornaVisita(secondi, comando);
            case BATTAGLIA -> List.of();
        };
    }

    private List<EventoPartita> aggiornaEsplorazione(final double secondi,
                                                     final Optional<Direzione> comando) {
        final List<EventoPartita> risultato = new ArrayList<>();

        for (final EventoEsplorazione evento : esplorazione.aggiorna(secondi, comando)) {
            switch (evento) {
                case EventoEsplorazione.ZonaCambiata ignorato ->
                        risultato.add(new EventoPartita.ZonaCambiata());

                case EventoEsplorazione.PortaToccata porta -> {
                    if (porta.ospedale()) {
                        fase = FaseGioco.DENTRO_OSPEDALE;
                        cronometroVisita = 0.0;
                        risultato.add(new EventoPartita.IngressoOspedale());
                        return risultato;
                    }
                }

                case EventoEsplorazione.PassoCompletato passo -> {
                    final boolean erbaAlta =
                            mondo.zonaCorrente().generaIncontri(passo.casella());
                    final Optional<Creatura> selvatica = generatore.valuta(
                            erbaAlta, mondo.zonaCorrente().tabellaIncontri(), casuale);
                    if (selvatica.isPresent()) {
                        apriBattaglia(selvatica.get(), risultato);
                        return risultato;
                    }
                }
            }
        }

        return risultato;
    }

    private void apriBattaglia(final Creatura selvatica,
                               final List<EventoPartita> risultato) {
        battaglia = new Battaglia(compagno, selvatica, casuale);
        aperturaBattaglia = battaglia.inizia();
        fase = FaseGioco.BATTAGLIA;
        risultato.add(new EventoPartita.BattagliaIniziata());
    }

    private List<EventoPartita> aggiornaVisita(final double secondi,
                                               final Optional<Direzione> comando) {
        // Il personaggio non si muove, ma il comando va comunque guardato: chi
        // lascia il tasto qui dentro si aspetta di poter rientrare appena
        // uscito, e l'esplorazione da sola non lo vedrebbe mai.
        esplorazione.osservaSenzaMuovere(comando);

        cronometroVisita += secondi;
        if (cronometroVisita < DURATA_VISITA_OSPEDALE) {
            return List.of();
        }

        compagno.curaCompletamente();
        fase = FaseGioco.ESPLORAZIONE;
        cronometroVisita = 0.0;
        return List.of(new EventoPartita.UscitaOspedale());
    }

    /** Esegue un turno di battaglia. Valido solo in fase di battaglia. */
    public List<EventoBattaglia> scegliInBattaglia(final AzioneBattaglia azione) {
        if (fase != FaseGioco.BATTAGLIA || battaglia == null) {
            throw new IllegalStateException("Nessuna battaglia in corso");
        }
        return battaglia.esegui(azione);
    }

    /**
     * Chiude una battaglia conclusa e ne applica le conseguenze.
     *
     * Va chiamata dalla vista quando ha finito di mettere in scena gli eventi:
     * la logica sa cosa succede, ma non quando l'animazione e' terminata.
     */
    public List<EventoPartita> chiudiBattaglia() {
        if (fase != FaseGioco.BATTAGLIA || battaglia == null) {
            throw new IllegalStateException("Nessuna battaglia in corso");
        }
        if (!battaglia.finita()) {
            throw new IllegalStateException("La battaglia non e' ancora finita");
        }

        final Esito esito = battaglia.esito();
        battaglia = null;
        aperturaBattaglia = List.of();
        fase = FaseGioco.ESPLORAZIONE;

        if (esito != Esito.SCONFITTA) {
            return List.of();
        }

        // Creatura stesa: si torna al punto di partenza curati. E' la
        // convenzione del genere, ed evita che una battaglia persa blocchi la
        // partita quando non esistono ne' oggetti ne' altre creature.
        compagno.curaCompletamente();
        mondo.vaiA(zonaDiPartenza);
        giocatore.movimento().teletrasporta(posizioneDiPartenza, Direzione.SUD);
        return List.of(new EventoPartita.SvenutoRiportatoACasa());
    }
}
