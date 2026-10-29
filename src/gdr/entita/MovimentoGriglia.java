package gdr.entita;

import gdr.nucleo.Direzione;
import gdr.nucleo.Posizione;

import java.util.Objects;

/**
 * Lo stato del passo in corso: da quale casella a quale casella, e a che punto.
 *
 * La posizione logica resta sempre una casella intera: il progresso fra 0 e 1
 * serve soltanto a interpolare il disegno. Chi disegna chiede
 * {@link #colonnaInterpolata()} e {@link #rigaInterpolata()} e moltiplica per
 * la dimensione del tile; la logica di gioco usa {@link #casella()}.
 *
 * Questa classe non sa nulla della mappa: non conosce i muri. Decidere se un
 * passo e' lecito spetta a chi ha la zona sottomano, cosi' il movimento resta
 * verificabile da solo.
 */
public final class MovimentoGriglia {

    /**
     * Tolleranza nel confronto del progresso con 1.
     *
     * Serve perche' in virgola mobile sommare quindici volte un quindicesimo
     * non fa 1 ma 0,999...: senza tolleranza il passo durerebbe un
     * aggiornamento in piu' del dovuto, e quattro caselle al secondo
     * diventerebbero 3,75. Misurato, non supposto.
     */
    private static final double TOLLERANZA = 1e-9;

    private final double velocita;

    private Posizione casella;
    private Posizione destinazione;
    private Direzione sguardo;
    private double progresso;
    private boolean inTransito;
    private int passiCompletati;

    /**
     * @param velocita caselle al secondo; deve essere positiva.
     */
    public MovimentoGriglia(final Posizione casellaIniziale,
                            final Direzione sguardoIniziale,
                            final double velocita) {
        if (velocita <= 0.0) {
            throw new IllegalArgumentException(
                    "La velocita' deve essere positiva, ricevuta " + velocita);
        }
        this.casella = Objects.requireNonNull(casellaIniziale, "La casella non puo' essere nulla");
        this.sguardo = Objects.requireNonNull(sguardoIniziale, "Lo sguardo non puo' essere nullo");
        this.velocita = velocita;
        this.destinazione = null;
        this.progresso = 0.0;
        this.inTransito = false;
        this.passiCompletati = 0;
    }

    /** La casella occupata: durante un passo resta quella di partenza. */
    public Posizione casella() {
        return casella;
    }

    /** La direzione verso cui il personaggio e' girato. */
    public Direzione sguardo() {
        return sguardo;
    }

    /** Vero se un passo e' in corso. */
    public boolean inTransito() {
        return inTransito;
    }

    /** Quanto e' avanzato il passo in corso, fra 0 e 1. */
    public double progresso() {
        return progresso;
    }

    /** Quante caselle sono state percorse in tutto; utile per alternare i piedi. */
    public int passiCompletati() {
        return passiCompletati;
    }

    /** Gira il personaggio senza muoverlo. */
    public void orienta(final Direzione direzione) {
        this.sguardo = Objects.requireNonNull(direzione, "La direzione non puo' essere nulla");
    }

    /** Avvia un passo verso la casella adiacente. */
    public void inizia(final Direzione direzione) {
        Objects.requireNonNull(direzione, "La direzione non puo' essere nulla");
        if (inTransito) {
            throw new IllegalStateException("Un passo e' gia' in corso verso " + destinazione);
        }
        this.sguardo = direzione;
        this.destinazione = casella.spostata(direzione);
        this.progresso = 0.0;
        this.inTransito = true;
    }

    /**
     * Fa scorrere il tempo sul passo in corso; non fa niente se non ce n'e'
     * uno.
     *
     * Il tempo che avanza oltre la fine del passo si scarta: un passo per
     * aggiornamento. Prima veniva restituito e riversato nel successivo, e la
     * camminata era continua al prezzo di aggiornamenti che concludevano due
     * passi, con i guai spiegati in {@link gdr.partita.Esplorazione#aggiorna}.
     * Con il passo fisso del ciclo di gioco la differenza non si vede: una
     * casella dura un numero intero di aggiornamenti.
     *
     * Attenzione se si toccano i numeri: scartare l'avanzo fa costare un passo
     * ceil(1 / (velocita * passo)) aggiornamenti, quindi la velocita' vera e'
     * quella dichiarata solo quando quel conto e' intero. Con 4 caselle al
     * secondo e 1/60 fa 15 esatti; a 1/30, o con velocita' 4,5, si camminerebbe
     * piu' lenti di quanto dice il numero. Misurato: a 1/30 vengono 3,75
     * caselle al secondo.
     */
    public void avanza(final double secondi) {
        if (!inTransito) {
            return;
        }

        progresso += secondi * velocita;
        if (progresso < 1.0 - TOLLERANZA) {
            return;
        }

        casella = destinazione;
        destinazione = null;
        progresso = 0.0;
        inTransito = false;
        passiCompletati++;
    }

    /** Sposta di colpo il personaggio, interrompendo ogni passo in corso. */
    public void teletrasporta(final Posizione nuovaCasella, final Direzione nuovoSguardo) {
        this.casella = Objects.requireNonNull(nuovaCasella, "La casella non puo' essere nulla");
        this.sguardo = Objects.requireNonNull(nuovoSguardo, "Lo sguardo non puo' essere nullo");
        this.destinazione = null;
        this.progresso = 0.0;
        this.inTransito = false;
    }

    /** Colonna in caselle frazionarie, per il disegno. */
    public double colonnaInterpolata() {
        if (!inTransito) {
            return casella.colonna();
        }
        return casella.colonna() + (destinazione.colonna() - casella.colonna()) * progresso;
    }

    /** Riga in caselle frazionarie, per il disegno. */
    public double rigaInterpolata() {
        if (!inTransito) {
            return casella.riga();
        }
        return casella.riga() + (destinazione.riga() - casella.riga()) * progresso;
    }
}
