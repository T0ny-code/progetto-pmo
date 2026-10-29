package gdr.grafica;

import gdr.nucleo.Direzione;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import java.awt.event.ActionEvent;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Traduce la tastiera in comandi.
 *
 * Nient'altro: qui non si decide se un passo e' lecito, non si sposta nessuno,
 * non si scelgono mosse. E' l'unico punto in cui il gioco sa che esistono i
 * tasti.
 *
 * Gli stessi tasti servono a due cose diverse. Camminare vuole sapere quale
 * direzione e' *tenuta premuta*, quindi si tiene un elenco dei tasti giu' e
 * vale il piu' recente: e' il comportamento che sorprende meno quando si cambia
 * strada di corsa. Un menu invece vuole le *singole pressioni*; per questo
 * esiste una coda separata, e un tasto tenuto giu' scorre il menu al ritmo con
 * cui il sistema operativo ripete la pressione, non sessanta volte al secondo.
 *
 * L'elenco contiene i nomi dei tasti e non le direzioni, anche se per
 * camminare serve la direzione. Il motivo e' che ogni direzione ha due tasti:
 * tenendo giu' la freccia su e sfiorando W, il rilascio di W cancellava
 * l'unica voce NORD dell'elenco e il personaggio si fermava con la freccia
 * ancora premuta.
 *
 * Attenzione: una InputMap associa una combinazione di tasti a una sola azione.
 * Registrare due volte "pressed UP" non aggiunge un secondo effetto, sostituisce
 * il primo. Per questo ogni tasto ha qui una sola azione, che fa entrambe le
 * cose.
 *
 * Limite noto che resta: le scorciatoie valgono finche' la finestra ha il
 * fuoco, e un rilascio avvenuto mentre il fuoco e' altrove non arriva. Chi
 * passa a un'altra finestra mentre cammina torna con il tasto ancora
 * registrato come premuto, finche' non lo premi e lo rilasci di nuovo. Per
 * accorgersene servirebbe sentire il fuoco della finestra, che da qui non si
 * raggiunge.
 */
public final class GestoreInput {

    /** I tasti che servono a navigare un menu, come pressioni singole. */
    public enum TastoMenu { SU, GIU, CONFERMA }

    /** Oltre questo numero di tasti in coda il giocatore sta solo pestando. */
    private static final int CODA_MASSIMA = 4;

    /** I tasti di direzione giu' in questo momento, dal meno al piu' recente. */
    private final Deque<String> premuti = new ArrayDeque<>();

    /** Che direzione comanda ogni tasto di direzione. */
    private final Map<String, Direzione> direzioni = new HashMap<>();

    private final Deque<TastoMenu> codaMenu = new ArrayDeque<>();

    /** Aggancia i tasti al componente; funziona finche' la finestra ha il fuoco. */
    public void installa(final JComponent componente) {
        // Direzioni: contano sia come movimento sia come scorrimento del menu.
        direzione(componente, "UP", Direzione.NORD, TastoMenu.SU);
        direzione(componente, "W", Direzione.NORD, TastoMenu.SU);
        direzione(componente, "DOWN", Direzione.SUD, TastoMenu.GIU);
        direzione(componente, "S", Direzione.SUD, TastoMenu.GIU);
        direzione(componente, "LEFT", Direzione.OVEST, null);
        direzione(componente, "A", Direzione.OVEST, null);
        direzione(componente, "RIGHT", Direzione.EST, null);
        direzione(componente, "D", Direzione.EST, null);

        // Tasti di sola conferma.
        soloMenu(componente, "Z", TastoMenu.CONFERMA);
        soloMenu(componente, "ENTER", TastoMenu.CONFERMA);
        soloMenu(componente, "SPACE", TastoMenu.CONFERMA);
    }

    /** La direzione attualmente tenuta premuta, se ce n'e' una. */
    public Optional<Direzione> comando() {
        return Optional.ofNullable(premuti.peekLast()).map(direzioni::get);
    }

    /** Il prossimo tasto di menu premuto, consumandolo. */
    public Optional<TastoMenu> prossimoTastoMenu() {
        return Optional.ofNullable(codaMenu.pollFirst());
    }

    /** Butta via i tasti di menu accumulati: si usa entrando in battaglia. */
    public void svuotaMenu() {
        codaMenu.clear();
    }

    /**
     * @param tastoMenu la voce di menu corrispondente, o null se questo tasto
     *                  non serve a navigare.
     */
    private void direzione(final JComponent componente, final String tasto,
                           final Direzione direzione, final TastoMenu tastoMenu) {
        direzioni.put(tasto, direzione);

        azione(componente, "pressed " + tasto, "premi-" + tasto, () -> {
            // Finche' il tasto resta giu' il sistema operativo ripete la
            // pressione: conta una volta sola, cosi' un tasto tenuto giu' non
            // ruba il turno a uno premuto dopo di lui.
            if (!premuti.contains(tasto)) {
                premuti.addLast(tasto);
            }
            if (tastoMenu != null) {
                accoda(tastoMenu);
            }
        });
        azione(componente, "released " + tasto, "rilascia-" + tasto,
                () -> premuti.remove(tasto));

        // Una InputMap confronta la combinazione esatta, modificatori
        // compresi: se mentre si rilascia la freccia e' giu' anche shift,
        // control o alt, "released UP" non scatta, il tasto resta segnato come
        // premuto e il personaggio cammina da solo finche' non lo si preme e
        // rilascia un'altra volta. Qui le tre varianti puntano alla stessa
        // azione di rilascio.
        anche(componente, "shift released " + tasto, "rilascia-" + tasto);
        anche(componente, "ctrl released " + tasto, "rilascia-" + tasto);
        anche(componente, "alt released " + tasto, "rilascia-" + tasto);
    }

    private void soloMenu(final JComponent componente, final String tasto,
                          final TastoMenu tastoMenu) {
        azione(componente, "pressed " + tasto, "menu-" + tasto, () -> accoda(tastoMenu));
    }

    private void accoda(final TastoMenu tasto) {
        if (codaMenu.size() < CODA_MASSIMA) {
            codaMenu.addLast(tasto);
        }
    }

    /** Aggancia una seconda combinazione a un'azione gia' registrata. */
    private static void anche(final JComponent componente, final String colpo,
                              final String nome) {
        componente.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(colpo), nome);
    }

    private static void azione(final JComponent componente, final String colpo,
                               final String nome, final Runnable effetto) {
        componente.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(colpo), nome);
        componente.getActionMap().put(nome, new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent evento) {
                effetto.run();
            }
        });
    }
}
