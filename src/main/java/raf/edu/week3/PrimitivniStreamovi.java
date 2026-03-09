package raf.edu.week3;

import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Nedelja 3 — Primitivni Streamovi: IntStream i DoubleStream
 *
 * Problem sa Stream<Integer>:
 *   Svaki int se pakuje u Integer objekat (autoboxing) — memorija i vreme.
 *   Za numeričke operacije koristimo specijalizovane primitivne streamove.
 *
 * IntStream  — za int vrednosti
 * LongStream — za long vrednosti
 * DoubleStream — za double vrednosti
 *
 * Prednosti:
 *   - Nema autoboxing-a → bolje performanse
 *   - Direktne metode: sum(), count(), min(), max(), average(), summaryStatistics()
 *   - IntStream.range() i rangeClosed() za iteraciju po broju
 */
public class PrimitivniStreamovi {

    record Student(String ime, int poeni, double prosek) {}

    public static void main(String[] args) {
        System.out.println("=== Kreiranje IntStream-a ===\n");
        kreiranjeIntStream();

        System.out.println("\n=== Agregacijske operacije na IntStream ===\n");
        agregacijeIntStream();

        System.out.println("\n=== summaryStatistics — sve odjednom ===\n");
        summaryStatistics();

        System.out.println("\n=== DoubleStream ===\n");
        doubleStreamPrimeri();

        System.out.println("\n=== mapToInt / mapToDouble — prelaz iz Stream<T> ===\n");
        prelaziIzStreamObjekata();

        System.out.println("\n=== Povratak na Stream objekata ===\n");
        povratakNaStreamObjekata();

        System.out.println("\n=== Poređenje: Stream<Integer> vs IntStream ===\n");
        poredjenjePerformansi();
    }

    // -----------------------------------------------------------------------
    // Kreiranje IntStream-a
    // -----------------------------------------------------------------------
    static void kreiranjeIntStream() {
        // of — eksplicitne vrednosti
        IntStream s1 = IntStream.of(1, 2, 3, 4, 5);
        System.out.print("of(1..5): ");
        s1.forEach(n -> System.out.print(n + " "));
        System.out.println();

        // range — [start, end)  ekskluzivni kraj
        IntStream s2 = IntStream.range(1, 6);
        System.out.print("range(1,6): ");
        s2.forEach(n -> System.out.print(n + " "));
        System.out.println();

        // rangeClosed — [start, end]  inkluzivni kraj
        IntStream s3 = IntStream.rangeClosed(1, 5);
        System.out.print("rangeClosed(1,5): ");
        s3.forEach(n -> System.out.print(n + " "));
        System.out.println();

        // iterate — beskonačan, koristimo limit()
        System.out.print("Prvih 6 parnih: ");
        IntStream.iterate(0, n -> n + 2)
                 .limit(6)
                 .forEach(n -> System.out.print(n + " "));
        System.out.println();
    }

    // -----------------------------------------------------------------------
    // Agregacijske terminalne operacije
    // Sve su "reduce" u suštini — ali specijalizovane i efikasne
    // -----------------------------------------------------------------------
    static void agregacijeIntStream() {
        // Napomena: svaki IntStream se može iskoristiti SAMO JEDNOM!
        // Moramo ga svaki put kreirati iznova.

        System.out.println("Brojevi 1–10:");

        System.out.println("  sum()     = " + IntStream.rangeClosed(1, 10).sum());
        System.out.println("  count()   = " + IntStream.rangeClosed(1, 10).count());

        // min i max vraćaju OptionalInt — stream može biti prazan
        OptionalInt min = IntStream.rangeClosed(1, 10).min();
        OptionalInt max = IntStream.rangeClosed(1, 10).max();
        System.out.println("  min()     = " + min.getAsInt());
        System.out.println("  max()     = " + max.getAsInt());

        // average vraća OptionalDouble
        OptionalDouble avg = IntStream.rangeClosed(1, 10).average();
        System.out.printf("  average() = %.1f%n", avg.getAsDouble());

        // Prazan stream — Optional štiti od greške
        OptionalInt minPrazan = IntStream.empty().min();
        System.out.println("  min() na praznom streamu: " + minPrazan); // OptionalInt.empty

        // Bezbedno korišćenje sa orElse
        int sigurniMin = IntStream.empty().min().orElse(-1);
        System.out.println("  min().orElse(-1): " + sigurniMin);
    }

    // -----------------------------------------------------------------------
    // summaryStatistics — svi statistički podaci u jednom prolazu
    // -----------------------------------------------------------------------
    static void summaryStatistics() {
        int[] poeni = {72, 45, 88, 91, 30, 67, 55, 100, 38, 76};

        IntSummaryStatistics stats = IntStream.of(poeni).summaryStatistics();

        System.out.println("Statistike za ispit:");
        System.out.println("  Broj studenata : " + stats.getCount());
        System.out.println("  Suma poena     : " + stats.getSum());
        System.out.println("  Minimum        : " + stats.getMin());
        System.out.println("  Maksimum       : " + stats.getMax());
        System.out.printf( "  Prosek         : %.2f%n", stats.getAverage());

        // Korisno i za filtriranje pa statistiku
        System.out.println("\nStatistike za položene (>= 50 poena):");
        IntSummaryStatistics statsPolozeni = IntStream.of(poeni)
                .filter(p -> p >= 50)
                .summaryStatistics();
        System.out.println("  Broj položenih: " + statsPolozeni.getCount());
        System.out.printf("  Prosek položenih: %.2f%n", statsPolozeni.getAverage());
    }

    // -----------------------------------------------------------------------
    // DoubleStream — isti koncept, za double vrednosti
    // -----------------------------------------------------------------------
    static void doubleStreamPrimeri() {
        double[] ocene = {7.5, 8.0, 9.5, 6.0, 10.0, 8.5};

        DoubleStream stream = DoubleStream.of(ocene);
        System.out.printf("Prosek ocena: %.2f%n", stream.average().orElse(0.0));

        // Generisanje — prvih 5 vrednosti sinusa
        System.out.print("sin(0°..80° korak 20°): ");
        DoubleStream.iterate(0, x -> x + 20)
                    .limit(5)
                    .map(deg -> Math.sin(Math.toRadians(deg)))
                    .forEach(v -> System.out.printf("%.3f ", v));
        System.out.println();
    }

    // -----------------------------------------------------------------------
    // mapToInt / mapToDouble — prelaz iz Stream<T> u primitivni stream
    // -----------------------------------------------------------------------
    static void prelaziIzStreamObjekata() {
        List<Student> studenti = List.of(
                new Student("Ana",    85, 8.5),
                new Student("Bojan",  62, 7.2),
                new Student("Čedomir", 91, 9.1),
                new Student("Dragana", 44, 5.5)
        );

        // mapToInt — Function<Student, Integer> → IntStream
        int ukupnoPoena = studenti.stream()
                .mapToInt(Student::poeni)
                .sum();
        System.out.println("Ukupno poena: " + ukupnoPoena);

        // mapToDouble
        double prosekProseka = studenti.stream()
                .mapToDouble(Student::prosek)
                .average()
                .orElse(0.0);
        System.out.printf("Prosečan prosek: %.2f%n", prosekProseka);

        // Broj položenih
        long polozeni = studenti.stream()
                .mapToInt(Student::poeni)
                .filter(p -> p >= 50)
                .count();
        System.out.println("Položilo: " + polozeni + "/" + studenti.size());
    }

    // -----------------------------------------------------------------------
    // Povratak na Stream<T> iz primitivnog Streama
    // -----------------------------------------------------------------------
    static void povratakNaStreamObjekata() {
        // boxed() → IntStream → Stream<Integer>
        List<Integer> listaIntova = IntStream.rangeClosed(1, 5)
                .boxed()
                .collect(Collectors.toList());
        System.out.println("boxed() → List<Integer>: " + listaIntova);

        // mapToObj() → IntStream → Stream<T>
        List<String> oznake = IntStream.rangeClosed(1, 5)
                .mapToObj(n -> "Zadatak #" + n)
                .collect(Collectors.toList());
        System.out.println("mapToObj: " + oznake);

        // asLongStream() / asDoubleStream() — konverzija između primitivnih
        double sumaKaoDouble = IntStream.rangeClosed(1, 5)
                .asDoubleStream()
                .map(n -> n * 1.1)
                .sum();
        System.out.printf("asDoubleStream().map(*1.1).sum() = %.2f%n", sumaKaoDouble);
    }

    // -----------------------------------------------------------------------
    // Ilustracija: zašto primitivni stream, a ne Stream<Integer>
    // -----------------------------------------------------------------------
    static void poredjenjePerformansi() {
        int N = 1_000_000;

        // Stream<Integer> — autoboxing svake vrednosti
        long t1 = System.currentTimeMillis();
        long suma1 = Stream.iterate(1, n -> n + 1)
                .limit(N)
                .mapToLong(Integer::longValue) // ovde se dešava unboxing
                .sum();
        long t2 = System.currentTimeMillis();

        // IntStream — bez autoboxinga
        long t3 = System.currentTimeMillis();
        long suma2 = IntStream.rangeClosed(1, N).asLongStream().sum();
        long t4 = System.currentTimeMillis();

        System.out.println("Suma 1.." + N + " = " + suma1);
        System.out.println("Stream<Integer> vreme : " + (t2 - t1) + " ms");
        System.out.println("IntStream vreme        : " + (t4 - t3) + " ms");
        System.out.println("(IntStream je generalno brži zbog izostanka autoboxinga)");
    }
}
