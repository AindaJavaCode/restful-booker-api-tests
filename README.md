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

## Bugs and API quirks found

While building this suite I found several places where restful-booker doesn't follow
HTTP conventions. The tests check the API's **actual** behaviour, so any change is
caught. Each item below is written up as a defect report.

| # | Area | Expected | Actual | Severity |
|---|------|----------|--------|----------|
| 1 | Missing booking on PUT / PATCH / DELETE | 404 Not Found | 405 Method Not Allowed | Medium |
| 2 | Missing required field on POST /booking | 400 Bad Request | 500 Internal Server Error | Medium |
| 3 | PUT /booking/{id} | Replaces the whole booking | Merges into it: omitted optional fields keep old values | Medium |
| 4 | Invalid credentials on POST /auth | 401 Unauthorized | 200 OK with `{"reason":"Bad credentials"}` | Medium |
| 5 | `Accept` header handling | Standard content negotiation | Exact string match; anything else returns 418 I'm a Teapot | Low |
| 6 | Successful DELETE | 200 OK or 204 No Content | 201 Created | Low |
| 7 | GET /ping health check | 200 OK | 201 Created | Low |

### Details

**1. 405 instead of 404 for a missing booking**
- **Steps:** `PUT`, `PATCH` or `DELETE` `/booking/2147483647` with a valid token
- **Expected:** `404 Not Found`, as `GET` correctly returns for the same ID
- **Actual:** `405 Method Not Allowed`
- **Impact:** 405 means the method isn't supported on this URL. Clients can't tell "booking doesn't exist" apart from "this operation isn't allowed", and the API is inconsistent with its own GET.

**2. 500 for invalid input on create**
- **Steps:** `POST /booking` with `firstname` omitted
- **Expected:** `400 Bad Request` with a message naming the missing field
- **Actual:** `500 Internal Server Error`
- **Impact:** a client error is reported as a server failure. This misleads clients, can trigger false alerts in monitoring, and gives no hint how to fix the request. PUT handles the same case correctly with 400.

**3. PUT merges instead of replacing**
- **Steps:** create a booking with `additionalneeds: "Breakfast"`, then `PUT` the booking without `additionalneeds`
- **Expected:** `additionalneeds` is removed, because PUT replaces the whole resource
- **Actual:** `"Breakfast"` is kept, because the server merges the new values into the stored booking
- **Impact:** clients can't clear optional fields, and PUT behaves like PATCH.

**4. 200 OK for failed login**
- **Steps:** `POST /auth` with a wrong password
- **Expected:** `401 Unauthorized`
- **Actual:** `200 OK` with `{"reason":"Bad credentials"}`
- **Impact:** clients and monitoring that rely on status codes treat failed logins as successes. The suite checks for a token in the body, not just the status, because of this.

**5. `Accept` must match exactly**
- **Steps:** `POST /booking` with `Accept: application/json, text/json`, which is valid HTTP and is REST Assured's default for JSON
- **Expected:** a JSON response
- **Actual:** `418 I'm a Teapot`
- **Impact:** standard HTTP clients fail by default. I found this while building the framework, and it's why `Specs` sets `Accept` to exactly `application/json`.

**6 and 7. 201 Created for DELETE and /ping**
- 201 means a new resource was created, but neither of these requests creates anything. Clients that check for 200 or 204 will report failures.
