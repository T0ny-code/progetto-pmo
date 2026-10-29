package gdr.grafica;

/**
 * Decide quale porzione della zona inquadrare.
 *
 * Segue il giocatore tenendolo al centro, ma si blocca ai bordi della zona per
 * non mostrare il vuoto oltre la mappa. Da' per scontato che la zona sia
 * almeno grande quanto la vista, come sono entrambe le zone del gioco.
 *
 * Nonostante stia in gdr.grafica non usa AWT: e' pura aritmetica, quindi le
 * si puo' ragionare su carta.
 */
public final class Telecamera {

    private final int larghezzaVista;
    private final int altezzaVista;

    /** Dimensioni della vista in caselle. */
    public Telecamera(final int larghezzaVista, final int altezzaVista) {
        if (larghezzaVista <= 0 || altezzaVista <= 0) {
            throw new IllegalArgumentException("La vista deve avere dimensioni positive");
        }
        this.larghezzaVista = larghezzaVista;
        this.altezzaVista = altezzaVista;
    }

    public int larghezzaVista() {
        return larghezzaVista;
    }

    public int altezzaVista() {
        return altezzaVista;
    }

    /** Colonna, in caselle frazionarie, mostrata al bordo sinistro della vista. */
    public double scostamentoColonna(final double colonnaGiocatore, final int larghezzaZona) {
        return scostamento(colonnaGiocatore, larghezzaZona, larghezzaVista);
    }

    /** Riga, in caselle frazionarie, mostrata al bordo superiore della vista. */
    public double scostamentoRiga(final double rigaGiocatore, final int altezzaZona) {
        return scostamento(rigaGiocatore, altezzaZona, altezzaVista);
    }

    private static double scostamento(final double posizione,
                                      final int dimensioneZona,
                                      final int dimensioneVista) {
        final double centrato = posizione + 0.5 - dimensioneVista / 2.0;
        final double massimo = dimensioneZona - dimensioneVista;
        return Math.max(0.0, Math.min(massimo, centrato));
    }
}
