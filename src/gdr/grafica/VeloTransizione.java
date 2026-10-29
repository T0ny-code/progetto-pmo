package gdr.grafica;

/**
 * Il velo nero che copre il cambio di zona.
 *
 * Parte dal nero pieno e si schiarisce fino a sparire: quando comincia, il
 * cambio di zona nella logica e' gia' avvenuto, quindi il velo copre il salto
 * invece di scandirlo.
 *
 * Non c'e' un campo "attivo": la dissolvenza e' in corso finche' il tempo
 * trascorso non ha raggiunto la durata, e partire con i due valori uguali
 * significa partire a velo gia' spento.
 *
 * Nonostante stia in gdr.grafica non usa AWT: e' un cronometro, e chi disegna
 * gli chiede soltanto quanto e' nero adesso.
 */
public final class VeloTransizione {

    private final double durata;
    private double trascorso;

    public VeloTransizione(final double durata) {
        if (durata <= 0.0) {
            throw new IllegalArgumentException("La durata deve essere positiva");
        }
        this.durata = durata;
        this.trascorso = durata;
    }

    /** Fa comparire il velo completamente nero. */
    public void avvia() {
        this.trascorso = 0.0;
    }

    /** Fa scorrere il tempo sulla dissolvenza. */
    public void avanza(final double secondi) {
        trascorso = Math.min(durata, trascorso + secondi);
    }

    /** Vero mentre la dissolvenza e' in corso. */
    public boolean attivo() {
        return trascorso < durata;
    }

    /** Opacita' del nero, da 1 (tutto nero) a 0 (trasparente). */
    public double opacita() {
        return 1.0 - trascorso / durata;
    }
}
