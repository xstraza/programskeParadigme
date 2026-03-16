package raf.edu.week4;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Nedelja 4 — Optimizacija rekurzije
 * Poglavlje 8: Optimizing Recursions
 *
 * Teme:
 *   1. Problem naivne rekurzije (StackOverflowError)
 *   2. Repni poziv (Tail Call) — preduslovi za optimizaciju
 *   3. Trampolining — simulacija TCO u Javi
 *   4. Memoizacija — keširanje rezultata čistih funkcija
 *   5. computeIfAbsent — elegantan API za memoizaciju
 */
public class RecursionOptimization {

    // =========================================================================
    // Sekcija 1: Problem naivne rekurzije
    // =========================================================================

    // Naivni faktorijel — StackOverflow za n > ~10000
    static long faktorijel(int n) {
        if (n <= 1) return 1;
        return n * faktorijel(n - 1); // n frejma na steku odjednom!
    }

    // Naivni Fibonači — eksponencijalna složenost O(2^n)
    // fib(50) zahteva ~2^50 ≈ 10^15 poziva — praktično beskonačno
    static long fibNaivno(int n) {
        if (n <= 1) return n;
        return fibNaivno(n - 1) + fibNaivno(n - 2);
    }

    // =========================================================================
    // Sekcija 2: Repni poziv (Tail Call)
    //
    // Repni poziv = rekurzivni poziv je POSLEDNJA operacija,
    // bez obrade rezultata. Kompajler ga može transformisati u petlju.
    // Java JVM ne podržava TCO automatski — simuliramo trampolingom.
    // =========================================================================

    // Faktorijel SA repnim pozivom — akumulator pamti međurezultat
    // NIJE repni poziv: n * faktorijel(n-1)  ← mora da sačeka rezultat
    // JESTE repni poziv: faktorijel(n-1, n * akum)  ← ništa posle poziva
    static long faktorijel_rep(int n, long akumulator) {
        if (n <= 1) return akumulator;
        return faktorijel_rep(n - 1, n * akumulator); // repni poziv
    }

    // Ovo je i dalje StackOverflow za velika n u Javi — ali oblik je ispravan
    // Trampolining to rešava:

    // =========================================================================
    // Sekcija 3: Trampolining
    //
    // Ideja: umesto da direktno pozivamo funkciju,
    // vraćamo "thunk" — odloženi poziv koji se iterativno izvršava u petlji.
    // Nema rekurzivnih frejma na steku!
    // =========================================================================

    @FunctionalInterface
    interface TailCall<T> {
        TailCall<T> apply();

        default boolean jeDone() { return false; }
        default T rezultat() { throw new IllegalStateException("Nije gotovo!"); }

        // Fabrička metoda za finalni rezultat
        static <T> TailCall<T> done(T vrednost) {
            return new TailCall<T>() {
                @Override public TailCall<T> apply()  { return this; }
                @Override public boolean jeDone()      { return true; }
                @Override public T rezultat()          { return vrednost; }
            };
        }

        // Fabrička metoda za sledeći korak
        static <T> TailCall<T> sledeci(TailCall<T> sledeci) {
            return sledeci;
        }
    }

    // Petlja koja "trampolira" — izvršava korake dok nije gotovo
    static <T> T trampolin(TailCall<T> poziv) {
        while (!poziv.jeDone()) {
            poziv = poziv.apply(); // sledeći korak, bez novog frejma
        }
        return poziv.rezultat();
    }

    // Faktorijel sa trampolinom — ne pravi nove frejme!
    static TailCall<Long> faktorijel_trampolin(int n, long akum) {
        if (n <= 1) return TailCall.done(akum);
        // Ne pozivamo direktno — vraćamo lambda (thunk)
        return () -> faktorijel_trampolin(n - 1, n * akum);
    }

    // Fibonači sa trampolinom
    static TailCall<Long> fib_trampolin(int n, long prethodni, long trenutni) {
        if (n == 0) return TailCall.done(prethodni);
        if (n == 1) return TailCall.done(trenutni);
        return () -> fib_trampolin(n - 1, trenutni, prethodni + trenutni);
    }

    // =========================================================================
    // Sekcija 4: Memoizacija
    //
    // Čista funkcija za iste argumente uvek daje isti rezultat.
    // Možemo keširati rezultate → dramatično ubrzanje.
    // =========================================================================

    // Memoizovani Fibonači — O(n) umesto O(2^n)
    private static final Map<Integer, Long> fibMemo = new HashMap<>();

    static long fibMemoizovano(int n) {
        if (n <= 1) return n;
        // computeIfAbsent: ako nema u mapi, izračunaj i sačuvaj
        return fibMemo.computeIfAbsent(n, k -> fibMemoizovano(k - 1) + fibMemoizovano(k - 2));
    }

    // Generička memoizacija — wrapper za bilo koju Function<T, R>
    static <T, R> Function<T, R> memoize(Function<T, R> fn) {
        Map<T, R> cache = new ConcurrentHashMap<>();
        return argument -> cache.computeIfAbsent(argument, fn);
    }

    // =========================================================================
    // Sekcija 5: Stream + Memoizacija
    //
    // Stream.iterate za iterativni (ne rekurzivni) Fibonači
    // — najelegantnije rešenje u Javi
    // =========================================================================

    static long fibStream(int n) {
        return Stream.iterate(new long[]{0, 1}, f -> new long[]{f[1], f[0] + f[1]})
                .limit(n + 1)
                .mapToLong(f -> f[0])
                .toArray()[(int) n]; // ili .skip(n).findFirst()
    }

    // Elegantnija varijanta — direktno findFirst
    static long fibStreamElegantno(int n) {
        if (n < 0) throw new IllegalArgumentException();
        return Stream.iterate(new long[]{0, 1}, f -> new long[]{f[1], f[0] + f[1]})
                .skip(n)
                .findFirst()
                .map(f -> f[0])
                .orElseThrow();
    }

    // =========================================================================
    // main
    // =========================================================================
    public static void main(String[] args) {
        System.out.println("=== 1. Naivna rekurzija — faktorijel ===\n");
        naivnaRekurzija();

        System.out.println("\n=== 2. Repni poziv — oblik koda ===\n");
        repniPoziv();

        System.out.println("\n=== 3. Trampolining ===\n");
        trampoliningPrimer();

        System.out.println("\n=== 4. Memoizacija — Fibonači ===\n");
        memoizacijaPrimer();

        System.out.println("\n=== 5. computeIfAbsent — API za keširanje ===\n");
        computeIfAbsentPrimer();

        System.out.println("\n=== 6. Stream.iterate — iterativni Fibonači ===\n");
        streamIteratePrimer();

        System.out.println("\n=== 7. Generička memoizacija ===\n");
        genericaMemoizacija();
    }

    static void naivnaRekurzija() {
        // Mali unos — radi
        System.out.println("10! = " + faktorijel(10));
        System.out.println("20! = " + faktorijel(20));

        // Veliko n — StackOverflowError!
        System.out.println("\nPokušaj faktorijel(100000):");
        try {
            System.out.println(faktorijel(100000));
        } catch (StackOverflowError e) {
            System.out.println("  StackOverflowError! Stek je iscrpljen.");
        }

        // Naivni Fibonači — proverimo performanse
        System.out.println("\nNaivni fibNaivno(40) — čekajte...");
        long t = System.currentTimeMillis();
        long r = fibNaivno(40);
        System.out.println("fib(40) = " + r + " (vreme: " + (System.currentTimeMillis()-t) + " ms)");
        System.out.println("fib(50) bi trajao SATE — eksponencijalna složenost!");
    }

    static void repniPoziv() {
        // Razlika između repnog i ne-repnog poziva
        System.out.println("Nije repni poziv:");
        System.out.println("  return n * faktorijel(n-1);");
        System.out.println("  ↑ mora da sačeka rezultat rekurzije, pa primeni *n");
        System.out.println("  → n frejma ostaje na steku istovremeno\n");

        System.out.println("Jeste repni poziv:");
        System.out.println("  return faktorijel_rep(n-1, n * akum);");
        System.out.println("  ↑ ništa se ne radi POSLE rekurzivnog poziva");
        System.out.println("  → teorijski, JVM može ponovo koristiti isti frejm");
        System.out.println("  → Java JVM to ne radi automatski, ali trampolining može!\n");

        // Mali primer radi
        System.out.println("faktorijel_rep(10, 1) = " + faktorijel_rep(10, 1));
    }

    static void trampoliningPrimer() {
        System.out.println("Faktorijel sa trampolinom (bez StackOverflow):");
        System.out.println("  10!      = " + trampolin(faktorijel_trampolin(10, 1)));
        System.out.println("  20!      = " + trampolin(faktorijel_trampolin(20, 1)));

        // Ovo bi bio StackOverflow bez trampolina!
        System.out.println("  100000!  ≡ ... (prebacuje se u petlju, nema stack problema)");
        try {
            long r = trampolin(faktorijel_trampolin(100000, 1));
            System.out.println("  100000! mod 10^18 = " + r + "  (overflow long, ali nema StackOverflow!)");
        } catch (Exception e) {
            System.out.println("  " + e);
        }

        System.out.println("\nFibonači sa trampolinom:");
        for (int i : new int[]{0, 1, 5, 10, 20, 40, 50}) {
            System.out.println("  fib(" + i + ") = " + trampolin(fib_trampolin(i, 0, 1)));
        }
    }

    static void memoizacijaPrimer() {
        // Memoizovani Fibonači — dramatično brži
        System.out.println("Sa memoizacijom:");
        long t = System.currentTimeMillis();
        for (int i = 0; i <= 50; i++) {
            System.out.print("fib(" + i + ")=" + fibMemoizovano(i) + " ");
            if ((i+1) % 5 == 0) System.out.println();
        }
        System.out.println("Vreme: " + (System.currentTimeMillis()-t) + " ms");
        System.out.println("Veličina keša: " + fibMemo.size() + " unosa");
    }

    static void computeIfAbsentPrimer() {
        // computeIfAbsent je srž memoizacije u Javi
        Map<String, Integer> cache = new HashMap<>();

        // Skupo izračunavanje (simulacija)
        Function<String, Integer> skupoIzraczunaj = s -> {
            System.out.println("  Računam za '" + s + "'... (skupo!)");
            return s.length() * 7 + s.hashCode() % 100;
        };

        System.out.println("Prvi poziv (nema u kešu):");
        cache.computeIfAbsent("java",   skupoIzraczunaj);
        cache.computeIfAbsent("stream", skupoIzraczunaj);

        System.out.println("\nDrugi poziv (već u kešu — skupo se NE izvršava):");
        cache.computeIfAbsent("java",   skupoIzraczunaj);
        cache.computeIfAbsent("stream", skupoIzraczunaj);

        System.out.println("\nKaš: " + cache);
    }

    static void streamIteratePrimer() {
        System.out.println("Fibonači koristeći Stream.iterate:");
        System.out.println("(iterativno, bez rekurzije, O(n))");

        System.out.print("Prvih 15: ");
        Stream.iterate(new long[]{0, 1}, f -> new long[]{f[1], f[0] + f[1]})
                .limit(15)
                .mapToLong(f -> f[0])
                .forEach(n -> System.out.print(n + " "));
        System.out.println();

        System.out.println("fib(70) = " + fibStreamElegantno(70));
        System.out.println("(nema rekurzije, nema stack problema, nema memoizacije — O(n))");
    }

    static void genericaMemoizacija() {
        // Skupo izračunavanje dužine stringa (simulacija)
        Function<Integer, Long> skupoFib = n -> fibMemoizovano(n);
        Function<Integer, Long> memoFib  = memoize(skupoFib);

        System.out.println("Generički memoize wrapper:");
        System.out.println("  memoFib(30) = " + memoFib.apply(30));
        System.out.println("  memoFib(30) = " + memoFib.apply(30) + " (iz keša)");

        // Primer sa skupim String operacijama
        Function<String, String> skupaTransformacija = memoize(s -> {
            System.out.println("  Računam transformaciju za: " + s);
            return s.chars()
                    .sorted()
                    .collect(StringBuilder::new, (sb, c) -> sb.append((char)c), StringBuilder::append)
                    .toString();
        });

        System.out.println("\nSortiranje karaktera (memoizovano):");
        System.out.println("  'java'   → " + skupaTransformacija.apply("java"));
        System.out.println("  'stream' → " + skupaTransformacija.apply("stream"));
        System.out.println("  'java'   → " + skupaTransformacija.apply("java") + " (keš!)");
    }
}
