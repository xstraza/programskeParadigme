# Pipeline — Data Processing / ETL

## Obrazac

ETL (Extract → Transform → Load) je jedan od najčešćih obrazaca u produkcijskom kodu.
Funkcionalni pristup ga modeluje kao **jedan stream pipeline** — svaki korak je
intermediate operacija, a izlaz je terminal operacija.

```
Sirovi podaci (CSV, JSON, API)
    │
    ▼
  parse()        — String → Record (Extract)
    │
    ▼
  filter()       — odbaci nevalidne
    │
    ▼
  map()          — obogati / transformiši (Transform)
    │
    ▼
  collect()      — agregiraj, grupiši, sačuvaj (Load)
```

## Zašto FP?

**Imperativni pristup:**
```java
List<Rezultat> rezultati = new ArrayList<>();
for (String linija : linije) {
    String[] delovi = linija.split(",");
    if (delovi.length < 4) continue;
    Transakcija t = parse(delovi);
    if (t.iznos() <= 0) continue;
    t = obogate(t);
    rezultati.add(t);
}
Map<String, Double> sumaPoKategoriji = new HashMap<>();
for (Rezultat r : rezultati) {
    sumaPoKategoriji.merge(r.kategorija(), r.iznos(), Double::sum);
}
```

**FP pristup:**
```java
Map<String, Double> sumaPoKategoriji = linije.stream()
    .map(Transakcija::parse)
    .flatMap(Optional::stream)        // odbaci nevalidne
    .filter(t -> t.iznos() > 0)
    .map(Transakcija::obogati)
    .collect(groupingBy(Transakcija::kategorija, summingDouble(Transakcija::iznos)));
```

Prednosti:
- **Čitljivost** — tok podataka se čita odozgo nadole
- **Kompozibilnost** — svaki korak je nezavisan, lako se dodaje/uklanja
- **Lenjost** — ako dodamo `limit()` ili `findFirst()`, ne obrađuju se svi elementi
- **Paralelizam** — `.parallelStream()` bez ikakve promene logike

## Primeri u kodu

- `DataPipeline.java` — kompletna ETL obrada finansijskih transakcija iz CSV-a

---
