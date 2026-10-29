package gdr.grafica;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

/**
 * La finestra che ospita il pannello di gioco.
 *
 * Resta ridimensionabile, che e' il comportamento predefinito di una finestra:
 * il pannello si limita a ingrandimenti interi, cosi' allargandola i pixel
 * restano quadrati.
 */
@SuppressWarnings("serial")
public final class FinestraGioco extends JFrame {

    public FinestraGioco(final PannelloGioco pannello) {
        super("Radura del Mattino");

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setContentPane(pannello);
        pack();
        setLocationRelativeTo(null);
    }
}
