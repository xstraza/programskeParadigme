package raf.edu.week3;

import java.util.*;
import java.util.function.*;

/**
 * Paradigme Programiranja — Nedelja 3
 * Zadaci za vežbe
 *
 * Napišite rešenje tamo gde piše // TODO
 * Ne menjajte potpise metoda.
 */
public class PracticeTasksForStudents {

    // =========================================================================
    // GRUPA A — IntStream i DoubleStream
    // =========================================================================

    /**
     * Koristeći IntStream.rangeClosed, izračunajte sumu kvadrata
     * svih parnih brojeva od 1 do n (uključivo).
     *
     * Primer: n = 6
     *   Parni: 2, 4, 6
     *   Kvadrati: 4, 16, 36
     *   Suma: 56
     */
    static int sumaKvadrataParnih(int n) {
        // TODO
        return 0;
    }

    /**
     * Data je lista ocena (double vrednosti).
     * Izračunajte prosek koristeći DoubleStream.
     * Ako je lista prazna, vratite 0.0.
     *
     * Primer: [6.0, 7.5, 9.0, 8.5] → 7.75
     */
    static double prosekOcena(List<Double> ocene) {
        // TODO
        return 0.0;
    }

    /**
     * Data je lista poena studenata (int vrednosti).
     * Koristeći summaryStatistics(), ispišite na konzolu:
     *   Broj studenata: X
     *   Minimum: X
     *   Maksimum: X
     *   Prosek: X.XX
     *   Položilo (>= 50): X
     *
     * Primer: [72, 45, 88, 30, 55]
     *   Broj studenata: 5
     *   Minimum: 30
     *   Maksimum: 88
     *   Prosek: 58.00
     *   Položilo (>= 50): 3
     */
    static void statistikeIspita(List<Integer> poeni) {
        // TODO
    }

    // =========================================================================
    // GRUPA B — reduce
    // =========================================================================

    /**
     * Izračunajte proizvod svih elemenata liste koristeći reduce.
     * Možete pretpostaviti da lista nije prazna.
     *
     * Primer: [2, 3, 4, 5] → 120
     */
    static int proizvod(List<Integer> brojevi) {
        // TODO
        return 0;
    }

    /**
     * Koristeći reduce (bez sorted, bez max), pronađite najdužu reč u listi.
     * Ako ima više reči iste maksimalne dužine, vratite prvu.
     * Ako je lista prazna, vratite prazan string.
     *
     * Primer: ["java", "stream", "lambda", "api"] → "stream"
     */
    static String najduzaRec(List<String> reci) {
        // TODO
        return "";
    }

    /**
     * Koristeći SAMO reduce (ne Collectors.joining),
     * spojite sve reči u listu u jednu String vrednost,
     * razdvojene separatorom. Ako je lista prazna, vratite "".
     *
     * Primer: ["pera", "mika", "laza"], separator = " | "
     *   → "pera | mika | laza"
     *
     * Primer: [], separator = ", " → ""
     */
    static String spoji(List<String> reci, String separator) {
        // TODO
        return "";
    }

    // =========================================================================
    // GRUPA C — flatMap na Stream
    // =========================================================================

    /**
     * Data je lista stringova koji predstavljaju višecifene brojeve.
     * Koristeći flatMap, napravite listu svih individualnih cifara
     * (kao karakteri, pa konvertovani u int).
     *
     * Primer: ["123", "45", "6"] → [1, 2, 3, 4, 5, 6]
     */
    static List<Integer> sveCifre(List<String> brojevi) {
        // TODO
        return List.of();
    }

    /**
     * Data je lista rečenica. Koristeći flatMap, pronađite sve
     * unikatne reči (case-insensitive, bez duplikata), sortirane
     * abecedno.
     *
     * Primer: ["Java je super", "stream je mocan"]
     *   → ["is", "java", "je", "mocan", "stream", "super"]
     */
    static List<String> unikatneReci(List<String> recenice) {
        // TODO
        return List.of();
    }

    /**
     * Data je matrica celih brojeva.
     * Koristeći flatMap, "sravnite" je u jednu listu, ali samo
     * elemenata koji su veći od proseka svih elemenata matrice.
     *
     * Primer:
     *   [[1, 2, 3],
     *    [4, 5, 6],
     *    [7, 8, 9]]
     *   Prosek: 5.0
     *   Rezultat: [6, 7, 8, 9]
     */
    static List<Integer> iznadProseka(List<List<Integer>> matrica) {
        // TODO
        return List.of();
    }

    // =========================================================================
    // GRUPA D — flatMap na Optional
    // =========================================================================

    // Model podataka za D zadatke
    record Zaposleni(String ime, Optional<String> email) {}
    record Kompanija(String naziv, Optional<Zaposleni> direktor) {}

    /**
     * Data je Optional<Kompanija>. Koristeći flatMap,
     * pronađite email direktora kompanije.
     * Ako kompanija ne postoji, direktor nije setovan,
     * ili direktor nema email, vratite Optional.empty().
     *
     * Primer:
     *   kompanija = Optional.of(
     *     new Kompanija("Acme",
     *       Optional.of(new Zaposleni("Ana", Optional.of("ana@acme.com"))))
     *   )
     *   → Optional["ana@acme.com"]
     */
    static Optional<String> emailDirektora(Optional<Kompanija> kompanija) {
        // TODO
        return Optional.empty();
    }

    /**
     * Data je lista stringova. Svaki string je ili:
     *   a) validan ceo broj (npr. "42")
     *   b) nešto drugo (npr. "abc")
     *
     * Koristeći Optional i flatMap:
     *   1. Pokušajte parsiranje svakog stringa
     *   2. Zadržite samo uspešno parsirane
     *   3. Uzmite samo parne vrednosti
     *   4. Svaku pomnožite sa 3
     *   5. Vratite kao listu
     *
     * Primer: ["4", "abc", "7", "2", "xyz", "10"]
     *   Parsirani: [4, 7, 2, 10]
     *   Parni: [4, 2, 10]
     *   *3: [12, 6, 30]
     */
    static List<Integer> obradiBrojeve(List<String> ulaz) {
        // TODO
        return List.of();
    }

    // =========================================================================
    // GRUPA E — Dizajn sa lambdama (kombinovani zadaci)
    // =========================================================================

    /**
     * Implementirajte metodu koja prima:
     *   - listu elemenata
     *   - transformaciju (Function)
     *   - uslov filtriranja (Predicate) — primenjuje se POSLE transformacije
     *   - operaciju agregacije (BinaryOperator)
     *   - početnu vrednost za agregaciju (identity)
     *
     * i vraća rezultat primene tih operacija redom.
     *
     * Primer:
     *   lista     = ["ana", "bojan", "cara", "dragana"]
     *   transform = String::length          (String → Integer)
     *   filter    = n -> n > 3              (samo dužine > 3)
     *   aggregate = Integer::sum
     *   identity  = 0
     *   → 5 + 6 = 11  (bojan=5, dragana=7... čekaj, cara=4, dragana=7)
     *   → 4 + 5 + 7 = 16
     */
    static <T, R> R transformFilterAggregate(
            List<T> lista,
            Function<T, R> transform,
            Predicate<R> filter,
            BinaryOperator<R> aggregate,
            R identity) {
        // TODO
        return identity;
    }

    /**
     * Data je lista studenata (ime, poeni, smer).
     * Koristeći streams (bez petlji), generišite izveštaj:
     *
     * Za svaki smer (sortirano abecedno) ispišite:
     *   Smer: <naziv>
     *     Studenti: <ime1>(poeni), <ime2>(poeni), ... (sortirano po poenima opadajuće)
     *     Prosek: <X.XX>
     *     Položilo: <X>/<ukupno>
     *
     * Primer:
     *   Smer: Informatika
     *     Studenti: Čedomir(91), Ana(85), Eva(30)
     *     Prosek: 68.67
     *     Položilo: 2/3
     */
    record Student(String ime, int poeni, String smer) {}

    static void grupniIzvestaj(List<Student> studenti) {
        // TODO
    }

    // =========================================================================
    // main — pokretanje testova
    // =========================================================================
    public static void main(String[] args) {
        System.out.println("Nedelja 3 — Testovi zadataka");

        // --- A1 ---
        System.out.println("─── A1: Suma kvadrata parnih ───");
        proveri("A1 n=6",  sumaKvadrataParnih(6),  56);
        proveri("A1 n=10", sumaKvadrataParnih(10), 220);
        proveri("A1 n=1",  sumaKvadrataParnih(1),  0);

        // --- A2 ---
        System.out.println("\n─── A2: Prosek ocena ───");
        proveriDouble("A2 [6,7.5,9,8.5]", prosekOcena(List.of(6.0,7.5,9.0,8.5)), 7.75);
        proveriDouble("A2 prazna",         prosekOcena(List.of()),                0.0);

        // --- A3 ---
        System.out.println("\n─── A3: Statistike ispita ───");
        statistikeIspita(List.of(72, 45, 88, 30, 55));

        // --- B1 ---
        System.out.println("\n─── B1: Proizvod ───");
        proveri("B1 [2,3,4,5]", proizvod(List.of(2, 3, 4, 5)), 120);
        proveri("B1 [1,1,1]",   proizvod(List.of(1, 1, 1)),    1);

        // --- B2 ---
        System.out.println("\n─── B2: Najduža reč ───");
        proveri("B2 [java,stream,lambda,api]",
                najduzaRec(List.of("java", "stream", "lambda", "api")), "stream");
        proveri("B2 prazna", najduzaRec(List.of()), "");

        // --- B3 ---
        System.out.println("\n─── B3: Spoji sa separatorom ───");
        proveri("B3 | separator", spoji(List.of("pera","mika","laza"), " | "), "pera | mika | laza");
        proveri("B3 prazna",      spoji(List.of(), ", "),                      "");
        proveri("B3 jedan el.",   spoji(List.of("solo"), ", "),                "solo");

        // --- C1 ---
        System.out.println("\n─── C1: Sve cifre ───");
        proveri("C1 [123,45,6]", sveCifre(List.of("123","45","6")), List.of(1,2,3,4,5,6));

        // --- C2 ---
        System.out.println("\n─── C2: Unikatne reči ───");
        List<String> c2 = unikatneReci(List.of("Java je super", "stream je mocan"));
        System.out.println("C2 rezultat: " + c2 + " (očekivano: abecedno sortirano, bez duplikata)");

        // --- C3 ---
        System.out.println("\n─── C3: Iznad proseka ───");
        List<List<Integer>> matrica = List.of(
                List.of(1, 2, 3),
                List.of(4, 5, 6),
                List.of(7, 8, 9)
        );
        proveri("C3 matrica 3x3", iznadProseka(matrica), List.of(6, 7, 8, 9));

        // --- D1 ---
        System.out.println("\n─── D1: Email direktora ───");
        var komp1 = Optional.of(new Kompanija("Acme",
                Optional.of(new Zaposleni("Ana", Optional.of("ana@acme.com")))));
        var komp2 = Optional.of(new Kompanija("Beta",
                Optional.of(new Zaposleni("Bob", Optional.empty()))));
        var komp3 = Optional.of(new Kompanija("Gama", Optional.empty()));

        proveri("D1 ima email",    emailDirektora(komp1), Optional.of("ana@acme.com"));
        proveri("D1 nema email",   emailDirektora(komp2), Optional.empty());
        proveri("D1 nema dir.",    emailDirektora(komp3), Optional.empty());

        // --- D2 ---
        System.out.println("\n─── D2: Obradi brojeve ───");
        proveri("D2", obradiBrojeve(List.of("4","abc","7","2","xyz","10")), List.of(12, 6, 30));

        // --- E1 ---
        System.out.println("\n─── E1: Transform-Filter-Aggregate ───");
        int e1 = transformFilterAggregate(
                List.of("ana", "bojan", "cara", "dragana"),
                String::length,
                n -> n > 3,
                Integer::sum,
                0);
        proveri("E1 suma duzina>3", e1, 16);

        // --- E2 ---
        System.out.println("\n─── E2: Grupni izveštaj ───");
        grupniIzvestaj(List.of(
                new Student("Ana",      85, "Informatika"),
                new Student("Bojan",    42, "Matematika"),
                new Student("Čedomir",  91, "Informatika"),
                new Student("Dragana",  55, "Matematika"),
                new Student("Eva",      30, "Informatika")
        ));
    }

    // =========================================================================
    // Pomoćne metode za provjeru rezultata — ne menjati
    // =========================================================================
    static <T> void proveri(String naziv, T dobijeno, T ocekivano) {
        boolean ok = Objects.equals(dobijeno, ocekivano);
        System.out.printf("  [%s] %s%n  Dobijeno:  %s%n  Očekivano: %s%n%n",
                ok ? "✓" : "✗", naziv, dobijeno, ocekivano);
    }

    static void proveriDouble(String naziv, double dobijeno, double ocekivano) {
        boolean ok = Math.abs(dobijeno - ocekivano) < 0.001;
        System.out.printf("  [%s] %s%n  Dobijeno:  %.4f%n  Očekivano: %.4f%n%n",
                ok ? "✓" : "✗", naziv, dobijeno, ocekivano);
    }
}
