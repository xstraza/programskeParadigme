package raf.edu.week3;

import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.stream.IntStream;

/**
 * Nedelja 3 — reduce: uopštena operacija svođenja (fold)
 *
 * reduce uzima kolekciju i "sažima" je u jednu vrednost.
 * sum, count, max, min su SPECIJALNI SLUČAJEVI operacije reduce.
 *
 * Matematički:
 *   reduce([a, b, c, d], f) = f(f(f(a, b), c), d)
 *
 * Tri potpisa:
 *   1. T reduce(T identity, BinaryOperator<T> op)       → T
 *   2. Optional<T> reduce(BinaryOperator<T> op)         → Optional<T>
 *   3. <U> U reduce(U identity, BiFunction<U,T,U> acc, BinaryOperator<U> combiner) → U
 *
 * BinaryOperator<T> je: (T, T) → T  (extend-uje BiFunction<T,T,T>)
 */
public class ReducePrimeri {

    public static void main(String[] args) {
        System.out.println("=== reduce sa identity (varijanta 1) ===\n");
        reduceSeIdentity();

        System.out.println("\n=== reduce bez identity (varijanta 2) ===\n");
        reduceBezIdentity();

        System.out.println("\n=== reduce je osnova svih agregacija ===\n");
        reduceKaoAggregation();

        System.out.println("\n=== Nestandardne reduce operacije ===\n");
        nestandardneReduce();

        System.out.println("\n=== Redosled je bitan (nekomutativne operacije) ===\n");
        redosledJeBitan();

        System.out.println("\n=== reduce za niz reči ===\n");
        stringReduce();
    }

    // -----------------------------------------------------------------------
    // Varijanta 1: sa identity
    // T reduce(T identity, BinaryOperator<T> accumulator)
    //
    // identity — neutralni element operacije:
    //   sabiranje   → 0    (0 + x = x)
    //   množenje    → 1    (1 * x = x)
    //   konkatenacija → "" ("" + s = s)
    //   AND         → true
    //   OR          → false
    // -----------------------------------------------------------------------
    static void reduceSeIdentity() {
        List<Integer> brojevi = List.of(1, 2, 3, 4, 5);

        // Sabiranje — identity je 0
        // Tok: 0 + 1 = 1; 1 + 2 = 3; 3 + 3 = 6; 6 + 4 = 10; 10 + 5 = 15
        int suma = brojevi.stream().reduce(0, (akumulator, element) -> akumulator + element);
        System.out.println("Suma [1..5] sa reduce: " + suma);

        // Isto, ali sa method reference
        int suma2 = brojevi.stream().reduce(0, Integer::sum);
        System.out.println("Suma sa Integer::sum: " + suma2);

        // Množenje — identity je 1
        // 1 * 1 = 1; 1 * 2 = 2; 2 * 3 = 6; 6 * 4 = 24; 24 * 5 = 120
        int proizvod = brojevi.stream().reduce(1, (a, b) -> a * b);
        System.out.println("Proizvod [1..5]: " + proizvod);

        // Sa identity vrednoscu — na praznom streamu vraca identity
        int sumaaPraznog = List.<Integer>of().stream().reduce(0, Integer::sum);
        System.out.println("Suma prazne liste (identity): " + sumaaPraznog); // 0
    }

    // -----------------------------------------------------------------------
    // Varijanta 2: bez identity — vraća Optional
    // Optional<T> reduce(BinaryOperator<T> op)
    //
    // Nema identity → na praznom streamu nema rezultata → Optional
    // -----------------------------------------------------------------------
    static void reduceBezIdentity() {
        List<Integer> brojevi = List.of(3, 7, 1, 9, 4, 6);
        List<Integer> prazan = List.of();

        // Maksimum — nema prirodnog neutralnog elementa
        Optional<Integer> max = brojevi.stream().reduce((a, b) -> a > b ? a : b);
        System.out.println("Max " + brojevi + ": " + max.orElse(-1));

        // Minimum
        Optional<Integer> min = brojevi.stream().reduce((a, b) -> a < b ? a : b);
        System.out.println("Min " + brojevi + ": " + min.orElse(-1));

        // Prazan stream — Optional je prazan
        Optional<Integer> maxPrazan = prazan.stream().reduce((a, b) -> a > b ? a : b);
        System.out.println("Max praznog: " + maxPrazan); // Optional.empty
        System.out.println("Max praznog sa orElse: " + maxPrazan.orElse(-1));
    }

    // -----------------------------------------------------------------------
    // reduce kao osnova svake agregacijske operacije
    // Veza između sum/count/max i reduce
    // -----------------------------------------------------------------------
    static void reduceKaoAggregation() {
        List<Integer> brojevi = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        // sum() = reduce(0, Integer::sum)
        int sumReduce = brojevi.stream().reduce(0, Integer::sum);
        int sumDirektno = brojevi.stream().mapToInt(Integer::intValue).sum();
        System.out.println("sum via reduce   : " + sumReduce);
        System.out.println("sum direktno     : " + sumDirektno);

        // count() — broji elemente; svaki element doprinosi 1
        long countReduce = (long) brojevi.stream().reduce(0, (acc, x) -> acc + 1);
        long countDirektno = brojevi.stream().count();
        System.out.println("count via reduce : " + countReduce);
        System.out.println("count direktno   : " + countDirektno);

        // max()
        Optional<Integer> maxReduce = brojevi.stream().reduce((a, b) -> a > b ? a : b);
        Optional<Integer> maxDirektno = brojevi.stream().max(Integer::compareTo);
        System.out.println("max via reduce   : " + maxReduce.orElseThrow());
        System.out.println("max direktno     : " + maxDirektno.orElseThrow());

        // Svoda na prosek — malo složenije, ali moguće
        // Treba pratiti i sumu i broj elemenata
        int[] sumCount = brojevi.stream()
                .reduce(new int[]{0, 0},
                        (acc, x) -> new int[]{acc[0] + x, acc[1] + 1},
                        (a, b)   -> new int[]{a[0] + b[0], a[1] + b[1]});
        double prosek = (double) sumCount[0] / sumCount[1];
        System.out.printf("prosek via reduce: %.1f%n", prosek);
    }

    // -----------------------------------------------------------------------
    // Nestandardne reduce operacije
    // reduce nije samo za matematiku — može biti bilo šta
    // -----------------------------------------------------------------------
    static void nestandardneReduce() {
        // Traženje najduže reči
        List<String> reci = List.of("java", "funkcionalno", "lambda", "stream", "programiranje");
        Optional<String> najduza = reci.stream()
                .reduce((a, b) -> a.length() >= b.length() ? a : b);
        System.out.println("Najduža reč: " + najduza.orElse("nema"));

        // Množenje samo parnih — kombinacija filter + reduce
        int proizvodParnihDo10 = IntStream.rangeClosed(1, 10)
                .filter(n -> n % 2 == 0)
                .reduce(1, (a, b) -> a * b);
        System.out.println("Proizvod parnih 1..10: " + proizvodParnihDo10); // 2*4*6*8*10 = 3840

        // Faktoriijel sa reduce
        int n = 6;
        int faktorijel = IntStream.rangeClosed(1, n).reduce(1, (a, b) -> a * b);
        System.out.println(n + "! = " + faktorijel); // 720

        // BinaryOperator se može definisati van pipeline-a i ponovo koristiti
        BinaryOperator<Integer> maxOp = (a, b) -> a > b ? a : b;
        BinaryOperator<Integer> minOp = (a, b) -> a < b ? a : b;

        List<Integer> podaci = List.of(15, 3, 42, 8, 27);
        System.out.println("Max: " + podaci.stream().reduce(Integer.MIN_VALUE, maxOp));
        System.out.println("Min: " + podaci.stream().reduce(Integer.MAX_VALUE, minOp));
    }

    // -----------------------------------------------------------------------
    // Redosled je bitan za nekomutativne operacije
    // reduce garantuje levo-asocijativnost na sekvencijalnom streamu
    // -----------------------------------------------------------------------
    static void redosledJeBitan() {
        // Oduzimanje NIJE komutativno: (((10-1)-2)-3) ≠ (((3-2)-1)-10)
        List<Integer> brojevi = List.of(10, 1, 2, 3);

        // Bez identity — levo-asocijativno: ((10-1)-2)-3 = 4
        Optional<Integer> rezultat = brojevi.stream()
                .reduce((a, b) -> {
                    System.out.println("  " + a + " - " + b + " = " + (a - b));
                    return a - b;
                });
        System.out.println("Rezultat oduzimanja: " + rezultat.orElse(0));
        // Korak po korak: 10-1=9, 9-2=7, 7-3=4

        // Napomena o paralelnim streamovima:
        // Za paralelni stream, reduce mora biti KOMUTATIVNA i ASOCIJATIVNA operacija!
        // Oduzimanje nije asocijativno: (a-b)-c ≠ a-(b-c)
        // Za paralelizaciju koristite samo: +, *, max, min, i slične
    }

    // -----------------------------------------------------------------------
    // reduce na Stringovima
    // -----------------------------------------------------------------------
    static void stringReduce() {
        List<String> reci = List.of("Funkcionalno", "programiranje", "je", "moćno");

        // Konkatenacija sa reduce
        String spojena = reci.stream().reduce("", (a, b) -> a + (a.isEmpty() ? "" : " ") + b);
        System.out.println("Spojena rečenica: " + spojena);

        // Bolje: koristiti Collectors.joining (optimizovano)
        String spojena2 = reci.stream().collect(java.util.stream.Collectors.joining(" "));
        System.out.println("Collectors.joining: " + spojena2);

        // Abecedni minimum (reč koja dolazi prva)
        Optional<String> abecedniMin = reci.stream()
                .map(String::toLowerCase)
                .reduce((a, b) -> a.compareTo(b) <= 0 ? a : b);
        System.out.println("Abecedni minimum: " + abecedniMin.orElse(""));
    }
}
