package gdr.partita;

import gdr.battaglia.AzioneBattaglia;
import gdr.battaglia.Battaglia;
import gdr.battaglia.Creatura;
import gdr.battaglia.Esito;
import gdr.entita.Giocatore;
import gdr.mondo.Mondo;
import gdr.nucleo.Direzione;
import gdr.nucleo.Posizione;
import gdr.prova.CasualeDiProva;
import gdr.prova.MappeDiProva;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prove sulla partita: la radice dello stato, dove mondo, giocatore, creatura,
 * incontri e battaglia si incontrano.
 *
 * Sono le prove piu' vicine al gioco vero: percorrono le tre fasi, i cinque
 * eventi e i tre modi in cui una battaglia puo' finire. Tre di esse esistono
 * perche' altrettanti difetti sono stati trovati eseguendo il gioco, e servono
 * a impedire che tornino.
 */
class PartitaTest {

    private static final double PASSO = 1.0 / 60.0;
    private static final double VELOCITA = 4.0;
    private static final int AGGIORNAMENTI_PER_CASELLA = 15;

    /** Quanto dura la visita in ospedale, in aggiornamenti: tre secondi. */
    private static final int DURATA_VISITA = 3 * 60;

    private Mondo mondo;
    private Giocatore giocatore;
    private Creatura compagno;
    private Partita partita;

    private void partiDa(final Mondo mondoScelto, final Posizione posizione,
                         final Direzione sguardo, final Creatura creatura) {
        mondo = mondoScelto;
        giocatore = new Giocatore(posizione, sguardo, VELOCITA);
        compagno = creatura;
        partita = new Partita(mondo, giocatore, compagno, CasualeDiProva.sempre());
    }

    /** Una passeggiata tranquilla: mondo senza fauna, compagno robusto. */
    private void passeggiata(final Posizione posizione, final Direzione sguardo) {
        partiDa(MappeDiProva.mondoTranquillo(), posizione, sguardo,
                new Creatura(MappeDiProva.VELOCE, 5));
    }

    private List<EventoPartita> avanza(final int aggiornamenti,
                                       final Optional<Direzione> comando) {
        final List<EventoPartita> tutti = new ArrayList<>();
        for (int i = 0; i < aggiornamenti; i++) {
            tutti.addAll(partita.aggiorna(PASSO, comando));
        }
        return tutti;
    }

    private static <T extends EventoPartita> int quanti(final Class<T> tipo,
                                                        final List<EventoPartita> eventi) {
        int conta = 0;
        for (final EventoPartita evento : eventi) {
            if (tipo.isInstance(evento)) {
                conta++;
            }
        }
        return conta;
    }

    /** Risolve la battaglia in corso scegliendo sempre la prima mossa. */
    private Esito combattiFinoAllaFine() {
        final Battaglia battaglia = partita.battaglia().orElseThrow();
        while (!battaglia.finita()) {
            partita.scegliInBattaglia(new AzioneBattaglia.UsaMossa(0));
        }
        return battaglia.esito();
    }

    @Test
    @DisplayName("si comincia esplorando, senza battaglia in corso")
    void statoIniziale() {
        passeggiata(MappeDiProva.LIBERA, Direzione.SUD);

        assertEquals(FaseGioco.ESPLORAZIONE, partita.fase());
        assertTrue(partita.battaglia().isEmpty());
        assertTrue(partita.aperturaBattaglia().isEmpty());
        assertTrue(partita.giocatoreVisibile());
    }

    @Test
    @DisplayName("attraversare un passaggio produce l'evento di cambio zona")
    void cambioZona() {
        passeggiata(new Posizione(4, 2), Direzione.EST);

        final List<EventoPartita> eventi =
                avanza(AGGIORNAMENTI_PER_CASELLA, Optional.of(Direzione.EST));

        assertEquals(1, quanti(EventoPartita.ZonaCambiata.class, eventi));
        assertEquals(MappeDiProva.ALTRA, mondo.zonaCorrente().identificatore());
    }

    @Test
    @DisplayName("sbattere contro la porta dell'ospedale fa entrare e poi uscire curati")
    void visitaInOspedale() {
        passeggiata(MappeDiProva.LIBERA, Direzione.NORD);
        compagno.subisci(compagno.psMassimi() - 1);
        assertEquals(1, compagno.psAttuali());

        // Si bussa e si entra.
        final List<EventoPartita> ingresso = avanza(2, Optional.of(Direzione.SUD));
        assertEquals(1, quanti(EventoPartita.IngressoOspedale.class, ingresso));
        assertEquals(FaseGioco.DENTRO_OSPEDALE, partita.fase());
        assertFalse(partita.giocatoreVisibile());

        // Si esce dopo tre secondi, curati.
        final List<EventoPartita> visita =
                avanza(DURATA_VISITA, Optional.of(Direzione.SUD));
        assertEquals(1, quanti(EventoPartita.UscitaOspedale.class, visita));
        assertEquals(FaseGioco.ESPLORAZIONE, partita.fase());
        assertTrue(partita.giocatoreVisibile());
        assertEquals(compagno.psMassimi(), compagno.psAttuali());
    }

    @Test
    @DisplayName("tenendo premuto non si rientra in ospedale in ciclo")
    void nessunCicloDiCure() {
        // Senza la memoria della porta toccata ci si curava ogni tre secondi
        // all'infinito, senza mai lasciare il tasto.
        passeggiata(MappeDiProva.LIBERA, Direzione.NORD);

        final List<EventoPartita> eventi = avanza(15 * 60, Optional.of(Direzione.SUD));

        assertEquals(1, quanti(EventoPartita.IngressoOspedale.class, eventi));
    }

    @Test
    @DisplayName("lasciando il tasto dentro l'ospedale si riesce a rientrare")
    void siRientraSeSiLasciaIlTastoDentro() {
        // Durante la visita l'esplorazione e' ferma: se il comando non le
        // venisse passato lo stesso, il rilascio del tasto andrebbe perso e non
        // si riuscirebbe piu' a rientrare.
        passeggiata(MappeDiProva.LIBERA, Direzione.NORD);

        avanza(2, Optional.of(Direzione.SUD));
        assertEquals(FaseGioco.DENTRO_OSPEDALE, partita.fase());

        // Si lascia il tasto mentre si e' dentro.
        avanza(30, Optional.empty());
        // Poi si ripreme fino a uscire e si bussa di nuovo.
        final List<EventoPartita> eventi =
                avanza(DURATA_VISITA + 60, Optional.of(Direzione.SUD));

        assertEquals(1, quanti(EventoPartita.IngressoOspedale.class, eventi));
    }

    @Test
    @DisplayName("osservaSenzaMuovere dimentica la porta senza spostare nessuno")
    void osservaSenzaMuovereNonSposta() {
        passeggiata(MappeDiProva.LIBERA, Direzione.NORD);
        final Esplorazione esplorazione = partita.esplorazione();

        esplorazione.aggiorna(PASSO, Optional.of(Direzione.SUD));
        esplorazione.osservaSenzaMuovere(Optional.empty());

        assertEquals(MappeDiProva.LIBERA, giocatore.posizione());
        assertFalse(giocatore.movimento().inTransito());
        // La porta e' stata dimenticata, quindi si riannuncia.
        assertFalse(esplorazione.aggiorna(PASSO, Optional.of(Direzione.SUD)).isEmpty());
    }

    @Test
    @DisplayName("un passo nell'erba alta apre una battaglia")
    void laErbaAltaApreUnaBattaglia() {
        partiDa(MappeDiProva.mondo(), MappeDiProva.LIBERA, Direzione.EST,
                new Creatura(MappeDiProva.VELOCE, 5));

        final List<EventoPartita> eventi =
                avanza(AGGIORNAMENTI_PER_CASELLA, Optional.of(Direzione.EST));

        assertEquals(1, quanti(EventoPartita.BattagliaIniziata.class, eventi));
        assertEquals(FaseGioco.BATTAGLIA, partita.fase());
        assertTrue(partita.battaglia().isPresent());
        assertEquals(1, partita.aperturaBattaglia().size());
    }

    @Test
    @DisplayName("in battaglia il tempo del mondo non scorre")
    void inBattagliaIlMondoSiFerma() {
        partiDa(MappeDiProva.mondo(), MappeDiProva.LIBERA, Direzione.EST,
                new Creatura(MappeDiProva.VELOCE, 5));
        avanza(AGGIORNAMENTI_PER_CASELLA, Optional.of(Direzione.EST));
        assertEquals(FaseGioco.BATTAGLIA, partita.fase());

        final Posizione dovErava = giocatore.posizione();
        final List<EventoPartita> eventi = avanza(120, Optional.of(Direzione.EST));

        assertTrue(eventi.isEmpty());
        assertEquals(dovErava, giocatore.posizione());
    }

    @Test
    @DisplayName("dopo una vittoria si torna a esplorare senza conseguenze")
    void vittoriaEritornoAllEsplorazione() {
        partiDa(MappeDiProva.mondo(), MappeDiProva.LIBERA, Direzione.EST,
                new Creatura(MappeDiProva.VELOCE, 5));
        avanza(AGGIORNAMENTI_PER_CASELLA, Optional.of(Direzione.EST));

        assertEquals(Esito.VITTORIA, combattiFinoAllaFine());
        assertTrue(partita.chiudiBattaglia().isEmpty());

        assertEquals(FaseGioco.ESPLORAZIONE, partita.fase());
        assertTrue(partita.battaglia().isEmpty());
        assertTrue(partita.aperturaBattaglia().isEmpty());
    }

    @Test
    @DisplayName("finita la battaglia il personaggio non cammina da solo")
    void nessunPassoFantasmaDopoLaBattaglia() {
        // Il passo iniziato nello stesso aggiornamento in cui ne finiva un
        // altro sopravviveva alla battaglia, e a battaglia chiusa il
        // personaggio avanzava di una casella senza che nessuno premesse niente.
        partiDa(MappeDiProva.mondo(), MappeDiProva.LIBERA, Direzione.EST,
                new Creatura(MappeDiProva.VELOCE, 5));
        avanza(AGGIORNAMENTI_PER_CASELLA, Optional.of(Direzione.EST));

        final Posizione dovEraPrima = giocatore.posizione();
        combattiFinoAllaFine();
        partita.chiudiBattaglia();

        avanza(120, Optional.empty());

        assertEquals(dovEraPrima, giocatore.posizione());
        assertFalse(giocatore.movimento().inTransito());
    }

    @Test
    @DisplayName("una sconfitta riporta curati al punto di partenza")
    void sconfittaEritornoACasa() {
        // Il compagno e' debole e lento: perde.
        partiDa(MappeDiProva.mondo(), MappeDiProva.LIBERA, Direzione.EST,
                new Creatura(MappeDiProva.LENTA, 1));
        avanza(AGGIORNAMENTI_PER_CASELLA, Optional.of(Direzione.EST));

        assertEquals(Esito.SCONFITTA, combattiFinoAllaFine());
        final List<EventoPartita> conseguenze = partita.chiudiBattaglia();

        assertEquals(1, quanti(EventoPartita.SvenutoRiportatoACasa.class, conseguenze));
        assertEquals(compagno.psMassimi(), compagno.psAttuali());
        assertEquals(MappeDiProva.PROVA, mondo.zonaCorrente().identificatore());
        assertEquals(MappeDiProva.LIBERA, giocatore.posizione());
        assertEquals(FaseGioco.ESPLORAZIONE, partita.fase());
        assertFalse(giocatore.movimento().inTransito());
    }

    @Test
    @DisplayName("una fuga chiude la battaglia senza conseguenze")
    void fugaEritornoAllEsplorazione() {
        partiDa(MappeDiProva.mondo(), MappeDiProva.LIBERA, Direzione.EST,
                new Creatura(MappeDiProva.VELOCE, 5));
        avanza(AGGIORNAMENTI_PER_CASELLA, Optional.of(Direzione.EST));

        partita.scegliInBattaglia(new AzioneBattaglia.Fuggi());
        assertEquals(Esito.FUGA, partita.battaglia().orElseThrow().esito());

        assertTrue(partita.chiudiBattaglia().isEmpty());
        assertEquals(FaseGioco.ESPLORAZIONE, partita.fase());
    }

    @Test
    @DisplayName("agire o chiudere fuori da una battaglia e' un errore")
    void chiamateFuoriPosto() {
        passeggiata(MappeDiProva.LIBERA, Direzione.SUD);

        assertThrows(IllegalStateException.class,
                () -> partita.scegliInBattaglia(new AzioneBattaglia.Fuggi()));
        assertThrows(IllegalStateException.class, () -> partita.chiudiBattaglia());
    }

    @Test
    @DisplayName("non si chiude una battaglia ancora in corso")
    void nonSiChiudeUnaBattagliaInCorso() {
        partiDa(MappeDiProva.mondo(), MappeDiProva.LIBERA, Direzione.EST,
                new Creatura(MappeDiProva.VELOCE, 5));
        avanza(AGGIORNAMENTI_PER_CASELLA, Optional.of(Direzione.EST));

        assertEquals(FaseGioco.BATTAGLIA, partita.fase());
        assertFalse(partita.battaglia().orElseThrow().finita());
        assertThrows(IllegalStateException.class, () -> partita.chiudiBattaglia());
    }

    @Test
    @DisplayName("un tempo non valido viene rifiutato")
    void tempoNonValido() {
        passeggiata(MappeDiProva.LIBERA, Direzione.SUD);

        assertThrows(IllegalArgumentException.class,
                () -> partita.aggiorna(-1.0, Optional.empty()));
        // Un NaN passerebbe un controllo scritto come "minore di zero", e
        // concluderebbe un passo per aggiornamento.
        assertThrows(IllegalArgumentException.class,
                () -> partita.aggiorna(Double.NaN, Optional.empty()));
        assertThrows(IllegalArgumentException.class,
                () -> partita.aggiorna(Double.POSITIVE_INFINITY, Optional.empty()));

        assertThrows(NullPointerException.class, () -> partita.aggiorna(PASSO, null));
    }

    @Test
    @DisplayName("un tempo di zero e' lecito e non fa succedere niente")
    void tempoZeroLecito() {
        passeggiata(MappeDiProva.LIBERA, Direzione.SUD);

        assertTrue(partita.aggiorna(0.0, Optional.of(Direzione.NORD)).isEmpty());
        assertEquals(MappeDiProva.LIBERA, giocatore.posizione());
    }
}
