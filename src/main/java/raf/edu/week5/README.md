# Paradigme Programiranja — Nedelja 5
## FP u praksi — obrasci iz produkcijskog koda

---

U prethodnim nedeljama smo naučili alate: lambda, stream, flatMap, reduce, Optional, memoizacija.
Ova nedelja ih spaja u **realne obrasce** koji se koriste u produkcijskom kodu.

Svaki paket je nezavisan mini-projekat sa svojim README-om i primerima.

## Paketi

| Paket | Tema | Ključni FP koncepti |
|-------|------|---------------------|
| [`pipeline`](pipeline/) | Data processing / ETL | Stream pipeline, map/filter/reduce, Collectors |
| [`validation`](validation/) | Validacija bez exception-a | Either pattern, Function composition, akumulacija grešaka |
| [`eventprocessing`](eventprocessing/) | Event sourcing | Immutable state, reduce/fold, sealed interface |
| [`builder`](builder/) | Konfiguracija i dekoratori | Function composition, andThen, UnaryOperator |

## Kako čitati

Svaki paket ima:
1. **README.md** — objašnjenje obrasca, zašto je FP pristup bolji, dijagrami
2. **Java fajlovi** — potpuno funkcionalni primeri koji se mogu pokrenuti

Predloženi redosled: `pipeline` → `validation` → `eventprocessing` → `builder`

---
