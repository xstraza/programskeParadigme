package raf.edu.week3;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Nedelja 3 — flatMap: mapiranje i sravnjivanje (flatten)
 *
 * flatMap rešava problem "stream of streams" — kada funkcija u map-u
 * vraća Stream<R> umesto R, dobijamo Stream<Stream<R>>.
 * flatMap to automatski "sravnjuje" u jedan Stream<R>.
 *
 * map:     T  →  R           =>  Stream<R>
 * flatMap: T  →  Stream<R>   =>  Stream<R>   (sravnjeno!)
 *
 * Intuicija: map transformiše 1:1, flatMap transformiše 1:N (ili 1:0).
 */
public class FlatMapPrimeri {

    record Autor(String ime, List<String> knjige) {}
    record Narudzbina(String kupac, List<String> stavke) {}

    public static void main(String[] args) {
        System.out.println("=== Motivacija: problem sa map ===\n");
        problemSaMap();

        System.out.println("\n=== flatMap rešenje ===\n");
        flatMapResenje();

        System.out.println("\n=== flatMap: 1 element → N elemenata ===\n");
        jednaNaVise();

        System.out.println("\n=== flatMap: 1 element → 0 elemenata (filtriranje) ===\n");
        jednaNaNula();

        System.out.println("\n=== Praktičan primer: autori i knjige ===\n");
        autoriIKnjige();

        System.out.println("\n=== map vs flatMap — vizuelno poređenje ===\n");
        mapVsFlatMap();

        System.out.println("\n=== flatMap na Optional ===\n");
        flatMapNaOptional();

        System.out.println("\n=== Uvod za narednu nedelju: flatMap na Stream i Optional ===\n");
        najavaFlatMap();
    }

    // -----------------------------------------------------------------------
    // Problem: map vraća Stream<String[]> — nije to što hoćemo
    // -----------------------------------------------------------------------
    static void problemSaMap() {
        List<String> recenice = List.of(
                "zdravo svete",
                "funkcionalno programiranje",
                "stream api"
        );

        // map(split) daje Stream<String[]> — svaka rečenica postaje niz
        Stream<String[]> problematican = recenice.stream()
                .map(r -> r.split(" "));

        // Ne možemo direktno raditi sa rečima — imamo stream nizova!
        System.out.println("Tip sa map: Stream<String[]>");
        problematican.forEach(niz -> System.out.println("  " + Arrays.toString(niz)));
        // ["zdravo", "svete"], ["funkcionalno", "programiranje"], ["stream", "api"]
        // Ovo nam nije korisno ako hoćemo sve reči u jednom streamu!
    }

    // -----------------------------------------------------------------------
    // flatMap rešava problem — dobijamo Stream<String>
    // -----------------------------------------------------------------------
    static void flatMapResenje() {
        List<String> recenice = List.of(
                "zdravo svete",
                "funkcionalno programiranje",
                "stream api"
        );

        // flatMap: svaka rečenica → Arrays.stream(split) → sve se sravnjuje
        List<String> sve_reci = recenice.stream()
                .flatMap(r -> Arrays.stream(r.split(" ")))
                .collect(Collectors.toList());

        System.out.println("Sve reči: " + sve_reci);
        // [zdravo, svete, funkcionalno, programiranje, stream, api]

        // Možemo nastaviti pipeline normalno
        long brojJednosloznih = recenice.stream()
                .flatMap(r -> Arrays.stream(r.split(" ")))
                .filter(rec -> !rec.contains(" "))
                .map(String::toUpperCase)
                .distinct()
                .count();
        System.out.println("Broj jedinstvenih reči (velika slova): " + brojJednosloznih);

        // Unikatne reči, sortirane
        List<String> unikatne = recenice.stream()
                .flatMap(r -> Arrays.stream(r.split(" ")))
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        System.out.println("Unikatne sortirane reči: " + unikatne);
    }

    // -----------------------------------------------------------------------
    // flatMap: svaki element → više elemenata (1:N)
    // -----------------------------------------------------------------------
    static void jednaNaVise() {
        List<Integer> brojevi = List.of(1, 2, 3, 4);

        // Svaki broj razvijamo u stream: n → [n, n*n]
        List<Integer> razvijeno = brojevi.stream()
                .flatMap(n -> Stream.of(n, n * n))
                .collect(Collectors.toList());
        System.out.println("Svaki broj i njegov kvadrat: " + razvijeno);
        // [1, 1, 2, 4, 3, 9, 4, 16]

        // Generisanje opsega za svaki element
        // Svaki n → [1, 2, ..., n]
        List<Integer> sveDo = List.of(1, 2, 3).stream()
                .flatMapToInt(n -> java.util.stream.IntStream.rangeClosed(1, n))
                .boxed()
                .collect(Collectors.toList());
        System.out.println("Svi opsezi: " + sveDo);
        // [1,  1,2,  1,2,3]
    }

    // -----------------------------------------------------------------------
    // flatMap: svaki element → 0 elemenata (filtriranje)
    // Ovo je alternativa filter-u, korisno kada odluka zavisi od parsiranja
    // -----------------------------------------------------------------------
    static void jednaNaNula() {
        List<String> ulaz = List.of("42", "abc", "17", "xyz", "100");

        // Parsiranje broja — može uspeti (1 element) ili ne (0 elemenata)
        List<Integer> validniBrojevi = ulaz.stream()
                .flatMap(s -> {
                    try {
                        return Stream.of(Integer.parseInt(s)); // 1 element
                    } catch (NumberFormatException e) {
                        return Stream.empty(); // 0 elemenata — element se "briše"
                    }
                })
                .collect(Collectors.toList());

        System.out.println("Validan parse: " + validniBrojevi);
        // [42, 17, 100]

        // Isti efekat, ali čišće sa Optional.stream() (Java 9+)
        List<Integer> saOptional = ulaz.stream()
                .map(s -> {
                    try { return Optional.of(Integer.parseInt(s)); }
                    catch (NumberFormatException e) { return Optional.<Integer>empty(); }
                })
                .flatMap(Optional::stream)  // Optional<T> → Stream od 0 ili 1 elementa
                .collect(Collectors.toList());

        System.out.println("Sa Optional.stream(): " + saOptional);
    }

    // -----------------------------------------------------------------------
    // Praktičan primer: lista autora, svaki autor ima listu knjiga
    // Hoćemo: jednu listu svih knjiga
    // -----------------------------------------------------------------------
    static void autoriIKnjige() {
        List<Autor> autori = List.of(
                new Autor("Venkat",  List.of("FP in Java", "Programming Concurrency", "Pragmatic Scala")),
                new Autor("Martin",  List.of("Clean Code", "Clean Architecture")),
                new Autor("Fowler",  List.of("Refactoring", "Patterns of Enterprise Application Architecture"))
        );

        // Sve knjige svih autora — flatMap "otvara" svaki autor i uzima mu knjige
        List<String> sveknjige = autori.stream()
                .flatMap(autor -> autor.knjige().stream())
                .collect(Collectors.toList());
        System.out.println("Sve knjige (" + sveknjige.size() + "):");
        sveknjige.forEach(k -> System.out.println("  - " + k));

        // Knjige sortirane po dužini naslova
        System.out.println("\nSortirano po dužini naslova:");
        autori.stream()
                .flatMap(a -> a.knjige().stream())
                .sorted(java.util.Comparator.comparingInt(String::length))
                .forEach(k -> System.out.println("  " + k));

        // Autori koji imaju više od 2 knjige
        System.out.println("\nAutori sa >2 knjige:");
        autori.stream()
                .filter(a -> a.knjige().size() > 2)
                .map(Autor::ime)
                .forEach(System.out::println);
    }

    // -----------------------------------------------------------------------
    // Vizuelno poređenje: map vs flatMap
    // -----------------------------------------------------------------------
    static void mapVsFlatMap() {
        List<List<Integer>> lista_lista = List.of(
                List.of(1, 2, 3),
                List.of(4, 5),
                List.of(6, 7, 8, 9)
        );

        // map — dobijamo Stream<List<Integer>> — ne sravnjuje
        System.out.println("Posle map(x -> x):");
        lista_lista.stream()
                .map(podlista -> podlista)
                .forEach(System.out::println);
        // [1, 2, 3]
        // [4, 5]
        // [6, 7, 8, 9]

        // flatMap — dobijamo Stream<Integer> — sravnjuje
        System.out.println("\nPosle flatMap(Collection::stream):");
        List<Integer> sravnjeno = lista_lista.stream()
                .flatMap(java.util.Collection::stream)
                .collect(Collectors.toList());
        System.out.println(sravnjeno);
        // [1, 2, 3, 4, 5, 6, 7, 8, 9]
    }

    // -----------------------------------------------------------------------
    // flatMap na Optional — rešava problem ugnežđenih Optional-a
    // Detalji za narednu nedelju, ovde uvod
    // -----------------------------------------------------------------------
    static void flatMapNaOptional() {
        // Metode koje mogu da ne vrate vrednost
        // (simulacija: lookup u mapi koja možda nema vrednost)
        java.util.Map<String, String> emailBaza = java.util.Map.of(
                "Ana", "ana@uni.rs",
                "Bojan", "bojan@uni.rs"
        );
        java.util.Map<String, String> gradBaza = java.util.Map.of(
                "ana@uni.rs", "Beograd"
        );

        Optional<String> ime = Optional.of("Ana");

        // SA flatMap — elegantno ulančavanje Optional operacija
        Optional<String> grad = ime
                .flatMap(n  -> Optional.ofNullable(emailBaza.get(n)))    // Ana → Optional<email>
                .flatMap(em -> Optional.ofNullable(gradBaza.get(em)));    // email → Optional<grad>

        System.out.println("Grad za 'Ana': " + grad.orElse("nepoznato"));

        // Kada nema vrednosti — Optional.empty() se "propagira"
        Optional<String> gradBojana = Optional.of("Bojan")
                .flatMap(n  -> Optional.ofNullable(emailBaza.get(n)))
                .flatMap(em -> Optional.ofNullable(gradBaza.get(em)));

        System.out.println("Grad za 'Bojan': " + gradBojana.orElse("nepoznato"));

        // Nepostojeci korisnik
        Optional<String> gradNepoznat = Optional.of("Nepostojeci")
                .flatMap(n  -> Optional.ofNullable(emailBaza.get(n)))
                .flatMap(em -> Optional.ofNullable(gradBaza.get(em)));

        System.out.println("Grad za 'Nepostojeci': " + gradNepoznat.orElse("nepoznato"));
    }

    // -----------------------------------------------------------------------
    // Najava za narednu nedelju: flatMap — detaljnije
    // -----------------------------------------------------------------------
    static void najavaFlatMap() {
        // Naredne nedelje:
        // - detaljniji primeri flatMap sa Stream i Optional
        // - flatMapping kao Collector
        // - join i collect naprednije
        // - kada map, a kada flatMap — dublja analiza

        System.out.println("Naredna nedelja: lenja evaluacija, optimizacija rekurzije,");
        System.out.println("i detaljniji flatMap na Stream i Optional.");
    }
}
