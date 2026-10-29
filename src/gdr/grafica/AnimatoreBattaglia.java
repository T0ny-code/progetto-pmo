package gdr.grafica;

import gdr.battaglia.Battaglia;
import gdr.battaglia.EventoBattaglia;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Mette in scena gli eventi di una battaglia, uno alla volta.
 *
 * La logica risolve il turno tutto insieme e restituisce gli eventi con i PS
 * gia' aggiornati. Questa classe li consuma con le sue durate e mantiene dei
 * valori *mostrati* distinti da quelli reali: la barra dei PS scende
 * gradualmente verso un numero che la logica ha gia' fissato.
 *
 * Qui il tempo e' in secondi come in tutto il resto del gioco; quanto resta in
 * scena ciascun evento lo dice {@link Testi#durata}.
 *
 * Finche' {@link #occupato()} e' vero il giocatore non puo' scegliere: si
 * guarda cosa sta succedendo.
 */
public final class AnimatoreBattaglia {

    /** Quanti punti di salute la barra percorre in un secondo. */
    private static final double VELOCITA_BARRA = 22.0;

    private final Deque<EventoBattaglia> coda = new ArrayDeque<>();

    private String messaggio = "";
    private double attesa;

    private double psMostratiMio;
    private double psBersaglioMio;
    private double psMostratiSuo;
    private double psBersaglioSuo;

    /** Prepara l'animatore all'inizio di una battaglia. */
    public void inizia(final Battaglia battaglia, final List<EventoBattaglia> apertura) {
        coda.clear();
        messaggio = "";
        attesa = 0.0;

        psMostratiMio = battaglia.attiva().psAttuali();
        psBersaglioMio = psMostratiMio;
        psMostratiSuo = battaglia.selvatica().psAttuali();
        psBersaglioSuo = psMostratiSuo;

        accoda(apertura);
    }

    /** Accoda gli eventi di un turno appena risolto. */
    public void accoda(final List<EventoBattaglia> eventi) {
        coda.addAll(eventi);
    }

    /** Fa scorrere il tempo sulla messa in scena. */
    public void avanza(final double secondi) {
        psMostratiMio = avvicina(psMostratiMio, psBersaglioMio, secondi);
        psMostratiSuo = avvicina(psMostratiSuo, psBersaglioSuo, secondi);

        if (attesa > 0.0) {
            attesa -= secondi;
            return;
        }
        if (coda.isEmpty()) {
            return;
        }

        final EventoBattaglia evento = coda.pollFirst();
        final String frase = Testi.frase(evento);
        if (!frase.isEmpty()) {
            messaggio = frase;
        }
        attesa = Testi.durata(evento);

        if (evento instanceof EventoBattaglia.DannoSubito danno) {
            if (danno.alGiocatore()) {
                psBersaglioMio = danno.psRimanenti();
            } else {
                psBersaglioSuo = danno.psRimanenti();
            }
        }
    }

    /** Vero se c'e' ancora qualcosa da mostrare: l'input resta bloccato. */
    public boolean occupato() {
        return !coda.isEmpty() || attesa > 0.0
                || Math.abs(psMostratiMio - psBersaglioMio) > 0.5
                || Math.abs(psMostratiSuo - psBersaglioSuo) > 0.5;
    }

    /** La frase attualmente in scena. */
    public String messaggio() {
        return messaggio;
    }

    /** PS della creatura del giocatore come li disegna la barra. */
    public double psMostratiMio() {
        return psMostratiMio;
    }

    /** PS dell'avversario come li disegna la barra. */
    public double psMostratiSuo() {
        return psMostratiSuo;
    }

    private static double avvicina(final double attuale, final double bersaglio,
                                   final double secondi) {
        final double passo = VELOCITA_BARRA * secondi;
        if (Math.abs(bersaglio - attuale) <= passo) {
            return bersaglio;
        }
        return attuale + Math.signum(bersaglio - attuale) * passo;
    }
}
