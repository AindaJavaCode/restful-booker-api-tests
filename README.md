![API Tests](https://github.com/AindaJavaCode/restful-booker-api-tests/actions/workflows/api-tests.yml/badge.svg)

# Restful Booker API Tests

REST API test suite for https://restful-booker.herokuapp.com, built with Java 17+, Maven, TestNG and REST Assured.

## Running

```bash
mvn test                      # full regression suite (parallel by class)
mvn test -Dsuite=smoke        # only tests in the "smoke" group
mvn test -Dbase.url=http://localhost:3001 -Dlog.all=true   # override any config key
```

Config lives in `src/main/resources/config.properties`. Each key can be overridden by a system
property (`-Dbase.url=...`) or an environment variable (`BASE_URL=...`).
Reports are written to `target/surefire-reports/`.

## Layout

```
src/main/java/com/restfulbooker/
  config/   Config            - layered config lookup
  client/   Specs             - shared request spec (base URI, JSON headers, timeouts, logging)
            *Client           - one class per API area; methods return raw Response
  models/   records           - request/response POJOs (Jackson)
  data/     BookingFactory    - random valid test data (Datafaker)
src/test/java/com/restfulbooker/
  base/     BaseTest          - clients, cached token, auto-cleanup of created bookings
  tests/    *Test             - test classes
src/test/resources/
  schemas/  JSON schemas for contract checks
  suites/   TestNG suite files (regression.xml, smoke.xml)
```

## Writing a test

Extend `BaseTest`, use `createBooking()` so the booking is removed after the class, and tag
quick, critical checks with `groups = "smoke"`.

## API quirks to know

- `Accept: application/json` is required or `POST /booking` returns 418 (set in `Specs`).
- `/ping` and `DELETE /booking/{id}` return **201**, not 200/204.
- Bad credentials on `/auth` return **200** with `{"reason": "Bad credentials"}`.
- Write operations without a valid token return **403**.
