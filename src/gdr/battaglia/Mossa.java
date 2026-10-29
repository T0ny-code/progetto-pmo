package gdr.battaglia;

import java.util.Objects;

/**
 * Una mossa, come definizione immutabile.
 *
 * Non esiste un elemento "normale": ogni mossa ha uno dei quattro elementi del
 * gioco. Ogni creatura ne conosce due del proprio elemento con profili
 * diversi, una debole e sicura e una potente e rischiosa, che e' una scelta
 * tattica vera anche con un solo elemento a disposizione.
 *
 * Le mosse si possono usare quante volte si vuole: non ci sono punti uso. Ne
 * avevamo messi, ma con trenta cariche e la cura gratuita all'ospedale non si
 * esaurivano mai, e un meccanismo che non entra in gioco e' solo codice e
 * numeri a schermo da spiegare.
 */
public record Mossa(String nome,
                    Elemento elemento,
                    int potenza,
                    int precisione) {

    public Mossa {
        Objects.requireNonNull(elemento, "L'elemento non puo' essere nullo");
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Il nome della mossa non puo' essere vuoto");
        }
        if (potenza <= 0) {
            throw new IllegalArgumentException("La potenza deve essere positiva: " + potenza);
        }
        if (precisione <= 0 || precisione > 100) {
            throw new IllegalArgumentException(
                    "La precisione va da 1 a 100, ricevuta " + precisione);
        }
    }

    /** La precisione come probabilita' fra 0 e 1. */
    public double probabilitaDiColpire() {
        return precisione / 100.0;
    }
}
