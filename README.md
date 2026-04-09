Football App is an application for organizing matches. full-stack application designed to manage bookings, user memberships, and reservation schedules.

## 🛠️ Tech Stack

- **Backend:** Java 25, Spring Boot 4.x, Spring Security (JWT)
- **Database:** PostgreSQL, Flyway (Database Migration), H2 for isolated tests
- **Documentation:** Swagger / OpenAPI 3.0
- **Key Tools:** Lombok, Validation, Pagination, Sorting, Scheduled Tasks

## 📂 Project Structure

- `backend/`: Spring Boot core application.
  - `config`: Configuration classes (Security, OpenAPI, Bean definitions).
  - `controller`: REST API endpoints.
  - `dto`: Data Transfer Objects for requests and responses.
  - `entity`: JPA/Hibernate database models.
  - `exception`: Custom domain exceptions (sealed classes) and message types.
  - `handler`: Global exception handler and generic API error wrappers.
  - `repository`: Data access layer (Spring Data JPA).
  - `service`: Business logic implementation.
- `frontend/`: React + TypeScript application.

## 🚀 Getting Started

1. Configure your PostgreSQL settings in `backend/src/main/resources/application.yaml` if your local database differs from the defaults.
2. Create `backend/.env` from `backend/.env.example`.
   Required keys are `PGUSER`, `PGPASSWORD`, and `JWT_SECRET`.
   `JWT_SECRET` must be a Base64-encoded signing key. `JWT_EXPIRATION` is optional.
3. Run `./mvnw spring-boot:run` in the backend directory.
4. Run `./mvnw test` in `backend/` to execute the isolated test suite against the H2 test profile.

## 🔐 Reservation And Auth Safeguards

- Reservation detail and cancellation access is restricted to the owner or an admin.
- Reservation creation now rejects past bookings, invalid time ranges, and overlapping active bookings on the same pitch.
- Partial-hour reservations are priced proportionally instead of being rounded down to zero.
- JWT signing no longer falls back to a hard-coded secret; startup now requires an explicit `JWT_SECRET`.

## 🗺️ Project Status

For detailed development phases and setup instructions, please refer to the [ROADMAP.md](./ROADMAP.md) file.
