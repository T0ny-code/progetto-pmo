package gdr.mondo;

/**
 * I terreni e le strutture che compongono una zona.
 *
 * Ogni tipo porta con se' il simbolo usato nelle mappe testuali, se si puo'
 * camminarci sopra e se puo' far scattare un incontro. Il tipo PASSAGGIO dice
 * soltanto "qui si cambia zona": la destinazione e' scritta nella lista di
 * passaggi della zona, cosi' lo stesso simbolo puo' portare in posti diversi.
 *
 * Gli edifici occupano piu' caselle e sono disegnati nella mappa testuale come
 * blocchi di EDIFICIO con una PORTA sulla fila in basso. Che aspetto abbia una
 * singola casella (tetto, muro, finestra) lo ricava il renderizzatore dai
 * vicini: vedi gdr.grafica.ParteEdificio. Gli interni sono fuori scope, quindi
 * nessuna porta e' calpestabile: sbatterci contro produce un evento, e per
 * l'ospedale quell'evento fa entrare il personaggio per qualche secondo.
 */
public enum TipoTile {

    SENTIERO('.', true, false),
    ERBA(',', true, false),
    ERBA_ALTA('*', true, true),
    ALBERO('A', false, false),
    ACQUA('~', false, false),
    PASSAGGIO('>', true, false),
    EDIFICIO('E', false, false),
    PORTA('P', false, false),
    OSPEDALE('O', false, false),
    PORTA_OSPEDALE('H', false, false);

    private final char simbolo;
    private final boolean calpestabile;
    private final boolean generaIncontri;

    TipoTile(final char simbolo, final boolean calpestabile, final boolean generaIncontri) {
        this.simbolo = simbolo;
        this.calpestabile = calpestabile;
        this.generaIncontri = generaIncontri;
    }

    /** Vero se il giocatore puo' entrare in questa casella. */
    public boolean calpestabile() {
        return calpestabile;
    }

    /** Vero se camminarci sopra puo' far scattare un incontro. */
    public boolean generaIncontri() {
        return generaIncontri;
    }

    /** Vero se fa parte di un edificio, ospedale compreso. */
    public boolean parteDiEdificio() {
        return this == EDIFICIO || this == PORTA || this == OSPEDALE || this == PORTA_OSPEDALE;
    }

    /** Vero se e' una porta, di casa o di ospedale. */
    public boolean porta() {
        return this == PORTA || this == PORTA_OSPEDALE;
    }

    /** Vero se appartiene all'ospedale. */
    public boolean ospedale() {
        return this == OSPEDALE || this == PORTA_OSPEDALE;
    }

    /** Il tipo corrispondente a un simbolo di mappa. */
    public static TipoTile daSimbolo(final char simbolo) {
        for (final TipoTile tipo : values()) {
            if (tipo.simbolo == simbolo) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Simbolo di mappa sconosciuto: '" + simbolo + "'");
    }
}
