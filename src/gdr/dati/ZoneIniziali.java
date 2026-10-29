package gdr.dati;

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
 * Le due zone esterne del primo traguardo, scritte come mappe testuali.
 *
 * Legenda dei simboli: '.' sentiero, ',' erba bassa, '*' erba alta,
 * 'A' albero, '~' acqua, '>' passaggio verso un'altra zona,
 * 'E' edificio, 'P' porta, 'O' ospedale, 'H' porta dell'ospedale.
 *
 * L'edificio centrale della radura e' l'ospedale: sbattendo contro la sua
 * porta il personaggio entra per qualche secondo e riesce con la creatura
 * curata. Gli interni non si vedono, quindi non vanno disegnati.
 *
 * I due passaggi sono affacciati sulla stessa riga: uscendo a est dalla radura
 * si entra nel bosco e viceversa. La casella d'arrivo e' sempre quella
 * adiacente al passaggio, mai il passaggio stesso, cosi' non si rimbalza
 * avanti e indietro fra le due zone.
 */
public final class ZoneIniziali {

    /** Identificatore della zona di partenza. */
    private static final String RADURA = "radura";

    /** Identificatore della seconda zona. */
    public static final String BOSCO = "bosco";

    /** Riga su cui si affacciano i due passaggi. */
    private static final int RIGA_PASSAGGIO = 7;

    /** Larghezza comune alle due mappe. */
    private static final int LARGHEZZA = 20;

    /**
     * Probabilita' di incontro per ogni passo compiuto nell'erba alta. Con i
     * passi di tregua imposti dal generatore viene un incontro ogni dieci
     * caselle circa: piu' spesso annoia, piu' raramente fa sembrare l'erba
     * alta inerte.
     */
    private static final double PROBABILITA_INCONTRO = 0.12;

    /**
     * La fauna della radura: tre specie equiprobabili.
     *
     * Prima erano due, con Rugiado al 75%, e in dieci incontri se ne vedevano
     * nove uguali: la zona sembrava avere un solo mostro.
     *
     * Sono tre e non quattro perche' Verzino resta fuori: e' lo scontro piu'
     * lungo del gioco, e la radura e' la zona in cui si impara a giocare. Non
     * e' piu' una questione di vincibilita' — a questi livelli si vince sempre
     * — ma di ordine in cui si incontrano le cose.
     */
    private static final TabellaIncontri FAUNA_RADURA = new TabellaIncontri(
            List.of(new VoceIncontro(Bestiario.RUGIADO, 2, 4),
                    new VoceIncontro(Bestiario.MAREZZO, 2, 4),
                    new VoceIncontro(Bestiario.FULIGNO, 2, 3)),
            PROBABILITA_INCONTRO);

    /**
     * La fauna del bosco: tutte e quattro le specie selvatiche, un quarto
     * ciascuna, con livelli piu' alti.
     *
     * Qui Verzino ci sta, ed e' il momento in cui conviene decidere se
     * combattere o scappare. Misurate con il compagno al livello 5 e la scelta
     * della mossa migliore, le quattro battaglie della zona danno:
     *
     *   Verzino   7,5 turni   75% di vittorie
     *   Fuligno   6,0 turni   67% di vittorie
     *   Rugiado   4,4 turni  100% di vittorie
     *   Marezzo   4,3 turni   98% di vittorie
     *
     * Verzino e' la battaglia piu' lunga, ma la piu' rischiosa e' Fuligno, che
     * ha l'attacco piu' alto e la mossa piu' potente del bestiario: e' corta
     * perche' si decide presto, non perche' sia facile. In entrambi i casi la
     * fuga e' una scelta sensata e non un ripiego obbligato.
     */
    private static final TabellaIncontri FAUNA_BOSCO = new TabellaIncontri(
            List.of(new VoceIncontro(Bestiario.VERZINO, 4, 6),
                    new VoceIncontro(Bestiario.RUGIADO, 4, 5),
                    new VoceIncontro(Bestiario.MAREZZO, 4, 6),
                    new VoceIncontro(Bestiario.FULIGNO, 4, 6)),
            PROBABILITA_INCONTRO);

    private static final List<String> MAPPA_RADURA = List.of(
            "AAAAAAAAAAAAAAAAAAAA",
            "A,EEEE,,OOOO,,EEEE,A",
            "A,EEEE,,OOOO,,EEEE,A",
            "A,EEEE,,OOOO,,EEEE,A",
            "A,EPEE,,OOHO,,EPEE,A",
            "A,,,,,,,,,,,,,,,,,,A",
            "A,,**,,,,,,,,,,***,A",
            "A..................>",
            "A,,,,,,,,,,,,,,,,,,A",
            "A,,~~~~,,,AAA,,,,,,A",
            "A,~~~~~~,,AAA,,***,A",
            "A,,~~~~,,,,,,,,***,A",
            "A,,,,,,,,,,,,,,,,,,A",
            "A,,,,,,,,,,,,,,,,,,A",
            "AAAAAAAAAAAAAAAAAAAA");

    private static final List<String> MAPPA_BOSCO = List.of(
            "AAAAAAAAAAAAAAAAAAAA",
            "A,,,A,,,,,A,,,,A,,,A",
            "A,***,,,***,,,***,,A",
            "A,***,,,***,,,***,,A",
            "A,,,,,A,,,,,A,,,,,,A",
            "A,,,,,,,,,,,,,,,,,,A",
            "A,,,,,,,,,,,,,,,,,,A",
            ">..................A",
            "A,,,,,,,,,,,,,,,,,,A",
            "A,,A,,***,,,A,,***,A",
            "A,,,,,***,,,,,,***,A",
            "A,,,,,,,,,,,,,,,,,,A",
            "A,,,A,,,,,AAA,,,,,,A",
            "A,,,,,,,,,,,,,,,,,,A",
            "AAAAAAAAAAAAAAAAAAAA");

    private ZoneIniziali() {
    }

    /** Il mondo del primo traguardo, con la radura come zona di partenza. */
    public static Mondo creaMondo() {
        final Zona radura = LettoreZona.leggi(RADURA, MAPPA_RADURA,
                List.of(new Passaggio(
                        new Posizione(LARGHEZZA - 1, RIGA_PASSAGGIO),
                        BOSCO,
                        new Posizione(1, RIGA_PASSAGGIO),
                        Direzione.EST)),
                FAUNA_RADURA);

        final Zona bosco = LettoreZona.leggi(BOSCO, MAPPA_BOSCO,
                List.of(new Passaggio(
                        new Posizione(0, RIGA_PASSAGGIO),
                        RADURA,
                        new Posizione(LARGHEZZA - 2, RIGA_PASSAGGIO),
                        Direzione.OVEST)),
                FAUNA_BOSCO);

        return new Mondo(List.of(radura, bosco), RADURA);
    }

    /** Dove comincia il giocatore, nella radura. */
    public static Posizione posizioneIniziale() {
        return new Posizione(4, RIGA_PASSAGGIO);
    }
}
