# Builder — Konfiguracija i dekoratori kroz Function kompoziciju

## Problem

Klasičan Builder pattern zahteva dosta boilerplate koda:

```java
class HttpRequest {
    private String url;
    private String method;
    private Map<String, String> headers;
    // ... 10+ polja

    static class Builder {
        // duplikat svih polja
        // setter za svako polje koji vraća this
        // build() metoda
        // 50+ linija koda za 10 polja
    }
}
```

## Rešenje: Function composition

Umesto Builder klase, svaka konfiguracija je `UnaryOperator<T>` — funkcija koja
prima config i vraća modifikovani config. Komponujemo ih sa `andThen`:

```
config1: Config → Config   (postavi URL)
config2: Config → Config   (dodaj header)
config3: Config → Config   (postavi timeout)

finalConfig = config1.andThen(config2).andThen(config3)
```

## Zašto FP?

- **Zero boilerplate** — nema Builder klase, nema dupliranih polja
- **Kompozicija** — konfiguracije se kombinuju kao funkcije
- **Reusable delovi** — čest header (auth, content-type) je funkcija koja se može ponovo koristiti
- **Condicionalno** — lako: `uslov ? configA : Function.identity()`
- **Decorator pattern** — ista ideja: `Function<Handler, Handler>` je dekorator

## Primeri u kodu

- `FunctionalBuilder.java` — HTTP request builder i middleware/decorator chain

---
