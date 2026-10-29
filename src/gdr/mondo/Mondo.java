package gdr.mondo;


import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * L'insieme delle zone e la zona in cui si trova il giocatore.
 *
 * La coerenza dei passaggi viene verificata in costruzione: una mappa mal
 * collegata fa fallire subito l'avvio invece di produrre un vicolo cieco
 * scoperto solo giocando.
 */
public final class Mondo {

    private final Map<String, Zona> zone = new LinkedHashMap<>();
    private String identificatoreCorrente;

    public Mondo(final Collection<Zona> zone, final String identificatoreIniziale) {
        Objects.requireNonNull(zone, "L'insieme delle zone non puo' essere nullo");
        Objects.requireNonNull(identificatoreIniziale, "La zona iniziale non puo' essere nulla");

        for (final Zona zona : zone) {
            final Zona precedente = this.zone.put(zona.identificatore(), zona);
            if (precedente != null) {
                throw new IllegalArgumentException(
                        "Due zone con lo stesso identificatore: " + zona.identificatore());
            }
        }
        if (!this.zone.containsKey(identificatoreIniziale)) {
            throw new IllegalArgumentException(
                    "La zona iniziale " + identificatoreIniziale + " non esiste");
        }
        this.identificatoreCorrente = identificatoreIniziale;

        verificaCoerenza();
    }

    /** La zona in cui si trova il giocatore. */
    public Zona zonaCorrente() {
        return zone.get(identificatoreCorrente);
    }

    /** Una zona per identificatore; serve solo qui dentro. */
    private Zona zona(final String identificatore) {
        final Zona zona = zone.get(identificatore);
        if (zona == null) {
            throw new IllegalArgumentException("Zona sconosciuta: " + identificatore);
        }
        return zona;
    }

    /**
     * Porta il giocatore in un'altra zona.
     *
     * La usano sia il passaggio attraversato camminando sia il ritorno al
     * punto di partenza dopo una sconfitta. La chiamata a zona() non serve a
     * leggere niente: serve a rifiutare subito un identificatore inesistente.
     */
    public void vaiA(final String identificatore) {
        zona(identificatore);
        identificatoreCorrente = identificatore;
    }

    /**
     * Controlla che ogni passaggio porti a una zona esistente, atterri su una
     * casella valida e abbia un corrispettivo che riporta indietro.
     */
    private void verificaCoerenza() {
        for (final Zona partenza : zone.values()) {
            for (final Passaggio passaggio : partenza.passaggi()) {
                final Zona destinazione = zone.get(passaggio.zonaDestinazione());
                if (destinazione == null) {
                    throw new IllegalStateException("Il passaggio di " + partenza.identificatore()
                            + " in " + passaggio.innesco() + " punta alla zona inesistente "
                            + passaggio.zonaDestinazione());
                }
                if (!destinazione.dentro(passaggio.arrivo())) {
                    throw new IllegalStateException("Il passaggio da " + partenza.identificatore()
                            + " a " + destinazione.identificatore() + " arriva in "
                            + passaggio.arrivo() + ", fuori dalla zona di destinazione");
                }
                if (!destinazione.calpestabile(passaggio.arrivo())) {
                    throw new IllegalStateException("Il passaggio da " + partenza.identificatore()
                            + " a " + destinazione.identificatore() + " arriva in "
                            + passaggio.arrivo() + ", su una casella non calpestabile");
                }
                if (destinazione.tipo(passaggio.arrivo()) == TipoTile.PASSAGGIO) {
                    throw new IllegalStateException("Il passaggio da " + partenza.identificatore()
                            + " a " + destinazione.identificatore() + " arriva in "
                            + passaggio.arrivo() + ", sopra un altro passaggio");
                }
                if (!esisteRitorno(destinazione, partenza.identificatore())) {
                    throw new IllegalStateException("La zona " + destinazione.identificatore()
                            + " non ha un passaggio di ritorno verso " + partenza.identificatore());
                }
            }
        }
    }

    private static boolean esisteRitorno(final Zona destinazione, final String versoZona) {
        for (final Passaggio passaggio : destinazione.passaggi()) {
            if (passaggio.zonaDestinazione().equals(versoZona)) {
                return true;
            }
        }
        return false;
    }
}
