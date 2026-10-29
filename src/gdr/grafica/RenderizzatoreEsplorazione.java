package gdr.grafica;

import gdr.dati.ZoneIniziali;
import gdr.entita.MovimentoGriglia;
import gdr.mondo.TipoTile;
import gdr.mondo.Zona;
import gdr.nucleo.Posizione;
import gdr.partita.Esplorazione;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;

/**
 * Disegna la fase di esplorazione a risoluzione logica.
 *
 * Non tocca lo stato del gioco: legge la zona e il movimento e produce pixel.
 * Il pannello Swing si limita poi a ingrandire l'immagine risultante.
 */
public final class RenderizzatoreEsplorazione {

    private static final int LATO = DisegnatoreTile.LATO;

    private final Telecamera telecamera;

    public RenderizzatoreEsplorazione(final Telecamera telecamera) {
        this.telecamera = telecamera;
    }

    /** Larghezza dell'immagine logica in pixel. */
    public int larghezzaLogica() {
        return telecamera.larghezzaVista() * LATO;
    }

    /** Altezza dell'immagine logica in pixel. */
    public int altezzaLogica() {
        return telecamera.altezzaVista() * LATO;
    }

    /**
     * @param tempo             secondi trascorsi, per le animazioni d'ambiente.
     * @param opacitaVelo       da 0 (nessun velo) a 1 (schermo nero).
     * @param giocatoreVisibile falso mentre il personaggio e' dentro l'ospedale.
     * @param messaggio         testo da mostrare in basso, vuoto per nessuno.
     */
    public void disegna(final Graphics2D g,
                        final Esplorazione esplorazione,
                        final double tempo,
                        final double opacitaVelo,
                        final boolean giocatoreVisibile,
                        final String messaggio) {
        final Zona zona = esplorazione.mondo().zonaCorrente();
        final MovimentoGriglia movimento = esplorazione.giocatore().movimento();

        final double scostamentoColonna =
                telecamera.scostamentoColonna(movimento.colonnaInterpolata(), zona.larghezza());
        final double scostamentoRiga =
                telecamera.scostamentoRiga(movimento.rigaInterpolata(), zona.altezza());

        // Arrotondiamo a pixel interi: i tile restano allineati e il disegno
        // non diventa sfocato quando l'immagine viene ingrandita.
        final int scostamentoX = (int) Math.round(scostamentoColonna * LATO);
        final int scostamentoY = (int) Math.round(scostamentoRiga * LATO);

        // Nel bosco l'erba alta e' piu' cupa: e' l'unica differenza di
        // terreno fra le due zone, e si riduce a scegliere un altro riquadro.
        final boolean bosco = ZoneIniziali.BOSCO.equals(zona.identificatore());

        disegnaTerreno(g, zona, scostamentoX, scostamentoY, tempo, bosco);
        if (giocatoreVisibile) {
            disegnaGiocatore(g, zona, movimento, scostamentoX, scostamentoY, bosco);
        }

        if (!messaggio.isEmpty()) {
            disegnaMessaggio(g, messaggio);
        }
        if (opacitaVelo > 0.0) {
            disegnaVelo(g, opacitaVelo);
        }
    }

    private void disegnaTerreno(final Graphics2D g, final Zona zona,
                                final int scostamentoX, final int scostamentoY,
                                final double tempo, final boolean bosco) {
        // Non si riempie prima lo sfondo: la telecamera non esce mai dalla
        // zona e il ciclo disegna una fila e una colonna in piu' della vista,
        // quindi ogni pixel riceve comunque il suo tile. Misurato: zero pixel
        // scoperti su tutte le posizioni possibili del giocatore nelle due
        // zone. Il conto regge perche' ogni zona e' piu' grande della vista,
        // come dice la Telecamera: una zona piu' piccola lascerebbe scoperta
        // una striscia, e siccome la tela viene riusata a ogni fotogramma li'
        // si vedrebbero i pixel del fotogramma precedente.
        final int primaColonna = Math.floorDiv(scostamentoX, LATO);
        final int primaRiga = Math.floorDiv(scostamentoY, LATO);

        for (int riga = primaRiga; riga <= primaRiga + telecamera.altezzaVista(); riga++) {
            for (int colonna = primaColonna;
                 colonna <= primaColonna + telecamera.larghezzaVista();
                 colonna++) {
                final Posizione posizione = new Posizione(colonna, riga);
                if (!zona.dentro(posizione)) {
                    continue;
                }
                final int x = colonna * LATO - scostamentoX;
                final int y = riga * LATO - scostamentoY;

                if (ParteEdificio.parteDiEdificio(zona, posizione)) {
                    DisegnatoreEdificio.disegna(g, ParteEdificio.di(zona, posizione), x, y);
                } else {
                    DisegnatoreTile.disegna(g, zona.tipo(posizione), riga, x, y, tempo, bosco);
                }
            }
        }
    }

    private void disegnaGiocatore(final Graphics2D g, final Zona zona,
                                  final MovimentoGriglia movimento,
                                  final int scostamentoX, final int scostamentoY,
                                  final boolean bosco) {
        final int x = (int) Math.round(movimento.colonnaInterpolata() * LATO) - scostamentoX;
        final int y = (int) Math.round(movimento.rigaInterpolata() * LATO) - scostamentoY;

        DisegnatorePersonaggio.disegna(g, x, y, movimento.sguardo(), fase(movimento));

        // Se il giocatore e' nell'erba alta, qualche ciuffo gli passa davanti:
        // si capisce a vista che li' possono comparire creature.
        //
        // La casella e' quella su cui il personaggio sta disegnato, non
        // movimento.casella(): durante un passo quella resta la casella di
        // partenza, e i ciuffi seguivano il personaggio fin sopra l'erba
        // bassa per tutta la durata del passo. Arrotondando, i ciuffi
        // cambiano a meta' casella, dove cambia anche la casella che il
        // personaggio copre di piu'.
        final Posizione sotto = new Posizione(
                (int) Math.round(movimento.colonnaInterpolata()),
                (int) Math.round(movimento.rigaInterpolata()));
        if (zona.tipo(sotto) == TipoTile.ERBA_ALTA) {
            DisegnatoreTile.ciuffiDavanti(g, x, y, bosco);
        }
    }

    /**
     * Ciclo di camminata a quattro tempi: nella prima meta' di ogni casella
     * una gamba avanza, nella seconda i piedi tornano affiancati, e la gamba
     * che avanza si alterna a ogni casella. Il fotogramma dipende dal
     * progresso del passo, non da un cronometro separato: cosi' i piedi sono
     * sempre in fase con lo spostamento.
     */
    private static int fase(final MovimentoGriglia movimento) {
        if (!movimento.inTransito()) {
            return DisegnatorePersonaggio.FASE_FERMO;
        }
        if (movimento.progresso() >= 0.5) {
            return DisegnatorePersonaggio.FASE_FERMO;
        }
        return movimento.passiCompletati() % 2 == 0
                ? DisegnatorePersonaggio.FASE_PASSO_SINISTRO
                : DisegnatorePersonaggio.FASE_PASSO_DESTRO;
    }

    /** Riquadro di testo in basso, come quello della battaglia. */
    private void disegnaMessaggio(final Graphics2D g, final String messaggio) {
        final Riquadri.Punto riquadro =
                Riquadri.corniceInBasso(g, larghezzaLogica(), altezzaLogica());

        // Sulla mappa il testo parte in alto: puo' occupare fino a tre righe.
        Riquadri.scrivi(g, messaggio, riquadro.xTesto(), riquadro.y() + 6,
                riquadro.larghezzaTesto());
    }

    private void disegnaVelo(final Graphics2D g, final double opacita) {
        final Composite precedente = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, (float) Math.min(1.0, Math.max(0.0, opacita))));
        g.setColor(Tavolozza.VELO);
        g.fillRect(0, 0, larghezzaLogica(), altezzaLogica());
        g.setComposite(precedente);
    }

}
