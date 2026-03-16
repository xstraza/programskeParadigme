# Paradigme Programiranja — Nedelja 4
## Lenja evaluacija, Optimizacija rekurzije, flatMap detaljno, peek, flatMapping

---

## Sadržaj

1. [Lenja evaluacija (Lazy Evaluation)](#1-lenja-evaluacija-lazy-evaluation)
2. [Optimizacija rekurzije — Tail Call i Memoizacija](#2-optimizacija-rekurzije)
3. [flatMap — napredni obrasci](#3-flatmap--napredni-obrasci)
4. [flatMap na Optional — nastavak](#4-flatmap-na-optional--nastavak)
5. [peek — prozor u pipeline](#5-peek--prozor-u-pipeline)
6. [map vs flatMap — kada koristiti šta](#6-map-vs-flatmap--kada-koristiti-šta)
7. [Collectors.joining — spajanje stringova](#7-collectorsjoining--spajanje-stringova)
8. [flatMapping kao Collector](#8-flatmapping-kao-collector)
9. [Primeri koda](#9-primeri-koda)

---

## 1. Lenja evaluacija (Lazy Evaluation)

### Šta znači "lenjo"?

Stream operacije su podeljene u dve vrste:
- **Intermediate (posredne)** — `filter`, `map`, `flatMap`, `sorted`, `distinct`, `limit`, `peek`... → **lenji su!**
- **Terminal (završne)** — `forEach`, `collect`, `count`, `sum`, `findFirst`, `anyMatch`... → **okidaju izračunavanje**

Posredne operacije se **ne izvršavaju** dok ne dođe terminalna operacija. Tek tada JVM prolazi kroz elemente.

```java
// Ovo NE radi ništa samo po sebi:
Stream<Integer> s = List.of(1, 2, 3, 4, 5).stream()
        .filter(n -> { System.out.println("filter: " + n); return n > 2; })
        .map(n -> { System.out.println("map: " + n); return n * 10; });

// Tek ovo pokreće sve:
s.findFirst();
// Ispis:
//   filter: 1
//   filter: 2
//   filter: 3   ← prvi koji prođe filter
//   map: 3      ← odmah se mapira
//   Gotovo! Ne prolazi dalje (findFirst je zadovoljan)
```

### Kratki spoj (Short-circuiting)

Neke terminalne operacije **ne moraju proći sve elemente**:

| Operacija | Kada staje |
|-----------|------------|
| `findFirst()` | Čim nađe prvi element |
| `findAny()` | Čim nađe bilo koji element |
| `anyMatch(p)` | Čim nađe prvi koji zadovoljava `p` |
| `noneMatch(p)` | Čim nađe prvi koji zadovoljava `p` |
| `allMatch(p)` | Čim nađe prvi koji NE zadovoljava `p` |
| `limit(n)` | Uzima samo prvih `n` elemenata |

```java
// findFirst sa limit — bez lenje evaluacije ovo bi bilo neefikasno!
Optional<Integer> prvi = IntStream.iterate(1, n -> n + 1) // BESKONAČAN stream
        .filter(n -> n % 17 == 0)
        .filter(n -> n % 13 == 0)
        .findFirst(); // staje čim nađe 221 (= 13 × 17)
```

### Vertikalni vs. horizontalni prolaz

Važno: stream NE prolazi sve elemente kroz filter, pa sve kroz map. Umesto toga, **svaki element prolazi kroz ceo pipeline** pre nego što se krene na sledeći.

```
Horizontalno (pogrešna intuicija):
  filter: [1,2,3,4,5] → [3,4,5]
  map:    [3,4,5]     → [30,40,50]

Vertikalno (stvarni tok):
  elem 1 → filter(fail) → skip
  elem 2 → filter(fail) → skip
  elem 3 → filter(pass) → map → 30
  elem 4 → filter(pass) → map → 40
  elem 5 → filter(pass) → map → 50
```

Ovo je ključno za razumevanje kratkog spoja i optimizacije redosleda operacija.

### Optimizacija: redosled filter i map

```java
// LOŠIJE: map se poziva na svakom elementu pre filtriranja
stream.map(skupaTransformacija).filter(uslov)

// BOLJE: filter eliminise elemente pre skupe transformacije
stream.filter(uslov).map(skupaTransformacija)
```

### Beskonačni streamovi

Lenja evaluacija omogućava rad sa **beskonačnim streamovima** — mogu se definisati jer se elementi generišu tek kada zatrebaju:

```java
// Beskonačan stream prirodnih brojeva
IntStream.iterate(1, n -> n + 1)
         .filter(n -> n % 2 != 0)   // neparni
         .limit(5)                   // uzmi prvih 5
         .forEach(System.out::println); // 1 3 5 7 9

// Fibonači niz — beskonačan, leno generisan
Stream.iterate(new long[]{0, 1}, f -> new long[]{f[1], f[0] + f[1]})
      .limit(10)
      .map(f -> f[0])
      .forEach(System.out::println);
```

---

## 2. Optimizacija rekurzije

### Problem: Stack Overflow kod naivne rekurzije

Klasična rekurzivna implementacija faktoriijela ili Fibonačija brzo generiše `StackOverflowError` za velike unose — svaki rekurzivni poziv dodaje novi stek frejm.

```java
// NAIVNO — Stack overflow za n > ~10000
int faktorijel(int n) {
    if (n == 0) return 1;
    return n * faktorijel(n - 1); // n poziva na steku odjednom!
}
```

### Tail Call Optimization (TCO) — konceptualno

U jezicima koji podržavaju TCO (Haskell, Scala, Kotlin), kompajler prepoznaje **repni poziv** (tail call) — rekurzivni poziv koji je **poslednja operacija** u funkciji — i pretvara ga u petlju. Java JVM to ne podržava direktno, ali možemo simulirati.

**Repni poziv** (tail call) = rekurzivni poziv je poslednja stvar koja se radi, bez obrade rezultata:

```java
// NIJE repni poziv — mora da sačeka rezultat n * faktorijel(n-1)
return n * faktorijel(n - 1);

// JESTE repni poziv — akumulator pamti međurezultat
return faktorijel(n - 1, n * akumulator);
```

### Trampolining u Javi

Java JVM nema TCO — čak i sa pravilnim repnim pozivom, stek raste. Trampolining je tehnika koja to zaobilazi: umesto da funkcija **poziva samu sebe**, ona **vraća opis sledećeg koraka** (tzv. *thunk* — odloženi poziv). Spoljna petlja onda izvršava korak po korak, uvek na istom stek frejmu.

#### Analogija

Zamislite rekurziju kao skakanje sa stepenica — svaki skok vas spušta nivo dublje, i morate se popeti nazad. Trampolining je kao da skačete na trampolini — svaki skok vas vraća na istu visinu, samo sa novim parametrima.

```
Klasična rekurzija (raste stek):        Trampolining (konstantan stek):
  fakt(5)                                 petlja:
    → 5 * fakt(4)                           korak(5, 1)  → vrati korak(4, 5)
        → 4 * fakt(3)                       korak(4, 5)  → vrati korak(3, 20)
            → 3 * fakt(2)                   korak(3, 20) → vrati korak(2, 60)
                → 2 * fakt(1)               korak(2, 60) → vrati korak(1, 120)
                    → 1                     korak(1,120) → vrati GOTOVO(120)
                ← 2
            ← 6                           Uvek 1 frejm na steku!
        ← 24
    ← 120
  5 frejma na steku odjednom!
```

#### Implementacija

Potrebna su nam dva koncepta:
1. **`TailCall<T>`** — predstavlja jedan korak izračunavanja. Može biti:
   - *sledeći korak* — lambda koja vraća novi `TailCall` (thunk)
   - *gotovo* — završna vrednost (`done(rezultat)`)
2. **`trampoline()`** — petlja koja izvršava korake dok ne dobije finalni rezultat

```java
@FunctionalInterface
interface TailCall<T> {
    TailCall<T> apply();           // sledeći korak
    default boolean isDone() { return false; }
    default T result() { throw new Error("nije gotovo"); }

    // Fabrika za finalni rezultat — prekida lanac
    static <T> TailCall<T> done(T v) {
        return new TailCall<T>() {
            public TailCall<T> apply() { return this; }
            public boolean isDone()    { return true; }
            public T result()          { return v; }
        };
    }
}

// Petlja koja "trampolira" — izvršava korak po korak, uvek isti frejm
static <T> T trampoline(TailCall<T> call) {
    while (!call.isDone()) call = call.apply();
    return call.result();
}
```

#### Korišćenje — faktorijel

Ključna razlika: funkcija **ne poziva samu sebe**, već **vraća lambdu** koja opisuje sledeći poziv:

```java
static TailCall<Long> faktorijel(int n, long akum) {
    if (n <= 1) return TailCall.done(akum);  // GOTOVO — vrati rezultat
    return () -> faktorijel(n - 1, n * akum); // SLEDEĆI KORAK — vrati thunk (lambdu)
    //     ↑ ovo je lambda, NE rekurzivni poziv!
    //       lambda se izvršava TEK kada je petlja pozove
}

// Poziv:
long rezultat = trampoline(faktorijel(100000, 1)); // ← radi! Nema StackOverflow
```

Šta se dešava korak po korak:
```
trampoline( faktorijel(4, 1) )
  → faktorijel(4, 1) vraća lambdu: () -> faktorijel(3, 4)      // nije gotovo
  → petlja poziva lambdu → faktorijel(3, 4)
  → faktorijel(3, 4) vraća lambdu: () -> faktorijel(2, 12)     // nije gotovo
  → petlja poziva lambdu → faktorijel(2, 12)
  → faktorijel(2, 12) vraća lambdu: () -> faktorijel(1, 24)    // nije gotovo
  → petlja poziva lambdu → faktorijel(1, 24)
  → faktorijel(1, 24) vraća TailCall.done(24)                  // GOTOVO!
  → petlja vraća 24
```

Svaki korak se izvršava u `while` petlji — **nikada nema više od jednog frejma na steku**.

### Memoizacija

**Memoizacija** = keširanje rezultata skupih funkcija. Ako funkcija za iste argumente uvek vraća isti rezultat (čista funkcija), možemo pamtiti rezultate.

```java
// Naivan Fibonači — eksponencijalna složenost O(2^n)
// fib(40) zahteva ~2 milijarde poziva!
long fib(int n) {
    if (n <= 1) return n;
    return fib(n - 1) + fib(n - 2);
}

// Sa memoizacijom — O(n)
Map<Integer, Long> memo = new HashMap<>();
long fibMemo(int n) {
    if (n <= 1) return n;
    return memo.computeIfAbsent(n, k -> fibMemo(k - 1) + fibMemo(k - 2));
}
```

`computeIfAbsent(key, fn)` — ako ključ ne postoji, izračunaj vrednost sa `fn` i sačuvaj je. Elegantno i thread-safe (sa `ConcurrentHashMap`).

---

## 3. flatMap — napredni obrasci

> Osnove `flatMap` (motivacija, sravnjivanje, `map` vs `flatMap`) su obrađene u nedelji 3.
> Ovde nastavljamo sa obrascima koji nisu pokriveni tada.

### Kombinatorika — Dekartov proizvod

`flatMap` može spariti svaki element jedne liste sa svakim elementom druge — bez ugnežđenih petlji:

```java
List<String> velicine = List.of("S", "M", "L");
List<String> boje     = List.of("crvena", "plava", "bela");

List<String> kombinacije = velicine.stream()
        .flatMap(v -> boje.stream().map(b -> v + "/" + b))
        .collect(Collectors.toList());
// ["S/crvena", "S/plava", "S/bela", "M/crvena", ...]
```

### Lookup sa Optional::stream

Kada rezultat lookupa možda ne postoji, `flatMap` + `Optional::stream` eliminiše elemente koji nisu nađeni — bez `filter` + `map`:

```java
Map<String, Integer> cenovnik = Map.of("jabuka", 100, "banana", 80);
List<String> korpa = List.of("jabuka", "breskva", "banana");

// Samo artikli koji postoje u cenovniku
List<Integer> cene = korpa.stream()
        .flatMap(p -> Optional.ofNullable(cenovnik.get(p)).stream())
        .collect(Collectors.toList());
// [100, 80]
```

### Višestruko sravnjivanje (više nivoa)

```java
// Firma → odeljenja → zaposleni → inicijali
firma.stream()
     .flatMap(f -> f.odeljenja().stream())
     .flatMap(o -> o.zaposleni().stream())
     .map(z -> z.ime().charAt(0) + "." + z.prezime().charAt(0) + ".")
     .forEach(System.out::println);
```

---

## 4. flatMap na Optional — nastavak

> Osnovni obrazac — zašto nastaje `Optional<Optional<T>>` i kako `flatMap` to rešava — obrađen je u nedelji 3 (sekcije 5 i 6).
> Ovde se fokusiramo na `Optional::stream` i obrasce u stream pipeline-u.

### Optional::stream (Java 9+)

Konverzija `Optional` → `Stream` od 0 ili 1 elementa. Ovo je most između dva sveta — omogućava da Optional rezultate koristimo direktno u stream pipeline-u:

```java
// Lista Optional vrednosti → samo prisutne
List<Optional<String>> mesavina = List.of(
    Optional.of("java"), Optional.empty(), Optional.of("stream")
);

List<String> prisutne = mesavina.stream()
        .flatMap(Optional::stream)      // Optional<String> → Stream<String> (0 ili 1)
        .collect(Collectors.toList());  // ["java", "stream"]

// Poređenje sa Java 8 stilom (bez Optional::stream):
mesavina.stream()
        .filter(Optional::isPresent)
        .map(Optional::get)             // ← manje elegantno, ali radi
        .collect(Collectors.toList());
```

### Praktičan obrazac: lanac lookupa u stream pipeline-u

Kada svaki korak u lancu može da "propadne" (vrati prazan Optional), `flatMap` omogućava elegantan lanac bez `if` provera:

```java
Map<String, String> emailBaza = Map.of("Ana", "ana@uni.rs");
Map<String, String> gradBaza  = Map.of("ana@uni.rs", "Beograd");

// Ime → email → grad (svaki korak može biti prazan)
String grad = Optional.of("Ana")
        .flatMap(n  -> Optional.ofNullable(emailBaza.get(n)))
        .flatMap(em -> Optional.ofNullable(gradBaza.get(em)))
        .orElse("(nepoznato)");
```

---

## 5. peek — prozor u pipeline

### Šta je peek?

`peek` je **intermediate** operacija koja "viri" u elemente koji prolaze kroz pipeline, **bez da ih menja**. Potpis:

```java
Stream<T> peek(Consumer<? super T> action)
```

Ključna svojstva:
- **Ne transformiše elemente** — za razliku od `map`, koji vraća novu vrednost, `peek` prima `Consumer` (ne `Function`) i prosleđuje element dalje neizmenjen.
- **Lenj je** — kao i svaka intermediate operacija, izvršava se tek kad dođe terminal operacija.
- **Vertikalan tok** — `peek` se izvršava u istom trenutku kad i element koji "prolazi" kroz njega, ne unapred za sve elemente.

### Primer: praćenje toka elemenata

```java
List.of("ana", "bojan", "cara", "dragana", "eva").stream()
        .peek(s  -> System.out.println("[ulaz]      " + s))
        .filter(s -> s.length() > 3)
        .peek(s  -> System.out.println("[filter OK] " + s))
        .map(String::toUpperCase)
        .peek(s  -> System.out.println("[posle map] " + s))
        .limit(2)
        .forEach(s -> System.out.println("[IZLAZ]     " + s));
```

Ispis (vertikalni tok — element po element):
```
[ulaz]      ana          ← ana ne prolazi filter (length=3), prelazimo na sledeći
[ulaz]      bojan
[filter OK] bojan
[posle map] BOJAN
[IZLAZ]     BOJAN        ← prvi element stigao do kraja
[ulaz]      cara
[filter OK] cara
[posle map] CARA
[IZLAZ]     CARA         ← limit(2) zadovoljan — stajemo!
                          ← dragana i eva se uopšte ne evaluiraju
```

### Kada koristiti peek?

**Korisno za debugging** — kada pipeline ne daje očekivani rezultat, ubacite `peek` između koraka da vidite šta prolazi:

```java
// "Zašto mi filter ne propušta ništa?"
rezultat = lista.stream()
    .peek(x -> System.out.println("pre filtera: " + x))  // ← šta ulazi?
    .filter(nekiUslov)
    .peek(x -> System.out.println("posle filtera: " + x)) // ← šta izlazi?
    .map(transformacija)
    .collect(Collectors.toList());
```

### Kada NE koristiti peek

`peek` prima `Consumer` — može da izvrši sporedni efekat (side effect). Ali to **ne treba raditi u produkcijskom kodu**:

```java
// LOŠE — sporedni efekti u peek-u
List<String> sakupljeno = new ArrayList<>();
stream.peek(sakupljeno::add)  // ← anti-pattern! Mutira eksterno stanje
      .filter(...)
      .collect(Collectors.toList());

// LOŠE — peek za transformaciju (ne radi kako se očekuje)
stream.peek(obj -> obj.setStatus("processed"))  // ← mutirajući peek, nepouzdano!
      .collect(Collectors.toList());
```

Zašto je ovo loše:
1. **Lenjost** — `peek` se ne izvršava za elemente koji ne stignu do terminal operacije (npr. filtrirani elementi, kratki spoj). Ne možemo se osloniti da će se peek izvršiti za sve elemente.
2. **Paralelni streamovi** — redosled izvršavanja peek-a nije garantovan u `.parallelStream()`.
3. **Čitljivost** — skriveni sporedni efekti otežavaju razumevanje koda.

**Pravilo**: `peek` koristiti **isključivo za debugging i logovanje**. Za bilo šta drugo, koristiti `map`, `forEach`, ili `collect`.

### peek vs forEach

| | `peek` | `forEach` |
|---|--------|-----------|
| Tip operacije | Intermediate (lenj) | Terminal (izvrši odmah) |
| Vraća | `Stream<T>` (nastavlja pipeline) | `void` (kraj pipeline-a) |
| Namena | Debugging / logovanje | Finalna akcija |

---

## 6. map vs flatMap — kada koristiti šta

> U nedelji 3 smo videli osnovnu razliku. Ovde je proširena tabela sa praktičnim heuristikama.

| Pitanje | map | flatMap |
|---------|-----|---------|
| Funkcija vraća **jednu vrednost** `T → R`? | ✓ | |
| Funkcija vraća **kontejner** `T → Stream<R>` ili `T → Optional<R>`? | | ✓ |
| Svaki element daje **tačno jedan** rezultat? | ✓ | |
| Svaki element daje **0, 1 ili više** rezultata? | | ✓ |
| Hoćemo da "sravnimo" ugnežđenu strukturu? | | ✓ |
| Rezultat je `Optional<Optional<T>>`? | | ✓ (sravnjuje) |

### Heuristika za odluku

```
1. Da li moja lambda vraća Stream, Optional, Collection ili niz?
   → DA  → flatMap  (sravnjuje ugnežđenu strukturu)
   → NE  → map      (prosta transformacija)

2. Da li dobijam Stream<Stream<T>> ili Optional<Optional<T>>?
   → DA  → trebalo je flatMap umesto map

3. Da li imam listu lista i hoću jednu ravnu listu?
   → DA  → flatMap(Collection::stream)
```

### Konkretni primeri

```java
// map: jedna reč → jedna reč (1:1)
stream.map(String::toUpperCase)

// flatMap: jedna rečenica → više reči (1:N)
stream.flatMap(r -> Arrays.stream(r.split(" ")))

// flatMap: jedan student → 0 ili 1 email (1:0..1)
stream.flatMap(s -> Optional.ofNullable(s.getEmail()).stream())

// flatMap: jedna narudžbina → više stavki (1:N)
narudzbine.stream().flatMap(n -> n.getStavke().stream())
```

---

## 7. Collectors.joining — spajanje stringova

`Collectors.joining` je specijalizovani kolektor za spajanje elemenata streama u jedan `String`.

### Tri varijante

```java
List<String> reci = List.of("Java", "je", "moćna");

// 1. Bez separatora — prosto nadovezivanje
reci.stream().collect(Collectors.joining());
// "Javajemoćna"

// 2. Sa separatorom
reci.stream().collect(Collectors.joining(" "));
// "Java je moćna"

// 3. Sa separatorom, prefixom i sufixom
reci.stream().collect(Collectors.joining(", ", "[", "]"));
// "[Java, je, moćna]"
```

### Praktični primeri

```java
// CSV red
List<String> polja = List.of("Ana", "21", "Beograd");
String csv = polja.stream().collect(Collectors.joining(","));
// "Ana,21,Beograd"

// SQL IN klauzula
List<Integer> ids = List.of(1, 5, 12, 30);
String sql = ids.stream()
        .map(String::valueOf)
        .collect(Collectors.joining(", ", "WHERE id IN (", ")"));
// "WHERE id IN (1, 5, 12, 30)"

// Joining nakon transformacije
String inicijali = List.of("Ana Petrović", "Bojan Ilić").stream()
        .map(ime -> ime.substring(0, 1))
        .collect(Collectors.joining(". ", "", "."));
// "A. B."
```

### joining vs String.join

```java
// String.join — radi samo sa nizovima / Iterable
String.join(", ", "a", "b", "c");         // "a, b, c"
String.join(", ", List.of("a", "b", "c")); // "a, b, c"

// Collectors.joining — radi sa streamovima, omogućava prethodno map/filter
stream.filter(...).map(...).collect(Collectors.joining(", "));
```

Koristiti `String.join` kad imamo gotovu listu/niz. Koristiti `Collectors.joining` kad radimo u stream pipeline-u.

---

## 8. flatMapping kao Collector

`Collectors.flatMapping` (Java 9+) kombinuje `flatMap` i `groupingBy` u jednom prolazu — korisno kada grupišete i svaki element ima kolekciju "pod-elemenata".

```java
record Narudzbina(String kupac, List<String> stavke) {}

// Bez flatMapping — neefikasno, dva koraka
Map<String, List<String>> stavkePoKupcu1 = narudzbine.stream()
        .collect(Collectors.groupingBy(
                Narudzbina::kupac,
                Collectors.mapping(n -> n.stavke(), Collectors.toList()) // ← daje List<List<String>>!
        ));

// Sa flatMapping — direktno sravnjuje
Map<String, List<String>> stavkePoKupcu2 = narudzbine.stream()
        .collect(Collectors.groupingBy(
                Narudzbina::kupac,
                Collectors.flatMapping(
                        n -> n.stavke().stream(), // Narudzbina → Stream<String>
                        Collectors.toList()
                )
        ));
// { "Ana": ["knjiga", "olovka", "sveska"], "Bojan": ["laptop"] }
```

### flatMapping vs flatMap + groupingBy

Zašto ne uraditi `flatMap` pa onda `groupingBy`?

```java
// Problem: posle flatMap-a gubimo vezu sa kupcem!
narudzbine.stream()
        .flatMap(n -> n.stavke().stream())  // ← sad imamo samo stavke, ne znamo čije su
        .collect(Collectors.groupingBy(...)); // ← ne možemo grupisati po kupcu

// flatMapping rešava: flatMap se dešava UNUTAR groupingBy, veza je očuvana
```

---

## 9. Primeri koda

Svi primeri se nalaze u `src/` folderu:

- [`LazyEvaluation.java`](src/LazyEvaluation.java) — lazy pipeline, kratki spoj, beskonačni streamovi, optimizacija redosleda, peek
- [`RecursionOptimization.java`](src/RecursionOptimization.java) — trampolining, memoizacija, `computeIfAbsent`
- [`FlatMapDetailed.java`](src/FlatMapDetailed.java) — kombinatorika, lookup sa `Optional::stream`, lanac Optional poziva, flatMapping

---
