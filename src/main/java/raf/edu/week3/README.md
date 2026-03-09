# Paradigme Programiranja — Nedelja 3
## flatMap, Funktori, Monade i Dizajniranje sa Lambda Izrazima

---

## Sadržaj

1. [Podsetnik: map i filter](#1-podsetnik-map-i-filter)
2. [Primitivni Streamovi: IntStream i DoubleStream](#2-primitivni-streamovi-intstream-i-doublestream)
3. [Svođenje na jednu vrednost: reduce](#3-svođenje-na-jednu-vrednost-reduce)
4. [flatMap — problem koji rešava](#4-flatmap--problem-koji-rešava)
5. [Funktori i Monade — konceptualni okvir iz FP](#5-funktori-i-monade--konceptualni-okvir-iz-fp)
6. [flatMap na Optional](#6-flatmap-na-optional)
7. [Dizajniranje sa lambda izrazima (poglavlje 5)](#7-dizajniranje-sa-lambda-izrazima-poglavlje-5)
8. [map vs flatMap — kada koristiti šta](#8-map-vs-flatmap--kada-koristiti-šta)
9. [Primeri koda](#9-primeri-koda)

---

## 1. Podsetnik: map i filter

Iz prethodnih nedelja:

- **`filter(Predicate<T>)`** — zadržava elemente koji zadovoljavaju uslov → isti tip, manje elemenata
- **`map(Function<T, R>)`** — transformiše svaki element → potencijalno drugačiji tip, isti broj elemenata

Oba su **intermediate** operacije i vraćaju novi `Stream`.

```
filter:  [1, 2, 3, 4, 5]  →(x > 2)→  [3, 4, 5]
map:     [1, 2, 3]        →(x * 2)→  [2, 4, 6]
```

---

## 2. Primitivni Streamovi: IntStream i DoubleStream

`Stream<Integer>` radi sa **boxovanim** vrednostima — svaki `int` se pakuje u `Integer` objekat, što je skupo. Java nudi specijalizovane primitivne streamove koji direktno rade sa primitivima.

| Klasa | Za tip | Dolazi iz |
|-------|--------|-----------|
| `IntStream` | `int` | `mapToInt(...)`, `IntStream.of(...)`, `IntStream.range(...)` |
| `LongStream` | `long` | `mapToLong(...)` |
| `DoubleStream` | `double` | `mapToDouble(...)` |

### Kreiranje

```java
IntStream.of(1, 2, 3, 4, 5)
IntStream.range(1, 6)        // [1, 2, 3, 4, 5] — ekskluzivni kraj
IntStream.rangeClosed(1, 5)  // [1, 2, 3, 4, 5] — inkluzivni kraj

List<String> reci = List.of("ana", "bojan", "cara");
IntStream duzine = reci.stream().mapToInt(String::length);
```

### Ugrađene agregacijske terminalne operacije

Primitivni streamovi imaju direktne metode za statistiku — za razliku od `Stream<T>` koji zahteva `Collectors`:

```java
IntStream brojevi = IntStream.rangeClosed(1, 10);

brojevi.sum();                  // 55
brojevi.count();                // 10
brojevi.min();                  // OptionalInt(1)
brojevi.max();                  // OptionalInt(10)
brojevi.average();              // OptionalDouble(5.5)
brojevi.summaryStatistics();    // IntSummaryStatistics{count=10, sum=55, min=1, max=10, avg=5.5}
```

> **Zašto `Optional`?** Jer stream može biti prazan — `min()` nad praznim streamom nema smisla.

### Povratak na Stream objekata

```java
IntStream.range(1, 4)
         .boxed()              // IntStream → Stream<Integer>
         .collect(Collectors.toList()); // [1, 2, 3]

IntStream.range(1, 4)
         .mapToObj(n -> "Item " + n)   // IntStream → Stream<String>
         .forEach(System.out::println);
```

---

## 3. Svođenje na jednu vrednost: reduce

`sum()`, `count()`, `min()`, `max()` su specijalizovani slučajevi **reduce** operacije. `reduce` je generalizacija: uzima kolekciju elemenata i "sažima" ih u jednu vrednost primenom binarne operacije.

### Matematički

```
reduce([a, b, c, d], f) = f(f(f(a, b), c), d)
```

Gde je `f` neka binarna operacija (sabiranje, množenje, konkatenacija...).

### Potpisi

```java
// Varijanta 1: sa identity (početnom vrednosću) — vraća T
T reduce(T identity, BinaryOperator<T> accumulator)

// Varijanta 2: bez identity — vraća Optional<T> jer stream može biti prazan
Optional<T> reduce(BinaryOperator<T> accumulator)

// Varijanta 3: sa combiner-om (za paralelne streamove)
<U> U reduce(U identity, BiFunction<U,T,U> accumulator, BinaryOperator<U> combiner)
```

### Primeri

```java
List<Integer> brojevi = List.of(1, 2, 3, 4, 5);

// Sabiranje — identity je 0 (neutralni element za +)
int suma = brojevi.stream().reduce(0, (a, b) -> a + b);  // 15
int suma2 = brojevi.stream().reduce(0, Integer::sum);     // isto, method reference

// Množenje — identity je 1
int proizvod = brojevi.stream().reduce(1, (a, b) -> a * b);  // 120

// Maksimum — bez identity (vraća Optional)
Optional<Integer> max = brojevi.stream().reduce((a, b) -> a > b ? a : b);
```

### reduce vs. sum/count/max

| Operacija | Skraćenica | reduce ekvivalent |
|-----------|------------|-------------------|
| `sum()` | `IntStream.sum()` | `reduce(0, Integer::sum)` |
| `count()` | `stream().count()` | `reduce(0L, (acc, x) -> acc + 1, Long::sum)` |
| `max()` | `stream().max(comparator)` | `reduce((a, b) -> a > b ? a : b)` |

---

## 4. flatMap — problem koji rešava

### Motivacija

Zamislimo da imamo listu rečenica i hoćemo listu svih reči:

```java
List<String> recenice = List.of("zdravo svete", "funkcionalno programiranje");

// Pokušaj sa map:
Stream<String[]> rezultat = recenice.stream().map(r -> r.split(" "));
// Dobijamo: Stream<String[]> — stream nizova! Ne stream reči!
// [["zdravo", "svete"], ["funkcionalno", "programiranje"]]
```

Problem: `map` svaku rečenicu pretvori u niz reči, a mi dobijamo **stream streamova** (ili stream nizova). Hoćemo **jedan ravan stream** svih reči.

### Rešenje: flatMap

```
map:     [["zdravo","svete"], ["funk.","prog."]]   ← Stream<String[]>
flatMap: ["zdravo", "svete", "funk.", "prog."]     ← Stream<String>
```

`flatMap`:
1. Primeni funkciju na svaki element (kao `map`)
2. "Sravni" (flatten) rezultat — umesto stream-of-streams, dobijamo jedan stream

```java
List<String> reci = recenice.stream()
        .flatMap(r -> Arrays.stream(r.split(" ")))  // svaku String → Stream<String>
        .collect(Collectors.toList());
// ["zdravo", "svete", "funkcionalno", "programiranje"]
```

### Potpis

```java
// map:     Function<T, R>          — T → R
// flatMap: Function<T, Stream<R>>  — T → Stream<R>
<R> Stream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper)
```

Ključna razlika: `map` uzima `T → R`, `flatMap` uzima `T → Stream<R>`.

---

## 5. Funktori i Monade — konceptualni okvir iz FP

Ove ideje dolaze iz teorije kategorija i opisuju **obrazac** koji se pojavljuje u `Stream`, `Optional`, i mnogim drugim kontejnerima.

### Funktor

**Funktor** je kontejner koji podržava operaciju `map` — može da primeni funkciju na svaki element koji čuva, a da pri tom ostane iste "forme".

```
Funktor<T>  +  (T → R)  →  Funktor<R>
```

U Javi:
- `Stream<T>` je funktor: `.map(T → R)` daje `Stream<R>`
- `Optional<T>` je funktor: `.map(T → R)` daje `Optional<R>`
- `List<T>` je funktor (konceptualno)

**Zakon funktora:**
1. `map(x -> x)` mora da bude isto kao da nije ništa urađeno (**identity**)
2. `map(f).map(g)` == `map(x -> g(f(x)))` (**kompozicija**)

```java
// Zakon 1:
Optional.of("java").map(x -> x).equals(Optional.of("java")); // true

// Zakon 2:
Optional.of("java")
    .map(String::toUpperCase)
    .map(s -> s + "!")
    .equals(
        Optional.of("java").map(s -> s.toUpperCase() + "!")
    ); // true
```

### Monada

**Monada** je funktor koji **uz to** podržava i operaciju `flatMap` (u FP teoriji zvanu `bind` ili `>>=`).

```
Monada<T>  +  (T → Monada<R>)  →  Monada<R>
```

Problem koji monada rešava: šta ako funkcija u `map`-u **sama vraća kontejner**?

```java
// Funkcija koja vraća Optional
Optional<String> findEmail(String ime) { ... }

Optional<String> ime = Optional.of("Ana");

// Sa map: dobijamo Optional<Optional<String>> — dvostruko umotano!
Optional<Optional<String>> dvostruko = ime.map(n -> findEmail(n));

// Sa flatMap: "sravnjuje" automatski → Optional<String>
Optional<String> email = ime.flatMap(n -> findEmail(n));
```

Analogija sa Streamom:
```java
// map:     svaka reč → Stream<Character>  =>  Stream<Stream<Character>>
// flatMap: svaka reč → Stream<Character>  =>  Stream<Character>  (sravnjeno)
```

### Zašto je ovo važno?

| Koncept | Java | Daje nam |
|---------|------|----------|
| Funktor | `Stream`, `Optional` sa `map` | Transformaciju bez razpakivanja |
| Monada | `Stream`, `Optional` sa `flatMap` | Kompoziciju operacija koje same vraćaju kontejner |

Monade su svuda u modernom programiranju: `CompletableFuture`, reaktivni streamovi (`Flux`, `Mono` — nedelja 8!), baze podataka sa `Optional` rezultatima.

---

## 6. flatMap na Optional

`flatMap` na `Optional` rešava problem **ugnežđenih Optional-a**:

```java
// Bez flatMap — problem:
Optional<String> ime = Optional.of("Ana");
Optional<Optional<String>> dvostruko = ime.map(n -> nadjiEmail(n)); // loše!

// Sa flatMap — ispravno:
Optional<String> email = ime.flatMap(n -> nadjiEmail(n));
```

### Praktičan primer: lanac Optional poziva

```java
record Korisnik(String ime, Optional<Adresa> adresa) {}
record Adresa(String grad, Optional<String> postanski) {}

Optional<Korisnik> korisnik = nadjiKorisnika("Ana");

// Bez flatMap — ružno:
Optional<String> postanski = Optional.empty();
if (korisnik.isPresent()) {
    if (korisnik.get().adresa().isPresent()) {
        if (korisnik.get().adresa().get().postanski().isPresent()) {
            postanski = korisnik.get().adresa().get().postanski();
        }
    }
}

// Sa flatMap — elegantno:
Optional<String> postanski2 = korisnik
        .flatMap(Korisnik::adresa)
        .flatMap(Adresa::postanski);
```

---

## 7. Dizajniranje sa lambda izrazima (poglavlje 5)

Lambda izrazi nisu samo sintaksni šećer — menjaju **način na koji dizajniramo** kod.

### 7.1 Razdvajanje brige (Separation of Concerns)

Razdvajanje "šta se radi" od "kako se parameterizuje":

```java
// BEZ lambda — moramo interfejs + klasu za svaku varijaciju
interface Strategija {
    boolean primeni(int vrednost);
}
class VecaOd10 implements Strategija { ... }
class Parna     implements Strategija { ... }

// SA lambda — strategija je samo vrednost
Predicate<Integer> vecaOd10 = n -> n > 10;
Predicate<Integer> parna    = n -> n % 2 == 0;

// Možemo ih proslediti kao parametre
public List<Integer> filtriraj(List<Integer> lista, Predicate<Integer> kriterijum) {
    return lista.stream().filter(kriterijum).collect(Collectors.toList());
}
```

### 7.2 Strategy Pattern sa lambdama

Klasični Strategy pattern postaje trivijalan:

```java
// Strategija sortiranja — bez novih klasa!
Comparator<Student> poImenu   = Comparator.comparing(Student::getIme);
Comparator<Student> poPoenima = Comparator.comparingInt(Student::getPoeni).reversed();

studenti.stream().sorted(poImenu).forEach(System.out::println);
studenti.stream().sorted(poPoenima).forEach(System.out::println);
```

### 7.3 Decorator Pattern sa lambdama

Umesto hijerarhije dekoratora, kompozicija funkcija:

```java
Function<String, String> trim     = String::trim;
Function<String, String> lower    = String::toLowerCase;
Function<String, String> normalize = trim.andThen(lower);

// Dodajemo "sloj" bez novih klasa
Function<String, String> withPrefix = normalize.andThen(s -> ">>>" + s);
```

### 7.4 Execute Around Method Pattern

Lambda kao argument koji se izvršava "oko" neke infrastrukture:

```java
// Upravljanje resursom — osiguravamo da se resurs uvek zatvori
public static <T> T withConnection(Function<Connection, T> action) {
    try (Connection conn = DriverManager.getConnection(...)) {
        return action.apply(conn); // izvršava prosleđeni kod
    } // conn se automatski zatvara
}

// Korišćenje — ne brinemo o zatvaranju konekcije
String rezultat = withConnection(conn -> conn.query("SELECT ..."));
```

### 7.5 Lambda kao fabrika objekata

```java
// Factory Method Pattern sa Supplier
Map<String, Supplier<Animal>> fabrika = Map.of(
    "pas",  Pas::new,
    "macka", Macka::new,
    "ptica", Ptica::new
);

Animal zivotinja = fabrika.get("pas").get(); // kreira Pas objekat
```

---

## 8. map vs flatMap — kada koristiti šta

| Situacija | Koristiti |
|-----------|-----------|
| Funkcija vraća **vrednost** `T → R` | `map` |
| Funkcija vraća **kontejner** `T → Stream<R>` ili `T → Optional<R>` | `flatMap` |
| Hoćemo da "sravnimo" listu lista | `flatMap` |
| Transformišemo svaki element, broj ostaje isti | `map` |
| Svaki element može dati nula ili više elemenata | `flatMap` |

```java
// map: String → String  (uvek 1 rezultat)
stream.map(String::toUpperCase)

// flatMap: String → Stream<String>  (može biti 0, 1 ili više reči)
stream.flatMap(r -> Arrays.stream(r.split(" ")))

// flatMap: String → Optional<String>  (može biti prazno)
stream.flatMap(ime -> Optional.ofNullable(imenik.get(ime)).stream())
```

---

## 9. Primeri koda

Svi primeri se nalaze u `src/` folderu:

- [`PrimitivniStreamovi.java`](src/PrimitivniStreamovi.java) — `IntStream`, `DoubleStream`, agregacije, `summaryStatistics`
- [`ReducePrimeri.java`](src/ReducePrimeri.java) — `reduce` sa i bez identity, veza sa `sum/max/count`
- [`FlatMapPrimeri.java`](src/FlatMapPrimeri.java) — `flatMap` na `Stream` i `Optional`, poređenje sa `map`
- [`FunktoriMonade.java`](src/FunktoriMonade.java) — ilustracija zakona funktora i monada kroz Java kod
- [`DizajnSaLambdama.java`](src/DizajnSaLambdama.java) — Strategy, Decorator i Execute-Around pattern sa lambdama