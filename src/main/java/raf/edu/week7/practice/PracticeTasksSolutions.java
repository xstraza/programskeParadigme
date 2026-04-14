package raf.edu.week7.practice;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.*;
import java.util.stream.*;

/**
 * Paradigme Programiranja — Nedelja 7
 * Priprema za kolokvijum — REŠENJA
 */
public class PracticeTasksSolutions {

    // =========================================================================
    // Zadatak 1 — filter + map + collect
    // =========================================================================

    static List<String> filterAndUppercase(List<String> reci, int minDuzina) {
        return reci.stream()
                .filter(s -> s.length() > minDuzina)
                .map(String::toUpperCase)
                .toList();
    }

    // =========================================================================
    // Zadatak 2 — reduce: najduži string
    // =========================================================================

    static String najduziString(List<String> reci) {
        return reci.stream()
                .reduce("", (a, b) -> a.length() >= b.length() ? a : b);
    }

    // =========================================================================
    // Zadatak 3 — Optional lančanje
    // =========================================================================

    static String nadjiStatus(String ime,
                              Map<String, String> korisnici,
                              Map<String, Integer> rangovi) {
        return Optional.ofNullable(korisnici.get(ime))
                .flatMap(email -> Optional.ofNullable(rangovi.get(email)))
                .filter(rang -> rang > 5)
                .map(rang -> "VIP: " + ime)
                .orElse("Regular: " + ime);
    }

    // =========================================================================
    // Zadatak 4 — Predicate kompozicija
    // =========================================================================

    static Predicate<Integer> spojiSve(List<Predicate<Integer>> predikati) {
        return predikati.stream()
                .reduce(n -> true, Predicate::and);
    }

    static List<Integer> filtrirajSaSvim(List<Integer> brojevi,
                                         List<Predicate<Integer>> predikati) {
        return brojevi.stream()
                .filter(spojiSve(predikati))
                .toList();
    }

    // =========================================================================
    // Zadatak 5 — Function kompozicija
    // =========================================================================

    static Function<String, String> napraviSlugifier() {
        UnaryOperator<String> trim = String::trim;
        UnaryOperator<String> lower = String::toLowerCase;
        UnaryOperator<String> dash = s -> s.replaceAll("\\s+", "-");
        return trim.andThen(lower).andThen(dash);
    }

    // =========================================================================
    // Zadatak 6 — flatMap unikatne reči
    // =========================================================================

    static List<String> unikatneReci(List<String> recenice) {
        return recenice.stream()
                .flatMap(s -> Arrays.stream(s.split("\\s+")))
                .map(String::toLowerCase)
                .distinct()
                .sorted()
                .toList();
    }

    // =========================================================================
    // Zadatak 7 — groupingBy + counting
    // =========================================================================

    static Map<Character, Long> brojPoPocetotnomSlovu(List<String> reci) {
        return reci.stream()
                .map(s -> Character.toUpperCase(s.charAt(0)))
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        Collectors.counting()
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<Character, Long>comparingByValue().reversed()
                        .thenComparing(Map.Entry::getKey))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    // =========================================================================
    // Zadatak 8 — partitioningBy prosek
    // =========================================================================

    static Map<Boolean, Double> prosekParnihNeparnih(List<Integer> brojevi) {
        return brojevi.stream()
                .collect(Collectors.partitioningBy(
                        n -> n % 2 == 0,
                        Collectors.averagingInt(Integer::intValue)
                ));
    }

    // =========================================================================
    // Zadatak 9 — Fibonacci
    // =========================================================================

    static List<Long> fibonacci(int n) {
        return Stream.iterate(
                        new long[]{0, 1},
                        f -> f.length > 0,
                        f -> new long[]{f[1], f[0] + f[1]}
                )
                .limit(n)
                .map(f -> f[0])
                .toList();
    }

    // =========================================================================
    // Zadatak 10 — Numerisana lista
    // =========================================================================

    static String numerisanaLista(List<String> imena) {
        return IntStream.range(0, imena.size())
                .mapToObj(i -> (i + 1) + ". " + imena.get(i))
                .collect(Collectors.joining(", "));
    }

    // =========================================================================
    // Zadatak 11 — Currying
    // =========================================================================

    static Function<Integer, Function<Integer, Function<Integer, Integer>>>
    linearnaFunkcija = a -> b -> x -> a * x + b;

    // =========================================================================
    // Zadatak 12 — Method references
    // =========================================================================

    static List<Integer> pipelineSaRefovima(List<String> stringovi) {
        return stringovi.stream()
                .map(String::trim)           // instance method on type
                .map(Integer::parseInt)       // static method ref
                .filter(n -> n > 0)
                .collect(Collectors.toCollection(ArrayList::new)); // constructor ref
    }

    // =========================================================================
    // Zadatak 13 — Memoizacija
    // =========================================================================

    static <T, R> Function<T, R> memoize(Function<T, R> fn) {
        Map<T, R> cache = new ConcurrentHashMap<>();
        return key -> cache.computeIfAbsent(key, fn);
    }

    // =========================================================================
    // Zadatak 14 — reduce u objekat
    // =========================================================================

    record Statistika(int brojReci, int brojKaraktera, String najduza) {
        static final Statistika PRAZNA = new Statistika(0, 0, "");

        Statistika dodaj(String rec) {
            return new Statistika(
                    brojReci + 1,
                    brojKaraktera + rec.length(),
                    rec.length() > najduza.length() ? rec : najduza
            );
        }

        Statistika spoji(Statistika other) {
            if (other.brojReci == 0) return this;
            if (this.brojReci == 0) return other;
            String duza = this.najduza.length() >= other.najduza.length()
                    ? this.najduza : other.najduza;
            return new Statistika(
                    brojReci + other.brojReci,
                    brojKaraktera + other.brojKaraktera,
                    duza
            );
        }
    }

    static Statistika statistikaReci(List<String> reci) {
        return reci.stream()
                .reduce(Statistika.PRAZNA,
                        (stat, rec) -> stat.dodaj(rec),
                        Statistika::spoji);
    }

    // =========================================================================
    // Zadatak 15 — Lazy Supplier
    // =========================================================================

    @SuppressWarnings("unchecked")
    static <T> Supplier<T> lazy(Supplier<T> supplier) {
        Object[] cache = {null};
        boolean[] computed = {false};
        return () -> {
            if (!computed[0]) {
                cache[0] = supplier.get();
                computed[0] = true;
            }
            return (T) cache[0];
        };
    }

    // =========================================================================
    // Zadatak 16 — Refactor: najčešća reč
    // =========================================================================

    static String najcescaRecImperativ(List<String> reci) {
        Map<String, Integer> brojac = new HashMap<>();
        for (String rec : reci) {
            String lower = rec.toLowerCase();
            brojac.put(lower, brojac.getOrDefault(lower, 0) + 1);
        }
        String najbolja = "";
        int maxBroj = 0;
        for (Map.Entry<String, Integer> entry : brojac.entrySet()) {
            if (entry.getValue() > maxBroj ||
                (entry.getValue() == maxBroj && entry.getKey().compareTo(najbolja) < 0)) {
                najbolja = entry.getKey();
                maxBroj = entry.getValue();
            }
        }
        return najbolja;
    }

    static String najcescaRecFP(List<String> reci) {
        return reci.stream()
                .map(String::toLowerCase)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()
                        .thenComparing(Map.Entry::getKey))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse("");
    }

    // =========================================================================
    // Zadatak 17 — Refactor: invertovana mapa
    // =========================================================================

    static Map<String, List<String>> invertujMapuImperativ(Map<String, List<String>> original) {
        Map<String, List<String>> rezultat = new TreeMap<>();
        for (Map.Entry<String, List<String>> entry : original.entrySet()) {
            String osoba = entry.getKey();
            for (String hobi : entry.getValue()) {
                rezultat.computeIfAbsent(hobi, k -> new ArrayList<>()).add(osoba);
            }
        }
        for (List<String> lista : rezultat.values()) {
            Collections.sort(lista);
        }
        return rezultat;
    }

    static Map<String, List<String>> invertujMapuFP(Map<String, List<String>> original) {
        return original.entrySet().stream()
                .flatMap(e -> e.getValue().stream()
                        .map(hobi -> Map.entry(hobi, e.getKey())))
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        TreeMap::new,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                ));
    }

    // =========================================================================
    // Zadatak 18 — Refactor: balansi
    // =========================================================================

    record Transakcija(String osoba, double iznos, String tip) {}

    static Map<String, Double> balansiImperativ(List<Transakcija> transakcije) {
        Map<String, Double> balansi = new HashMap<>();
        for (Transakcija t : transakcije) {
            double vrednost = t.tip().equals("UPLATA") ? t.iznos() : -t.iznos();
            balansi.merge(t.osoba(), vrednost, Double::sum);
        }
        List<Map.Entry<String, Double>> pozitivni = new ArrayList<>();
        for (Map.Entry<String, Double> entry : balansi.entrySet()) {
            if (entry.getValue() > 0) {
                pozitivni.add(entry);
            }
        }
        pozitivni.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        Map<String, Double> rezultat = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry : pozitivni) {
            rezultat.put(entry.getKey(), entry.getValue());
        }
        return rezultat;
    }

    static Map<String, Double> balansiFP(List<Transakcija> transakcije) {
        return transakcije.stream()
                .collect(Collectors.groupingBy(
                        Transakcija::osoba,
                        Collectors.summingDouble(t ->
                                t.tip().equals("UPLATA") ? t.iznos() : -t.iznos())
                ))
                .entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    // =========================================================================
    // Zadatak 19 — Refactor: matrica flat
    // =========================================================================

    static List<Integer> matricaFlatImperativ(List<List<Integer>> matrica) {
        List<Integer> rezultat = new ArrayList<>();
        for (List<Integer> red : matrica) {
            for (int n : red) {
                int kvadrat = n * n;
                if (kvadrat > 10) {
                    rezultat.add(kvadrat);
                }
            }
        }
        Collections.sort(rezultat);
        return rezultat;
    }

    static List<Integer> matricaFlatFP(List<List<Integer>> matrica) {
        return matrica.stream()
                .flatMap(Collection::stream)
                .map(n -> n * n)
                .filter(n -> n > 10)
                .sorted()
                .toList();
    }

    // =========================================================================
    // Zadatak 20 — Refactor: histogram
    // =========================================================================

    static List<Map.Entry<Character, Long>> histogramImperativ(String tekst, int topN) {
        Map<Character, Long> brojac = new HashMap<>();
        for (char c : tekst.toCharArray()) {
            if (Character.isLetter(c)) {
                char lower = Character.toLowerCase(c);
                brojac.merge(lower, 1L, Long::sum);
            }
        }
        List<Map.Entry<Character, Long>> sortiran = new ArrayList<>(brojac.entrySet());
        sortiran.sort((a, b) -> {
            int cmp = Long.compare(b.getValue(), a.getValue());
            return cmp != 0 ? cmp : Character.compare(a.getKey(), b.getKey());
        });
        return sortiran.subList(0, Math.min(topN, sortiran.size()));
    }

    static List<Map.Entry<Character, Long>> histogramFP(String tekst, int topN) {
        return tekst.chars()
                .filter(Character::isLetter)
                .map(Character::toLowerCase)
                .mapToObj(c -> (char) c)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<Character, Long>comparingByValue().reversed()
                        .thenComparing(Map.Entry::getKey))
                .limit(topN)
                .toList();
    }

    // =========================================================================
    // main
    // =========================================================================
    public static void main(String[] args) {
        System.out.println("Nedelja 7 — Priprema za kolokvijum — Rešenja\n");

        // --- Zadatak 1 ---
        System.out.println("─── 1: Filter + Uppercase ───");
        proveri("Z1", filterAndUppercase(List.of("ana", "bo", "cara", "d", "eva"), 2),
                List.of("ANA", "CARA", "EVA"));

        // --- Zadatak 2 ---
        System.out.println("─── 2: Najduži string ───");
        proveri("Z2a", najduziString(List.of("java", "fp", "stream", "go")), "stream");
        proveri("Z2b", najduziString(List.of()), "");

        // --- Zadatak 3 ---
        System.out.println("─── 3: Optional lančanje ───");
        Map<String, String> korisnici = Map.of("Ana", "ana@x.com", "Bojan", "bojan@x.com");
        Map<String, Integer> rangovi = Map.of("ana@x.com", 8, "bojan@x.com", 3);
        proveri("Z3a VIP",     nadjiStatus("Ana", korisnici, rangovi), "VIP: Ana");
        proveri("Z3b Regular", nadjiStatus("Bojan", korisnici, rangovi), "Regular: Bojan");
        proveri("Z3c Missing", nadjiStatus("Xyz", korisnici, rangovi), "Regular: Xyz");

        // --- Zadatak 4 ---
        System.out.println("─── 4: Predicate kompozicija ───");
        List<Predicate<Integer>> pred = List.of(n -> n > 0, n -> n < 100, n -> n % 2 == 0);
        proveri("Z4", filtrirajSaSvim(List.of(3, 50, -4, 72, 101, 8), pred),
                List.of(50, 72, 8));

        // --- Zadatak 5 ---
        System.out.println("─── 5: Function kompozicija ───");
        Function<String, String> slugify = napraviSlugifier();
        proveri("Z5", slugify.apply("  Hello World  "), "hello-world");

        // --- Zadatak 6 ---
        System.out.println("─── 6: flatMap unikatne reči ───");
        proveri("Z6", unikatneReci(List.of("Hello World", "world of FP", "hello fp")),
                List.of("fp", "hello", "of", "world"));

        // --- Zadatak 7 ---
        System.out.println("─── 7: groupingBy + counting ───");
        var r7 = brojPoPocetotnomSlovu(List.of("ana", "ada", "bob", "cara", "aca"));
        proveri("Z7 A", r7.get('A'), 3L);
        proveri("Z7 B", r7.get('B'), 1L);
        proveri("Z7 C", r7.get('C'), 1L);

        // --- Zadatak 8 ---
        System.out.println("─── 8: partitioningBy prosek ───");
        var r8 = prosekParnihNeparnih(List.of(1, 2, 3, 4, 5, 6));
        proveriDouble("Z8 parni",   r8.get(true),  4.0);
        proveriDouble("Z8 neparni", r8.get(false), 3.0);

        // --- Zadatak 9 ---
        System.out.println("─── 9: Fibonacci ───");
        proveri("Z9", fibonacci(7), List.of(0L, 1L, 1L, 2L, 3L, 5L, 8L));

        // --- Zadatak 10 ---
        System.out.println("─── 10: Numerisana lista ───");
        proveri("Z10", numerisanaLista(List.of("Ana", "Bojan", "Cara")),
                "1. Ana, 2. Bojan, 3. Cara");

        // --- Zadatak 11 ---
        System.out.println("─── 11: Currying ───");
        proveri("Z11a", linearnaFunkcija.apply(2).apply(3).apply(5), 13);
        var dupliranje = linearnaFunkcija.apply(2).apply(0);
        proveri("Z11b dupliranje", dupliranje.apply(7), 14);
        var inkrement = linearnaFunkcija.apply(1).apply(1);
        proveri("Z11c inkrement", inkrement.apply(9), 10);

        // --- Zadatak 12 ---
        System.out.println("─── 12: Method references ───");
        proveri("Z12", pipelineSaRefovima(List.of(" 3 ", "-1", " 7 ", "0", " 5")),
                List.of(3, 7, 5));

        // --- Zadatak 13 ---
        System.out.println("─── 13: Memoizacija ───");
        int[] pozivi = {0};
        Function<Integer, Integer> skupo = x -> { pozivi[0]++; return x * x; };
        Function<Integer, Integer> memo = memoize(skupo);
        memo.apply(5); memo.apply(5); memo.apply(3);
        proveri("Z13 pozivi", pozivi[0], 2);

        // --- Zadatak 14 ---
        System.out.println("─── 14: Reduce u objekat ───");
        Statistika st = statistikaReci(List.of("hello", "fp", "world", "java"));
        proveri("Z14 brojReci", st.brojReci(), 4);
        proveri("Z14 brojKar",  st.brojKaraktera(), 16);
        proveri("Z14 najduza",  st.najduza(), "hello");

        // --- Zadatak 15 ---
        System.out.println("─── 15: Lazy Supplier ───");
        int[] lazyPozivi = {0};
        Supplier<String> orig = () -> { lazyPozivi[0]++; return "rezultat"; };
        Supplier<String> l = lazy(orig);
        proveri("Z15a", l.get(), "rezultat");
        proveri("Z15b", l.get(), "rezultat");
        proveri("Z15c pozivi", lazyPozivi[0], 1);

        // --- Zadatak 16 ---
        System.out.println("─── 16: Refactor — najčešća reč ───");
        List<String> reci16 = List.of("Java", "fp", "java", "Stream", "java", "fp");
        proveri("Z16 imp", najcescaRecImperativ(reci16), "java");
        proveri("Z16 fp",  najcescaRecFP(reci16), "java");

        // --- Zadatak 17 ---
        System.out.println("─── 17: Refactor — invertovana mapa ───");
        Map<String, List<String>> hobiji = Map.of(
                "Ana",   List.of("tenis", "plivanje"),
                "Bojan", List.of("tenis", "fudbal"),
                "Cara",  List.of("plivanje", "fudbal", "tenis")
        );
        var imp17 = invertujMapuImperativ(hobiji);
        var fp17  = invertujMapuFP(hobiji);
        proveri("Z17", fp17, imp17);

        // --- Zadatak 18 ---
        System.out.println("─── 18: Refactor — balansi ───");
        List<Transakcija> transakcije = List.of(
                new Transakcija("Ana",   500, "UPLATA"),
                new Transakcija("Ana",   200, "ISPLATA"),
                new Transakcija("Bojan", 100, "UPLATA"),
                new Transakcija("Bojan", 150, "ISPLATA"),
                new Transakcija("Cara",  300, "UPLATA"),
                new Transakcija("Cara",   50, "ISPLATA")
        );
        var imp18 = balansiImperativ(transakcije);
        var fp18  = balansiFP(transakcije);
        proveri("Z18", fp18, imp18);

        // --- Zadatak 19 ---
        System.out.println("─── 19: Refactor — matrica flat ───");
        List<List<Integer>> matrica = List.of(
                List.of(1, 2, 3),
                List.of(4, 5),
                List.of(0, 6)
        );
        var imp19 = matricaFlatImperativ(matrica);
        var fp19  = matricaFlatFP(matrica);
        proveri("Z19", fp19, imp19);

        // --- Zadatak 20 ---
        System.out.println("─── 20: Refactor — histogram ───");
        String tekst = "Hello World! Hello FP!";
        var imp20 = histogramImperativ(tekst, 3);
        var fp20  = histogramFP(tekst, 3);
        proveri("Z20", fp20, imp20);
    }

    static <T> void proveri(String naziv, T dobijeno, T ocekivano) {
        boolean ok = Objects.equals(dobijeno, ocekivano);
        System.out.printf("  [%s] %s%n  Dobijeno:  %s%n  Očekivano: %s%n%n",
                ok ? "+" : "✗", naziv, dobijeno, ocekivano);
    }

    static void proveriDouble(String naziv, double dobijeno, double ocekivano) {
        boolean ok = Math.abs(dobijeno - ocekivano) < 0.001;
        System.out.printf("  [%s] %s%n  Dobijeno:  %s%n  Očekivano: %s%n%n",
                ok ? "+" : "✗", naziv, dobijeno, ocekivano);
    }
}
