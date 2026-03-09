package raf.edu.week3;

import java.util.Comparator;
import java.util.List;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * Nedelja 3 — Dizajniranje sa lambda izrazima (poglavlje 5)
 *
 * Lambda izrazi menjaju način na koji dizajniramo kod:
 *   - Manje klasa i interfejsa
 *   - Ponašanje kao vrednost — prosleđuje se, čuva, komponuje
 *   - Klasični OOP paterni postaju trivijalni
 *
 * Teme:
 *   1. Razdvajanje brige (Separation of Concerns)
 *   2. Strategy Pattern sa lambdama
 *   3. Decorator Pattern / Kompozicija funkcija
 *   4. Execute Around Method Pattern
 *   5. Lambda kao fabrika (Factory Method Pattern)
 */
public class DizajnSaLambdama {

    // -----------------------------------------------------------------------
    // Model podataka za primere
    // -----------------------------------------------------------------------
    record Student(String ime, int poeni, String smer) {}

    // -----------------------------------------------------------------------
    // 1. Razdvajanje brige — "šta" od "kako parametrizujemo"
    //
    //    Uobičajen problem: imamo istu strukturu koda, ali se menja
    //    samo jedan mali deo logike (uslov, transformacija, akcija).
    //    Sa lambdama, taj deo postaje parametar.
    // -----------------------------------------------------------------------
    static void separationOfConcerns() {
        List<Student> studenti = List.of(
                new Student("Ana",     85, "Informatika"),
                new Student("Bojan",   42, "Matematika"),
                new Student("Čedomir", 91, "Informatika"),
                new Student("Dragana", 55, "Matematika"),
                new Student("Eva",     30, "Informatika")
        );

        // PROBLEM: hoćemo da filterujemo studente po različitim kriterijumima
        // BEZ lambda — morali bismo pisati više metoda ili interfejs + klase:
        //   filtrirajPoSmeru(studenti, "Informatika") { for ... if (s.smer().equals(...)) }
        //   filtrirajPoPoduslovu(studenti, 50)        { for ... if (s.poeni() > ...) }

        // SA lambda — jedan opšti metod, kriterijum je parametar
        System.out.println("Informatika studenti:");
        filtriraj(studenti, s -> s.smer().equals("Informatika"))
                .forEach(s -> System.out.println("  " + s.ime() + " (" + s.poeni() + ")"));

        System.out.println("Studenti sa > 50 poena:");
        filtriraj(studenti, s -> s.poeni() > 50)
                .forEach(s -> System.out.println("  " + s.ime() + " (" + s.poeni() + ")"));

        System.out.println("Informatika sa > 50 poena:");
        filtriraj(studenti, s -> s.smer().equals("Informatika") && s.poeni() > 50)
                .forEach(s -> System.out.println("  " + s.ime()));
    }

    // Opšta metoda — "briga" za filtriranje, kriterijum je parametar
    static List<Student> filtriraj(List<Student> studenti, Predicate<Student> kriterijum) {
        return studenti.stream().filter(kriterijum).collect(Collectors.toList());
    }

    // -----------------------------------------------------------------------
    // 2. Strategy Pattern sa lambdama
    //
    //    Klasično: interfejs + klase za svaku strategiju
    //    Sa lambda: strategija je samo vrednost (lambda ili method reference)
    // -----------------------------------------------------------------------
    static void strategyPattern() {
        List<Student> studenti = List.of(
                new Student("Ana",     85, "Informatika"),
                new Student("Bojan",   42, "Matematika"),
                new Student("Čedomir", 91, "Informatika"),
                new Student("Dragana", 55, "Matematika")
        );

        // Strategije sortiranja — bez ijedne nove klase!
        Comparator<Student> poImenu      = Comparator.comparing(Student::ime);
        Comparator<Student> poPoenima    = Comparator.comparingInt(Student::poeni);
        Comparator<Student> poPoenimaDes = Comparator.comparingInt(Student::poeni).reversed();
        Comparator<Student> poSmeru      = Comparator.comparing(Student::smer);

        System.out.println("Po imenu:");
        studenti.stream().sorted(poImenu)
                .forEach(s -> System.out.println("  " + s.ime()));

        System.out.println("Po poenima (opadajuće):");
        studenti.stream().sorted(poPoenimaDes)
                .forEach(s -> System.out.println("  " + s.ime() + ": " + s.poeni()));

        // Kompozicija strategija — po smeru, pa po poenima opadajuće
        Comparator<Student> kombinovano = poSmeru.thenComparing(poPoenimaDes);
        System.out.println("Po smeru, pa poenima:");
        studenti.stream().sorted(kombinovano)
                .forEach(s -> System.out.printf("  %-10s %-12s %d%n",
                        s.ime(), s.smer(), s.poeni()));
    }

    // -----------------------------------------------------------------------
    // 3. Decorator Pattern / Kompozicija funkcija
    //
    //    Dodajemo "slojeve" transformacije bez novih klasa.
    //    Function.andThen() i Function.compose() su naše oruđe.
    // -----------------------------------------------------------------------
    static void decoratorPattern() {
        // Bazne transformacije — gradivni blokovi
        Function<String, String> trim      = String::trim;
        Function<String, String> malaSlova = String::toLowerCase;
        Function<String, String> ukloniSpecijalne = s -> s.replaceAll("[^a-zA-Z0-9 ]", "");
        Function<String, String> dodajPrefix = s -> ">>>" + s;

        // Kompozicija sa andThen — f.andThen(g) znači: prvo f, pa g
        Function<String, String> normalizuj = trim.andThen(malaSlova);
        System.out.println(normalizuj.apply("  Zdravo, Svete!  "));

        // Dodajemo slojeve — nema novih klasa
        Function<String, String> ocisti = trim.andThen(ukloniSpecijalne).andThen(malaSlova);
        System.out.println(ocisti.apply("  Zdravo, Svete!! (2024)  "));

        // Sa compose — suprotan redosled: f.compose(g) = g pa f
        Function<String, String> prefixOcisceno = dodajPrefix.compose(normalizuj);
        System.out.println(prefixOcisceno.apply("  JAVA  "));

        // Praktičan primer: lanac obrade teksta za pretragu
        Function<String, String> pripremizaPretragu =
                trim.andThen(malaSlova).andThen(ukloniSpecijalne);

        List<String> ulaz = List.of("  Java STREAM  ", "  Lambda!  ", "  FUNKCIONALNO  ");
        List<String> pripremljeno = ulaz.stream()
                .map(pripremizaPretragu)
                .collect(Collectors.toList());
        System.out.println("Pripremljeno za pretragu: " + pripremljeno);
    }

    // -----------------------------------------------------------------------
    // 4. Execute Around Method Pattern
    //
    //    Infrastruktura (setup, teardown, logging...) je u metodi domaćinu.
    //    Korisna logika se prosleđuje kao lambda — "izvršava se oko" nje.
    //    Garantuje se da se infrastruktura uvek izvršava.
    // -----------------------------------------------------------------------

    // Simulacija skupog resursa (npr. DB konekcija, fajl...)
    static class Resurs {
        private final String naziv;
        private boolean otvoren = false;

        Resurs(String naziv) { this.naziv = naziv; }

        void otvori() {
            otvoren = true;
            System.out.println("  [Resurs] Otvoren: " + naziv);
        }

        void zatvori() {
            otvoren = false;
            System.out.println("  [Resurs] Zatvoren: " + naziv);
        }

        String procitaj() {
            if (!otvoren) throw new IllegalStateException("Resurs nije otvoren!");
            return "Podaci iz: " + naziv;
        }
    }

    // Execute-Around: infrastruktura (otvori/zatvori) je ovde zarobljena
    // Korisna logika dolazi spolja kao Function
    static <T> T saResursom(String naziv, Function<Resurs, T> akcija) {
        Resurs r = new Resurs(naziv);
        try {
            r.otvori();
            return akcija.apply(r);  // izvršava prosleđenu lambdu
        } finally {
            r.zatvori();  // uvek se izvršava, čak i ako akcija baci exception
        }
    }

    static void executeAroundPattern() {
        // Korisnik ne mora da zna ništa o otvaranju/zatvaranju
        String rezultat = saResursom("baza.db", resurs -> resurs.procitaj());
        System.out.println("Rezultat: " + rezultat);

        // Druga akcija, ista infrastruktura
        int duzina = saResursom("podaci.txt", resurs -> resurs.procitaj().length());
        System.out.println("Dužina podataka: " + duzina);

        // Sa Consumer — kada nema povratne vrednosti
        // (analogno: saResursom koji prima Consumer<Resurs>)
    }

    // -----------------------------------------------------------------------
    // 5. Lambda kao fabrika (Factory Method Pattern)
    //
    //    Supplier<T> je fabrika bez argumenata.
    //    Function<String, T> je fabrika sa konfiguracijom.
    // -----------------------------------------------------------------------

    interface Poruka {
        String tekst();
    }

    record EmailPoruka(String sadrzaj) implements Poruka {
        public String tekst() { return "[EMAIL] " + sadrzaj; }
    }

    record SMSPoruka(String sadrzaj) implements Poruka {
        public String tekst() { return "[SMS] " + sadrzaj; }
    }

    record PushPoruka(String sadrzaj) implements Poruka {
        public String tekst() { return "[PUSH] " + sadrzaj; }
    }

    static void factoryPattern() {
        // Fabrika kao mapa: String → Supplier<Poruka> (ili Function<String, Poruka>)
        java.util.Map<String, Function<String, Poruka>> fabrika = java.util.Map.of(
                "email", EmailPoruka::new,
                "sms",   SMSPoruka::new,
                "push",  PushPoruka::new
        );

        // Kreiranje poruka bez switch/if
        List<String> kanali = List.of("email", "sms", "push", "email");
        String sadrzaj = "Vaš ispit je zakazan za ponedeljak.";

        kanali.stream()
                .map(k -> fabrika.getOrDefault(k, EmailPoruka::new).apply(sadrzaj))
                .forEach(p -> System.out.println(p.tekst()));
    }

    // -----------------------------------------------------------------------
    // main
    // -----------------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("=== 1. Razdvajanje brige (Separation of Concerns) ===\n");
        separationOfConcerns();

        System.out.println("\n=== 2. Strategy Pattern ===\n");
        strategyPattern();

        System.out.println("\n=== 3. Decorator Pattern / Kompozicija funkcija ===\n");
        decoratorPattern();

        System.out.println("\n=== 4. Execute Around Method Pattern ===\n");
        executeAroundPattern();

        System.out.println("\n=== 5. Factory Method Pattern ===\n");
        factoryPattern();
    }
}
