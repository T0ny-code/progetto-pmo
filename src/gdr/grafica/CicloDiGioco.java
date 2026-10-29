package gdr.grafica;

import javax.swing.Timer;
import java.util.Objects;

/**
 * Il battito del gioco: un solo ciclo, a passo temporale fisso.
 *
 * Accumula il tempo realmente trascorso e fa avanzare la logica a passi
 * costanti di 1/60 di secondo. Il vantaggio e' che la simulazione si comporta
 * allo stesso modo su macchine lente e veloci, e che basta
 * chiamare l'aggiornamento un numero noto di volte per sapere dove finisce il
 * giocatore.
 *
 * Il tempo accumulato e' limitato: se la finestra resta ferma per qualche
 * secondo (trascinata, o il portatile riaperto) non recuperiamo centinaia di
 * passi tutti insieme, che equivarrebbe a teletrasportare il giocatore.
 */
public final class CicloDiGioco {

    /** Passo della simulazione: sessanta aggiornamenti al secondo. */
    private static final double PASSO_FISSO = 1.0 / 60.0;

    private static final double MASSIMO_ACCUMULO = 0.25;
    private static final int RITARDO_TIMER = 15;

    private final PannelloGioco pannello;
    private final Timer timer;

    private long ultimoIstante;
    private double accumulato;

    public CicloDiGioco(final PannelloGioco pannello) {
        this.pannello = Objects.requireNonNull(pannello);
        this.timer = new Timer(RITARDO_TIMER, evento -> battito());
    }

    /** Avvia il ciclo. Va chiamato sul thread di Swing. */
    public void avvia() {
        ultimoIstante = System.nanoTime();
        accumulato = 0.0;
        timer.start();
    }

    private void battito() {
        final long adesso = System.nanoTime();
        final double trascorso = (adesso - ultimoIstante) / 1_000_000_000.0;
        ultimoIstante = adesso;

        accumulato = Math.min(MASSIMO_ACCUMULO, accumulato + trascorso);

        while (accumulato >= PASSO_FISSO) {
            pannello.aggiorna(PASSO_FISSO);
            accumulato -= PASSO_FISSO;
        }

        pannello.repaint();
    }
}
