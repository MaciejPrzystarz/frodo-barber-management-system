                                                                 ## Frodo Barber Management System

Frodo Barber Management System to aplikacja webowa wspierająca zarządzanie salonem barberskim mojego brata.  
Klienci mogą założyć konto, przeglądać dostępne usługi i umawiać wizyty, a barberzy oraz administratorzy mają własne panele do zarządzania terminarzem i obsługą salonu.

Projekt działa w oparciu o **Spring Boot, Thymeleaf i MySQL**, czyli klasyczne podejście do aplikacji backendowych z widokami.  
Logowanie i role użytkowników obsługuje **Spring Security**.

Całość realizuję jako **projekt portfolio**, wykorzystując wiedzę zdobytą na studiach, kursach oraz z własnej pracy.

---

## Technologie

- **Java 21** – główny język aplikacji
- **Spring Boot** – fundament projektu i konfiguracja
- **Spring MVC** – obsługa warstwy webowej
- **Thymeleaf** – generowanie widoków po stronie serwera
- **Spring Data JPA** – komunikacja z bazą danych
- **MySQL / H2** – baza produkcyjna i baza developerska
- **Spring Security** – logowanie, role i ochrona zasobów
- **Lombok** – redukcja boilerplate’u
- **Maven** – zarządzanie zależnościami
- **Bootstrap** – podstawowe style i layout
- **AssertJ + JUnit** – testy jednostkowe i integracyjne

---

## Funkcjonalności

- Rejestracja i logowanie użytkowników
- Role użytkowników: **klient**, **barber**, **administrator**
- Przegląd dostępnych usług barberskich
- Umawianie wizyt przez klientów
- Panel barbera z listą wizyt i terminarzem
- Panel administratora do zarządzania usługami
- Podstawowa walidacja formularzy
- Bezpieczne przechowywanie haseł (**BCrypt**)
- Widoki oparte na **Thymeleaf + Bootstrap**

---

## Struktura katalogów

```text
frodo-barber-management-system
├── src
│   ├── main
│   │   ├── java
│   │   │   └── pl
│   │   │       └── frodo
│   │   │           └── frodobarber
│   │   │               ├── controller      # kontrolery MVC (AuthController, ServiceController itd.)
│   │   │               ├── dto             # obiekty DTO do komunikacji z widokami
│   │   │               ├── entity          # encje JPA (User, Service, Appointment...)
│   │   │               ├── repository      # interfejsy JPA Repository
│   │   │               ├── security        # konfiguracja Spring Security
│   │   │               ├── service         # logika biznesowa (UserService, AppointmentService...)
│   │   ├── resources
│   │   │   ├── static                      # CSS, JS, obrazki
│   │   │   ├── templates                   # widoki Thymeleaf (HTML)
│   │   │   ├── application.properties      # profil główny
│   │   │   ├── application-dev.properties  # profil dev (H2)
│   │   │   └── application-prod.properties # profil prod (MySQL)
│   └── test
│       └── java                            # testy jednostkowe i integracyjne
│
├── pom.xml                                 # zależności Maven
├── README.md                               # opis projektu
└── .gitignore                              # ignorowane pliki
