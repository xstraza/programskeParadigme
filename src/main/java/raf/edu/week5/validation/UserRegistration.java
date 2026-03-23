package raf.edu.week5.validation;

import java.util.*;
import java.util.function.*;
import java.util.regex.*;

/**
 * Nedelja 5 — Praktičan primer: validacija korisničke registracije
 *
 * Demonstrira:
 *   1. Pojedinačne validacije kao čiste funkcije
 *   2. Akumulacija svih grešaka (ne samo prve)
 *   3. Kombinovanje nezavisnih validacija sa combine
 *   4. Chaining zavisnih validacija sa flatMap
 *   5. Batch validacija liste korisnika
 */
public class UserRegistration {

    // =========================================================================
    // Domenski model
    // =========================================================================

    record RegistracijaZahtev(String ime, String email, String lozinka, int godine) {}

    record Korisnik(String ime, String email, int godine) {
        @Override
        public String toString() {
            return String.format("Korisnik[%s, %s, %d god.]", ime, email, godine);
        }
    }

    // =========================================================================
    // Pojedinačne validacije — svaka je čista funkcija
    // =========================================================================

    static Validation<String> validirajIme(String ime) {
        return Validation.validateAll(ime,
                i -> Validation.ensure(i, s -> s != null && !s.isBlank(),
                        "Ime je obavezno"),
                i -> Validation.ensure(i, s -> s != null && s.length() >= 2,
                        "Ime mora imati bar 2 karaktera"),
                i -> Validation.ensure(i, s -> s != null && s.length() <= 50,
                        "Ime ne sme biti duže od 50 karaktera")
        );
    }

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    static Validation<String> validirajEmail(String email) {
        return Validation.validateAll(email,
                e -> Validation.ensure(e, s -> s != null && !s.isBlank(),
                        "Email je obavezan"),
                e -> Validation.ensure(e, s -> s != null && EMAIL_PATTERN.matcher(s).matches(),
                        "Email format nije ispravan (primer: korisnik@domen.rs)")
        );
    }

    static Validation<String> validirajLozinku(String lozinka) {
        return Validation.validateAll(lozinka,
                l -> Validation.ensure(l, s -> s != null && s.length() >= 8,
                        "Lozinka mora imati bar 8 karaktera"),
                l -> Validation.ensure(l, s -> s != null && s.chars().anyMatch(Character::isUpperCase),
                        "Lozinka mora sadržati bar jedno veliko slovo"),
                l -> Validation.ensure(l, s -> s != null && s.chars().anyMatch(Character::isDigit),
                        "Lozinka mora sadržati bar jednu cifru")
        );
    }

    static Validation<Integer> validirajGodine(int godine) {
        return Validation.validateAll(godine,
                g -> Validation.ensure(g, x -> x >= 18,
                        "Morate imati bar 18 godina"),
                g -> Validation.ensure(g, x -> x <= 150,
                        "Neispravne godine (max 150)")
        );
    }

    // =========================================================================
    // Kombinovana validacija — akumulira SVE greške
    // =========================================================================

    static Validation<Korisnik> validirajRegistraciju(RegistracijaZahtev zahtev) {
        // Svaka validacija se izvršava NEZAVISNO — sve greške se sakupljaju
        Validation<String>  ime     = validirajIme(zahtev.ime());
        Validation<String>  email   = validirajEmail(zahtev.email());
        Validation<String>  lozinka = validirajLozinku(zahtev.lozinka());
        Validation<Integer> godine  = validirajGodine(zahtev.godine());

        // Combine: spoji ime + email, pa rezultat toga + godine
        // Lozinka se validira ali ne ulazi u Korisnik objekat (samo proveravamo)
        return ime
                .combine(email, (i, e) -> new String[]{i, e})
                .combine(godine, (ie, g) -> new Korisnik(ie[0], ie[1], g))
                .flatMap(korisnik -> lozinka.map(l -> korisnik));
        // ^ flatMap za lozinku: ako lozinka nije validna, ceo rezultat je Invalid
    }

    // =========================================================================
    // Batch validacija — stream svih zahteva
    // =========================================================================

    static void obradiRegistracije(List<RegistracijaZahtev> zahtevi) {
        System.out.println("Obrada " + zahtevi.size() + " registracija:\n");

        zahtevi.forEach(zahtev -> {
            Validation<Korisnik> rezultat = validirajRegistraciju(zahtev);

            // fold: obradimo oba ishoda
            String poruka = rezultat.fold(
                    greske -> String.format("  ODBIJENO [%s]:\n%s",
                            zahtev.ime() == null ? "(null)" : zahtev.ime(),
                            greske.stream()
                                    .map(g -> "    - " + g)
                                    .collect(java.util.stream.Collectors.joining("\n"))),
                    korisnik -> "  REGISTROVAN: " + korisnik
            );
            System.out.println(poruka);
            System.out.println();
        });

        // Statistika
        long uspesnih = zahtevi.stream()
                .map(UserRegistration::validirajRegistraciju)
                .filter(Validation::isValid)
                .count();

        System.out.printf("Rezultat: %d/%d uspešnih registracija%n", uspesnih, zahtevi.size());
    }

    // =========================================================================
    // main
    // =========================================================================

    public static void main(String[] args) {
        System.out.println("=== 1. Pojedinačne validacije ===\n");

        System.out.println("Ime 'Ana':    " + validirajIme("Ana"));
        System.out.println("Ime '':       " + validirajIme(""));
        System.out.println("Email ok:     " + validirajEmail("ana@uni.rs"));
        System.out.println("Email loš:    " + validirajEmail("not-an-email"));
        System.out.println("Lozinka ok:   " + validirajLozinku("Sigurna123"));
        System.out.println("Lozinka loša: " + validirajLozinku("abc"));

        System.out.println("\n=== 2. Kompletna registracija — akumulacija grešaka ===\n");

        // Sve OK
        var ok = validirajRegistraciju(
                new RegistracijaZahtev("Ana Petrović", "ana@uni.rs", "Sigurna123", 22));
        System.out.println("Validan zahtev: " + ok);

        // Više grešaka odjednom — korisnik vidi SVE
        var lose = validirajRegistraciju(
                new RegistracijaZahtev("", "loš-email", "abc", 15));
        System.out.println("Nevalidan zahtev: " + lose);
        System.out.println("Greške: " + lose.getErrors());

        System.out.println("\n=== 3. Batch obrada ===\n");

        obradiRegistracije(List.of(
                new RegistracijaZahtev("Ana Petrović",  "ana@uni.rs",   "Sigurna123", 22),
                new RegistracijaZahtev("",              "loš-email",    "abc",         15),
                new RegistracijaZahtev("Bojan Ilić",    "bojan@uni.rs", "pass",        25),
                new RegistracijaZahtev("Cara Marković", "cara@uni.rs",  "MojaSifra1",  19),
                new RegistracijaZahtev("D",             "d@d.rs",       "Kratka1",     200)
        ));
    }
}
