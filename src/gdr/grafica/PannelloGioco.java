package gdr.grafica;

import gdr.battaglia.AzioneBattaglia;
import gdr.battaglia.Battaglia;
import gdr.nucleo.Direzione;
import gdr.partita.EventoPartita;
import gdr.partita.FaseGioco;
import gdr.partita.Partita;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.Optional;

/**
 * La superficie di disegno e il collegamento fra i tasti e la partita.
 *
 * Disegna a risoluzione logica su una tela piccola e poi la ingrandisce di un
 * fattore intero con interpolazione a pixel netti: e' cosi' che si ottiene
 * l'aspetto anni '90 senza sfocature.
 *
 * Tiene i due tempi dell'interfaccia sulla mappa: la dissolvenza fra le zone e
 * quanto resta scritto un messaggio. Le durate con cui si mettono in scena gli
 * eventi di battaglia stanno in {@link Testi#durata}, accanto alle frasi.
 * La logica non sa niente ne' degli uni ne' delle altre.
 */
// JPanel e' serializzabile, quindi con -Xlint il compilatore segnala ogni campo
// non serializzabile. Questo pannello non viene mai serializzato: e' una
// finestra, non un dato da salvare.
@SuppressWarnings("serial")
public final class PannelloGioco extends JPanel {

    /** Durata della dissolvenza dopo un cambio di zona, in secondi. */
    private static final double DURATA_TRANSIZIONE = 0.35;

    /** Quanto resta scritto il messaggio dopo essersi risvegliati. */
    private static final double DURATA_MESSAGGIO = 2.2;

    /** Vista in caselle. */
    private static final int VISTA_LARGHEZZA = 15;
    private static final int VISTA_ALTEZZA = 11;

    private final Partita partita;
    private final RenderizzatoreEsplorazione renderizzatoreMappa;
    private final GestoreInput input = new GestoreInput();
    private final VeloTransizione velo = new VeloTransizione(DURATA_TRANSIZIONE);
    private final AnimatoreBattaglia animatore = new AnimatoreBattaglia();
    private final BufferedImage tela;

    private double tempo;
    private String messaggioMappa = "";
    private double durataMessaggioMappa;
    private boolean menuAperto;
    private int voceSelezionata;

    public PannelloGioco(final Partita partita, final int ingrandimento) {
        this.partita = Objects.requireNonNull(partita);
        this.renderizzatoreMappa = new RenderizzatoreEsplorazione(
                new Telecamera(VISTA_LARGHEZZA, VISTA_ALTEZZA));
        this.tela = new BufferedImage(renderizzatoreMappa.larghezzaLogica(),
                renderizzatoreMappa.altezzaLogica(), BufferedImage.TYPE_INT_RGB);

        setPreferredSize(new Dimension(
                renderizzatoreMappa.larghezzaLogica() * ingrandimento,
                renderizzatoreMappa.altezzaLogica() * ingrandimento));
        setBackground(Color.BLACK);
        input.installa(this);
    }

    /** Fa avanzare il gioco di un passo temporale fisso. */
    public void aggiorna(final double secondi) {
        tempo += secondi;
        velo.avanza(secondi);
        if (durataMessaggioMappa > 0.0) {
            durataMessaggioMappa -= secondi;
            if (durataMessaggioMappa <= 0.0) {
                messaggioMappa = "";
            }
        }

        if (partita.fase() == FaseGioco.BATTAGLIA) {
            aggiornaBattaglia(secondi);
        } else {
            aggiornaMondo(secondi);
        }
    }

    private void aggiornaMondo(final double secondi) {
        // Durante la dissolvenza i tasti si ignorano: altrimenti si comincia a
        // camminare dentro il nero, senza vedere dove.
        final Optional<Direzione> comando =
                velo.attivo() ? Optional.empty() : input.comando();

        for (final EventoPartita evento : partita.aggiorna(secondi, comando)) {
            switch (evento) {
                case EventoPartita.ZonaCambiata ignorato -> velo.avvia();

                // Durata zero: il messaggio resta finche' la logica non dice
                // che la visita e' finita, e azzera il conto alla rovescia di
                // un messaggio precedente che altrimenti lo cancellerebbe a
                // meta' visita.
                case EventoPartita.IngressoOspedale ignorato ->
                        mostraSullaMappa(Testi.visitaOspedale(), 0.0);

                case EventoPartita.UscitaOspedale ignorato -> messaggioMappa = "";

                // Lo svenimento non arriva da qui ma da chiudiBattaglia: il
                // caso serve solo a tenere esaustivo lo switch.
                case EventoPartita.SvenutoRiportatoACasa ignorato -> { }

                case EventoPartita.BattagliaIniziata ignorato -> {
                    animatore.inizia(partita.battaglia().orElseThrow(),
                            partita.aperturaBattaglia());
                    input.svuotaMenu();
                    menuAperto = false;
                    voceSelezionata = 0;
                }
            }
        }
    }

    private void aggiornaBattaglia(final double secondi) {
        animatore.avanza(secondi);

        // Finche' c'e' qualcosa in scena il giocatore guarda e non scegle.
        if (animatore.occupato()) {
            menuAperto = false;
            return;
        }

        final Battaglia battaglia = partita.battaglia().orElseThrow();

        if (battaglia.finita()) {
            chiudiBattaglia();
            return;
        }

        menuAperto = true;
        leggiMenu();
    }

    private void chiudiBattaglia() {
        // chiudiBattaglia restituisce l'evento di svenimento oppure niente.
        if (!partita.chiudiBattaglia().isEmpty()) {
            velo.avvia();
            mostraSullaMappa(Testi.svenimento(), DURATA_MESSAGGIO);
        }
        menuAperto = false;
        input.svuotaMenu();
    }

    private void leggiMenu() {
        final Optional<GestoreInput.TastoMenu> tasto = input.prossimoTastoMenu();
        if (tasto.isEmpty()) {
            return;
        }

        switch (tasto.get()) {
            case SU -> voceSelezionata = (voceSelezionata + RenderizzatoreBattaglia.VOCI_MENU - 1)
                    % RenderizzatoreBattaglia.VOCI_MENU;
            case GIU -> voceSelezionata =
                    (voceSelezionata + 1) % RenderizzatoreBattaglia.VOCI_MENU;
            case CONFERMA -> confermaScelta();
        }
    }

    private void confermaScelta() {
        final AzioneBattaglia azione =
                voceSelezionata == RenderizzatoreBattaglia.VOCE_FUGA
                        ? new AzioneBattaglia.Fuggi()
                        : new AzioneBattaglia.UsaMossa(voceSelezionata);

        animatore.accoda(partita.scegliInBattaglia(azione));
        menuAperto = false;
    }

    private void mostraSullaMappa(final String testo, final double durata) {
        this.messaggioMappa = testo;
        this.durataMessaggioMappa = durata;
    }

    @Override
    protected void paintComponent(final Graphics contesto) {
        super.paintComponent(contesto);

        final Graphics2D logico = tela.createGraphics();
        if (partita.fase() == FaseGioco.BATTAGLIA) {
            RenderizzatoreBattaglia.disegna(logico, partita.battaglia().orElseThrow(),
                    animatore, tempo, menuAperto, voceSelezionata);
        } else {
            renderizzatoreMappa.disegna(logico, partita.esplorazione(), tempo,
                    velo.opacita(), partita.giocatoreVisibile(), messaggioMappa);
        }
        logico.dispose();

        final Graphics2D g = (Graphics2D) contesto;
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        // Solo ingrandimenti interi: a fattori frazionari i pixel dei tile
        // diventerebbero di dimensioni diverse fra loro.
        final int scala = Math.max(1, Math.min(
                getWidth() / tela.getWidth(), getHeight() / tela.getHeight()));
        final int larghezza = tela.getWidth() * scala;
        final int altezza = tela.getHeight() * scala;

        g.drawImage(tela, (getWidth() - larghezza) / 2, (getHeight() - altezza) / 2,
                larghezza, altezza, null);
    }
}
