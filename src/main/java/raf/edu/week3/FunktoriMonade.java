package raf.edu.week3;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Nedelja 3 — Funktori i Monade: konceptualni okvir iz FP
 *
 * Ove ideje dolaze iz teorije kategorija i matematički opisuju
 * obrasce koje već koristimo kroz Stream i Optional.
 *
 * Cilj: razumeti ZAŠTO Stream i Optional imaju upravo map i flatMap —
 * to nije slučajno, oni su implementacije dubokih matematičkih struktura.
 *
 * FUNKTOR = kontejner koji podržava map
 * MONADA  = funktor koji podržava i flatMap (+ "wrap" tj. of/just)
 */
public class FunktoriMonade {

    // -----------------------------------------------------------------------
    // Pomoćna klasa: naš vlastiti Box<T> — minimalni funktor
    // -----------------------------------------------------------------------
    static class Box<T> {
        private final T vrednost;

        private Box(T vrednost) {
            this.vrednost = vrednost;
        }

        // "wrap" / "unit" / "return" — pakuje vrednost u kontejner
        public static <T> Box<T> of(T vrednost) {
            return new Box<>(vrednost);
        }

        // map — funktorska operacija: Box<T> + (T → R) → Box<R>
        public <R> Box<R> map(Function<T, R> f) {
            return Box.of(f.apply(vrednost));
        }

        // flatMap — monadska operacija: Box<T> + (T → Box<R>) → Box<R>
        public <R> Box<R> flatMap(Function<T, Box<R>> f) {
            return f.apply(vrednost);  // f već vraća Box, ne "duplo pakujemo"
        }

        public T get() { return vrednost; }

        @Override
        public String toString() { return "Box[" + vrednost + "]"; }
    }

    public static void main(String[] args) {
        System.out.println("=== Funktor: Box<T> — vlastita implementacija ===\n");
        funktorPrimer();

        System.out.println("\n=== Zakoni funktora ===\n");
        zakoniFunktora();

        System.out.println("\n=== Optional kao funktor ===\n");
        optionalKaoFunktor();

        System.out.println("\n=== Stream kao funktor ===\n");
        streamKaoFunktor();

        System.out.println("\n=== Problem: map koji vraća kontejner ===\n");
        problemMapKontejner();

        System.out.println("\n=== Monada: flatMap rešava problem ===\n");
        monadaFlatMap();

        System.out.println("\n=== Monada u praksi: Optional ===\n");
        monadaOptional();

        System.out.println("\n=== Zašto su monade korisne? ===\n");
        zastoMonade();
    }

    // -----------------------------------------------------------------------
    // Funktor: kontejner + map
    // -----------------------------------------------------------------------
    static void funktorPrimer() {
        Box<Integer> broj = Box.of(5);
        System.out.println("Početak: " + broj);

        // map: Box<Integer> → Box<String>
        Box<String> tekst = broj.map(n -> "Broj " + n);
        System.out.println("map(n -> \"Broj \" + n): " + tekst);

        // map: ulančavanje
        Box<Integer> rezultat = Box.of("zdravo")
                .map(String::length)          // Box<String> → Box<Integer>
                .map(n -> n * 2);              // Box<Integer> → Box<Integer>
        System.out.println("\"zdravo\".length * 2 = " + rezultat);

        // Ključno: map ne "razpakuje" — uvek vraća Box
    }

    // -----------------------------------------------------------------------
    // Zakoni funktora — mora da važi za svaki ispravan funktor
    // -----------------------------------------------------------------------
    static void zakoniFunktora() {
        // Zakon 1: Identity — map(x -> x) ne menja ništa
        Box<String> original = Box.of("java");
        Box<String> posleIdentity = original.map(x -> x);
        System.out.println("Zakon 1 (identity): " + original.get().equals(posleIdentity.get())); // true

        // Zakon 2: Kompozicija — map(f).map(g) == map(g ∘ f)
        Function<String, String> f = String::toUpperCase;
        Function<String, Integer> g = String::length;

        Box<Integer> dvaProlaza = Box.of("hello").map(f).map(g);
        Box<Integer> jedanProlaz = Box.of("hello").map(f.andThen(g));
        System.out.println("Zakon 2 (kompozicija): " + dvaProlaza.get().equals(jedanProlaz.get())); // true

        // Isti zakoni važe za Optional i Stream
        Optional<String> opt = Optional.of("java");
        System.out.println("Optional identity: " +
                opt.map(x -> x).equals(opt)); // true

        System.out.println("Optional kompozicija: " +
                opt.map(String::toUpperCase).map(String::length)
                   .equals(opt.map(s -> s.toUpperCase().length()))); // true
    }

    // -----------------------------------------------------------------------
    // Optional kao funktor
    // -----------------------------------------------------------------------
    static void optionalKaoFunktor() {
        Optional<String> sa = Optional.of("Funkcionalno");
        Optional<String> bez = Optional.empty();

        // map na Optional — primenjuje funkciju SAMO ako vrednost postoji
        System.out.println("map na postojećoj: " + sa.map(String::length));  // Optional[12]
        System.out.println("map na praznoj:    " + bez.map(String::length)); // Optional.empty

        // Ulančavanje — nema if-ova!
        Optional<String> rezultat = Optional.of("  Java  ")
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(s -> s.length() > 3);
        System.out.println("Ulančano: " + rezultat);

        // "Kratki spoj" — ako bilo koji korak vrati empty, ostatak se preskače
        Optional<String> prazanUsred = Optional.of("kratko")
                .map(String::trim)
                .filter(s -> s.length() > 100) // vraća empty
                .map(String::toUpperCase);      // ovo se NE izvršava
        System.out.println("Kratki spoj: " + prazanUsred); // Optional.empty
    }

    // -----------------------------------------------------------------------
    // Stream kao funktor
    // -----------------------------------------------------------------------
    static void streamKaoFunktor() {
        List<String> reci = List.of("java", "stream", "funktor");

        // map: Stream<String> → Stream<Integer>
        List<Integer> duzine = reci.stream()
                .map(String::length)
                .collect(Collectors.toList());
        System.out.println("Dužine: " + duzine);

        // Kompozicija zakona — isti rezultat
        List<String> dvaProlaza = reci.stream()
                .map(String::toUpperCase)
                .map(s -> s + "!")
                .collect(Collectors.toList());

        List<String> jedanProlaz = reci.stream()
                .map(s -> s.toUpperCase() + "!")
                .collect(Collectors.toList());

        System.out.println("Zakon kompozicije na Stream: " + dvaProlaza.equals(jedanProlaz));
    }

    // -----------------------------------------------------------------------
    // Problem: šta ako funkcija u map-u SAMA vraća kontejner?
    // -----------------------------------------------------------------------
    static void problemMapKontejner() {
        // Funkcija koja vraća Box
        Function<Integer, Box<String>> opisBroja = n -> Box.of("Broj " + n);

        Box<Integer> broj = Box.of(42);

        // Pokušaj sa map → dobijamo Box<Box<String>> — dvostruko umotano!
        Box<Box<String>> dvostruko = broj.map(opisBroja);
        System.out.println("map + f:T→Box<R> = " + dvostruko);
        // Box[Box[Broj 42]] — ovo nije korisno!

        // Isti problem sa Optional:
        Function<String, Optional<String>> nadjiEmail = ime ->
                ime.equals("Ana") ? Optional.of("ana@uni.rs") : Optional.empty();

        Optional<String> imeOpt = Optional.of("Ana");
        Optional<Optional<String>> dvostrukoOpt = imeOpt.map(nadjiEmail);
        System.out.println("Optional<Optional<>>: " + dvostrukoOpt);
        // Optional[Optional[ana@uni.rs]] — ne možemo direktno koristiti
    }

    // -----------------------------------------------------------------------
    // Monada: flatMap "sravnjuje" dvostruko umotavanje
    // -----------------------------------------------------------------------
    static void monadaFlatMap() {
        Function<Integer, Box<String>> opisBroja = n -> Box.of("Broj " + n);

        Box<Integer> broj = Box.of(42);

        // flatMap umesto map → dobijamo Box<String> direktno
        Box<String> opis = broj.flatMap(opisBroja);
        System.out.println("flatMap + f:T→Box<R> = " + opis);
        // Box[Broj 42] — ispravno!

        // Ulančavanje flatMap operacija
        Box<String> rezultat = Box.of(5)
                .flatMap(n -> Box.of(n * 2))       // Box<Integer> → Box<Integer>
                .flatMap(n -> Box.of("Vrednost: " + n)); // Box<Integer> → Box<String>
        System.out.println("Ulančani flatMap: " + rezultat);
    }

    // -----------------------------------------------------------------------
    // Optional kao monada — praktičan primer bez if-ova
    // -----------------------------------------------------------------------
    static void monadaOptional() {
        java.util.Map<String, String> emailBaza = java.util.Map.of(
                "Ana", "ana@uni.rs", "Bojan", "bojan@uni.rs"
        );
        java.util.Map<String, String> telefonBaza = java.util.Map.of(
                "ana@uni.rs", "+381 11 123 456"
        );

        // SA flatMap — monada omogućava čist lanac bez if-ova
        // Svaki korak može "propustiti" ili "prekinuti" lanac
        for (String ime : List.of("Ana", "Bojan", "Cara")) {
            String telefon = Optional.of(ime)
                    .flatMap(n  -> Optional.ofNullable(emailBaza.get(n)))
                    .flatMap(em -> Optional.ofNullable(telefonBaza.get(em)))
                    .orElse("nema telefona");
            System.out.println(ime + " → " + telefon);
        }
    }

    // -----------------------------------------------------------------------
    // Zašto su monade toliko korisne?
    // -----------------------------------------------------------------------
    static void zastoMonade() {
        System.out.println("Monade se pojavljuju svuda u modernom programiranju:");
        System.out.println();
        System.out.println("  Optional<T>          — vrednost koja možda ne postoji");
        System.out.println("  Stream<T>            — nula ili više vrednosti (lista kao monada)");
        System.out.println("  CompletableFuture<T> — vrednost koja će biti dostupna u budućnosti");
        System.out.println("  Flux<T> / Mono<T>    — reaktivni streamovi (nedelja 8!)");
        System.out.println();
        System.out.println("Sve imaju:");
        System.out.println("  - 'wrap'   : Optional.of(),  Stream.of(),  Mono.just()");
        System.out.println("  - 'map'    : transformacija sadržaja");
        System.out.println("  - 'flatMap': kompozicija operacija koje vraćaju isti kontejner");
        System.out.println();
        System.out.println("Naučiti jednu monadu → razumeti sve!");
    }
}
