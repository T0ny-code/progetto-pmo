package gdr.prova;

import gdr.battaglia.Elemento;
import gdr.battaglia.Mossa;
import gdr.battaglia.Specie;
import gdr.incontri.TabellaIncontri;
import gdr.incontri.VoceIncontro;
import gdr.mondo.LettoreZona;
import gdr.mondo.Mondo;
import gdr.mondo.Passaggio;
import gdr.mondo.Zona;
import gdr.nucleo.Direzione;
import gdr.nucleo.Posizione;

import java.util.List;

/**
 * Un mondo minimo, creature e mosse inventate apposta per le prove.
 *
 * Le prove non usano il contenuto vero del gioco (gdr.dati) tranne dove e'
 * proprio quello a essere messo alla prova: altrimenti ribilanciare una specie
 * farebbe fallire prove che parlano di tutt'altro. Qui i numeri sono tondi e
 * scelti per rendere i conti verificabili a mente.
 *
 * La mappa "prova" e' questa, cinque righe per sei colonne:
 *
 *   AAAAAA
 *   A....A
 *   A..*.&gt;
 *   A.HP.A
 *   AAAAAA
 *
 * Contiene tutto quello che serve: caselle calpestabili, alberi contro cui
 * sbattere, erba alta in (3, 2), la porta di un ospedale in (2, 3), la porta
 * di una casa qualsiasi in (3, 3) e un passaggio in (5, 2) verso la seconda
 * zona.
 */
public final class MappeDiProva {

    /** Identificatore della prima zona. */
    public static final String PROVA = "prova";

    /** Identificatore della seconda zona. */
    public static final String ALTRA = "altra";

    /** L'unica casella di erba alta della prima zona. */
    public static final Posizione ERBA_ALTA = new Posizione(3, 2);

    /** La porta dell'ospedale della prima zona. */
    public static final Posizione PORTA_OSPEDALE = new Posizione(2, 3);

    /** La porta di una casa qualsiasi: sbatterci contro non deve fare niente. */
    public static final Posizione PORTA_DI_CASA = new Posizione(3, 3);

    /** La casella che innesca il cambio di zona. */
    public static final Posizione PASSAGGIO = new Posizione(5, 2);

    /** Dove si arriva entrando nella seconda zona. */
    public static final Posizione ARRIVO_IN_ALTRA = new Posizione(1, 2);

    /** Una casella libera al centro della prima zona. */
    public static final Posizione LIBERA = new Posizione(2, 2);

    private static final List<String> MAPPA_PROVA = List.of(
            "AAAAAA",
            "A....A",
            "A..*.>",
            "A.HP.A",
            "AAAAAA");

    private static final List<String> MAPPA_ALTRA = List.of(
            "AAAAAA",
            "A....A",
            ">....A",
            "A....A",
            "AAAAAA");

    /** Mossa debole e sempre a segno. */
    public static final Mossa COLPO_SICURO = new Mossa("Colpo sicuro", Elemento.ELETTRO, 50, 100);

    /** Mossa potente ma che puo' mancare. */
    public static final Mossa COLPO_RISCHIOSO =
            new Mossa("Colpo rischioso", Elemento.ELETTRO, 80, 50);

    /** Specie d'elettro, veloce. */
    public static final Specie VELOCE = new Specie(
            "Veloce", Elemento.ELETTRO,
            20, 20, 10, 20,
            List.of(COLPO_SICURO, COLPO_RISCHIOSO));

    /** Specie d'acqua, lenta: l'elettro le e' superefficace contro. */
    public static final Specie LENTA = new Specie(
            "Lenta", Elemento.ACQUA,
            20, 20, 10, 5,
            List.of(new Mossa("Spruzzetto", Elemento.ACQUA, 50, 100),
                    new Mossa("Ondatina", Elemento.ACQUA, 80, 50)));

    /** Specie d'acqua con la stessa velocita' di VELOCE: serve ai pareggi. */
    public static final Specie PARI = new Specie(
            "Pari", Elemento.ACQUA,
            20, 20, 10, 20,
            List.of(new Mossa("Spruzzetto", Elemento.ACQUA, 50, 100),
                    new Mossa("Ondatina", Elemento.ACQUA, 80, 50)));

    private MappeDiProva() {
    }

    /**
     * Il mondo a due zone collegate nei due versi, con l'erba alta che fa
     * scattare un incontro al primo passo utile.
     */
    public static Mondo mondo() {
        return mondo(faunaCerta());
    }

    /** Lo stesso mondo ma senza fauna: ci si cammina senza mai incontrare niente. */
    public static Mondo mondoTranquillo() {
        return mondo(faunaVuota());
    }

    private static Mondo mondo(final TabellaIncontri fauna) {
        final Zona prova = LettoreZona.leggi(PROVA, MAPPA_PROVA,
                List.of(new Passaggio(PASSAGGIO, ALTRA, ARRIVO_IN_ALTRA, Direzione.EST)),
                fauna);

        final Zona altra = LettoreZona.leggi(ALTRA, MAPPA_ALTRA,
                List.of(new Passaggio(new Posizione(0, 2), PROVA,
                        new Posizione(4, 2), Direzione.OVEST)),
                fauna);

        return new Mondo(List.of(prova, altra), PROVA);
    }

    /** Fauna di una sola specie, con probabilita' 1: l'incontro dipende solo dalla tregua. */
    public static TabellaIncontri faunaCerta() {
        return new TabellaIncontri(List.of(new VoceIncontro(LENTA, 3, 3)), 1.0);
    }

    /** Fauna vuota: non si incontra mai niente. */
    public static TabellaIncontri faunaVuota() {
        return new TabellaIncontri(List.of(), 0.0);
    }
}
