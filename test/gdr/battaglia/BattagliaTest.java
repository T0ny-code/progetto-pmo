package gdr.battaglia;

import gdr.prova.CasualeDiProva;
import gdr.prova.MappeDiProva;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prove sulla macchina a turni.
 *
 * La battaglia e' sincrona e istantanea: riceve un'azione, risolve il turno
 * intero e restituisce la lista ordinata di cio' che e' accaduto. Qui si
 * verificano l'ordine degli eventi, i tre esiti possibili e il fatto che ogni
 * chiamata fuori posto fallisca a voce alta invece di corrompere lo stato.
 *
 * Con la sorgente casuale di prova i numeri sono prevedibili: l'attaccante di
 * livello 1 con attacco 20 contro una difesa 10 e una mossa da 50 di potenza fa
 * 5 di danno neutro e 10 se superefficace.
 */
class BattagliaTest {

    private static final int DANNO_SUPEREFFICACE = 10;
    private static final int DANNO_NEUTRO = 5;

    /** Quanti eventi produce un turno in cui entrambi colpiscono, uno dei due in modo efficace. */
    private static final int EVENTI_DI_UNO_SCAMBIO = 5;

    /** Verifica il tipo di un evento e lo restituisce gia' convertito. */
    private static <T extends EventoBattaglia> T come(final Class<T> tipo,
                                                      final EventoBattaglia evento) {
        assertTrue(tipo.isInstance(evento));
        return tipo.cast(evento);
    }

    private static Creatura veloce() {
        return new Creatura(MappeDiProva.VELOCE, 1);
    }

    private static Creatura lenta() {
        return new Creatura(MappeDiProva.LENTA, 1);
    }

    @Test
    @DisplayName("inizia annuncia l'avversario con nome e livello")
    void iniziaAnnunciaLAvversario() {
        final Creatura selvatica = new Creatura(MappeDiProva.LENTA, 4);
        final List<EventoBattaglia> apertura =
                new Battaglia(veloce(), selvatica, CasualeDiProva.sempre()).inizia();

        assertEquals(1, apertura.size());
        final EventoBattaglia.Comparsa comparsa =
                come(EventoBattaglia.Comparsa.class, apertura.get(0));
        assertEquals("Lenta", comparsa.nomeCreatura());
        assertEquals(4, comparsa.livello());
    }

    @Test
    @DisplayName("chi e' piu' veloce colpisce per primo, e gli eventi escono in ordine")
    void ordineDelTurnoPerVelocita() {
        final Creatura mia = veloce();
        final Creatura sua = lenta();
        final Battaglia b = new Battaglia(mia, sua, CasualeDiProva.sempre());

        final List<EventoBattaglia> turno = b.esegui(new AzioneBattaglia.UsaMossa(0));

        assertEquals(EVENTI_DI_UNO_SCAMBIO, turno.size());

        // Prima il giocatore, che e' piu' veloce.
        assertTrue(come(EventoBattaglia.MossaUsata.class, turno.get(0)).delGiocatore());
        // L'elettro contro l'acqua e' superefficace, e lo si annuncia.
        assertEquals(2.0, come(EventoBattaglia.EfficaciaRilevata.class, turno.get(1))
                .moltiplicatore(), 0.0);
        final EventoBattaglia.DannoSubito dannoSuo =
                come(EventoBattaglia.DannoSubito.class, turno.get(2));
        assertFalse(dannoSuo.alGiocatore());
        assertEquals(sua.psMassimi() - DANNO_SUPEREFFICACE, dannoSuo.psRimanenti());

        // Poi l'avversario, con un colpo neutro che non annuncia nessuna efficacia.
        assertFalse(come(EventoBattaglia.MossaUsata.class, turno.get(3)).delGiocatore());
        final EventoBattaglia.DannoSubito dannoMio =
                come(EventoBattaglia.DannoSubito.class, turno.get(4));
        assertTrue(dannoMio.alGiocatore());
        assertEquals(mia.psMassimi() - DANNO_NEUTRO, dannoMio.psRimanenti());
    }

    @Test
    @DisplayName("a parita' di velocita' tocca al giocatore")
    void pareggioDiVelocitaVaAlGiocatore() {
        final Battaglia b = new Battaglia(veloce(), new Creatura(MappeDiProva.PARI, 1),
                CasualeDiProva.sempre());

        final List<EventoBattaglia> turno = b.esegui(new AzioneBattaglia.UsaMossa(0));

        assertTrue(come(EventoBattaglia.MossaUsata.class, turno.get(0)).delGiocatore());
    }

    @Test
    @DisplayName("stendere l'avversario chiude la battaglia con una vittoria")
    void vittoria() {
        final Creatura sua = lenta();
        final Battaglia b = new Battaglia(veloce(), sua, CasualeDiProva.sempre());

        b.esegui(new AzioneBattaglia.UsaMossa(0));
        assertFalse(b.finita());

        final List<EventoBattaglia> ultimo = b.esegui(new AzioneBattaglia.UsaMossa(0));

        assertTrue(b.finita());
        assertEquals(Esito.VITTORIA, b.esito());
        assertTrue(sua.esausta());

        final EventoBattaglia.CreaturaEsausta esausta = come(
                EventoBattaglia.CreaturaEsausta.class, ultimo.get(ultimo.size() - 2));
        assertFalse(esausta.delGiocatore());
        assertEquals("Lenta", esausta.nomeCreatura());
        assertEquals(Esito.VITTORIA, come(EventoBattaglia.FineBattaglia.class,
                ultimo.get(ultimo.size() - 1)).esito());
    }

    @Test
    @DisplayName("essere stesi chiude la battaglia con una sconfitta")
    void sconfitta() {
        // Qui il compagno e' la creatura lenta: l'avversario colpisce per primo
        // ed e' superefficace contro di lei.
        final Creatura mia = lenta();
        final Battaglia b = new Battaglia(mia, veloce(), CasualeDiProva.sempre());

        b.esegui(new AzioneBattaglia.UsaMossa(0));
        assertFalse(b.finita());

        final List<EventoBattaglia> ultimo = b.esegui(new AzioneBattaglia.UsaMossa(0));

        assertTrue(b.finita());
        assertEquals(Esito.SCONFITTA, b.esito());
        assertTrue(mia.esausta());
        assertTrue(come(EventoBattaglia.CreaturaEsausta.class,
                ultimo.get(ultimo.size() - 2)).delGiocatore());
    }

    @Test
    @DisplayName("una fuga riuscita chiude la battaglia senza colpi")
    void fugaRiuscita() {
        final Creatura mia = veloce();
        final Creatura sua = lenta();
        final Battaglia b = new Battaglia(mia, sua, CasualeDiProva.sempre());

        final List<EventoBattaglia> turno = b.esegui(new AzioneBattaglia.Fuggi());

        assertEquals(2, turno.size());
        come(EventoBattaglia.FugaRiuscita.class, turno.get(0));
        assertEquals(Esito.FUGA,
                come(EventoBattaglia.FineBattaglia.class, turno.get(1)).esito());
        assertTrue(b.finita());
        assertEquals(Esito.FUGA, b.esito());

        // Nessuno dei due si e' fatto niente.
        assertEquals(mia.psMassimi(), mia.psAttuali());
        assertEquals(sua.psMassimi(), sua.psAttuali());
    }

    @Test
    @DisplayName("una fuga fallita lascia il turno all'avversario")
    void fugaFallita() {
        // Qui la probabilita' di fuga e' al suo massimo, 0,95: serve una
        // frazione piu' alta per farla fallire.
        final Creatura mia = veloce();
        final Battaglia b = new Battaglia(mia, lenta(), CasualeDiProva.conFrazioni(0.99));

        final List<EventoBattaglia> turno = b.esegui(new AzioneBattaglia.Fuggi());

        come(EventoBattaglia.FugaFallita.class, turno.get(0));
        assertFalse(b.finita());
        assertFalse(come(EventoBattaglia.MossaUsata.class, turno.get(1)).delGiocatore());
        assertEquals(mia.psMassimi() - DANNO_NEUTRO, mia.psAttuali());
    }

    @Test
    @DisplayName("una mossa che manca non fa danno")
    void mossaMancata() {
        // La mossa di indice 1 ha precisione 50: con una frazione di 0,99 non
        // va a segno.
        final Creatura sua = lenta();
        final Battaglia b = new Battaglia(veloce(), sua, CasualeDiProva.conFrazioni(0.99));

        final List<EventoBattaglia> turno = b.esegui(new AzioneBattaglia.UsaMossa(1));

        final EventoBattaglia.MossaMancata mancata =
                come(EventoBattaglia.MossaMancata.class, turno.get(0));
        assertTrue(mancata.delGiocatore());
        assertEquals("Veloce", mancata.nomeCreatura());
        assertEquals(sua.psMassimi(), sua.psAttuali());
    }

    @Test
    @DisplayName("l'esito si puo' chiedere solo a battaglia finita")
    void esitoSoloAFineBattaglia() {
        final Battaglia b = new Battaglia(veloce(), lenta(), CasualeDiProva.sempre());

        assertFalse(b.finita());
        assertThrows(IllegalStateException.class, b::esito);
    }

    @Test
    @DisplayName("dopo la fine non si puo' piu' agire")
    void nessunaAzioneDopoLaFine() {
        final Battaglia b = new Battaglia(veloce(), lenta(), CasualeDiProva.sempre());
        b.esegui(new AzioneBattaglia.Fuggi());
        assertTrue(b.finita());

        assertThrows(IllegalStateException.class,
                () -> b.esegui(new AzioneBattaglia.UsaMossa(0)));
    }

    @Test
    @DisplayName("un indice di mossa inesistente viene rifiutato senza toccare lo stato")
    void indiceDiMossaInesistente() {
        final Creatura mia = veloce();
        final Creatura sua = lenta();
        final Battaglia b = new Battaglia(mia, sua, CasualeDiProva.sempre());

        for (final int indice : new int[] {-1, 2, 99, Integer.MIN_VALUE, Integer.MAX_VALUE}) {
            assertThrows(IndexOutOfBoundsException.class,
                    () -> b.esegui(new AzioneBattaglia.UsaMossa(indice)));
        }

        assertEquals(mia.psMassimi(), mia.psAttuali());
        assertEquals(sua.psMassimi(), sua.psAttuali());
        assertFalse(b.finita());
        // E la battaglia resta giocabile.
        assertEquals(EVENTI_DI_UNO_SCAMBIO, b.esegui(new AzioneBattaglia.UsaMossa(0)).size());
    }

    @Test
    @DisplayName("non si comincia una battaglia con una creatura gia' stesa")
    void nonSiCominciaConUnaCreaturaStesa() {
        final Creatura stesa = veloce();
        stesa.subisci(stesa.psMassimi());
        assertTrue(stesa.esausta());

        assertThrows(IllegalArgumentException.class,
                () -> new Battaglia(stesa, lenta(), CasualeDiProva.sempre()));
        assertThrows(IllegalArgumentException.class,
                () -> new Battaglia(veloce(), stesa, CasualeDiProva.sempre()));
    }

    @Test
    @DisplayName("la creatura attiva e quella selvatica sono quelle passate al costruttore")
    void creaturePassateAlCostruttore() {
        final Creatura mia = veloce();
        final Creatura sua = lenta();
        final Battaglia b = new Battaglia(mia, sua, CasualeDiProva.sempre());

        assertEquals(mia, b.attiva());
        assertEquals(sua, b.selvatica());
    }
}
