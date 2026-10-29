package gdr.grafica;

import gdr.dati.Bestiario;
import gdr.dati.ZoneIniziali;
import gdr.entita.Giocatore;
import gdr.mondo.Mondo;
import gdr.nucleo.CasualeStandard;
import gdr.nucleo.Direzione;
import gdr.partita.Partita;

import javax.swing.SwingUtilities;

/**
 * Punto d'ingresso del gioco.
 *
 * Monta il mondo, il giocatore e la finestra, poi avvia il ciclo. E' l'unico
 * posto in cui i pezzi si conoscono fra loro.
 *
 * I comandi sono elencati nel LEGGIMI: tenerli scritti anche qui vorrebbe
 * dire allineare due elenchi a mano.
 */
public final class Avvio {

    /** Caselle al secondo: una casella ogni quarto di secondo. */
    private static final double VELOCITA_CAMMINO = 4.0;

    /** Fattore di ingrandimento iniziale della finestra. */
    private static final int INGRANDIMENTO = 3;

    private Avvio() {
    }

    public static void main(final String[] argomenti) {
        SwingUtilities.invokeLater(Avvio::costruisciEAvvia);
    }

    private static void costruisciEAvvia() {
        // I fogli di sprite si caricano subito, non al primo disegno: se un
        // PNG manca o e' della misura sbagliata il gioco deve fermarsi adesso,
        // con un messaggio, invece di aprire una finestra nera e ripetere
        // l'errore sessanta volte al secondo.
        FoglioSprite.carica(DisegnatoreTile.FOGLIO, DisegnatoreTile.LATO);
        FoglioSprite.carica("personaggio.png", DisegnatorePersonaggio.LATO_FOGLIO);
        FoglioSprite.carica("creature.png", DisegnatoreCreatura.LATO);

        final Mondo mondo = ZoneIniziali.creaMondo();
        final Giocatore giocatore = new Giocatore(
                ZoneIniziali.posizioneIniziale(),
                Direzione.SUD,
                VELOCITA_CAMMINO);
        final Partita partita = new Partita(
                mondo, giocatore, Bestiario.compagnoIniziale(), new CasualeStandard());

        final PannelloGioco pannello = new PannelloGioco(partita, INGRANDIMENTO);
        final FinestraGioco finestra = new FinestraGioco(pannello);

        finestra.setVisible(true);
        new CicloDiGioco(pannello).avvia();
    }
}
