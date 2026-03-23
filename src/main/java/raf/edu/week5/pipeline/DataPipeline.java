package raf.edu.week5.pipeline;

import java.util.*;
import java.util.stream.*;

import static java.util.stream.Collectors.*;

/**
 * Nedelja 5 — Data Processing Pipeline (ETL)
 *
 * Scenario: finansijske transakcije pristigle kao CSV stringovi.
 * Pipeline: parse → validate → enrich → aggregate → report
 *
 * Svaki korak je stream operacija. Ceo ETL tok je jedan pipeline.
 */
public class DataPipeline {

    // =========================================================================
    // Domenski model — immutable record-i
    // =========================================================================

    record Transakcija(
            String id,
            String kategorija,
            double iznos,
            String valuta,
            String opis
    ) {
        // Obogaćena transakcija — konvertovana u RSD
        Transakcija uRSD(Map<String, Double> kursevi) {
            double kurs = kursevi.getOrDefault(valuta, 1.0);
            return new Transakcija(id, kategorija, iznos * kurs, "RSD", opis);
        }
    }

    record Izvestaj(
            String kategorija,
            long brojTransakcija,
            double ukupanIznos,
            double prosecanIznos,
            double maxIznos
    ) {
        @Override
        public String toString() {
            return String.format("  %-15s | %3d transakcija | ukupno: %10.2f RSD | prosek: %8.2f | max: %8.2f",
                    kategorija, brojTransakcija, ukupanIznos, prosecanIznos, maxIznos);
        }
    }

    // =========================================================================
    // 1. EXTRACT — parsiranje sirovih CSV linija
    // =========================================================================

    /**
     * Parse jedne CSV linije. Vraća Optional.empty() za nevalidne linije.
     * Ovo je čista funkcija: String → Optional<Transakcija>
     */
    static Optional<Transakcija> parse(String csvLinija) {
        try {
            String[] delovi = csvLinija.split(",");
            if (delovi.length < 5) return Optional.empty();
            return Optional.of(new Transakcija(
                    delovi[0].trim(),
                    delovi[1].trim(),
                    Double.parseDouble(delovi[2].trim()),
                    delovi[3].trim(),
                    delovi[4].trim()
            ));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    // =========================================================================
    // 2. TRANSFORM — validacija i obogaćivanje
    // =========================================================================

    static boolean jeValidna(Transakcija t) {
        return t.iznos() > 0
                && !t.kategorija().isBlank()
                && !t.id().isBlank();
    }

    // =========================================================================
    // 3. LOAD — agregacija i generisanje izveštaja
    // =========================================================================

    static Izvestaj napraviIzvestaj(String kategorija, List<Transakcija> transakcije) {
        DoubleSummaryStatistics stats = transakcije.stream()
                .mapToDouble(Transakcija::iznos)
                .summaryStatistics();

        return new Izvestaj(
                kategorija,
                stats.getCount(),
                stats.getSum(),
                stats.getAverage(),
                stats.getMax()
        );
    }

    // =========================================================================
    // PIPELINE — sve zajedno
    // =========================================================================

    static List<Izvestaj> obradiTransakcije(List<String> csvLinije, Map<String, Double> kursevi) {
        return csvLinije.stream()
                // 1. EXTRACT: String → Optional<Transakcija> → Transakcija
                .map(DataPipeline::parse)
                .flatMap(Optional::stream)          // odbaci nevalidne (0 elemenata)

                // 2. TRANSFORM: validacija + konverzija valute
                .filter(DataPipeline::jeValidna)
                .map(t -> t.uRSD(kursevi))          // konvertuj u RSD

                // 3. LOAD: grupiši po kategoriji → napravi izveštaj za svaku
                .collect(groupingBy(Transakcija::kategorija))
                .entrySet().stream()
                .map(e -> napraviIzvestaj(e.getKey(), e.getValue()))

                // Sortiraj izveštaje po ukupnom iznosu (opadajuće)
                .sorted(Comparator.comparingDouble(Izvestaj::ukupanIznos).reversed())
                .toList();
    }

    // =========================================================================
    // Bonus: pipeline sa peek za logovanje (debugging u produkciji)
    // =========================================================================

    static List<Izvestaj> obradiSaLogom(List<String> csvLinije, Map<String, Double> kursevi) {
        long[] parsiranih = {0};
        long[] validnih = {0};

        List<Izvestaj> rezultat = csvLinije.stream()
                .map(DataPipeline::parse)
                .flatMap(Optional::stream)
                .peek(t -> parsiranih[0]++)                 // broji parsirane
                .filter(DataPipeline::jeValidna)
                .peek(t -> validnih[0]++)                   // broji validne
                .map(t -> t.uRSD(kursevi))
                .collect(groupingBy(Transakcija::kategorija))
                .entrySet().stream()
                .map(e -> napraviIzvestaj(e.getKey(), e.getValue()))
                .sorted(Comparator.comparingDouble(Izvestaj::ukupanIznos).reversed())
                .toList();

        System.out.printf("  [LOG] Ulaznih linija: %d | Parsiranih: %d | Validnih: %d | Kategorija: %d%n",
                csvLinije.size(), parsiranih[0], validnih[0], rezultat.size());

        return rezultat;
    }

    // =========================================================================
    // Bonus: Collectors.teeing — dva agregata u jednom prolazu
    // =========================================================================

    /**
     * teeing spaja dva kolektora i kombinuje rezultate.
     * Primer: istovremeno računamo ukupan iznos i broj transakcija.
     */
    static void teeingPrimer(List<String> csvLinije, Map<String, Double> kursevi) {
        record Sumarno(double ukupno, long broj) {}

        Sumarno sumarno = csvLinije.stream()
                .map(DataPipeline::parse)
                .flatMap(Optional::stream)
                .filter(DataPipeline::jeValidna)
                .map(t -> t.uRSD(kursevi))
                .collect(Collectors.teeing(
                        summingDouble(Transakcija::iznos),    // kolektor 1
                        counting(),                            // kolektor 2
                        Sumarno::new                           // kombinuj rezultate
                ));

        System.out.printf("  Teeing: ukupno %.2f RSD iz %d transakcija%n",
                sumarno.ukupno(), sumarno.broj());
    }

    // =========================================================================
    // main
    // =========================================================================

    public static void main(String[] args) {
        // Simulirani CSV podaci (u praksi: Files.lines(), API poziv, itd.)
        List<String> csv = List.of(
                "TXN001, Hrana,     1200,  RSD, Kupovina u Maxiju",
                "TXN002, Transport,   35,  EUR, Gorivo",
                "TXN003, Hrana,      800,  RSD, Pekara",
                "TXN004, Zabava,     20,   EUR, Bioskop",
                "TXN005, NEVALIDNA,  -50,  RSD, Negativan iznos",     // biće odbačena
                "ovo nije csv uopste",                                  // biće odbačena
                "TXN006, Transport, 150,   EUR, Avionska karta",
                "TXN007, Hrana,     2500,  RSD, Restoran",
                "TXN008, Računi,   5000,  RSD, Struja",
                "TXN009, Računi,    100,  EUR, Internet",
                "TXN010, Zabava,   1500,  RSD, Konzert",
                "TXN011, Hrana,      50,  EUR, Dostava",
                "TXN012, Transport, 300,  RSD, Taksi"
        );

        // Kursna lista
        Map<String, Double> kursevi = Map.of(
                "RSD", 1.0,
                "EUR", 117.5,
                "USD", 108.0
        );

        // --- Osnovni pipeline ---
        System.out.println("=== Osnovni ETL Pipeline ===\n");
        List<Izvestaj> izvestaji = obradiTransakcije(csv, kursevi);
        System.out.println("  Kategorija        | Br. trans.  | Ukupno              | Prosek          | Max");
        System.out.println("  " + "─".repeat(90));
        izvestaji.forEach(System.out::println);

        // --- Sa logom ---
        System.out.println("\n=== Pipeline sa logom (peek) ===\n");
        obradiSaLogom(csv, kursevi);

        // --- Teeing ---
        System.out.println("\n=== Collectors.teeing ===\n");
        teeingPrimer(csv, kursevi);

        // --- Demonstracija kompozibilnosti ---
        System.out.println("\n=== Kompozibilnost: top 3 kategorije po proseku ===\n");
        csvLinije_top3(csv, kursevi);
    }

    static void csvLinije_top3(List<String> csvLinije, Map<String, Double> kursevi) {
        // Isti pipeline, drugačiji završetak — to je moć kompozicije
        String top3 = csvLinije.stream()
                .map(DataPipeline::parse)
                .flatMap(Optional::stream)
                .filter(DataPipeline::jeValidna)
                .map(t -> t.uRSD(kursevi))
                .collect(groupingBy(Transakcija::kategorija, averagingDouble(Transakcija::iznos)))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(3)
                .map(e -> String.format("  %s: %.2f RSD prosek", e.getKey(), e.getValue()))
                .collect(joining("\n"));

        System.out.println(top3);
    }
}
