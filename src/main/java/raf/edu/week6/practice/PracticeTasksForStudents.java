package raf.edu.week6.practice;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * Paradigme Programiranja — Nedelja 6
 * Zadaci za vežbe
 *
 * Zadaci 1-3: klasični (parallel, error handling, currying)
 * Zadaci 4-5: imperativni kod → prepišite u FP stil
 *
 * Napišite rešenje tamo gde piše // TODO
 * Ne menjajte potpise metoda.
 */
public class PracticeTasksForStudents {

    // =========================================================================
    // Zadatak 1 — Parallel reduce: statistika velikog niza
    //
    // Data je lista celih brojeva. Koristeći PARALELNI stream i reduce
    // sa TRI argumenta (identity, accumulator, combiner), izračunajte:
    //   - minimum
    //   - maksimum
    //   - sumu
    //   - broj elemenata
    //
    // NE koristite IntStream.summaryStatistics, min(), max(), count() itd.
    // Poenta je da vidite kako reduce sa combiner-om radi na paralelnom streamu.
    //
    // Pomoć: koristite record Stats sa PRAZAN instancom i metodama
    //   dodaj(int) i spoji(Stats).
    //
    // Primer: [3, 1, 4, 1, 5, 9] → Stats[min=1, max=9, suma=23, broj=6]
    // =========================================================================

    record Stats(int min, int max, long suma, int broj) {
        static final Stats PRAZAN = new Stats(Integer.MAX_VALUE, Integer.MIN_VALUE, 0, 0);

        Stats dodaj(int n) {
            // TODO
            return PRAZAN;
        }

        Stats spoji(Stats other) {
            // TODO
            return PRAZAN;
        }
    }

    static Stats paralelnaStatistika(List<Integer> brojevi) {
        return Stats.PRAZAN;
    }

    // =========================================================================
    // Zadatak 2 — Error handling: sigurno parsiranje sa Either
    //
    // Data je lista stringova koja predstavlja unose korisnika.
    // Svaki unos treba parsirati kao ceo broj.
    // Koristite Either<String, Integer>:
    //   - Left(poruka greške) za neuspeh
    //   - Right(vrednost) za uspeh
    //
    // Metoda treba da vrati:
    //   a) listu svih USPEŠNO parsiranih vrednosti
    //   b) listu svih GREŠAKA sa originalnim unosom
    //
    // Vratite kao Map.Entry<List<Integer>, List<String>> gde:
    //   key = lista uspešnih vrednosti
    //   value = lista greški u formatu "Neispravan unos: X"
    //
    // Primer: ["42", "abc", "7", "", "13"]
    //   → Entry([42, 7, 13], ["Neispravan unos: abc", "Neispravan unos: "])
    // =========================================================================

    sealed interface Either<L, R> {
        record Left<L, R>(L value) implements Either<L, R> {}
        record Right<L, R>(R value) implements Either<L, R> {}

        static <L, R> Either<L, R> left(L value) { return new Left<>(value); }
        static <L, R> Either<L, R> right(R value) { return new Right<>(value); }

        default boolean isRight() { return this instanceof Right; }
    }

    static Map.Entry<List<Integer>, List<String>> parsirajUnose(List<String> unosi) {
        // TODO:
        // 1. mapirati svaki unos u Either<String, Integer>
        // 2. razdvojiti na uspešne (Right) i neuspešne (Left)
        // 3. vratiti kao Map.entry(uspesni, greske)
        return Map.entry(List.of(), List.of());
    }

    // =========================================================================
    // Zadatak 3 — Currying: konfigurabilni formateri
    //
    // Implementirajte curried funkciju za formatiranje tabele:
    //   1. Prvi argument: separator (npr. " | ")
    //   2. Drugi argument: lista širina kolona (npr. [10, 5, 15])
    //   3. Treći argument: lista vrednosti (stringovi)
    //   4. Rezultat: formatiran red tabele
    //
    // Svaka vrednost se dopuni razmacima do zadate širine kolone (padRight).
    // Ako ima više vrednosti nego kolona, višak se ignoriše.
    // Ako ima manje vrednosti nego kolona, prazne kolone se popune razmacima.
    //
    // Primer:
    //   formater = tableFormatter.apply(" | ").apply(List.of(10, 5, 8))
    //   formater.apply(List.of("Ana", "92", "IT"))  → "Ana        | 92    | IT      "
    //   formater.apply(List.of("Bojan", "45"))       → "Bojan      | 45    |         "
    // =========================================================================

    static Function<String, Function<List<Integer>, Function<List<String>, String>>>
    tableFormatter = separator -> widths -> values -> {
        // TODO
        return "";
    };

    // =========================================================================
    // Zadatak 4 — Refactoring: obrada porudžbina
    //
    // Imperativni kod ispod računa ukupan prihod po državi,
    // ali samo za porudžbine koje su isporučene i čija vrednost > 100.
    //
    // Prepišite u FP stil sa streamovima.
    // =========================================================================

    record Porudzbina(String kupac, String drzava, double vrednost, String status) {}

    static Map<String, Double> prihodPoDrzaviImperativ(List<Porudzbina> porudzbine) {
        Map<String, Double> rezultat = new HashMap<>();
        for (Porudzbina p : porudzbine) {
            if (p.status().equals("ISPORUCENO") && p.vrednost() > 100) {
                rezultat.merge(p.drzava(), p.vrednost(), Double::sum);
            }
        }
        // Sortiraj po prihodu opadajuće i vrati kao LinkedHashMap (čuva redosled)
        List<Map.Entry<String, Double>> sortiran = new ArrayList<>(rezultat.entrySet());
        sortiran.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        Map<String, Double> sortiranaMap = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry : sortiran) {
            sortiranaMap.put(entry.getKey(), entry.getValue());
        }
        return sortiranaMap;
    }

    /** FP stil — TODO */
    static Map<String, Double> prihodPoDrzaviFP(List<Porudzbina> porudzbine) {
        // TODO
        return Map.of();
    }

    // =========================================================================
    // Zadatak 5 — Refactoring: transformacija i grupisanje
    //
    // Imperativni kod ispod:
    //   1. Filtrira zaposlene koji rade > 2 godine
    //   2. Za svaki departman pravi listu "Ime (pozicija)"
    //   3. Sortira zaposlene unutar svakog departmana abecedno
    //   4. Vraća mapu: departman → lista formatiranih stringova
    //
    // Prepišite u FP stil sa streamovima.
    // =========================================================================

    record Zaposleni(String ime, String departman, String pozicija, int godineStaza) {}

    static Map<String, List<String>> zaposlenaPoDeptImperativ(List<Zaposleni> zaposleni) {
        Map<String, List<String>> rezultat = new TreeMap<>();
        for (Zaposleni z : zaposleni) {
            if (z.godineStaza() > 2) {
                String formatted = z.ime() + " (" + z.pozicija() + ")";
                rezultat.computeIfAbsent(z.departman(), k -> new ArrayList<>()).add(formatted);
            }
        }
        for (List<String> lista : rezultat.values()) {
            Collections.sort(lista);
        }
        return rezultat;
    }

    /** FP stil — TODO */
    static Map<String, List<String>> zaposlenaPoDeptFP(List<Zaposleni> zaposleni) {
        // TODO
        return Map.of();
    }

    // =========================================================================
    // main — pokretanje testova
    // =========================================================================
    public static void main(String[] args) {
        System.out.println("Nedelja 6 — Testovi zadataka\n");

        // --- Zadatak 1 ---
        System.out.println("─── 1: Paralelna statistika ───");
        Stats s1 = paralelnaStatistika(List.of(3, 1, 4, 1, 5, 9));
        proveri("Z1 min",  s1.min(),  1);
        proveri("Z1 max",  s1.max(),  9);
        proveri("Z1 suma", s1.suma(), 23L);
        proveri("Z1 broj", s1.broj(), 6);

        Stats s2 = paralelnaStatistika(List.of(42));
        proveri("Z1 jedan element", s2, new Stats(42, 42, 42, 1));

        // --- Zadatak 2 ---
        System.out.println("─── 2: Either parsiranje ───");
        var r2 = parsirajUnose(List.of("42", "abc", "7", "", "13"));
        proveri("Z2 uspesni", r2.getKey(), List.of(42, 7, 13));
        proveri("Z2 greske",  r2.getValue(),
                List.of("Neispravan unos: abc", "Neispravan unos: "));

        // --- Zadatak 3 ---
        System.out.println("─── 3: Table formatter ───");
        var formater = tableFormatter.apply(" | ").apply(List.of(10, 5, 8));
        proveri("Z3 pun red",
                formater.apply(List.of("Ana", "92", "IT")),
                "Ana        | 92    | IT      ");
        proveri("Z3 kratak red",
                formater.apply(List.of("Bojan", "45")),
                "Bojan      | 45    |         ");

        // --- Zadatak 4 ---
        System.out.println("─── 4: Prihod po državi ───");
        List<Porudzbina> porudzbine = List.of(
                new Porudzbina("Ana",    "Srbija",  250, "ISPORUCENO"),
                new Porudzbina("Bojan",  "Srbija",  80,  "ISPORUCENO"),  // < 100
                new Porudzbina("Cara",   "BiH",     300, "ISPORUCENO"),
                new Porudzbina("Dragan", "Srbija",  150, "OTKAZANO"),    // otkazano
                new Porudzbina("Eva",    "BiH",     200, "ISPORUCENO"),
                new Porudzbina("Filip",  "CG",      120, "ISPORUCENO")
        );
        Map<String, Double> imp4 = prihodPoDrzaviImperativ(porudzbine);
        Map<String, Double> fp4  = prihodPoDrzaviFP(porudzbine);
        System.out.println("  Imperativ: " + imp4);
        System.out.println("  FP:        " + fp4);
        proveri("Z4", fp4, imp4);

        // --- Zadatak 5 ---
        System.out.println("─── 5: Zaposleni po departmanu ───");
        List<Zaposleni> zaposleni = List.of(
                new Zaposleni("Ana",    "IT",  "Developer",   5),
                new Zaposleni("Bojan",  "IT",  "QA",          1),  // < 2 godine
                new Zaposleni("Cara",   "HR",  "Recruiter",   3),
                new Zaposleni("Dragan", "IT",  "DevOps",      4),
                new Zaposleni("Eva",    "HR",  "Manager",     7),
                new Zaposleni("Filip",  "IT",  "Developer",   3)
        );
        Map<String, List<String>> imp5 = zaposlenaPoDeptImperativ(zaposleni);
        Map<String, List<String>> fp5  = zaposlenaPoDeptFP(zaposleni);
        System.out.println("  Imperativ: " + imp5);
        System.out.println("  FP:        " + fp5);
        proveri("Z5", fp5, imp5);
    }

    // =========================================================================
    // Pomoćne metode — ne menjati
    // =========================================================================
    static <T> void proveri(String naziv, T dobijeno, T ocekivano) {
        boolean ok = Objects.equals(dobijeno, ocekivano);
        System.out.printf("  [%s] %s%n  Dobijeno:  %s%n  Očekivano: %s%n%n",
                ok ? "✓" : "✗", naziv, dobijeno, ocekivano);
    }
}
