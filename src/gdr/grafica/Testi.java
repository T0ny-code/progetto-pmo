package gdr.grafica;

import gdr.battaglia.Elemento;
import gdr.battaglia.Esito;
import gdr.battaglia.EventoBattaglia;

/**
 * Trasforma un evento di battaglia nella frase da mostrare.
 *
 * La lingua sta qui e non nella logica: gli eventi portano nomi e numeri, non
 * testo. Cosi' la logica resta verificabile senza confrontare stringhe, e
 * tradurre il gioco vorrebbe dire toccare solo questa classe.
 */
public final class Testi {

    private Testi() {
    }

    /**
     * La frase per un evento, o stringa vuota se l'evento non ha niente da
     * dire e serve solo ad animare qualcosa, come il calo dei PS.
     */
    public static String frase(final EventoBattaglia evento) {
        return switch (evento) {
            case EventoBattaglia.Comparsa comparsa ->
                    "Un " + comparsa.nomeCreatura() + " selvatico"
                            + " di livello " + comparsa.livello() + " compare!";

            case EventoBattaglia.MossaUsata mossa -> mossa.delGiocatore()
                    ? mossa.nomeCreatura() + " usa " + mossa.nomeMossa() + "!"
                    : "Il " + mossa.nomeCreatura() + " selvatico usa "
                            + mossa.nomeMossa() + "!";

            case EventoBattaglia.MossaMancata mancata -> mancata.delGiocatore()
                    ? mancata.nomeCreatura() + " manca il colpo!"
                    : "Il " + mancata.nomeCreatura() + " selvatico manca il colpo!";

            case EventoBattaglia.EfficaciaRilevata efficacia ->
                    efficacia.moltiplicatore() > Elemento.NEUTRO
                            ? "È superefficace!"
                            : "Non è molto efficace...";

            case EventoBattaglia.CreaturaEsausta esausta -> esausta.delGiocatore()
                    ? esausta.nomeCreatura() + " non ce la fa più!"
                    : "Il " + esausta.nomeCreatura() + " selvatico è esausto!";

            case EventoBattaglia.FugaRiuscita ignorato -> "Sei riuscito a scappare!";

            case EventoBattaglia.FugaFallita ignorato -> "Non riesci a scappare!";

            case EventoBattaglia.FineBattaglia fine -> frase(fine.esito());

            // Il danno non si racconta: si vede scendere la barra.
            case EventoBattaglia.DannoSubito ignorato -> "";
        };
    }

    private static String frase(final Esito esito) {
        return switch (esito) {
            case VITTORIA -> "Hai vinto lo scontro!";
            case SCONFITTA -> "La tua creatura è stata stesa...";
            case FUGA -> "";
        };
    }

    /** Quanto tenere in scena un evento, in secondi. */
    public static double durata(final EventoBattaglia evento) {
        return switch (evento) {
            case EventoBattaglia.Comparsa ignorato -> 1.3;
            case EventoBattaglia.MossaUsata ignorato -> 0.9;
            case EventoBattaglia.MossaMancata ignorato -> 1.0;
            case EventoBattaglia.EfficaciaRilevata ignorato -> 0.9;
            case EventoBattaglia.DannoSubito ignorato -> 0.6;
            case EventoBattaglia.CreaturaEsausta ignorato -> 1.3;
            case EventoBattaglia.FugaRiuscita ignorato -> 1.2;
            case EventoBattaglia.FugaFallita ignorato -> 1.1;
            case EventoBattaglia.FineBattaglia ignorato -> 1.5;
        };
    }

    /** Il messaggio mostrato mentre il personaggio e' dentro l'ospedale. */
    public static String visitaOspedale() {
        return "La tua creatura si sta rimettendo...";
    }

    /** Il messaggio mostrato quando la creatura e' stata stesa. */
    public static String svenimento() {
        return "Ti sei risvegliato all'ospedale.";
    }
}
