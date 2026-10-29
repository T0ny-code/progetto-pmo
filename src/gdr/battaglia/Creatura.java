package gdr.battaglia;

import java.util.List;
import java.util.Objects;

/**
 * Una creatura viva: la sua specie, il livello, i PS che le restano e le mosse
 * che conosce.
 *
 * Le statistiche si ricavano dal livello con una formula deliberatamente
 * semplice, cosi' si puo' controllare a mente cosa succede quando si alza un
 * livello: valore = base + base * (livello - 1) / 5, a divisione intera.
 */
public final class Creatura {

    /**
     * Ogni livello aggiunge un quinto della statistica base.
     *
     * Con un decimo il livello non contava quasi nulla: una creatura selvatica
     * di livello 2 aveva quasi gli stessi PS del compagno di livello 5, e con
     * un elemento vantaggioso lo stendeva sempre. Misurato su diecimila
     * battaglie simulate.
     */
    private static final int DIVISORE_CRESCITA = 5;

    private final Specie specie;
    private final int livello;
    private final int psMassimi;
    private final int attacco;
    private final int difesa;
    private final int velocita;

    private int psAttuali;

    public Creatura(final Specie specie, final int livello) {
        this.specie = Objects.requireNonNull(specie, "La specie non puo' essere nulla");
        if (livello <= 0) {
            throw new IllegalArgumentException("Il livello deve essere positivo: " + livello);
        }
        this.livello = livello;

        this.psMassimi = statistica(specie.psBase(), livello);
        this.attacco = statistica(specie.attaccoBase(), livello);
        this.difesa = statistica(specie.difesaBase(), livello);
        this.velocita = statistica(specie.velocitaBase(), livello);
        this.psAttuali = psMassimi;
    }

    /** La formula di crescita, in un posto solo per le quattro statistiche. */
    private static int statistica(final int base, final int livello) {
        return base + base * (livello - 1) / DIVISORE_CRESCITA;
    }

    public Specie specie() {
        return specie;
    }

    public String nome() {
        return specie.nome();
    }

    public Elemento elemento() {
        return specie.elemento();
    }

    public int livello() {
        return livello;
    }

    public int psMassimi() {
        return psMassimi;
    }

    public int psAttuali() {
        return psAttuali;
    }

    public int attacco() {
        return attacco;
    }

    public int difesa() {
        return difesa;
    }

    public int velocita() {
        return velocita;
    }

    /**
     * Le mosse che conosce, in sola lettura.
     *
     * Sono quelle della specie e non cambiano: non essendoci punti uso, non
     * c'e' nessuno stato per mossa da tenere sulla singola creatura.
     */
    public List<Mossa> mosse() {
        return specie.mosseApprese();
    }

    /** Vero se non ha piu' PS e non puo' combattere. */
    public boolean esausta() {
        return psAttuali <= 0;
    }

    /** Applica un danno, senza scendere sotto zero. */
    public void subisci(final int danno) {
        if (danno < 0) {
            throw new IllegalArgumentException("Il danno non puo' essere negativo: " + danno);
        }
        psAttuali = Math.max(0, psAttuali - danno);
    }

    /** Riporta i PS al massimo. */
    public void curaCompletamente() {
        psAttuali = psMassimi;
    }
}
