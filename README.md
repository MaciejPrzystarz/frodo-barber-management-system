# Frodo Barber Management System

Aplikacja webowa do zarządzania rezerwacjami w salonie barberskim. Klienci rezerwują wizyty online, barberzy zarządzają terminarzem i urlopami, a administrator obsługuje użytkowników i usługi. Backend oparty o Spring Boot, warstwa widoku w Thymeleaf, baza PostgreSQL (prod) lub H2 (dev).

Projekt typu MVP / portfolio, przygotowany pod deployment w kontenerze.

---

## Live demo

- **Aplikacja:** https://frodo-barber-management-system.onrender.com
- **Konta demo:**
  - Admin - `admin1@demo.pl` / `admin123`
  - Barber - `barber1@demo.pl` / `barber123`
  - Klient - `client1@demo.pl` / `client123`

> Instancja na Renderze (free tier) usypia po okresie bezczynności - pierwsze żądanie po przerwie może uruchamiać się kilkadziesiąt sekund.

---

## Funkcjonalności

- Rejestracja i logowanie użytkowników (Spring Security, hasła hashowane BCryptem)
- Trzy role z osobnymi widokami: `CLIENT`, `BARBER`, `ADMIN`
- Rezerwacja wizyt przez klienta z walidacją terminu i dostępności slotów
- Statusy wizyt: `PENDING`, `BOOKED`, `DONE`, `CANCELLED`, `REJECTED`, `DIDNT_SHOW_UP`
- Generowanie dostępnych slotów na podstawie godzin pracy, czasu trwania usługi i istniejących rezerwacji
- Panel barbera: lista wizyt oczekujących, dodawanie wizyty dla istniejącego/nowego klienta, zgłaszanie urlopu
- Tygodniowe podsumowanie pracy barbera (liczba zrealizowanych wizyt, przepracowane godziny, przychód, najlepszy dzień tygodnia)
- Panel admina: - zarządzanie użytkownikami i usługami
- Profil dev z bazą H2 i seed danych (`data-dev.sql`), profil prod z PostgreSQL
- Dockerfile (multi-stage build) gotowy pod deployment

---

## Stack

- **Java 21**
- **Spring Boot 4.0.1** - Spring MVC, Spring Data JPA, Spring Security, Validation
- **Thymeleaf** + `thymeleaf-extras-springsecurity6` - widoki SSR
- **PostgreSQL** (prod) / **H2** (dev, test)
- **Bootstrap** + własne style CSS
- **Lombok**
- **Maven** + plugin Checkstyle (faza `validate`)
- **JUnit 5 + Mockito + AssertJ** - testy jednostkowe wybranych reguł biznesowych w warstwie serwisowej
- **Docker** (multi-stage: `maven:3.9-eclipse-temurin-21` → `eclipse-temurin:21-jre`)

---

## Najważniejsze reguły biznesowe

Logika rezerwacji znajduje się w `BookingService`:

- **Pierwsza wizyta klienta zapisywana jest ze statusem `PENDING`** - wymaga akceptacji barbera. Kolejne (po realizacji pierwszej) trafiają od razu jako `BOOKED`.
- **Aktywne wizyty (`BOOKED`, `PENDING`) blokują sloty czasowe** w terminarzu barbera.
- Wizyty w stanie `CANCELLED`, `REJECTED`, `DONE`, `DIDNT_SHOW_UP` **nie blokują dostępności** - czas znów jest wolny.
- Klient może mieć maksymalnie **3 aktywne wizyty** jednocześnie.
- Pomiędzy aktywnymi wizytami klienta wymagany jest odstęp **co najmniej 10 dni**.
- Wizytę można umówić **najwcześniej 60 minut** od bieżącej chwili i **najpóźniej 45 dni** naprzód; rezerwacje wstecz są odrzucane.
- Jeżeli klient ma wizytę w stanie `PENDING`, nie może utworzyć kolejnej do czasu jej akceptacji.
- **Urlop barbera blokuje rezerwacje** w danych dniach (`VacationService.isBarberOnVacation`).
- Sloty generowane są w przedziale **08:00-18:00** w 10-minutowych krokach, z uwzględnieniem czasu trwania wybranej usługi.

> **Założenie MVP:** rezerwacja po stronie klienta obsługuje obecnie **jednego barbera** (`findFirstByRole(BARBER)`). Obsługa wielu barberów (wybór barbera przy rezerwacji) jest planowana.

---

## Struktura projektu

```text
src
├── main
│   ├── java/pl/frodo/barber
│   │   ├── controller     # AuthController, HomeController, ClientController, BarberController, AdminController
│   │   ├── dto            # formularze i widokowe DTO (UserFormDto, MyWeekDto, VacationFormDto, ...)
│   │   ├── model          # encje JPA: User, Customer, ServiceItem, Appointment, Vacation, enumy Role / AppointmentStatus
│   │   ├── repository     # Spring Data JPA repositories
│   │   ├── security       # SecurityConfig, DbUserDetailsService
│   │   └── service        # AuthService, BookingService, AppointmentService, VacationService, WeeklyStatsService
│   └── resources
│       ├── static/        # CSS, obrazy
│       ├── templates/     # widoki Thymeleaf (auth, admin, barber, client, fragments)
│       ├── application.properties        # ustawienia wspólne (aktywuje profil dev)
│       ├── application-dev.properties    # H2 + seed data
│       ├── application-prod.properties   # PostgreSQL via env vars
│       ├── application-test.properties   # H2 w trybie PostgreSQL dla testów
│       ├── data-dev.sql                  # dane developerskie
│       └── data-demo-postgres.sql        # dane demo dla profilu prod (zasiew ręczny)
├── test/java/pl/frodo/barber/service     # unit testy: Booking, Appointment, Vacation, WeeklyStats
├── Dockerfile
├── checkstyle.xml
└── pom.xml
```

---

## Uruchomienie lokalne

Wymagania: **JDK 21**, **Maven 3.9+** (lub wrapper).

### Profil `dev` (domyślny) - H2 in-memory

```bash
mvn spring-boot:run
```

- Aplikacja: `http://localhost:8080`
- Konsola H2: `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:mem:testdb`
  - Użytkownik: `sa`, hasło: puste
- Dane testowe ładowane z `src/main/resources/data-dev.sql`.

### Profil `prod` - PostgreSQL

Wymagane zmienne środowiskowe:

```
DB_URL=jdbc:postgresql://<host>:<port>/<database>
DB_USERNAME=<user>
DB_PASSWORD=<password>
PORT=8080            # opcjonalne, domyślnie 8080
```

Uruchomienie:

```bash
mvn clean package -DskipTests
java -Dspring.profiles.active=prod -jar target/frodo-barber-management-system-0.0.1-SNAPSHOT.jar
```

W profilu prod `ddl-auto=update` - schemat zarządzany przez Hibernate (planowana migracja na Flyway/Liquibase, patrz „Status projektu”).

### Docker

```bash
docker build -t frodo-barber .
docker run --rm -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/frodo \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=postgres \
  frodo-barber
```

---

## Testy

```bash
mvn test
```

- **JUnit 5 + Mockito + AssertJ** - unit testy wybranych reguł biznesowych w warstwie serwisowej (`BookingServiceTest`, `AppointmentServiceTest`, `VacationServiceTest`, `WeeklyStatsServiceTest`).
- Repozytoria są mockowane - testy nie wymagają działającej bazy.
- Dodatkowo jeden test podnosi kontekst Springa (`FrodoBarberManagementSystemApplicationTests`) - używa profilu `test` (H2 w trybie PostgreSQL).
- Testy integracyjne end-to-end są w planach (patrz „Status projektu”).

---

## Deployment

- Multi-stage Dockerfile: build w obrazie `maven:3.9-eclipse-temurin-21`, runtime w `eclipse-temurin:21-jre`.
- Aplikacja nasłuchuje na porcie z `${PORT:8080}` zgodne z konwencją platform typu Render/Heroku
- W profilu prod ustawione `server.forward-headers-strategy=framework` oraz `spring.jpa.open-in-view=false`.
- Konsola H2 wyłączona w prod.

---

## Status projektu

- **MVP / portfolio project** - aplikacja przygotowana pod deployment, zawiera kluczową logikę domenową i podstawowe testy jednostkowe.
- Stabilne: rezerwacje, role, statusy wizyt, urlopy, podsumowania tygodniowe, dwuprofilowa konfiguracja bazy.
- Planowane:
  - powiadomienia mailowe (potwierdzenie/odrzucenie wizyty),
  - lepszy widok kalendarza dla barbera i klienta,
  - więcej testów - w szczególności **testy integracyjne** (Spring Boot Test + Testcontainers/PostgreSQL),
  - migracje schematu przez **Flyway** lub **Liquibase** zamiast `ddl-auto=update`,
  - dalsze porządkowanie kodu i rozbudowa walidacji biznesowej
