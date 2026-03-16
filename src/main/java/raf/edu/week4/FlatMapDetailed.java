package raf.edu.week4;

import java.util.*;
import java.util.stream.*;
import java.util.function.*;

/**
 * Nedelja 4 — flatMap: napredni obrasci na Stream i Optional
 *
 * Osnove flatMap (motivacija, sravnjivanje, map vs flatMap) su
 * obrađene u nedelji 3 (FlatMapPrimeri.java). Ovde nastavljamo sa:
 *
 *   1. Kombinatorika — Dekartov proizvod dve liste
 *   2. Opcioni elementi — lookup i parsiranje kroz flatMap
 *   3. flatMap na Optional — lanac koji može da "propadne"
 *   4. Optional::stream — most između stream pipeline-a i Optional-a
 */
public class FlatMapDetailed {

    // Domenski objekti
    record Narudzbina(String kupac, List<Stavka> stavke) {}
    record Stavka(String naziv, int kolicina) {}
    record Korisnik(String ime, Optional<Adresa> adresa) {}
    record Adresa(String grad, Optional<String> postanskiBroj) {}

    public static void main(String[] args) {
        System.out.println("=== 1. Kombinatorika — Dekartov proizvod ===\n");
        kombinatorika();

        System.out.println("\n=== 2. Opcioni elementi — lookup i parsiranje ===\n");
        opcioniElementi();

        System.out.println("\n=== 3. flatMap na Optional — lanac ===\n");
        flatMapNaOptional();

        System.out.println("\n=== 4. Optional::stream — most između svetova ===\n");
        optionalStream();

        System.out.println("\n=== 5. Collectors.flatMapping — flatMap unutar groupingBy ===\n");
        flatMappingPrimer();
    }

    // -----------------------------------------------------------------------
    // 1. Kombinatorika — Dekartov proizvod
    //    Svaki element jedne liste spariti sa svakim iz druge — bez petlji.
    //    flatMap + map unutar njega = ugneždena iteracija deklarativno.
    // -----------------------------------------------------------------------
    static void kombinatorika() {
        List<String> velicine = List.of("S", "M", "L", "XL");
        List<String> boje     = List.of("crvena", "plava", "bela");

        // Svaka veličina → stream svih boja sparenih s njom → sve se sravnjuje
        System.out.println("Sve kombinacije veličina i boja:");
        List<String> kombinacije = velicine.stream()
                .flatMap(v -> boje.stream().map(b -> v + "/" + b))
                .collect(Collectors.toList());
        kombinacije.forEach(k -> System.out.print(k + "  "));
        System.out.println();
        System.out.println("Ukupno: " + kombinacije.size() + " kombinacija");

        // Parovi iz jedne liste — bez ponavljanja i bez (a,a)
        List<Integer> cifre = List.of(1, 2, 3, 4);
        System.out.println("\nSvi neuređeni parovi iz " + cifre + ":");
        cifre.stream()
                .flatMap(a -> cifre.stream()
                        .filter(b -> b > a)
                        .map(b -> "(" + a + "," + b + ")"))
                .forEach(p -> System.out.print(p + " "));
        System.out.println();

        // Narudzbine — sve stavke obogaćene imenom kupca
        List<Narudzbina> narudzbine = List.of(
                new Narudzbina("Ana",   List.of(new Stavka("knjiga", 2), new Stavka("olovka", 5))),
                new Narudzbina("Bojan", List.of(new Stavka("laptop", 1))),
                new Narudzbina("Ana",   List.of(new Stavka("sveska", 3)))
        );

        System.out.println("\nSve stavke sa kupcem:");
        narudzbine.stream()
                .flatMap(n -> n.stavke().stream()
                        .map(s -> n.kupac() + " → " + s.naziv() + " x" + s.kolicina()))
                .forEach(System.out::println);

        // Ukupna kolicina po kupcu koristeći flatMap + groupingBy
        System.out.println("\nUkupna kolicina po kupcu:");
        narudzbine.stream()
                .flatMap(n -> n.stavke().stream()
                        .map(s -> Map.entry(n.kupac(), s.kolicina())))
                .collect(Collectors.groupingBy(Map.Entry::getKey,
                        Collectors.summingInt(Map.Entry::getValue)))
                .forEach((k, v) -> System.out.println("  " + k + ": " + v));
    }

    // -----------------------------------------------------------------------
    // 2. Opcioni elementi — parsiranje, lookup, validacija
    //    flatMap kao alternativa try-catch bloku unutar stream pipeline-a.
    //    Svaki element daje 0 elemenata (nevalidan) ili 1 element (validan).
    // -----------------------------------------------------------------------
    static void opcioniElementi() {
        List<String> ulaz = List.of("42", "hello", "17", "", "100", "abc", "-5");

        // Sigurno parsiranje — nevalidan string daje Optional.empty() → 0 elemenata
        System.out.println("Ulaz: " + ulaz);
        List<Integer> validni = ulaz.stream()
                .flatMap(s -> parseIntSigurno(s).stream()) // Optional::stream: 0 ili 1
                .collect(Collectors.toList());
        System.out.println("Validan parse: " + validni);

        // Samo pozitivni parsirani
        List<Integer> pozitivni = ulaz.stream()
                .flatMap(s -> parseIntSigurno(s).stream())
                .filter(n -> n > 0)
                .collect(Collectors.toList());
        System.out.println("Pozitivni:     " + pozitivni);

        // Lookup u mapi — artikli koji ne postoje u cenovniku se tiho preskaču
        Map<String, Integer> cenovnik = Map.of("jabuka", 100, "kruška", 120, "banana", 80);
        List<String> korpa = List.of("jabuka", "breskva", "banana", "šljiva", "kruška");

        System.out.println("\nCene dostupnih artikala:");
        korpa.stream()
                .flatMap(p -> Optional.ofNullable(cenovnik.get(p))
                        .map(c -> p + ": " + c + " din")
                        .stream())
                .forEach(System.out::println);

        int ukupno = korpa.stream()
                .flatMap(p -> Optional.ofNullable(cenovnik.get(p)).stream())
                .mapToInt(Integer::intValue)
                .sum();
        System.out.println("Ukupno dostupno: " + ukupno + " din");
    }

    static Optional<Integer> parseIntSigurno(String s) {
        try {
            return Optional.of(Integer.parseInt(s.trim()));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    // -----------------------------------------------------------------------
    // 3. flatMap na Optional — lanac koji može da "propadne"
    //
    //    Kada funkcija u map-u vraća Optional<R>, dobijamo Optional<Optional<R>>.
    //    flatMap to sravnjuje. Svaki korak može "prekinuti" lanac vrativši empty.
    //    Rezultat: nema if-ova, nema null provjera, nema ugnežđavanja.
    // -----------------------------------------------------------------------
    static void flatMapNaOptional() {
        List<Korisnik> korisnici = List.of(
                new Korisnik("Ana",     Optional.of(new Adresa("Beograd",  Optional.of("11000")))),
                new Korisnik("Bojan",   Optional.of(new Adresa("Novi Sad", Optional.empty()))),
                new Korisnik("Čedomir", Optional.empty())
        );

        System.out.println("Poštanski broj za svakog korisnika:");
        korisnici.forEach(k -> {
            String postanski = Optional.of(k)
                    .flatMap(Korisnik::adresa)         // Korisnik → Optional<Adresa>
                    .flatMap(Adresa::postanskiBroj)    // Adresa   → Optional<String>
                    .orElse("(nije dostupno)");        // default ako bilo koji korak vrati empty
            System.out.println("  " + k.ime() + ": " + postanski);
        });

        // Poređenje: isti rezultat bez flatMap — "piramida of doom"
        System.out.println("\nIsti rezultat bez flatMap (ružno):");
        korisnici.forEach(k -> {
            String postanski = "(nije dostupno)";
            if (k.adresa().isPresent()) {
                if (k.adresa().get().postanskiBroj().isPresent()) {
                    postanski = k.adresa().get().postanskiBroj().get();
                }
            }
            System.out.println("  " + k.ime() + ": " + postanski);
        });

        // flatMap se kombinuje sa filter i map unutar lanca
        System.out.println("\nGradovi korisnika koji imaju poštanski broj (velika slova):");
        korisnici.stream()
                .flatMap(k -> Optional.of(k)
                        .flatMap(Korisnik::adresa)
                        .filter(a -> a.postanskiBroj().isPresent())
                        .map(a -> a.grad().toUpperCase())
                        .stream())
                .forEach(System.out::println);
    }

    // -----------------------------------------------------------------------
    // 4. Optional::stream — most između stream pipeline-a i Optional-a
    //    Java 9+: Optional<T>.stream() daje Stream od 0 ili 1 elementa.
    //    Korisno kada radimo sa streamom koji sadrži Optional vrednosti.
    // -----------------------------------------------------------------------
    static void optionalStream() {
        // Lista koja meša prisutne i odsutne vrednosti
        List<Optional<String>> mesavina = List.of(
                Optional.of("java"),
                Optional.empty(),
                Optional.of("stream"),
                Optional.empty(),
                Optional.of("lambda")
        );

        // Moderni stil — Optional::stream (Java 9+)
        System.out.println("Samo prisutne vrednosti (Optional::stream):");
        List<String> prisutne = mesavina.stream()
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
        System.out.println("  " + prisutne);

        // Stariji stil za poređenje — filter + map (Java 8)
        System.out.println("Isti rezultat (filter + map, Java 8):");
        List<String> prisutneStaro = mesavina.stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        System.out.println("  " + prisutneStaro);

        // Praktičan primer: stream ID-ova → lookup → samo pronađeni
        Map<Integer, String> imenik = Map.of(1, "Ana", 3, "Čedomir", 5, "Eva");
        List<Integer> ids = List.of(1, 2, 3, 4, 5, 6);

        System.out.println("\nIme za id-ove " + ids + " (samo pronađeni):");
        ids.stream()
                .flatMap(id -> Optional.ofNullable(imenik.get(id)).stream())
                .forEach(System.out::println);

        // Kombinacija: Optional lanac unutar stream pipeline-a
        Map<String, String> emailBaza = Map.of("Ana", "ana@uni.rs", "Bojan", "bojan@uni.rs");
        Map<String, String> gradBaza  = Map.of("ana@uni.rs", "Beograd");

        System.out.println("\nGrad za svakog korisnika:");
        List.of("Ana", "Bojan", "Nepostojeci").forEach(ime -> {
            String grad = Optional.of(ime)
                    .flatMap(n  -> Optional.ofNullable(emailBaza.get(n)))
                    .flatMap(em -> Optional.ofNullable(gradBaza.get(em)))
                    .orElse("(nepoznato)");
            System.out.println("  " + ime + " → " + grad);
        });
    }

    // -----------------------------------------------------------------------
    // 5. Collectors.flatMapping — flatMap unutar groupingBy
    //
    //    Problem: kada grupišemo elemente koji sadrže kolekcije, i hoćemo
    //    sravnjenu listu po grupi. flatMap pre groupingBy gubi vezu sa grupom.
    //    flatMapping rešava: flatMap se dešava UNUTAR kolektora.
    // -----------------------------------------------------------------------
    static void flatMappingPrimer() {
        List<Narudzbina> narudzbine = List.of(
                new Narudzbina("Ana",   List.of(new Stavka("knjiga", 2), new Stavka("olovka", 5))),
                new Narudzbina("Bojan", List.of(new Stavka("laptop", 1))),
                new Narudzbina("Ana",   List.of(new Stavka("sveska", 3)))
        );

        // Bez flatMapping — dobijamo List<List<Stavka>> po kupcu (ugnežđeno!)
        Map<String, List<List<Stavka>>> losije = narudzbine.stream()
                .collect(Collectors.groupingBy(
                        Narudzbina::kupac,
                        Collectors.mapping(Narudzbina::stavke, Collectors.toList())
                ));
        System.out.println("Bez flatMapping (ugnežđeno):");
        losije.forEach((k, v) -> System.out.println("  " + k + ": " + v));

        // Sa flatMapping — direktno sravnjuje stavke po kupcu
        Map<String, List<String>> stavkePoKupcu = narudzbine.stream()
                .collect(Collectors.groupingBy(
                        Narudzbina::kupac,
                        Collectors.flatMapping(
                                n -> n.stavke().stream().map(Stavka::naziv),
                                Collectors.toList()
                        )
                ));
        System.out.println("\nSa flatMapping (sravnjeno):");
        stavkePoKupcu.forEach((k, v) -> System.out.println("  " + k + ": " + v));

        // Ukupna količina po kupcu koristeći flatMapping + summingInt
        Map<String, Integer> kolicinaPoKupcu = narudzbine.stream()
                .collect(Collectors.groupingBy(
                        Narudzbina::kupac,
                        Collectors.flatMapping(
                                n -> n.stavke().stream(),
                                Collectors.summingInt(Stavka::kolicina)
                        )
                ));
        System.out.println("\nUkupna količina po kupcu:");
        kolicinaPoKupcu.forEach((k, v) -> System.out.println("  " + k + ": " + v));
    }
}
