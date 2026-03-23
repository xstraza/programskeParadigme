# Validation — Akumulacija grešaka bez exception-a

## Problem

Klasičan pristup validaciji: baci exception na prvu grešku.

```java
void registruj(String ime, String email, int godine) {
    if (ime == null || ime.isBlank()) throw new IllegalArgumentException("Ime je obavezno");
    if (!email.contains("@"))        throw new IllegalArgumentException("Neispravan email");
    if (godine < 0 || godine > 150)  throw new IllegalArgumentException("Neispravne godine");
    // ...
}
```

Problemi:
- **Fail-fast** — korisnik dobija samo prvu grešku, mora da ispravi i pošalje ponovo
- **Exception za kontrolu toka** — exception nije izuzetak, već očekivano ponašanje
- **Teško za kompoziciju** — ne možemo kombinovati validacije bez try-catch

## Rešenje: Validation tip (Either pattern)

Umesto exception-a, svaka validacija vraća `Validation<T>` — ili uspešnu vrednost, ili listu grešaka:

```
Validation<T>
  ├── Valid(value)         — sadrži uspešnu vrednost
  └── Invalid(errors)      — sadrži listu grešaka (ne samo jednu!)
```

Ključna razlika od Optional: **akumuliramo SVE greške**, ne stajemo na prvoj.

## FP obrazac

```
input ──→ validacija1 ──→ Validation<A>
      ──→ validacija2 ──→ Validation<B>    ──combine──→ Validation<Rezultat>
      ──→ validacija3 ──→ Validation<C>
```

Svaka validacija je **čista funkcija**: `T → Validation<R>`.
Kombinujemo ih bez if/else, bez try/catch, bez mutable stanja.

## Zašto FP?

- **Sve greške odjednom** — korisnik vidi kompletnu listu šta treba da ispravi
- **Čiste funkcije** — svaka validacija je nezavisna, testabilna, reusable
- **Kompozicija** — validacije se kombinuju kao funkcije (`andThen`, `combine`)
- **Tip-safe** — kompajler garantuje da se greške obrađuju

## Primeri u kodu

- `Validation.java` — generički Validation tip sa combine/map/flatMap
- `UserRegistration.java` — praktičan primer validacije korisničke registracije

---
