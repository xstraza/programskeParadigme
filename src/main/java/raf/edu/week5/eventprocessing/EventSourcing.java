package raf.edu.week5.eventprocessing;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.*;

/**
 * Nedelja 5 — Event Sourcing: Immutable stanje kroz reduce
 *
 * Scenario: bankovni račun gde se svaka operacija beleži kao event.
 * Stanje računa se NIKAD ne mutira — uvek se rekonstruiše iz eventova.
 *
 * Demonstrira:
 *   1. Sealed interface za tipove eventova
 *   2. reduce/fold za rekonstrukciju stanja
 *   3. Time-travel: stanje u bilo kom trenutku
 *   4. Undo: ponovi sve osim poslednjeg eventa
 *   5. Audit log: kompletna istorija promena
 */
public class EventSourcing {

    // =========================================================================
    // Eventi — sealed interface garantuje da su svi tipovi poznati
    // =========================================================================

    sealed interface BankEvent {
        LocalDateTime vreme();
        String opis();
    }

    record Otvaranje(LocalDateTime vreme, String vlasnik, double pocetniDepozit)
            implements BankEvent {
        public String opis() { return String.format("Otvaranje računa za %s (%.2f RSD)", vlasnik, pocetniDepozit); }
    }

    record Uplata(LocalDateTime vreme, double iznos, String izvor)
            implements BankEvent {
        public String opis() { return String.format("Uplata %.2f RSD od: %s", iznos, izvor); }
    }

    record Isplata(LocalDateTime vreme, double iznos, String svrha)
            implements BankEvent {
        public String opis() { return String.format("Isplata %.2f RSD za: %s", iznos, svrha); }
    }

    record Kamata(LocalDateTime vreme, double procenat)
            implements BankEvent {
        public String opis() { return String.format("Obračun kamate %.1f%%", procenat); }
    }

    // =========================================================================
    // Stanje — immutable record, rekonstruiše se iz eventova
    // =========================================================================

    record StanjeRacuna(
            String vlasnik,
            double balans,
            int brojTransakcija,
            List<String> istorija     // audit log
    ) {
        static final StanjeRacuna POCETNO = new StanjeRacuna("", 0, 0, List.of());

        /**
         * Ključna funkcija: (Stanje, Event) → NovoStanje
         * Ovo je čista funkcija — nema side-effecta, uvek isti rezultat za iste argumente.
         */
        StanjeRacuna primeni(BankEvent event) {
            List<String> novaIstorija = new ArrayList<>(istorija);
            novaIstorija.add(event.opis());

            return switch (event) {
                case Otvaranje o -> new StanjeRacuna(
                        o.vlasnik(), o.pocetniDepozit(), brojTransakcija + 1, novaIstorija);

                case Uplata u -> new StanjeRacuna(
                        vlasnik, balans + u.iznos(), brojTransakcija + 1, novaIstorija);

                case Isplata i -> {
                    if (i.iznos() > balans) {
                        novaIstorija.set(novaIstorija.size() - 1,
                                i.opis() + " [ODBIJENA - nedovoljno sredstava]");
                        yield new StanjeRacuna(vlasnik, balans, brojTransakcija + 1, novaIstorija);
                    }
                    yield new StanjeRacuna(
                            vlasnik, balans - i.iznos(), brojTransakcija + 1, novaIstorija);
                }

                case Kamata k -> new StanjeRacuna(
                        vlasnik, balans * (1 + k.procenat() / 100), brojTransakcija + 1, novaIstorija);
            };
        }

        @Override
        public String toString() {
            return String.format("Račun[%s | balans: %.2f RSD | transakcija: %d]",
                    vlasnik, balans, brojTransakcija);
        }
    }

    // =========================================================================
    // Event sourcing operacije
    // =========================================================================

    /**
     * Rekonstrukcija stanja: reduce svih eventova.
     * Ceo niz eventova → jedno stanje.
     */
    static StanjeRacuna rekonstruisi(List<BankEvent> eventi) {
        return eventi.stream()
                .reduce(StanjeRacuna.POCETNO,
                        StanjeRacuna::primeni,
                        (a, b) -> b);  // combiner za paralelne streamove (ne koristi se ovde)
    }

    /**
     * Time-travel: stanje računa u datom trenutku.
     * Primenimo samo evente koji su se desili pre tog trenutka.
     */
    static StanjeRacuna stanjeNaDatum(List<BankEvent> eventi, LocalDateTime doKada) {
        return eventi.stream()
                .filter(e -> !e.vreme().isAfter(doKada))
                .reduce(StanjeRacuna.POCETNO,
                        StanjeRacuna::primeni,
                        (a, b) -> b);
    }

    /**
     * Undo: primenimo sve evente osim poslednjeg.
     */
    static StanjeRacuna undo(List<BankEvent> eventi) {
        if (eventi.isEmpty()) return StanjeRacuna.POCETNO;
        return rekonstruisi(eventi.subList(0, eventi.size() - 1));
    }

    /**
     * Audit log: ispis kompletne istorije.
     */
    static void auditLog(List<BankEvent> eventi) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

        System.out.println("  Datum             | Opis");
        System.out.println("  " + "─".repeat(65));

        // Kumulativni ispis sa balansom posle svakog eventa
        eventi.stream()
                .reduce(StanjeRacuna.POCETNO,
                        (stanje, event) -> {
                            StanjeRacuna novo = stanje.primeni(event);
                            System.out.printf("  %s | %-45s | %.2f RSD%n",
                                    event.vreme().format(fmt), event.opis(), novo.balans());
                            return novo;
                        },
                        (a, b) -> b);
    }

    // =========================================================================
    // main
    // =========================================================================

    public static void main(String[] args) {
        // Niz eventova — ovo je "source of truth"
        LocalDateTime now = LocalDateTime.of(2024, 1, 15, 9, 0);

        List<BankEvent> eventi = List.of(
                new Otvaranje(now,                          "Ana Petrović", 10000),
                new Uplata(now.plusDays(5),                  3000,  "Plata"),
                new Isplata(now.plusDays(7),                 2500,  "Kirija"),
                new Uplata(now.plusDays(15),                 5000,  "Freelance"),
                new Isplata(now.plusDays(20),                1000,  "Računi"),
                new Kamata(now.plusDays(30),                 0.5),
                new Isplata(now.plusDays(35),                50000, "Auto"),   // biće odbijena
                new Uplata(now.plusDays(40),                 2000,  "Poklon")
        );

        // 1. Rekonstrukcija finalnog stanja
        System.out.println("=== 1. Finalno stanje (reduce svih eventova) ===\n");
        StanjeRacuna finalno = rekonstruisi(eventi);
        System.out.println("  " + finalno);

        // 2. Time-travel
        System.out.println("\n=== 2. Time-travel: stanje na dan 20.01.2024 ===\n");
        StanjeRacuna stanje20jan = stanjeNaDatum(eventi, now.plusDays(5));
        System.out.println("  " + stanje20jan);

        // 3. Undo
        System.out.println("\n=== 3. Undo: stanje bez poslednjeg eventa ===\n");
        StanjeRacuna undoStanje = undo(eventi);
        System.out.println("  Pre undo:  " + finalno);
        System.out.println("  Posle undo: " + undoStanje);

        // 4. Audit log
        System.out.println("\n=== 4. Kompletni audit log ===\n");
        auditLog(eventi);

        // 5. Statistika iz eventova
        System.out.println("\n=== 5. Statistika iz eventova ===\n");
        statistika(eventi);
    }

    static void statistika(List<BankEvent> eventi) {
        // Koliko uplata, isplata, ukupan promet — sve iz stream-a eventova
        double ukupnoUplata = eventi.stream()
                .filter(e -> e instanceof Uplata)
                .mapToDouble(e -> ((Uplata) e).iznos())
                .sum();

        double ukupnoIsplata = eventi.stream()
                .filter(e -> e instanceof Isplata)
                .mapToDouble(e -> ((Isplata) e).iznos())
                .sum();

        long brojUplata = eventi.stream().filter(e -> e instanceof Uplata).count();
        long brojIsplata = eventi.stream().filter(e -> e instanceof Isplata).count();

        System.out.printf("  Uplata:  %d transakcija, ukupno %.2f RSD%n", brojUplata, ukupnoUplata);
        System.out.printf("  Isplata: %d transakcija, ukupno %.2f RSD%n", brojIsplata, ukupnoIsplata);
        System.out.printf("  Neto:    %.2f RSD%n", ukupnoUplata - ukupnoIsplata);
    }
}
