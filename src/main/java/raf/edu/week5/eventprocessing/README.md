# Event Processing — Immutable stanje kroz reduce

## Obrazac

Event sourcing je obrazac gde se **stanje ne čuva direktno**, već se rekonstruiše
iz niza događaja (event-ova). Finalno stanje je rezultat **fold/reduce** operacije
nad listom eventova.

```
Event1 → Event2 → Event3 → ... → EventN
                    │
                    ▼
         reduce(početnoStanje, (stanje, event) → novoStanje)
                    │
                    ▼
              Finalno stanje
```

## Zašto FP?

**Imperativni pristup (mutable):**
```java
class BankovniRacun {
    private double stanje = 0;
    void uplata(double iznos) { stanje += iznos; }
    void isplata(double iznos) { stanje -= iznos; }
    // Ko je promenio stanje? Kada? Zašto? — ne znamo
}
```

**FP pristup (immutable + eventi):**
```java
record Stanje(double balans, List<String> istorija) {}

Stanje finalnoStanje = eventi.stream()
    .reduce(Stanje.POCETNO, Stanje::primeni, (a, b) -> a);
// Svaka promena je zabeležena. Stanje se može rekonstruisati do bilo kog trenutka.
```

Prednosti:
- **Audit trail** — svaka promena je event, ništa se ne gubi
- **Time travel** — možemo rekonstruisati stanje u bilo kom trenutku
- **Testabilnost** — `primeni(stanje, event) → novoStanje` je čista funkcija
- **Undo** — jednostavno: primenimo sve evente osim poslednjeg
- **Paralelizam** — eventi su immutable, nema race condition-a

## Primeri u kodu

- `EventSourcing.java` — bankovni račun modelovan kroz evente

---
