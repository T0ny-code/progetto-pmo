package gdr.battaglia;

import java.util.List;
import java.util.Objects;

/**
 * La definizione immutabile di una creatura: nome, elemento, statistiche base
 * e mosse che impara.
 *
 * Le statistiche qui sono quelle al livello 1; la creatura viva le ricava dal
 * proprio livello.
 */
public record Specie(String nome,
                     Elemento elemento,
                     int psBase,
                     int attaccoBase,
                     int difesaBase,
                     int velocitaBase,
                     List<Mossa> mosseApprese) {

    /** Quante mosse conosce ogni specie: una debole e sicura, una potente e rischiosa. */
    public static final int MOSSE_PER_SPECIE = 2;

    public Specie {
        Objects.requireNonNull(elemento, "L'elemento non puo' essere nullo");
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Il nome della specie non puo' essere vuoto");
        }
        if (psBase <= 0 || attaccoBase <= 0 || difesaBase <= 0 || velocitaBase <= 0) {
            throw new IllegalArgumentException(
                    "Le statistiche base di " + nome + " devono essere positive");
        }
        // Esattamente due, perche' la scelta fra una mossa sicura e una
        // rischiosa e' l'unica decisione tattica del gioco. E' da qui che il
        // menu di battaglia ricava quante voci disegnare.
        if (mosseApprese == null || mosseApprese.size() != MOSSE_PER_SPECIE) {
            throw new IllegalArgumentException(
                    nome + " deve conoscere esattamente " + MOSSE_PER_SPECIE + " mosse");
        }
        mosseApprese = List.copyOf(mosseApprese);
    }
}
