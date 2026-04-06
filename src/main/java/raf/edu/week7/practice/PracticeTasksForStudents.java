package raf.edu.week7.practice;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.*;
import java.util.stream.*;

/**
 * Paradigme Programiranja — Nedelja 7
 * Priprema za kolokvijum — 20 zadataka
 *
 * Zadaci 1-15:  klasicni FP zadaci (razni koncepti)
 * Zadaci 16-20: imperativni kod → prepišite u FP stil
 *
 * Napišite rešenje tamo gde piše // TODO
 * Ne menjajte potpise metoda.
 */
public class PracticeTasksForStudents {

    // =========================================================================
    // Zadatak 1 — filter + map + collect
    //
    // Data je lista stringova. Vratite listu koja sadrži SAMO stringove
    // duže od N karaktera, pretvorene u UPPERCASE.
    //
    // Primer: (["ana", "bo", "cara", "d", "eva"], 2) → ["ANA", "CARA", "EVA"]
    // =========================================================================

    static List<String> filterAndUppercase(List<String> reci, int minDuzina) {
        return Collections.emptyList();
    }

    // =========================================================================
    // Zadatak 2 — reduce: pronađi najduži string
    //
    // Koristeći SAMO reduce (ne max, ne sorted), pronađite najduži string.
    // Ako je lista prazna, vratite "".
    //
    // Primer: ["java", "fp", "stream", "go"] → "stream"
    // =========================================================================

    static String najduziString(List<String> reci) {
        return "";
    }

    // =========================================================================
    // Zadatak 3 — Optional lančanje
    //
    // Data je mapa korisnik→email i mapa email→rang.
    // Za dato korisničko ime:
    //   1. Nađi email (ne mora postojati)
    //   2. Nađi rang za taj email (ne mora postojati)
    //   3. Ako rang postoji i > 5, vrati "VIP: ime"
    //   4. Inače vrati "Regular: ime"
    //
    // Koristite Optional sa map/flatMap/filter — BEZ if/else.
    //
    // Primer: korisnici={"Ana"->"ana@x.com"}, rangovi={"ana@x.com"->8}
    //   nadjiStatus("Ana") → "VIP: Ana"
    //   nadjiStatus("Xyz") → "Regular: Xyz"
    // =========================================================================

    static String nadjiStatus(String ime,
                              Map<String, String> korisnici,
                              Map<String, Integer> rangovi) {
        return "";
    }

    // =========================================================================
    // Zadatak 4 — Predicate kompozicija
    //
    // Napišite metodu koja prima listu Predicate<Integer> i vraća
    // jedan Predicate koji je TRUE samo ako SVI prosleđeni predikati važe.
    //
    // Zatim koristite taj kompozitni predikat da filtrirate listu brojeva.
    //
    // Primer: predikati = [n -> n > 0, n -> n < 100, n -> n % 2 == 0]
    //   filtriranje([3, 50, -4, 72, 101, 8]) → [50, 72, 8]
    // =========================================================================

    static Predicate<Integer> spojiSve(List<Predicate<Integer>> predikati) {
        // TODO — koristite reduce za spajanje predikata sa .and()
        return n -> false;
    }

    static List<Integer> filtrirajSaSvim(List<Integer> brojevi,
                                         List<Predicate<Integer>> predikati) {
        // TODO — koristite spojiSve pa filtrirajte
        return List.of();
    }

    // =========================================================================
    // Zadatak 5 — Function kompozicija sa andThen
    //
    // Date su tri transformacije stringa:
    //   1. trim()
    //   2. toLowerCase()
    //   3. zameni razmake sa "-"
    //
    // Koristeći Function.andThen(), napravite JEDNU kompozitnu funkciju
    // koja primenjuje sve tri transformacije redom.
    //
    // Primer: "  Hello World  " → "hello-world"
    // =========================================================================

    static Function<String, String> napraviSlugifier() {
        // TODO — tri UnaryOperator<String> spojene sa andThen
        return s -> s;
    }

    // =========================================================================
    // Zadatak 6 — flatMap na streamu
    //
    // Data je lista rečenica. Vratite listu SVIH UNIKATNIH reči (lowercase),
    // sortirano abecedno. Reči su razdvojene razmakom.
    //
    // Primer: ["Hello World", "world of FP", "hello fp"]
    //   → ["fp", "hello", "of", "world"]
    // =========================================================================

    static List<String> unikatneReci(List<String> recenice) {
        // TODO
        return List.of();
    }

    // =========================================================================
    // Zadatak 7 — groupingBy + counting
    //
    // Data je lista stringova. Vratite mapu koja pokazuje koliko puta se
    // pojavljuje svako PRVO SLOVO (uppercase). Sortirajte po broju pojavljivanja
    // opadajuće, pa po slovu rastuće.
    //
    // Primer: ["ana", "ada", "bob", "cara", "aca"]
    //   → {A=3, B=1, C=1}   (A ima 3, B i C po 1)
    // =========================================================================

    static Map<Character, Long> brojPoPocetotnomSlovu(List<String> reci) {
        // TODO
        return Map.of();
    }

    // =========================================================================
    // Zadatak 8 — partitioningBy
    //
    // Data je lista celih brojeva. Particionirajte ih na parne i neparne,
    // i za svaku grupu izračunajte PROSEK (double).
    //
    // Vratite Map<Boolean, Double> gde true = prosek parnih, false = prosek neparnih.
    //
    // Primer: [1, 2, 3, 4, 5, 6] → {false=3.0, true=4.0}
    //   parni: 2,4,6 → prosek 4.0
    //   neparni: 1,3,5 → prosek 3.0
    // =========================================================================

    static Map<Boolean, Double> prosekParnihNeparnih(List<Integer> brojevi) {
        // TODO
        return Map.of();
    }

    // =========================================================================
    // Zadatak 9 — Stream.iterate: Fibonacci
    //
    // Generišite prvih N Fibonacci brojeva koristeći Stream.iterate
    // sa tri argumenta (seed, hasNext, next).
    //
    // Seed je par (a=0, b=1). Svaki korak generiše sledeći par.
    //
    // Primer: 7 → [0, 1, 1, 2, 3, 5, 8]
    // =========================================================================

    static List<Long> fibonacci(int n) {
        // TODO — koristite Stream.iterate sa int[] ili record kao stanje
        return List.of();
    }

    // =========================================================================
    // Zadatak 10 — Collectors.joining sa formatiranjem
    //
    // Data je lista imena. Formatirajte ih kao numerisanu listu:
    //   "1. Ana, 2. Bojan, 3. Cara"
    //
    // Koristite IntStream.range + Collectors.joining.
    //
    // Primer: ["Ana", "Bojan", "Cara"] → "1. Ana, 2. Bojan, 3. Cara"
    // =========================================================================

    static String numerisanaLista(List<String> imena) {
        // TODO
        return "";
    }

    // =========================================================================
    // Zadatak 11 — Currying
    //
    // Implementirajte curried funkciju za matematičku formulu:
    //   rezultat = (a * x + b)
    //
    //   1. Prvi argument: a (množilac)
    //   2. Drugi argument: b (sabira)
    //   3. Treći argument: x (vrednost)
    //   4. Rezultat: a * x + b
    //
    // Zatim koristite parcijalnu primenu da napravite:
    //   - dupliranje:  a=2, b=0  →  f(x) = 2x
    //   - inkrement:   a=1, b=1  →  f(x) = x + 1
    //
    // Primer: linearnaFunkcija.apply(2).apply(3).apply(5) = 2*5+3 = 13
    // =========================================================================

    static Function<Integer, Function<Integer, Function<Integer, Integer>>>
    linearnaFunkcija = a -> b -> x -> {
        // TODO
        return 0;
    };

    // =========================================================================
    // Zadatak 12 — Method reference: tri tipa
    //
    // Data je lista stringova. Primenite pipeline koji koristi
    // TRI tipa method reference:
    //   1. Instance method on type — String::trim
    //   2. Static method ref      — Integer::parseInt
    //   3. Constructor reference   — ArrayList::new (u collect)
    //
    // Pipeline: lista stringova (sa razmacima) → trim → parsiraj u int
    //   → filtriraj pozitivne → sakupi u novu ArrayList
    //
    // Primer: [" 3 ", "-1", " 7 ", "0", " 5"] → [3, 7, 5]
    // =========================================================================

    static List<Integer> pipelineSaRefovima(List<String> stringovi) {
        // TODO — koristite String::trim, Integer::parseInt, i collect
        return List.of();
    }

    // =========================================================================
    // Zadatak 13 — Memoizacija
    //
    // Napišite generičku metodu memoize koja prima Function<T,R>
    // i vraća novu Function<T,R> koja kešira rezultate.
    //
    // Koristite ConcurrentHashMap.
    //
    // Primer:
    //   Function<Integer, Integer> skupoRacunanje = ...
    //   Function<Integer, Integer> memo = memoize(skupoRacunanje);
    //   memo.apply(5) // računa
    //   memo.apply(5) // vraća iz keša
    // =========================================================================

    static <T, R> Function<T, R> memoize(Function<T, R> fn) {
        // TODO
        return fn;
    }

    // =========================================================================
    // Zadatak 14 — reduce sa 3 argumenta: akumulacija u objekat
    //
    // Data je lista stringova (reči). Koristeći reduce sa TRI argumenta
    // (identity, accumulator, combiner), izračunajte:
    //   - ukupan broj reči
    //   - ukupan broj karaktera (svih reči zajedno)
    //   - najdužu reč
    //
    // NE koristite count(), mapToInt().sum(), max() itd.
    //
    // Primer: ["hello", "fp", "world", "java"]
    //   → Statistika[brojReci=4, brojKaraktera=16, najduza="hello"]
    // =========================================================================

    record Statistika(int brojReci, int brojKaraktera, String najduza) {
        static final Statistika PRAZNA = new Statistika(0, 0, "");

        Statistika dodaj(String rec) {
            // TODO
            return PRAZNA;
        }

        Statistika spoji(Statistika other) {
            // TODO
            return PRAZNA;
        }
    }

    static Statistika statistikaReci(List<String> reci) {
        // TODO — koristite stream().reduce sa 3 argumenta
        return Statistika.PRAZNA;
    }

    // =========================================================================
    // Zadatak 15 — Supplier + lazy evaluacija
    //
    // Implementirajte metodu lazy() koja prima Supplier<T> i vraća
    // novi Supplier<T> koji:
    //   - prvi put pozove originalni supplier i zapamti rezultat
    //   - svaki sledeći put vrati zapamćeni rezultat BEZ ponovnog poziva
    //
    // Primer:
    //   Supplier<String> orig = () -> { System.out.println("Computing!"); return "X"; };
    //   Supplier<String> l = lazy(orig);
    //   l.get() // štampa "Computing!", vraća "X"
    //   l.get() // NE štampa ništa, vraća "X"
    // =========================================================================

    @SuppressWarnings("unchecked")
    static <T> Supplier<T> lazy(Supplier<T> supplier) {
        // TODO — keširajte rezultat nakon prvog poziva
        return supplier;
    }

    // =========================================================================
    // =========================================================================
    //   REFAKTORISANJE: Zadaci 16-20
    //   Imperativni kod → prepišite u FP stil
    // =========================================================================
    // =========================================================================

    // =========================================================================
    // Zadatak 16 — Refactoring: pronađi najčešću reč
    //
    // Imperativni kod ispod broji pojavljivanja svake reči u listi
    // i vraća onu sa najviše pojavljivanja. Ako ih ima više, vraća
    // prvu abecedno.
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

    /** FP stil — TODO */
    static String najcescaRecFP(List<String> reci) {
        // TODO
        return "";
    }

    // =========================================================================
    // Zadatak 17 — Refactoring: invertovana mapa
    //
    // Imperativni kod prima mapu osoba→lista hobija i pravi invertovanu
    // mapu hobi→lista osoba (sortirano abecedno).
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

    /** FP stil — TODO */
    static Map<String, List<String>> invertujMapuFP(Map<String, List<String>> original) {
        // TODO
        return Map.of();
    }

    // =========================================================================
    // Zadatak 18 — Refactoring: spajanje i sumarizacija
    //
    // Imperativni kod prima listu transakcija (osoba, iznos, tip)
    // i za svaku osobu računa BALANS (UPLATA dodaje, ISPLATA oduzima).
    // Vraća samo osobe sa pozitivnim balansom, sortirano po balansu opadajuće.
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

    /** FP stil — TODO */
    static Map<String, Double> balansiFP(List<Transakcija> transakcije) {
        // TODO
        return Map.of();
    }

    // =========================================================================
    // Zadatak 19 — Refactoring: matrica → flat lista sa transformacijom
    //
    // Imperativni kod prima matricu (lista listi) celih brojeva,
    // kvadrira svaki element, filtrira one > 10, i vraća sortiranu listu.
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

    /** FP stil — TODO */
    static List<Integer> matricaFlatFP(List<List<Integer>> matrica) {
        // TODO
        return List.of();
    }

    // =========================================================================
    // Zadatak 20 — Refactoring: histogram karaktera
    //
    // Imperativni kod prima string i pravi histogram (mapa karakter→broj),
    // ali samo za slova (ne razmake/interpunkciju), case-insensitive.
    // Vraća top N najčešćih karaktera sortirano po broju opadajuće.
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

    /** FP stil — TODO */
    static List<Map.Entry<Character, Long>> histogramFP(String tekst, int topN) {
        // TODO
        return List.of();
    }

    // =========================================================================
    // main — pokretanje testova
    // =========================================================================
    public static void main(String[] args) {
        System.out.println("Nedelja 7 — Priprema za kolokvijum — Testovi\n");

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
        proveri("Z11a", linearnaFunkcija.apply(2).apply(3).apply(5), 13); // 2*5+3
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
        proveri("Z13 pozivi", pozivi[0], 2); // 5 jednom, 3 jednom = 2 poziva

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

    // =========================================================================
    // Pomoćne metode — ne menjati
    // =========================================================================
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
