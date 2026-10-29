package gdr.partita;

import gdr.entita.Giocatore;
import gdr.mondo.Mondo;
import gdr.mondo.TipoTile;
import gdr.nucleo.Direzione;
import gdr.nucleo.Posizione;
import gdr.prova.MappeDiProva;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prove sull'esplorazione: il punto in cui il movimento incontra la mappa.
 *
 * E' qui che si decide se un passo e' lecito, cosa c'e' sulla casella raggiunta
 * e quali eventi vengono riferiti a chi disegna. Due comportamenti valgono una
 * prova ciascuno perche' sono gia' stati sbagliati una volta: la porta che si
 * annuncia una volta sola, e il fatto che un aggiornamento concluda al massimo
 * un passo.
 */
class EsplorazioneTest {

    private static final double PASSO = 1.0 / 60.0;
    private static final double VELOCITA = 4.0;
    private static final int AGGIORNAMENTI_PER_CASELLA = 15;

    private Mondo mondo;
    private Giocatore giocatore;
    private Esplorazione esplorazione;

    private void partiDa(final Posizione posizione, final Direzione sguardo) {
        mondo = MappeDiProva.mondoTranquillo();
        giocatore = new Giocatore(posizione, sguardo, VELOCITA);
        esplorazione = new Esplorazione(mondo, giocatore);
    }

    /** Fa passare un certo numero di aggiornamenti e raccoglie tutti gli eventi. */
    private List<EventoEsplorazione> avanza(final int aggiornamenti,
                                            final Optional<Direzione> comando) {
        final List<EventoEsplorazione> tutti = new ArrayList<>();
        for (int i = 0; i < aggiornamenti; i++) {
            tutti.addAll(esplorazione.aggiorna(PASSO, comando));
        }
        return tutti;
    }

    private static <T extends EventoEsplorazione> List<T> soli(final Class<T> tipo,
                                                               final List<EventoEsplorazione> da) {
        final List<T> scelti = new ArrayList<>();
        for (final EventoEsplorazione evento : da) {
            if (tipo.isInstance(evento)) {
                scelti.add(tipo.cast(evento));
            }
        }
        return scelti;
    }

    @Test
    @DisplayName("senza comando non succede niente")
    void senzaComandoNienteEventi() {
        partiDa(MappeDiProva.LIBERA, Direzione.SUD);

        final List<EventoEsplorazione> eventi = avanza(60, Optional.empty());

        assertTrue(eventi.isEmpty());
        assertEquals(MappeDiProva.LIBERA, giocatore.posizione());
    }

    @Test
    @DisplayName("un passo concluso viene annunciato una volta, con la casella raggiunta")
    void passoCompletato() {
        partiDa(MappeDiProva.LIBERA, Direzione.SUD);

        final List<EventoEsplorazione> eventi =
                avanza(AGGIORNAMENTI_PER_CASELLA, Optional.of(Direzione.NORD));

        final List<EventoEsplorazione.PassoCompletato> passi =
                soli(EventoEsplorazione.PassoCompletato.class, eventi);
        assertEquals(1, passi.size());
        assertEquals(new Posizione(2, 1), passi.get(0).casella());
        assertEquals(new Posizione(2, 1), giocatore.posizione());
    }

    @Test
    @DisplayName("un passo iniziato si conclude anche se si lascia il tasto")
    void ilPassoIniziatoSiConclude() {
        partiDa(MappeDiProva.LIBERA, Direzione.SUD);

        avanza(1, Optional.of(Direzione.NORD));
        assertTrue(giocatore.movimento().inTransito());

        final List<EventoEsplorazione> eventi =
                avanza(AGGIORNAMENTI_PER_CASELLA, Optional.empty());

        assertEquals(1, soli(EventoEsplorazione.PassoCompletato.class, eventi).size());
        assertEquals(new Posizione(2, 1), giocatore.posizione());
        assertFalse(giocatore.movimento().inTransito());
    }

    @Test
    @DisplayName("contro un albero non si passa, ma ci si gira comunque")
    void controUnAlberoCiSiGiraSoltanto() {
        // (1, 1) ha un albero a ovest, in (0, 1).
        partiDa(new Posizione(1, 1), Direzione.EST);

        final List<EventoEsplorazione> eventi = avanza(60, Optional.of(Direzione.OVEST));

        assertTrue(eventi.isEmpty());
        assertEquals(new Posizione(1, 1), giocatore.posizione());
        assertEquals(Direzione.OVEST, giocatore.movimento().sguardo());
    }

    @Test
    @DisplayName("la porta dell'ospedale si annuncia una volta sola finche' si resta premuti")
    void laPortaSiAnnunciaUnaVoltaSola() {
        partiDa(MappeDiProva.LIBERA, Direzione.NORD);

        final List<EventoEsplorazione> eventi = avanza(120, Optional.of(Direzione.SUD));

        final List<EventoEsplorazione.PortaToccata> porte =
                soli(EventoEsplorazione.PortaToccata.class, eventi);
        assertEquals(1, porte.size());
        assertTrue(porte.get(0).ospedale());
        assertEquals(MappeDiProva.LIBERA, giocatore.posizione());
    }

    @Test
    @DisplayName("lasciando il tasto la porta si puo' toccare di nuovo")
    void lasciandoIlTastoLaPortaSiRiannuncia() {
        partiDa(MappeDiProva.LIBERA, Direzione.NORD);

        assertEquals(1, soli(EventoEsplorazione.PortaToccata.class,
                avanza(30, Optional.of(Direzione.SUD))).size());

        avanza(5, Optional.empty());

        assertEquals(1, soli(EventoEsplorazione.PortaToccata.class,
                avanza(30, Optional.of(Direzione.SUD))).size());
    }

    @Test
    @DisplayName("girandosi altrove la porta si puo' toccare di nuovo")
    void girandosiAltroveLaPortaSiRiannuncia() {
        partiDa(MappeDiProva.LIBERA, Direzione.NORD);

        assertEquals(1, soli(EventoEsplorazione.PortaToccata.class,
                avanza(30, Optional.of(Direzione.SUD))).size());

        // Un passo verso nord porta via dalla porta.
        avanza(AGGIORNAMENTI_PER_CASELLA, Optional.of(Direzione.NORD));
        // E tornando indietro la si ritrova.
        avanza(AGGIORNAMENTI_PER_CASELLA, Optional.of(Direzione.SUD));

        assertEquals(1, soli(EventoEsplorazione.PortaToccata.class,
                avanza(30, Optional.of(Direzione.SUD))).size());
    }

    @Test
    @DisplayName("anche la porta di una casa qualsiasi si annuncia, ma non e' dell'ospedale")
    void portaDiCasaNonEOspedale() {
        // Da (3, 2), l'erba alta, la porta di casa sta appena sotto.
        partiDa(MappeDiProva.ERBA_ALTA, Direzione.NORD);

        final List<EventoEsplorazione.PortaToccata> porte = soli(
                EventoEsplorazione.PortaToccata.class, avanza(60, Optional.of(Direzione.SUD)));

        assertEquals(1, porte.size());
        assertFalse(porte.get(0).ospedale());
    }

    @Test
    @DisplayName("il passaggio cambia zona e deposita accanto, non sopra il passaggio stesso")
    void ilPassaggioCambiaZona() {
        partiDa(new Posizione(4, 2), Direzione.EST);

        final List<EventoEsplorazione> eventi =
                avanza(AGGIORNAMENTI_PER_CASELLA, Optional.of(Direzione.EST));

        assertEquals(1, soli(EventoEsplorazione.ZonaCambiata.class, eventi).size());
        assertEquals(MappeDiProva.ALTRA, mondo.zonaCorrente().identificatore());
        assertEquals(MappeDiProva.ARRIVO_IN_ALTRA, giocatore.posizione());
        assertNotEquals(TipoTile.PASSAGGIO, mondo.zonaCorrente().tipo(giocatore.posizione()));
        assertEquals(Direzione.EST, giocatore.movimento().sguardo());
    }

    @Test
    @DisplayName("la casella del passaggio non viene annunciata come passo")
    void ilPassaggioNonEUnPassoNormale() {
        // Altrimenti chi legge gli eventi cercherebbe un incontro su quella
        // casella, ma nella zona in cui e' appena arrivato.
        partiDa(new Posizione(4, 2), Direzione.EST);

        final List<EventoEsplorazione> eventi =
                avanza(AGGIORNAMENTI_PER_CASELLA, Optional.of(Direzione.EST));

        assertTrue(soli(EventoEsplorazione.PassoCompletato.class, eventi).isEmpty());
    }

    @Test
    @DisplayName("un aggiornamento lungo conclude al massimo un passo")
    void unPassoPerAggiornamento() {
        // Con un secondo intero si percorrerebbero quattro caselle: se ne fa
        // una sola, e la lista contiene un solo evento. E' cio' che impedisce
        // a un passo della prima zona di essere risolto con la mappa della
        // seconda.
        partiDa(new Posizione(1, 1), Direzione.EST);

        final List<EventoEsplorazione> eventi =
                esplorazione.aggiorna(1.0, Optional.of(Direzione.EST));

        assertEquals(1, eventi.size());
        assertEquals(new Posizione(2, 1), giocatore.posizione());
    }

    @Test
    @DisplayName("mondo e giocatore sono quelli passati al costruttore")
    void mondoEGiocatoreEsposti() {
        partiDa(MappeDiProva.LIBERA, Direzione.SUD);

        assertEquals(mondo, esplorazione.mondo());
        assertEquals(giocatore, esplorazione.giocatore());
    }
}
