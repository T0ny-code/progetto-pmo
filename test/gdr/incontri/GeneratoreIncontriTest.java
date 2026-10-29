package gdr.incontri;

import gdr.battaglia.Creatura;
import gdr.prova.CasualeDiProva;
import gdr.prova.MappeDiProva;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prove sulla regola degli incontri e sulla tregua.
 *
 * La tregua e' il periodo di grazia dopo un incontro: per qualche passo non puo'
 * partirne un altro, altrimenti uscendo da una battaglia ne comincerebbe subito
 * un'altra. Le prove usano una tabella con probabilita' 1, cosi' l'unico
 * fattore che resta e' proprio la tregua.
 */
class GeneratoreIncontriTest {

    /** Quanti passi di grazia impone il generatore dopo un incontro. */
    private static final int PASSI_DI_TREGUA = 3;

    private static final boolean NELL_ERBA_ALTA = true;
    private static final boolean SUL_SENTIERO = false;

    private static Optional<Creatura> passo(final GeneratoreIncontri generatore,
                                            final boolean erbaAlta) {
        return generatore.valuta(erbaAlta, MappeDiProva.faunaCerta(), CasualeDiProva.sempre());
    }

    @Test
    @DisplayName("il primo passo nell'erba alta puo' gia' far scattare un incontro")
    void siParteFuoriDallaTregua() {
        assertTrue(passo(new GeneratoreIncontri(), NELL_ERBA_ALTA).isPresent());
    }

    @Test
    @DisplayName("fuori dall'erba alta non si incontra mai niente")
    void fuoriDallErbaAltaNienteIncontri() {
        final GeneratoreIncontri generatore = new GeneratoreIncontri();

        for (int i = 0; i < 50; i++) {
            assertFalse(passo(generatore, SUL_SENTIERO).isPresent());
        }
    }

    @Test
    @DisplayName("dopo un incontro i due passi successivi sono sicuri, il terzo no")
    void duePassiSicuriPoiSiRicomincia() {
        final GeneratoreIncontri generatore = new GeneratoreIncontri();

        assertTrue(passo(generatore, NELL_ERBA_ALTA).isPresent());

        assertFalse(passo(generatore, NELL_ERBA_ALTA).isPresent());
        assertFalse(passo(generatore, NELL_ERBA_ALTA).isPresent());
        assertTrue(passo(generatore, NELL_ERBA_ALTA).isPresent());
    }

    @Test
    @DisplayName("anche camminare sul sentiero consuma la tregua")
    void ancheIlSentieroConsumaLaTregua() {
        // E' il punto meno ovvio della regola: il contatore sale a ogni passo
        // concluso, non solo nell'erba. Cosi' uscendo dall'erba, facendo tre
        // passi sul sentiero e rientrando, la tregua e' gia' finita.
        final GeneratoreIncontri generatore = new GeneratoreIncontri();

        assertTrue(passo(generatore, NELL_ERBA_ALTA).isPresent());

        for (int i = 0; i < PASSI_DI_TREGUA; i++) {
            assertFalse(passo(generatore, SUL_SENTIERO).isPresent());
        }

        // Rientrando nell'erba l'incontro puo' gia' ripartire.
        assertTrue(passo(generatore, NELL_ERBA_ALTA).isPresent());
    }

    @Test
    @DisplayName("una tabella vuota non produce incontri")
    void tabellaVuota() {
        final GeneratoreIncontri generatore = new GeneratoreIncontri();

        for (int i = 0; i < 20; i++) {
            assertFalse(generatore.valuta(NELL_ERBA_ALTA, MappeDiProva.faunaVuota(),
                    CasualeDiProva.sempre()).isPresent());
        }
    }

    @Test
    @DisplayName("con il dado sfavorevole non scatta niente, anche fuori tregua")
    void dadoSfavorevole() {
        final GeneratoreIncontri generatore = new GeneratoreIncontri();
        final TabellaIncontri meta = new TabellaIncontri(
                List.of(new VoceIncontro(MappeDiProva.LENTA, 2, 2)), 0.5);

        // Frazione 0,9 contro una probabilita' di 0,5: non accade.
        assertFalse(generatore.valuta(NELL_ERBA_ALTA, meta,
                CasualeDiProva.conFrazioni(0.9)).isPresent());
        // Frazione 0,1: accade.
        assertTrue(generatore.valuta(NELL_ERBA_ALTA, meta,
                CasualeDiProva.conFrazioni(0.1)).isPresent());
    }

    @Test
    @DisplayName("la creatura sorteggiata ha la specie e un livello dell'intervallo")
    void sorteggioDellaCreatura() {
        final TabellaIncontri tabella = new TabellaIncontri(
                List.of(new VoceIncontro(MappeDiProva.LENTA, 3, 6)), 1.0);

        // Il sorteggio e' in due tempi: prima la specie, poi il livello.
        for (int scarto = 0; scarto < 4; scarto++) {
            final Creatura creatura =
                    tabella.sorteggia(CasualeDiProva.conInteri(0, scarto));
            assertEquals("Lenta", creatura.nome());
            assertEquals(3 + scarto, creatura.livello());
        }
    }

    @Test
    @DisplayName("l'intervallo dei livelli conta gli estremi")
    void livelliPossibiliComprendeGliEstremi() {
        assertEquals(1, new VoceIncontro(MappeDiProva.LENTA, 4, 4).livelliPossibili());
        assertEquals(3, new VoceIncontro(MappeDiProva.LENTA, 4, 6).livelliPossibili());
    }

    @Test
    @DisplayName("una voce o una tabella mal formate vengono rifiutate")
    void voceOTabellaMalFormate() {
        assertThrows(IllegalArgumentException.class,
                () -> new VoceIncontro(MappeDiProva.LENTA, 0, 3));
        assertThrows(IllegalArgumentException.class,
                () -> new VoceIncontro(MappeDiProva.LENTA, 6, 4));

        assertThrows(IllegalArgumentException.class,
                () -> new TabellaIncontri(List.of(), -0.1));
        assertThrows(IllegalArgumentException.class,
                () -> new TabellaIncontri(List.of(), 1.1));
    }

    @Test
    @DisplayName("la tabella non si lascia modificare da fuori")
    void tabellaImmutabile() {
        final TabellaIncontri tabella = MappeDiProva.faunaCerta();

        assertThrows(UnsupportedOperationException.class, () -> tabella.voci().clear());
    }
}
