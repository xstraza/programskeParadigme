package raf.edu.week4;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Nedelja 4 — Lenja evaluacija (Lazy Evaluation)
 * Poglavlje 7: Being Lazy
 *
 * Ključne ideje:
 *   1. Intermediate operacije su LENJE — ne izvršavaju se dok ne dođe terminal
 *   2. Tok je VERTIKALAN — svaki element prolazi ceo pipeline pre sledećeg
 *   3. Kratki spoj (short-circuit) — neke terminal operacije staju rano
 *   4. Beskonačni streamovi su mogući SAMO zbog lenje evaluacije
 *   5. Redosled operacija utiče na performanse
 */
public class LazyEvaluation {

    public static void main(String[] args) {
        System.out.println("=== 1. Kada se operacije stvarno izvršavaju? ===\n");
        kadaSeIzvrsavaju();

        System.out.println("\n=== 2. Vertikalni tok kroz pipeline ===\n");
        vertikalniTok();

        System.out.println("\n=== 3. Kratki spoj ===\n");
        kratakSpoj();

        System.out.println("\n=== 4. Beskonačni streamovi ===\n");
        beskonacniStreamovi();

        System.out.println("\n=== 5. Optimizacija redosleda operacija ===\n");
        optimizacijaRedosleda();

        System.out.println("\n=== 6. peek — prozor u pipeline ===\n");
        peekPrimer();
    }

    // -----------------------------------------------------------------------
    // 1. Dokazujemo lenost: intermediate operacije se ne izvršavaju
    //    dok ne dođe terminalna operacija
    // -----------------------------------------------------------------------
    static void kadaSeIzvrsavaju() {
        System.out.println("Kreiranje stream pipeline-a...");

        // Ovo ne štampa ništa samo po sebi!
        Stream<Integer> pipeline = List.of(1, 2, 3, 4, 5).stream()
                .filter(n -> {
                    System.out.println("  filter evaluira: " + n);
                    return n % 2 == 0;
                })
                .map(n -> {
                    System.out.println("  map evaluira: " + n);
                    return n * 10;
                });

        System.out.println("Pipeline definisan. Još ništa nije izvršeno.");
        System.out.println("Sada pozivamo terminalnu operaciju (collect)...\n");

        // TEK OVO pokreće sve:
        var rezultat = pipeline.toList();
        System.out.println("\nRezultat: " + rezultat);
    }

    // -----------------------------------------------------------------------
    // 2. Tok je VERTIKALAN — elem po elem kroz ceo pipeline
    //    Ne: filter sve, pa map sve — nego: elem1 kroz sve, pa elem2 kroz sve
    // -----------------------------------------------------------------------
    static void vertikalniTok() {
        System.out.println("Pratimo tok elemenata kroz filter → map → limit:");
        System.out.println("(očekujemo vertikalni redosled, ne horizontalni)\n");

        List.of(1, 2, 3, 4, 5, 6, 7, 8).stream()
                .filter(n -> {
                    System.out.println("  filter(" + n + ")");
                    return n % 2 == 0;
                })
                .map(n -> {
                    System.out.println("  map(" + n + ") → " + (n * 10));
                    return n * 10;
                })
                .limit(2)  // ← kratki spoj: staje nakon 2 elementa
                .forEach(n -> System.out.println("  forEach: " + n));

        System.out.println("\nKljučna observacija: svaki element prolazi CELI pipeline");
        System.out.println("pre nego što se krene na sledeći element.");
        System.out.println("I stajemo rano zahvaljujući limit(2)!");
    }

    // -----------------------------------------------------------------------
    // 3. Kratki spoj — terminal operacije koje ne prolaze sve elemente
    // -----------------------------------------------------------------------
    static void kratakSpoj() {
        List<Integer> lista = List.of(3, 7, 11, 2, 9, 4, 15, 6);

        // anyMatch — staje čim nađe prvi koji zadovoljava uslov
        System.out.println("anyMatch(n > 10):");
        boolean ima = lista.stream()
                .peek(n -> System.out.print("  provjera " + n + "... "))
                .anyMatch(n -> {
                    boolean r = n > 10;
                    System.out.println(r ? "DA — gotovo!" : "ne");
                    return r;
                });
        System.out.println("Rezultat: " + ima);

        // findFirst — staje čim nađe prvi
        System.out.println("\nfindFirst(parni):");
        Optional<Integer> prvi = lista.stream()
                .peek(n -> System.out.print("  provjera " + n + " "))
                .filter(n -> n % 2 == 0)
                .findFirst();
        System.out.println("\nPrvi parni: " + prvi.orElse(-1));

        // allMatch — staje čim nađe prvi koji NIJE zadovoljen
        System.out.println("\nallMatch(n > 0):");
        boolean sviPozitivni = lista.stream()
                .peek(n -> System.out.print("  " + n + " "))
                .allMatch(n -> n > 0);
        System.out.println("\nSvi pozitivni: " + sviPozitivni);
    }

    // -----------------------------------------------------------------------
    // 4. Beskonačni streamovi — moguće SAMO zbog lenje evaluacije
    //    Elementi se generišu po zahtevu, ne unapred
    // -----------------------------------------------------------------------
    static void beskonacniStreamovi() {
        // iterate(seed, f) — svaki element je f(prethodni)
        System.out.println("Prvih 10 prirodnih brojeva:");
        IntStream.iterate(1, n -> n + 1)
                .limit(10)
                .forEach(n -> System.out.print(n + " "));
        System.out.println();

        // Neparni brojevi
        System.out.println("Prvih 8 neparnih:");
        IntStream.iterate(1, n -> n + 2)
                .limit(8)
                .forEach(n -> System.out.print(n + " "));
        System.out.println();

        // Stepeno2
        System.out.println("Stepeni broja 2 (2^0 .. 2^10):");
        Stream.iterate(1L, n -> n * 2)
                .limit(11)
                .forEach(n -> System.out.print(n + " "));
        System.out.println();

        // Fibonači niz — stanje se prenosi kao niz
        System.out.println("Prvih 15 Fibonači brojeva:");
        Stream.iterate(new long[]{0, 1}, f -> new long[]{f[1], f[0] + f[1]})
                .limit(15)
                .mapToLong(f -> f[0])
                .forEach(n -> System.out.print(n + " "));
        System.out.println();

        // generate(Supplier) — nezavisni elementi (npr. random)
        System.out.println("5 slučajnih parnih brojeva između 0-100:");
        Stream.generate(() -> (int)(Math.random() * 50) * 2)
                .distinct()
                .limit(5)
                .forEach(n -> System.out.print(n + " "));
        System.out.println();

        // Pronalaženje prvog prostog broja > 1000 — bez beskonačnog streama
        // ovo bi morali da znamo unapred koliko iteracija treba
        int prviProsti = IntStream.iterate(1001, n -> n + 2)
                .filter(LazyEvaluation::jeProsti)
                .findFirst()
                .orElseThrow();
        System.out.println("Prvi prosti broj > 1000: " + prviProsti);
    }

    static boolean jeProsti(int n) {
        if (n < 2) return false;
        return IntStream.rangeClosed(2, (int) Math.sqrt(n))
                .allMatch(d -> n % d != 0);
    }

    // -----------------------------------------------------------------------
    // 5. Redosled operacija utiče na performanse
    //    filter pre map = manje mapa (skupih operacija)
    // -----------------------------------------------------------------------
    static void optimizacijaRedosleda() {
        List<Integer> veliki = IntStream.rangeClosed(1, 1_000_000)
                .boxed().toList();

        // Brojač poziva skupe transformacije
        int[] brojacA = {0};
        int[] brojacB = {0};

        // Redosled A: map (skupo) → filter
        long tA = System.currentTimeMillis();
        long sumaA = veliki.stream()
                .map(n -> { brojacA[0]++; return n * n; })   // poziva se na SVIM
                .filter(n -> n % 2 == 0)
                .limit(100)
                .count();
        long vremeA = System.currentTimeMillis() - tA;

        // Redosled B: filter → map (skupo)
        long tB = System.currentTimeMillis();
        long sumaB = veliki.stream()
                .filter(n -> n % 2 == 0)                     // eliminišemo pola
                .map(n -> { brojacB[0]++; return n * n; })   // poziva se na MANJE
                .limit(100)
                .count();
        long vremeB = System.currentTimeMillis() - tB;

        System.out.println("map→filter: map pozvan " + brojacA[0] + " puta");
        System.out.println("filter→map: map pozvan " + brojacB[0] + " puta");
        System.out.println("Poruka: filtrirajte PRE skupe transformacije!");
    }

    // -----------------------------------------------------------------------
    // 6. peek — "špijunira" elemente bez menjanja pipeline-a
    //    Korisno za debugging; NE koristiti za sporedne efekte u produkciji
    // -----------------------------------------------------------------------
    static void peekPrimer() {
        System.out.println("Pratimo šta prolazi kroz svaki korak:");

        List.of("ana", "bojan", "cara", "dragana", "eva").stream()
                .peek(s  -> System.out.println("  [ulaz]  " + s))
                .filter(s -> s.length() > 3)
                .peek(s  -> System.out.println("  [filter OK] " + s))
                .map(String::toUpperCase)
                .peek(s  -> System.out.println("  [posle map] " + s))
                .limit(2)
                .forEach(s -> System.out.println("  [IZLAZ] " + s));
    }
}