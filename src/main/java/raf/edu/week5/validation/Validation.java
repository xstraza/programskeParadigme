package raf.edu.week5.validation;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * Nedelja 5 — Generički Validation tip
 *
 * Inspirisan Vavr-ovim Validation i Haskell-ovim Either.
 * Dve varijante:
 *   - Valid(value)   — uspešna validacija
 *   - Invalid(errors) — neuspešna, sa listom grešaka
 *
 * Ključna razlika od Optional:
 *   - Optional gubi informaciju O GREŠCI (samo "nema vrednosti")
 *   - Validation AKUMULIRA sve greške — korisnik vidi SVE probleme odjednom
 *
 * Ključna razlika od Exception:
 *   - Exception je fail-fast: prva greška prekida sve
 *   - Validation nastavlja i sakuplja sve greške
 */
public sealed interface Validation<T> {

    // Dve varijante
    record Valid<T>(T value) implements Validation<T> {}
    record Invalid<T>(List<String> errors) implements Validation<T> {}

    // =========================================================================
    // Fabričke metode
    // =========================================================================

    static <T> Validation<T> valid(T value) {
        return new Valid<>(value);
    }

    static <T> Validation<T> invalid(String error) {
        return new Invalid<>(List.of(error));
    }

    static <T> Validation<T> invalid(List<String> errors) {
        return new Invalid<>(List.copyOf(errors));
    }

    // =========================================================================
    // Provera stanja
    // =========================================================================

    default boolean isValid() {
        return this instanceof Valid;
    }

    default T getValue() {
        if (this instanceof Valid<T> v) return v.value();
        throw new NoSuchElementException("Validation is invalid: " + ((Invalid<T>) this).errors());
    }

    default List<String> getErrors() {
        if (this instanceof Invalid<T> inv) return inv.errors();
        return List.of();
    }

    // =========================================================================
    // map — transformiši vrednost ako je Valid
    // =========================================================================

    default <R> Validation<R> map(Function<T, R> fn) {
        return switch (this) {
            case Valid<T> v -> valid(fn.apply(v.value()));
            case Invalid<T> inv -> invalid(inv.errors());
        };
    }

    // =========================================================================
    // flatMap — chain-uj validacije (fail-fast: staje na prvoj grešci)
    // =========================================================================

    default <R> Validation<R> flatMap(Function<T, Validation<R>> fn) {
        return switch (this) {
            case Valid<T> v -> fn.apply(v.value());
            case Invalid<T> inv -> invalid(inv.errors());
        };
    }

    // =========================================================================
    // combine — spoji dve validacije, AKUMULIRAJUĆI greške
    //
    // Ovo je ključna operacija: obe validacije se izvršavaju nezavisno
    // i ako obe imaju greške, sve greške se sakupljaju.
    // =========================================================================

    default <U, R> Validation<R> combine(Validation<U> other, BiFunction<T, U, R> combiner) {
        return switch (this) {
            case Valid<T> v1 -> switch (other) {
                case Valid<U> v2 -> valid(combiner.apply(v1.value(), v2.value()));
                case Invalid<U> inv2 -> invalid(inv2.errors());
            };
            case Invalid<T> inv1 -> switch (other) {
                case Valid<U> v2 -> invalid(inv1.errors());
                case Invalid<U> inv2 -> {
                    List<String> sveGreske = new ArrayList<>(inv1.errors());
                    sveGreske.addAll(inv2.errors());
                    yield invalid(sveGreske);
                }
            };
        };
    }

    // =========================================================================
    // Pomoćne metode za kreiranje validacija iz uslova
    // =========================================================================

    /**
     * Proveri uslov. Ako je ispunjen → Valid(value), inače → Invalid(error).
     */
    static <T> Validation<T> ensure(T value, Predicate<T> uslov, String greska) {
        return uslov.test(value) ? valid(value) : invalid(greska);
    }

    /**
     * Kombinator: primeni sve validacije na istu vrednost, akumuliraj greške.
     */
    @SafeVarargs
    static <T> Validation<T> validateAll(T value, Function<T, Validation<T>>... validacije) {
        List<String> sveGreske = Arrays.stream(validacije)
                .map(v -> v.apply(value))
                .filter(v -> !v.isValid())
                .flatMap(v -> v.getErrors().stream())
                .toList();

        return sveGreske.isEmpty() ? valid(value) : invalid(sveGreske);
    }

    // =========================================================================
    // fold — univerzalan "raspakivanje": obradi oba slučaja
    // =========================================================================

    default <R> R fold(Function<List<String>, R> onInvalid, Function<T, R> onValid) {
        return switch (this) {
            case Valid<T> v -> onValid.apply(v.value());
            case Invalid<T> inv -> onInvalid.apply(inv.errors());
        };
    }
}
