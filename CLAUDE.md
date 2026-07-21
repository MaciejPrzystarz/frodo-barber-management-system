# CLAUDE.md — Frodo Barber Management System

## Project overview

Frodo Barber Management System is a portfolio web application built as a realistic barber shop management system.

The project is intended to demonstrate Junior Java Developer skills with a clean, understandable Spring Boot MVC architecture.

The application should stay junior-friendly, readable and practical. Do not over-engineer the code.

## Main goal

The goal is to build a realistic but not overcomplicated system for:

- booking appointments,
- managing clients,
- managing barbers,
- managing services,
- managing appointment statuses,
- handling barber vacations,
- showing simple dashboards and statistics.

The project should look like something that could be shown during a Junior Java Developer interview.

## Tech stack

Use and respect the current stack:

- Java 21
- Spring Boot
- Spring MVC
- Spring Security
- Spring Data JPA / Hibernate
- Thymeleaf
- Maven
- H2 for local development
- PostgreSQL for production
- Render deployment
- GitHub Actions / CI
- HTML / CSS / minimal JavaScript only when useful

Do not propose React, Angular, Vue, microservices, Kafka, Redis, cloud architecture or advanced enterprise patterns unless explicitly asked.

## User roles

The application has three main roles:

- ADMIN
- BARBER
- CLIENT

Each role should have access only to its own area.

### ADMIN

Admin should manage the system:

- dashboard overview,
- barbers,
- clients/customers,
- services,
- appointments,
- vacations,
- basic salon settings if needed.

### BARBER

Barber should manage barber-side work:

- own appointments,
- appointment statuses,
- weekly statistics,
- vacations,
- adding appointments for existing or new non-registered customers.

### CLIENT

Client should be able to:

- view dashboard,
- select service,
- view available slots,
- book appointments,
- see own appointments,
- see barber vacation notices when relevant.

## Domain context

The core business domain is a barber shop appointment system.

Important existing concepts:

- appointments,
- users,
- customers,
- services,
- vacations,
- appointment statuses,
- barber weekly statistics.

Appointment statuses may include:

- BOOKED
- PENDING
- REJECTED
- CANCELLED
- DONE
- DIDNT_SHOW_UP

Do not rename existing statuses unless there is a strong reason.

## Important booking rules

When working on booking logic, keep these business rules in mind:

1. A client can have max 3 future appointments.
2. The first appointment from a client may become PENDING.
3. There is a 10-day rule for client bookings.
4. Cancelled and rejected appointments should usually not block future bookings.
5. Appointments cannot be booked in the past.
6. Appointments should not be booked too close to the current time.
7. Appointments should not be booked more than 45 days in advance.
8. Barber vacations should block booking slots.
9. Appointment overlaps must be prevented.

Overlap rule:
Default working hours used in the project:

08:00-18:00

Default slot step:

10 minutes
Architecture style

Keep the architecture simple and junior-friendly.

Prefer:

Controllers for HTTP and MVC concerns.
Services for business logic.
Repositories for database queries.
DTOs/forms only where they actually simplify the code.
Thymeleaf templates for views.
Simple validation methods returning clear results.

Avoid:

overusing interfaces for every service,
unnecessary abstractions,
complex generic helpers,
overcomplicated DTO mapping,
advanced DDD,
event-driven architecture,
moving too much logic into controllers,
putting RedirectAttributes inside services,
large God services.
Service guidelines

Services should not be responsible for MVC details.

Avoid this in services:

RedirectAttributes redirectAttributes
Model model
HttpServletRequest request

Prefer returning:

Optional<String>
boolean
List<Something>
SomethingDto

or a simple result object if really needed.

Controllers should decide:

redirect path,
flash messages,
model attributes,
view names.
Suggested service responsibilities
BookingService

Should focus on client booking use cases:

validating client booking rules,
checking appointment limits,
generating available slots,
checking overlapping appointments,
saving client appointments.
AppointmentService

Should focus on barber/admin appointment management:

adding appointment for existing customer,
adding appointment for new non-registered customer,
changing appointment status,
validating appointment slot,
checking vacation conflicts,
managing appointment lists.
WeeklyStatsService

Should focus only on weekly statistics:

current week range,
DONE appointments only,
total duration,
realized revenue,
formatting week labels,
returning data for barber my-week view.

Do not put weekly stats inside BookingService.

Controller guidelines

Controllers should be thin and readable.

A good controller method should:

receive request data,
call service,
handle validation result,
add model attributes or flash messages,
return view or redirect.

Avoid putting business rules directly in controllers.

Thymeleaf guidelines

Use Thymeleaf in a simple and readable way.

Prefer:

clear template names,
shared fragments for navbar/footer if already used,
readable conditionals,
clear form validation messages,
simple tables/cards.

Avoid:

very complex expressions in templates,
duplicated huge HTML sections,
logic that should belong in Java services.
CSS / UI guidelines

The UI should look clean, modern and usable but not overdesigned.

Prefer:

readable layouts,
good spacing,
responsive design,
simple cards/tables/forms,
clear CTA buttons,
useful empty states,
meaningful page titles.

Avoid:

massive CSS bloat,
excessive blur filters,
unnecessary animations,
heavy JavaScript,
complicated frontend frameworks.

If CSS is too large, suggest safe refactoring into smaller files, but do not rewrite the whole UI without asking.

Admin panel direction

The admin panel should be realistic but not oversized.

Recommended admin pages:

/admin/dashboard
/admin/barbers
/admin/customers
/admin/services
/admin/appointments
/admin/vacations
Admin dashboard should show:
number of upcoming appointments,
number of today's appointments,
number of clients/customers,
number of barbers,
simple revenue/statistics from DONE appointments,
recent appointments,
quick links to important admin sections.
Admin barbers page should allow:
listing barbers,
adding barber,
editing barber basic data,
activating/deactivating barber if supported,
viewing barber appointments or vacations.
Admin customers page should allow:
listing customers,
searching by name, surname, phone or email,
viewing customer appointment history,
adding/editing customer if needed.
Admin services page should allow:
listing services,
adding services,
editing name, price and duration,
disabling services instead of hard deleting if appointments exist.
Admin appointments page should allow:
listing all appointments,
filtering by date, barber, status, customer,
changing appointment status,
adding appointment manually,
viewing appointment details.
Admin vacations page should allow:
listing barber vacations,
adding vacation,
editing/removing vacation,
preventing appointment booking during vacation.

Avoid advanced admin features unless asked:

online payments,
invoices,
SMS reminders,
email campaigns,
advanced CRM,
multi-branch salon management,
complex work schedules,
calendar drag-and-drop.
Database and data guidelines

Respect existing entities and relationships.

Before proposing a new entity, check if the existing model can handle the use case.

Only propose new fields/entities when they are really useful for MVP.

Prefer simple field names.

For vacation dates, prefer:

startDate
endDate

rather than confusing names like:

startTime
endTime

when the field stores LocalDate.

Be careful with migrations and existing data.sql.

Profiles and environments

The project uses separate development and production behavior.

Typical setup:

H2 for local development
PostgreSQL for production
Render deployment
environment variables for production database

Production database variables may include:

DB_URL
DB_USERNAME
DB_PASSWORD

Do not break existing profile configuration.

Do not remove demo data without asking.

Demo and portfolio requirements

This project is for job interviews, so it should be easy to understand and easy to run.

The README should include:

short project description,
tech stack,
main features,
roles,
demo credentials,
local run instructions,
database/profile information,
deployed link if available,
screenshots if available.

Avoid hiding important project behavior in unclear code.

Testing direction

When adding or changing business rules, suggest simple tests where reasonable.

Good test areas:

booking validation,
overlap detection,
vacation blocking,
appointment status changes,
weekly statistics,
service validation.

Keep tests understandable for a junior project.

Checkstyle / CI

If Checkstyle is used, keep code formatted consistently.

Do not add a Checkstyle plugin configuration unless the project already supports it or the user asks.

If CI exists, do not break it.

Typical CI expectations:

build project,
run tests,
optionally run checkstyle,
package application.
Git and change style

When suggesting changes, group them logically.

Prefer commit messages like:

feat: add admin appointment management
feat: add barber vacation validation
fix: ignore cancelled appointments in booking limit
refactor: move weekly stats to dedicated service
test: add booking validation tests
docs: update README with demo credentials

Do not suggest huge unrelated changes in one commit.

Response style for Claude

When analyzing the project, respond in a practical way.

Use this structure when possible:

What I found
What is good
What should be improved
Risk / bug potential
Junior-friendly recommendation
Concrete implementation plan
Files likely affected
Suggested commit message

Do not immediately rewrite large parts of the project unless explicitly asked.

When asked to implement something:

first inspect related files,
explain the smallest safe change,
then modify only necessary files,
keep existing naming and style where reasonable.
Important rule

This is a Junior Java Developer portfolio project.

Make the project better, cleaner and more realistic, but do not make it look like an overengineered senior enterprise system.

Prefer simple, clear, working code over clever code.

```java
newStart.isBefore(existingEnd) && newEnd.isAfter(existingStart)