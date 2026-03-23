package raf.edu.week5.builder;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * Nedelja 5 — Builder i Decorator pattern kroz Function kompoziciju
 *
 * Demonstrira:
 *   1. Builder bez Builder klase — UnaryOperator kompozicija
 *   2. Reusable konfiguracije kao funkcije
 *   3. Conditional building
 *   4. Middleware/Decorator chain — Function<Handler, Handler>
 */
public class FunctionalBuilder {

    // =========================================================================
    // 1. HTTP Request Builder — bez Builder klase
    // =========================================================================

    record HttpRequest(
            String method,
            String url,
            Map<String, String> headers,
            Map<String, String> queryParams,
            String body,
            int timeoutMs
    ) {
        /** Početna konfiguracija */
        static HttpRequest prazni() {
            return new HttpRequest("GET", "", Map.of(), Map.of(), "", 30000);
        }

        // "With" metode — vraćaju NOVU instancu (immutable)
        HttpRequest withMethod(String method)    { return new HttpRequest(method, url, headers, queryParams, body, timeoutMs); }
        HttpRequest withUrl(String url)          { return new HttpRequest(method, url, headers, queryParams, body, timeoutMs); }
        HttpRequest withBody(String body)        { return new HttpRequest(method, url, headers, queryParams, body, timeoutMs); }
        HttpRequest withTimeout(int ms)          { return new HttpRequest(method, url, headers, queryParams, body, ms); }

        HttpRequest withHeader(String key, String value) {
            var noviHeaderi = new HashMap<>(headers);
            noviHeaderi.put(key, value);
            return new HttpRequest(method, url, Collections.unmodifiableMap(noviHeaderi), queryParams, body, timeoutMs);
        }

        HttpRequest withParam(String key, String value) {
            var noviParams = new HashMap<>(queryParams);
            noviParams.put(key, value);
            return new HttpRequest(method, url, headers, Collections.unmodifiableMap(noviParams), body, timeoutMs);
        }

        @Override
        public String toString() {
            return String.format("%s %s%s\n  Headers: %s\n  Body: %s\n  Timeout: %dms",
                    method, url,
                    queryParams.isEmpty() ? "" : "?" + queryParams.entrySet().stream()
                            .map(e -> e.getKey() + "=" + e.getValue())
                            .collect(Collectors.joining("&")),
                    headers, body.isEmpty() ? "(none)" : body, timeoutMs);
        }
    }

    // =========================================================================
    // Konfiguracije kao funkcije — reusable, composable
    // =========================================================================

    // Svaka konfiguracija je UnaryOperator<HttpRequest>: HttpRequest → HttpRequest

    static UnaryOperator<HttpRequest> get(String url) {
        return req -> req.withMethod("GET").withUrl(url);
    }

    static UnaryOperator<HttpRequest> post(String url) {
        return req -> req.withMethod("POST").withUrl(url);
    }

    static UnaryOperator<HttpRequest> withAuth(String token) {
        return req -> req.withHeader("Authorization", "Bearer " + token);
    }

    static UnaryOperator<HttpRequest> withJson() {
        return req -> req.withHeader("Content-Type", "application/json")
                .withHeader("Accept", "application/json");
    }

    static UnaryOperator<HttpRequest> withBody(String body) {
        return req -> req.withBody(body);
    }

    static UnaryOperator<HttpRequest> withTimeout(int ms) {
        return req -> req.withTimeout(ms);
    }

    static UnaryOperator<HttpRequest> withParam(String key, String value) {
        return req -> req.withParam(key, value);
    }

    // Conditional builder — primeni samo ako je uslov ispunjen
    static UnaryOperator<HttpRequest> when(boolean uslov, UnaryOperator<HttpRequest> config) {
        return uslov ? config : UnaryOperator.identity();
    }

    /**
     * Primeni listu konfiguracija redom — reduce ih u jednu.
     */
    @SafeVarargs
    static HttpRequest build(UnaryOperator<HttpRequest>... configs) {
        return Arrays.stream(configs)
                .reduce(UnaryOperator.identity(), (a, b) -> r -> b.apply(a.apply(r)))
                .apply(HttpRequest.prazni());
    }

    // =========================================================================
    // 2. Middleware / Decorator — Function<Handler, Handler>
    // =========================================================================

    /** Simulirani request/response */
    record Request(String path, Map<String, String> headers) {}
    record Response(int status, String body) {}

    /** Handler: obradi request, vrati response */
    @FunctionalInterface
    interface Handler {
        Response handle(Request request);
    }

    /** Middleware: prima handler, vraća "obogaćeni" handler */
    // To je Function<Handler, Handler> — klasičan decorator pattern

    static Function<Handler, Handler> logging() {
        return next -> request -> {
            System.out.println("    [LOG] " + request.path());
            Response response = next.handle(request);
            System.out.println("    [LOG] → " + response.status());
            return response;
        };
    }

    static Function<Handler, Handler> timing() {
        return next -> request -> {
            long start = System.nanoTime();
            Response response = next.handle(request);
            long elapsed = (System.nanoTime() - start) / 1_000_000;
            System.out.println("    [TIME] " + elapsed + "ms");
            return response;
        };
    }

    static Function<Handler, Handler> authCheck(String requiredToken) {
        return next -> request -> {
            String token = request.headers().getOrDefault("Authorization", "");
            if (!token.equals("Bearer " + requiredToken)) {
                System.out.println("    [AUTH] Unauthorized!");
                return new Response(401, "Unauthorized");
            }
            System.out.println("    [AUTH] OK");
            return next.handle(request);
        };
    }

    static Function<Handler, Handler> errorHandler() {
        return next -> request -> {
            try {
                return next.handle(request);
            } catch (Exception e) {
                System.out.println("    [ERROR] " + e.getMessage());
                return new Response(500, "Internal Server Error");
            }
        };
    }

    /**
     * Komponuj middleware-ove u lanac.
     * Redosled: prvi middleware je "najspoljniji" (prvi vidi request, poslednji vidi response).
     */
    @SafeVarargs
    static Handler applyMiddleware(Handler handler, Function<Handler, Handler>... middlewares) {
        // Primeni middleware-ove obrnutim redom (innermost first)
        return Arrays.stream(middlewares)
                .reduce(Function.identity(), Function::andThen)
                .apply(handler);
    }

    // =========================================================================
    // main
    // =========================================================================

    public static void main(String[] args) {
        System.out.println("=== 1. Functional Builder — HTTP Request ===\n");

        // Reusable konfiguracije
        UnaryOperator<HttpRequest> apiDefaults = req ->
                withJson().andThen(withAuth("my-token")).andThen(withTimeout(5000)).apply(req);

        // Prosti zahtevi — čitaju se kao specifikacija
        HttpRequest get1 = build(
                get("https://api.example.com/users"),
                withParam("page", "1"),
                withParam("limit", "20"),
                apiDefaults
        );
        System.out.println("GET zahtev:\n" + get1);

        System.out.println();

        HttpRequest post1 = build(
                post("https://api.example.com/users"),
                apiDefaults,
                withBody("{\"ime\": \"Ana\", \"email\": \"ana@test.rs\"}")
        );
        System.out.println("POST zahtev:\n" + post1);

        // Conditional building
        boolean debug = true;
        HttpRequest debugReq = build(
                get("https://api.example.com/status"),
                when(debug, withParam("verbose", "true")),
                when(debug, withTimeout(60000))
        );
        System.out.println("\nDebug zahtev:\n" + debugReq);

        // =========================================================================
        System.out.println("\n=== 2. Middleware Chain — Decorator Pattern ===\n");

        // "Biznis logika" — pravi handler
        Handler userHandler = request -> {
            System.out.println("    [HANDLER] Obrađujem " + request.path());
            return new Response(200, "{\"user\": \"Ana\"}");
        };

        // Komponuj middleware stack
        Handler fullStack = applyMiddleware(userHandler,
                errorHandler(),
                logging(),
                timing(),
                authCheck("secret-token")
        );

        // Zahtev sa validnim tokenom
        System.out.println("  Zahtev sa validnim tokenom:");
        var okRequest = new Request("/api/users/1",
                Map.of("Authorization", "Bearer secret-token"));
        Response r1 = fullStack.handle(okRequest);
        System.out.println("  Rezultat: " + r1.status() + " " + r1.body());

        System.out.println();

        // Zahtev bez tokena
        System.out.println("  Zahtev bez tokena:");
        var badRequest = new Request("/api/users/1", Map.of());
        Response r2 = fullStack.handle(badRequest);
        System.out.println("  Rezultat: " + r2.status() + " " + r2.body());

        // =========================================================================
        System.out.println("\n=== 3. Pipeline of Transformations ===\n");
        transformationPipeline();
    }

    // =========================================================================
    // 3. Pipeline transformacija — lista UnaryOperator-a
    // =========================================================================

    static void transformationPipeline() {
        // Transformacije teksta kao funkcije
        List<UnaryOperator<String>> transformacije = List.of(
                String::trim,
                String::toLowerCase,
                s -> s.replaceAll("\\s+", " "),       // višestruki razmaci → jedan
                s -> s.replaceAll("[^a-z0-9 ]", ""),  // ukloni specijalne karaktere
                s -> s.substring(0, Math.min(s.length(), 50))  // ograniči dužinu
        );

        // Komponuj SVE transformacije u jednu funkciju
        UnaryOperator<String> normalize = transformacije.stream()
                .reduce(UnaryOperator.identity(), (a, b) -> s -> b.apply(a.apply(s)));

        // Primeni na podatke
        List<String> ulaz = List.of(
                "  Hello   WORLD!!! ",
                "   FP je SUPER @#$% za Java   programere  ",
                "   VEOMA dugačak string koji treba da bude skraćen na pedeset karaktera maksimalno!!!"
        );

        System.out.println("Normalizacija stringova:");
        ulaz.stream()
                .map(s -> "  \"" + s + "\" → \"" + normalize.apply(s) + "\"")
                .forEach(System.out::println);
    }
}
