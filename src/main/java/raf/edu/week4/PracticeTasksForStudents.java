package raf.edu.week4;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * Paradigme Programiranja — Nedelja 4
 * Zadaci za vežbu
 *
 * Napišite rešenje tamo gde piše // TODO
 * Ne menjajte potpise metoda.
 */
public class PracticeTasksForStudents {
    /**
     * Data je lista studenata (ime, poeni). Koristeći Collectors.joining,
     * napravite string u formatu:
     *   "Rang lista: 1. Ana (92), 2. Bojan (85), 3. Cara (78)"
     *
     * Studente sortirajte po poenima opadajuće. Rang počinje od 1.
     *
     * Pomoć: koristite IntStream.range ili AtomicInteger za rang.
     */
    record StudentPoeni(String ime, int poeni) {}

    static String rangLista(List<StudentPoeni> studenti) {
        // TODO
        return "";
    }

    /**
     * Data je lista tagova za blog postove.
     * Svaki post ima naslov i listu tagova.
     * Koristeći Collectors.flatMapping unutar groupingBy,
     * grupisajte NASLOVE postova po tagu.
     *
     * Primer:
     *   postovi = [
     *     ("Java Streams", ["java", "fp"]),
     *     ("Kotlin Intro", ["kotlin", "fp"]),
     *     ("Java Optional", ["java"])
     *   ]
     *   → {"java": ["Java Streams", "Java Optional"],
     *      "fp": ["Java Streams", "Kotlin Intro"],
     *      "kotlin": ["Kotlin Intro"]}
     *
     * Napomena: ovde flatMapping ide "unazad" — za svaki post, razvijamo
     * tagove i za svaki tag emitujemo naslov posta.
     */
    record BlogPost(String naslov, List<String> tagovi) {}

    static Map<String, List<String>> nasloviPoTagu(List<BlogPost> postovi) {
        // TODO
        return Map.of();
    }

    /**
     * Data je lista narudžbina (kupac, lista stavki sa cenama).
     * Koristeći Collectors.flatMapping unutar groupingBy,
     * izračunajte ukupnu potrošnju po kupcu.
     *
     * Primer:
     *   narudžbine = [
     *     ("Ana", [("hleb", 80), ("mleko", 120)]),
     *     ("Bojan", [("sok", 150)]),
     *     ("Ana", [("jaja", 200)])
     *   ]
     *   → {"Ana": 400, "Bojan": 150}
     */
    record StavkaCena(String naziv, int cena) {}
    record NarudzbinaDetaljno(String kupac, List<StavkaCena> stavke) {}

    static Map<String, Integer> potrosnjaPoKupcu(List<NarudzbinaDetaljno> narudzbine) {
        // TODO
        return Map.of();
    }

    /**
     * Implementirajte memoizovanu verziju funkcije koja računa n-ti
     * Tribonači broj. Tribonači niz: T(0)=0, T(1)=0, T(2)=1,
     * T(n) = T(n-1) + T(n-2) + T(n-3).
     *
     * Koristite Map<Integer, Long> kao keš (containsKey/put, ili computeIfAbsent
     * sa ConcurrentHashMap — pažnja: HashMap.computeIfAbsent ne dozvoljava
     * rekurzivnu modifikaciju iste mape!).
     *
     * Primer: tribonaci(4) → 2  (0, 0, 1, 1, 2)
     * Primer: tribonaci(7) → 13 (0, 0, 1, 1, 2, 4, 7, 13)
     */
    private static final Map<Integer, Long> triboCache = new HashMap<>();

    static long tribonaci(int n) {
        // TODO
        return 0;
    }

    /**
     * Implementirajte generičku memoize funkciju koja prima Function<T, R>
     * i vraća novu Function<T, R> koja kešira rezultate.
     *
     * Primer:
     *   Function<String, Integer> duzina = memoize(s -> {
     *       System.out.println("Računam za: " + s);
     *       return s.length();
     *   });
     *   duzina.apply("java");   // štampa "Računam za: java", vraća 4
     *   duzina.apply("java");   // NE štampa ništa (iz keša), vraća 4
     */
    static <T, R> Function<T, R> memoize(Function<T, R> fn) {
        // TODO
        return fn;
    }

    // =========================================================================
    // GRUPA G — Kombinovani zadaci
    // =========================================================================

    /**
     * Koristeći Stream.iterate, generišite Collatz niz počev od zadatog broja.
     * Pravilo: ako je n paran → n/2, ako je neparan → 3n+1.
     * Niz se završava kada stignemo do 1 (uključiti 1 u rezultat).
     *
     * Pomoć: koristite iterate sa tri argumenta (Java 9+):
     *   Stream.iterate(seed, hasNext, next)
     *
     * Primer: n = 6 → [6, 3, 10, 5, 16, 8, 4, 2, 1]
     */
    static List<Integer> collatzNiz(int n) {
        // TODO
        return List.of();
    }

    /**
     * Data je lista rečenica. Za svaku rečenicu:
     *   1. Podelite na reči (split po razmaku)
     *   2. Za svaku reč proverite da li postoji u rečniku (mapa reč → prevod)
     *   3. Ako postoji, zamenite prevodom; ako ne, ostavite originalnu reč
     *   4. Spojite nazad u rečenicu koristeći Collectors.joining
     *
     * Koristite Optional i joining.
     *
     * Primer:
     *   recnik = {"hello": "zdravo", "world": "svete", "good": "dobar"}
     *   recenice = ["hello world", "good day"]
     *   → ["zdravo svete", "dobar day"]
     */
    static List<String> prevedeneRecenice(List<String> recenice, Map<String, String> recnik) {
        // TODO
        return List.of();
    }

    // =========================================================================
    // main — pokretanje testova
    // =========================================================================
    public static void main(String[] args) {
        System.out.println("Nedelja 4 — Testovi zadataka\n");

        // --- E1 ---
        System.out.println("─── E1: Rang lista ───");
        String rang = rangLista(List.of(
                new StudentPoeni("Cara", 78),
                new StudentPoeni("Ana", 92),
                new StudentPoeni("Bojan", 85)));
        System.out.println("  E1: " + rang);
        System.out.println("  Očekivano: Rang lista: 1. Ana (92), 2. Bojan (85), 3. Cara (78)");
        System.out.println();

        // --- E2 ---
        System.out.println("─── E2: Naslovi po tagu ───");
        var postovi = List.of(
                new BlogPost("Java Streams", List.of("java", "fp")),
                new BlogPost("Kotlin Intro", List.of("kotlin", "fp")),
                new BlogPost("Java Optional", List.of("java")));
        Map<String, List<String>> e2 = nasloviPoTagu(postovi);
        System.out.println("  E2: " + e2);
        proveri("E2 java",   e2.getOrDefault("java", List.of()),   List.of("Java Streams", "Java Optional"));
        proveri("E2 fp",     e2.getOrDefault("fp", List.of()),     List.of("Java Streams", "Kotlin Intro"));
        proveri("E2 kotlin", e2.getOrDefault("kotlin", List.of()), List.of("Kotlin Intro"));

        // --- E3 ---
        System.out.println("─── E3: Potrošnja po kupcu ───");
        var nar = List.of(
                new NarudzbinaDetaljno("Ana", List.of(new StavkaCena("hleb", 80), new StavkaCena("mleko", 120))),
                new NarudzbinaDetaljno("Bojan", List.of(new StavkaCena("sok", 150))),
                new NarudzbinaDetaljno("Ana", List.of(new StavkaCena("jaja", 200))));
        Map<String, Integer> e3 = potrosnjaPoKupcu(nar);
        proveri("E3 Ana",   e3.getOrDefault("Ana", 0),   400);
        proveri("E3 Bojan", e3.getOrDefault("Bojan", 0), 150);

        // --- F1 ---
        System.out.println("─── F1: Tribonači ───");
        proveri("F1 t(4)", tribonaci(4), 2L);
        proveri("F1 t(7)", tribonaci(7), 13L);
        proveri("F1 t(0)", tribonaci(0), 0L);

        // --- F2 ---
        System.out.println("─── F2: Memoize ───");
        int[] brojac = {0};
        Function<String, Integer> duzina = memoize(s -> { brojac[0]++; return s.length(); });
        duzina.apply("java");
        duzina.apply("java");
        duzina.apply("stream");
        proveri("F2 pozivi", brojac[0], 2); // samo 2 stvarna poziva, ne 3

        // --- G1 ---
        System.out.println("─── G1: Collatz niz ───");
        proveri("G1 n=6", collatzNiz(6), List.of(6, 3, 10, 5, 16, 8, 4, 2, 1));
        proveri("G1 n=1", collatzNiz(1), List.of(1));

        // --- G2 ---
        System.out.println("─── G2: Prevedene rečenice ───");
        Map<String, String> recnik = Map.of("hello", "zdravo", "world", "svete", "good", "dobar");
        proveri("G2", prevedeneRecenice(List.of("hello world", "good day"), recnik),
                List.of("zdravo svete", "dobar day"));
    }

    // =========================================================================
    // Pomoćne metode za provjeru rezultata — ne menjati
    // =========================================================================
    static <T> void proveri(String naziv, T dobijeno, T ocekivano) {
        boolean ok = Objects.equals(dobijeno, ocekivano);
        System.out.printf("  [%s] %s%n  Dobijeno:  %s%n  Očekivano: %s%n%n",
                ok ? "✓" : "✗", naziv, dobijeno, ocekivano);
    }
}
