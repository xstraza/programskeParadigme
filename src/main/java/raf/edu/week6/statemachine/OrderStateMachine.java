package raf.edu.week6.statemachine;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.*;

/**
 * Nedelja 6 — State Machine: životni ciklus narudžbine
 *
 * Stanja i tranzicije su modelovani deklarativno:
 *   - sealed enum za stanja
 *   - enum za akcije
 *   - mapa (stanje, akcija) → novo stanje
 *   - obrada niza akcija kroz reduce
 */
public class OrderStateMachine {

    // =========================================================================
    // Stanja i akcije
    // =========================================================================

    enum Status {
        KREIRANA, PLACENA, POSLATA, ISPORUCENA, OTKAZANA, VRACENA
    }

    enum Akcija {
        PLATI, POSALJI, ISPORUCI, OTKAZI, VRATI
    }

    // =========================================================================
    // Narudžbina — immutable record
    // =========================================================================

    record Narudzbina(
            String id,
            Status status,
            double iznos,
            List<String> istorija   // audit log tranzicija
    ) {
        static Narudzbina kreiraj(String id, double iznos) {
            return new Narudzbina(id, Status.KREIRANA, iznos,
                    List.of("[" + LocalDateTime.now().toLocalTime().withNano(0) + "] Kreirana"));
        }

        Narudzbina predjiU(Status noviStatus, String opis) {
            List<String> novaIstorija = new ArrayList<>(istorija);
            novaIstorija.add("[" + LocalDateTime.now().toLocalTime().withNano(0) + "] " + opis);
            return new Narudzbina(id, noviStatus, iznos, novaIstorija);
        }

        @Override
        public String toString() {
            return String.format("Narudzbina[%s | %s | %.2f RSD]", id, status, iznos);
        }
    }

    // =========================================================================
    // Tranzicije — deklarativna mapa
    //
    // Ovo je "srce" state machine-a. Sve dozvoljene tranzicije su na jednom mestu.
    // Dodavanje novog stanja/akcije = novi red u mapi, ništa drugo se ne menja.
    // =========================================================================

    record Tranzicija(Status iz, Akcija akcija) {}

    record TranzicijaRezultat(Status u, String opis) {}

    private static final Map<Tranzicija, TranzicijaRezultat> TRANZICIJE = Map.of(
            new Tranzicija(Status.KREIRANA, Akcija.PLATI),
            new TranzicijaRezultat(Status.PLACENA, "Plaćanje izvršeno"),

            new Tranzicija(Status.KREIRANA, Akcija.OTKAZI),
            new TranzicijaRezultat(Status.OTKAZANA, "Otkazana pre plaćanja"),

            new Tranzicija(Status.PLACENA, Akcija.POSALJI),
            new TranzicijaRezultat(Status.POSLATA, "Poslata kurirskom službom"),

            new Tranzicija(Status.PLACENA, Akcija.OTKAZI),
            new TranzicijaRezultat(Status.OTKAZANA, "Otkazana posle plaćanja — refund"),

            new Tranzicija(Status.POSLATA, Akcija.ISPORUCI),
            new TranzicijaRezultat(Status.ISPORUCENA, "Isporučena kupcu"),

            new Tranzicija(Status.POSLATA, Akcija.VRATI),
            new TranzicijaRezultat(Status.VRACENA, "Vraćena — kupac odbio prijem"),

            new Tranzicija(Status.ISPORUCENA, Akcija.VRATI),
            new TranzicijaRezultat(Status.VRACENA, "Vraćena — reklamacija")
    );

    // =========================================================================
    // Primena akcije — čista funkcija
    // =========================================================================

    /**
     * Primeni akciju na narudžbinu.
     * Ako tranzicija nije dozvoljena, vraća Optional.empty().
     */
    static Optional<Narudzbina> primeniAkciju(Narudzbina narudzbina, Akcija akcija) {
        return Optional.ofNullable(TRANZICIJE.get(new Tranzicija(narudzbina.status(), akcija)))
                .map(rez -> narudzbina.predjiU(rez.u(), rez.opis()));
    }

    /**
     * Primeni niz akcija redom. Staje na prvoj nedozvoljenoj tranziciji.
     */
    static Narudzbina primeniAkcije(Narudzbina narudzbina, List<Akcija> akcije) {
        Narudzbina trenutna = narudzbina;
        for (Akcija a : akcije) {
            Optional<Narudzbina> sledeca = primeniAkciju(trenutna, a);
            if (sledeca.isEmpty()) {
                System.out.printf("  ODBIJENA: %s u stanju %s%n", a, trenutna.status());
                return trenutna;
            }
            trenutna = sledeca.get();
        }
        return trenutna;
    }

    /**
     * FP verzija: primeni niz akcija koristeći reduce.
     * Koristi Optional da propagira neuspeh.
     */
    static Optional<Narudzbina> primeniAkcijeFP(Narudzbina narudzbina, List<Akcija> akcije) {
        return akcije.stream()
                .reduce(
                        Optional.of(narudzbina),
                        (optNar, akcija) -> optNar.flatMap(n -> primeniAkciju(n, akcija)),
                        (a, b) -> a.isPresent() ? a : b  // combiner (za paralelne)
                );
    }

    // =========================================================================
    // Pomoćne: dozvoljene akcije, validacija puta
    // =========================================================================

    /** Koje akcije su dozvoljene iz datog stanja? */
    static List<Akcija> dozvoljeneAkcije(Status status) {
        return TRANZICIJE.keySet().stream()
                .filter(t -> t.iz() == status)
                .map(Tranzicija::akcija)
                .sorted()
                .toList();
    }

    /** Da li je niz akcija validan put od KREIRANA do cilja? */
    static boolean jeValidanPut(List<Akcija> akcije, Status cilj) {
        Optional<Narudzbina> rezultat = primeniAkcijeFP(Narudzbina.kreiraj("test", 0), akcije);
        return rezultat.isPresent() && rezultat.get().status() == cilj;
    }

    // =========================================================================
    // main
    // =========================================================================
    public static void main(String[] args) {
        System.out.println("=== 1. Uspešan put: kreirana → plaćena → poslata → isporučena ===\n");
        Narudzbina n1 = Narudzbina.kreiraj("N-001", 5000);
        Narudzbina rezultat1 = primeniAkcije(n1, List.of(
                Akcija.PLATI, Akcija.POSALJI, Akcija.ISPORUCI));
        System.out.println("  " + rezultat1);
        System.out.println("  Istorija:");
        rezultat1.istorija().forEach(s -> System.out.println("    " + s));

        System.out.println("\n=== 2. Neuspešna tranzicija: ne možeš poslati pre plaćanja ===\n");
        Narudzbina n2 = Narudzbina.kreiraj("N-002", 3000);
        Narudzbina rezultat2 = primeniAkcije(n2, List.of(
                Akcija.POSALJI));  // nedozvoljeno iz KREIRANA!
        System.out.println("  " + rezultat2);

        System.out.println("\n=== 3. Otkazivanje ===\n");
        Narudzbina n3 = Narudzbina.kreiraj("N-003", 7000);
        Narudzbina rezultat3 = primeniAkcije(n3, List.of(
                Akcija.PLATI, Akcija.OTKAZI));
        System.out.println("  " + rezultat3);
        System.out.println("  Istorija:");
        rezultat3.istorija().forEach(s -> System.out.println("    " + s));

        System.out.println("\n=== 4. FP verzija sa Optional chain ===\n");
        Narudzbina n4 = Narudzbina.kreiraj("N-004", 2500);
        Optional<Narudzbina> fp1 = primeniAkcijeFP(n4, List.of(
                Akcija.PLATI, Akcija.POSALJI, Akcija.ISPORUCI));
        System.out.println("  Uspešan put: " + fp1);

        Optional<Narudzbina> fp2 = primeniAkcijeFP(n4, List.of(
                Akcija.PLATI, Akcija.ISPORUCI));  // ne može direktno plaćena → isporučena
        System.out.println("  Neuspešan put: " + fp2);

        System.out.println("\n=== 5. Dozvoljene akcije po stanju ===\n");
        for (Status s : Status.values()) {
            List<Akcija> dozvoljene = dozvoljeneAkcije(s);
            System.out.printf("  %-12s → %s%n", s, dozvoljene.isEmpty() ? "(završno stanje)" : dozvoljene);
        }

        System.out.println("\n=== 6. Validacija puteva ===\n");
        System.out.println("  PLATI→POSALJI→ISPORUCI vodi do ISPORUCENA? "
                + jeValidanPut(List.of(Akcija.PLATI, Akcija.POSALJI, Akcija.ISPORUCI), Status.ISPORUCENA));
        System.out.println("  PLATI→OTKAZI vodi do OTKAZANA? "
                + jeValidanPut(List.of(Akcija.PLATI, Akcija.OTKAZI), Status.OTKAZANA));
        System.out.println("  OTKAZI→PLATI vodi do PLACENA? "
                + jeValidanPut(List.of(Akcija.OTKAZI, Akcija.PLATI), Status.PLACENA));

        System.out.println("\n=== 7. Batch obrada narudžbina ===\n");
        batchObrada();
    }

    static void batchObrada() {
        // Simuliramo batch: lista narudžbina sa nizom akcija za svaku
        record NarudzbinaPlan(String id, double iznos, List<Akcija> akcije) {}

        List<NarudzbinaPlan> planovi = List.of(
                new NarudzbinaPlan("N-101", 1500, List.of(Akcija.PLATI, Akcija.POSALJI, Akcija.ISPORUCI)),
                new NarudzbinaPlan("N-102", 800,  List.of(Akcija.PLATI, Akcija.OTKAZI)),
                new NarudzbinaPlan("N-103", 3200, List.of(Akcija.PLATI, Akcija.POSALJI, Akcija.VRATI)),
                new NarudzbinaPlan("N-104", 500,  List.of(Akcija.POSALJI))  // nevalidan
        );

        // Obrada svih narudžbina — stream pipeline
        Map<Boolean, List<String>> rezultati = planovi.stream()
                .map(plan -> {
                    Narudzbina n = Narudzbina.kreiraj(plan.id(), plan.iznos());
                    Optional<Narudzbina> rezultat = primeniAkcijeFP(n, plan.akcije());
                    return Map.entry(plan.id(), rezultat);
                })
                .collect(Collectors.partitioningBy(
                        e -> e.getValue().isPresent(),
                        Collectors.mapping(
                                e -> e.getValue()
                                        .map(n -> e.getKey() + " → " + n.status())
                                        .orElse(e.getKey() + " → NEUSPEŠNO"),
                                Collectors.toList()
                        )
                ));

        System.out.println("  Uspešne:");
        rezultati.get(true).forEach(s -> System.out.println("    " + s));
        System.out.println("  Neuspešne:");
        rezultati.get(false).forEach(s -> System.out.println("    " + s));
    }
}
