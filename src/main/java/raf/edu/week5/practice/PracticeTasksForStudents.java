package raf.edu.week5.practice;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * Paradigme Programiranja — Nedelja 5
 * Zadaci za vežbe
 *
 * Zadaci 1-3: klasični zadaci (stream pipeline, reduce, flatMap + compose)
 * Zadaci 4-5: dat je imperativni kod — prepišite ga u FP stil
 *
 * Napišite rešenje tamo gde piše // TODO
 * Ne menjajte potpise metoda.
 */
public class PracticeTasksForStudents {

    // =========================================================================
    // Zadatak 1 — ETL pipeline: obrada log zapisa
    //
    // Data je lista log zapisa u formatu "LEVEL|timestamp|poruka".
    // Neki zapisi su neispravni (nemaju 3 dela).
    //
    // Napravite metodu koja:
    //   1. Parsira svaki zapis u LogEntry (koristite parseLog pomoćnu metodu)
    //   2. Odbaci neispravne (flatMap + Optional::stream)
    //   3. Zadrži samo zapise sa nivoom ERROR ili WARN
    //   4. Grupiše po nivou
    //   5. Za svaki nivo napravi sumarni string koristeći Collectors.joining:
    //      "ERROR (2): Disk full; Out of memory"
    //      "WARN (1): High CPU usage"
    //   6. Sortira po nivou abecedno i vrati kao listu stringova
    //
    // Primer:
    //   ulaz = ["ERROR|10:00|Disk full", "INFO|10:01|Started", "WARN|10:02|High CPU",
    //           "nevalidan zapis", "ERROR|10:03|Out of memory"]
    //   → ["ERROR (2): Disk full; Out of memory", "WARN (1): High CPU"]
    // =========================================================================

    record LogEntry(String level, String timestamp, String message) {}

    static Optional<LogEntry> parseLog(String line) {
        String[] parts = line.split("\\|");
        if (parts.length != 3) return Optional.empty();
        return Optional.of(new LogEntry(parts[0].trim(), parts[1].trim(), parts[2].trim()));
    }

    static List<String> sumarniLogIzvestaj(List<String> logZapisi) {
        // TODO
        return List.of();
    }

    // =========================================================================
    // Zadatak 2 — Reduce: kumulativni bilans
    //
    // Data je lista transakcija (opis, iznos). Pozitivan iznos = prihod,
    // negativan = rashod.
    //
    // Koristeći SAMO reduce (ne collect, ne forEach), izračunajte:
    //   - ukupan prihod (suma pozitivnih)
    //   - ukupan rashod (suma negativnih, kao pozitivan broj)
    //   - bilans (prihod - rashod)
    //   - broj transakcija
    //
    // Vratite rezultat kao Bilans record.
    //
    // Pomoć: definišite Bilans.PRAZAN i metodu Bilans.dodaj(Transakcija).
    //
    // Primer:
    //   transakcije = [("Plata", 5000), ("Kirija", -2000), ("Freelance", 1500), ("Računi", -800)]
    //   → Bilans[prihod=6500.0, rashod=2800.0, bilans=3700.0, brojTransakcija=4]
    // =========================================================================

    record Transakcija(String opis, double iznos) {}

    record Bilans(double prihod, double rashod, double bilans, int brojTransakcija) {
        static final Bilans PRAZAN = new Bilans(0, 0, 0, 0);

        // TODO: implementirajte metodu dodaj
        Bilans dodaj(Transakcija t) {
            return PRAZAN; // TODO
        }

        // TODO: implementirajte metodu spoji (za combine u reduce)
        Bilans spoji(Bilans other) {
            return PRAZAN; // TODO
        }
    }

    static Bilans izracunajBilans(List<Transakcija> transakcije) {
        // TODO: koristite reduce sa Bilans.PRAZAN, dodaj, spoji
        return Bilans.PRAZAN;
    }

    // =========================================================================
    // Zadatak 3 — Function composition: pipeline transformacija
    //
    // Implementirajte metodu koja prima listu transformacija (UnaryOperator<String>)
    // i vraća JEDNU funkciju koja ih sve primenjuje redom.
    //
    // Zatim, koristeći tu metodu, napravite "sanitizer" za korisničke unose:
    //   1. trim
    //   2. lowercase
    //   3. zameni višestruke razmake jednim
    //   4. ukloni sve osim slova, cifara i razmaka
    //   5. ograniči na maxLen karaktera
    //
    // Primer:
    //   compose([trim, lower]) primenjena na "  HELLO  " → "hello"
    //
    //   sanitize("  Hello   WORLD @#$ 123  ", 20) → "hello world  123"
    // =========================================================================

    static UnaryOperator<String> compose(List<UnaryOperator<String>> transformacije) {
        // TODO: redukcija liste transformacija u jednu
        return UnaryOperator.identity();
    }

    static String sanitize(String input, int maxLen) {
        // TODO: napravite listu transformacija i pozovite compose
        return input;
    }

    // =========================================================================
    // Zadatak 4 — Refactoring: inventar prodavnice
    //
    // Ispod je dat imperativni kod koji:
    //   - filtrira proizvode koji su na stanju (količina > 0)
    //   - grupiše ih po kategoriji
    //   - za svaku kategoriju računa ukupnu vrednost (cena * količina)
    //   - sortira kategorije po ukupnoj vrednosti opadajuće
    //   - vraća top N kategorija kao listu stringova
    //     u formatu "Kategorija: XXXXX.XX din"
    //
    // Prepišite metodu inventarIzvestajFP tako da radi ISTO
    // ali koristeći streams, bez ijedne petlje ili if-a.
    // =========================================================================

    record Proizvod(String naziv, String kategorija, double cena, int kolicina) {}

    /** Imperativni stil — NE MENJATI, ovo je referenca. */
    static List<String> inventarIzvestajImperativ(List<Proizvod> proizvodi, int topN) {
        // Korak 1: filtriraj i grupiši
        Map<String, Double> vrednostPoKategoriji = new HashMap<>();
        for (Proizvod p : proizvodi) {
            if (p.kolicina() > 0) {
                double vrednost = p.cena() * p.kolicina();
                vrednostPoKategoriji.merge(p.kategorija(), vrednost, Double::sum);
            }
        }

        // Korak 2: sortiraj po vrednosti opadajuće
        List<Map.Entry<String, Double>> sortiran = new ArrayList<>(vrednostPoKategoriji.entrySet());
        sortiran.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        // Korak 3: uzmi top N i formatiraj
        List<String> rezultat = new ArrayList<>();
        for (int i = 0; i < Math.min(topN, sortiran.size()); i++) {
            Map.Entry<String, Double> entry = sortiran.get(i);
            rezultat.add(String.format("%s: %.2f din", entry.getKey(), entry.getValue()));
        }
        return rezultat;
    }

    /** FP stil — TODO: prepišite gornju metodu koristeći streams. */
    static List<String> inventarIzvestajFP(List<Proizvod> proizvodi, int topN) {
        // TODO
        return List.of();
    }

    // =========================================================================
    // Zadatak 5 — Refactoring: analiza teksta
    //
    // Ispod je dat imperativni kod koji:
    //   - deli tekst na reči (po razmaku)
    //   - broji pojavljivanja svake reči (case-insensitive)
    //   - ignoriše "stop reči" (predlozi, veznici itd.)
    //   - sortira po broju pojavljivanja opadajuće
    //   - za reči sa istim brojem — abecedno
    //   - vraća top N reči sa brojem pojavljivanja
    //     u formatu "reč: X"
    //
    // Prepišite metodu analizaTekstaFP tako da radi ISTO
    // ali koristeći streams, bez ijedne petlje ili if-a.
    // =========================================================================

    static final Set<String> STOP_RECI = Set.of(
            "i", "u", "na", "je", "za", "sa", "se", "da", "ne", "od", "do", "a", "ali", "ili"
    );

    /** Imperativni stil — NE MENJATI, ovo je referenca. */
    static List<String> analizaTekstaImperativ(String tekst, int topN) {
        // Korak 1: podeli na reči, prebroji (case-insensitive, bez stop reči)
        String[] reci = tekst.toLowerCase().split("\\s+");
        Map<String, Integer> brojac = new HashMap<>();
        for (String rec : reci) {
            String cista = rec.replaceAll("[^a-zčćšđž]", "");
            if (!cista.isEmpty() && !STOP_RECI.contains(cista)) {
                brojac.merge(cista, 1, Integer::sum);
            }
        }

        // Korak 2: sortiraj po broju opadajuće, pa abecedno
        List<Map.Entry<String, Integer>> sortiran = new ArrayList<>(brojac.entrySet());
        sortiran.sort((a, b) -> {
            int cmp = Integer.compare(b.getValue(), a.getValue());
            if (cmp != 0) return cmp;
            return a.getKey().compareTo(b.getKey());
        });

        // Korak 3: top N
        List<String> rezultat = new ArrayList<>();
        for (int i = 0; i < Math.min(topN, sortiran.size()); i++) {
            Map.Entry<String, Integer> e = sortiran.get(i);
            rezultat.add(e.getKey() + ": " + e.getValue());
        }
        return rezultat;
    }

    /** FP stil — TODO: prepišite gornju metodu koristeći streams. */
    static List<String> analizaTekstaFP(String tekst, int topN) {
        // TODO
        return List.of();
    }

    // =========================================================================
    // main — pokretanje testova
    // =========================================================================
    public static void main(String[] args) {
        System.out.println("Nedelja 5 — Testovi zadataka\n");

        // --- Zadatak 1 ---
        System.out.println("─── 1: Sumarni log izveštaj ───");
        List<String> logovi = List.of(
                "ERROR|10:00|Disk full",
                "INFO|10:01|Started",
                "WARN|10:02|High CPU",
                "nevalidan zapis",
                "ERROR|10:03|Out of memory",
                "INFO|10:04|Request OK",
                "WARN|10:05|Low disk space",
                "DEBUG|10:06|Cache hit"
        );
        proveri("Z1", sumarniLogIzvestaj(logovi),
                List.of("ERROR (2): Disk full; Out of memory",
                        "WARN (2): High CPU; Low disk space"));

        // --- Zadatak 2 ---
        System.out.println("─── 2: Bilans ───");
        List<Transakcija> transakcije = List.of(
                new Transakcija("Plata", 5000),
                new Transakcija("Kirija", -2000),
                new Transakcija("Freelance", 1500),
                new Transakcija("Računi", -800)
        );
        Bilans b = izracunajBilans(transakcije);
        proveriDouble("Z2 prihod",  b.prihod(),  6500.0);
        proveriDouble("Z2 rashod",  b.rashod(),  2800.0);
        proveriDouble("Z2 bilans",  b.bilans(),  3700.0);
        proveri("Z2 broj",          b.brojTransakcija(), 4);

        Bilans prazan = izracunajBilans(List.of());
        proveri("Z2 prazan", prazan, Bilans.PRAZAN);

        // --- Zadatak 3 ---
        System.out.println("─── 3: Function composition ───");
        UnaryOperator<String> pipeline = compose(List.of(
                String::trim,
                String::toLowerCase
        ));
        proveri("Z3 compose", pipeline.apply("  HELLO  "), "hello");
        proveri("Z3 sanitize", sanitize("  Hello   WORLD @#$ 123  ", 20), "hello world  123");
        proveri("Z3 sanitize short", sanitize("abcdef", 3), "abc");

        // --- Zadatak 4 ---
        System.out.println("─── 4: Inventar (imperativ vs FP) ───");
        List<Proizvod> proizvodi = List.of(
                new Proizvod("Laptop",     "Elektronika", 80000, 5),
                new Proizvod("Miš",        "Elektronika", 2000,  20),
                new Proizvod("Stolica",    "Nameštaj",    15000, 8),
                new Proizvod("Sto",        "Nameštaj",    25000, 3),
                new Proizvod("Java knjiga","Knjige",       3000,  0),  // nema na stanju
                new Proizvod("FP knjiga",  "Knjige",       2500, 12),
                new Proizvod("Monitor",    "Elektronika", 35000, 3)
        );
        List<String> imperative4 = inventarIzvestajImperativ(proizvodi, 3);
        List<String> fp4 = inventarIzvestajFP(proizvodi, 3);
        System.out.println("  Imperativ: " + imperative4);
        System.out.println("  FP:        " + fp4);
        proveri("Z4", fp4, imperative4);

        // --- Zadatak 5 ---
        System.out.println("─── 5: Analiza teksta (imperativ vs FP) ───");
        String tekst = "Java je moćan jezik. Java se koristi za web aplikacije i za mobilne aplikacije. "
                + "Funkcionalno programiranje u Java jeziku je sve popularnije. "
                + "Stream API je deo Java platforme.";
        List<String> imperative5 = analizaTekstaImperativ(tekst, 5);
        List<String> fp5 = analizaTekstaFP(tekst, 5);
        System.out.println("  Imperativ: " + imperative5);
        System.out.println("  FP:        " + fp5);
        proveri("Z5", fp5, imperative5);
    }

    // =========================================================================
    // Pomoćne metode — ne menjati
    // =========================================================================
    static <T> void proveri(String naziv, T dobijeno, T ocekivano) {
        boolean ok = Objects.equals(dobijeno, ocekivano);
        System.out.printf("  [%s] %s%n  Dobijeno:  %s%n  Očekivano: %s%n%n",
                ok ? "✓" : "✗", naziv, dobijeno, ocekivano);
    }

    static void proveriDouble(String naziv, double dobijeno, double ocekivano) {
        boolean ok = Math.abs(dobijeno - ocekivano) < 0.01;
        System.out.printf("  [%s] %s%n  Dobijeno:  %.2f%n  Očekivano: %.2f%n%n",
                ok ? "✓" : "✗", naziv, dobijeno, ocekivano);
    }
}
