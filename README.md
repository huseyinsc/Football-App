Football App is an application for organizing matches. full-stack application designed to manage bookings, user memberships, and reservation schedules.

## 🛠️ Tech Stack

- **Backend:** Java 25, Spring Boot 4.x, Spring Security (JWT)
- **Database:** PostgreSQL, Flyway (Database Migration), H2 for isolated tests
- **Documentation:** Swagger / OpenAPI 3.0
- **Key Tools:** Lombok, Validation, Pagination, Sorting, Scheduled Tasks

## 📂 Project Structure

- `backend/`: Spring Boot core application.
    - `config`: Configuration classes (Security, OpenAPI, Bean definitions).
    - `controller`: REST API endpoints (Auth, Reservations, Pitches, Users, Contacts, Match-Requests).
    - `dto`: Data Transfer Objects for requests and responses.
    - `entity`: JPA/Hibernate database models (User, Pitch, Reservation, UserContact, MatchRequest, UserBlock, ContactStrike).
    - `exception`: Custom domain exceptions (sealed classes) and message types.
    - `handler`: Global exception handler and generic API error wrappers.
    - `repository`: Data access layer (Spring Data JPA).
    - `service`: Business logic implementation.
- `frontend/`: React + TypeScript application.

## 🏆 v1.2.0 Social & Match Management Highlights

- **Friends & Contacts Network:** Users can now build their personal football network by sending and accepting friend requests. Contacts are bidirectional (linking both users automatically).
- **Anti-Spam "2-Strike" System:** To prevent spam, if a user's friend requests are rejected or ignored twice, an automatic block is triggered via `FriendRequestCleanupJob`.
- **Match Join Requests:** Reservations now support a decoupled joining flow. Users request to join, and organizers approve them.
- **Organizer Invites:** Organizers can directly invite their friends to join a match for faster team building.
- **Dynamic Join Policies:** Reservations support `PUBLIC`, `FRIENDS_ONLY` (only contacts can join), or `INVITE_ONLY` participation models.
- **Enhanced Global Paging:** Full pagination support for User Search and all social list views (Contacts, Invites, Requests).
- **Premium Swagger Documentation:** Highly informative error examples with specific `MessageType` codes (e.g., 1001, 1007, 1009) to streamline frontend development.

## 🏆 v1.1.0 Release Highlights

- **Multi-User Reservations:** Support for team bookings where multiple players can join the same pitch reservation (`POST /reservations/{id}/join`).
- **Enhanced Domain Model:** Proper JPA relationships with ReservationParticipant junction entity.
- **Complete Update Coverage:** Added PUT endpoints for Pitches, Users, and Reservations (now including Pitch and Organizer modification).
- **Scheduled Auto-Cleanup:** `ReservationCleanupJob` automatically purges expired/completed reservations older than 30 days to save DB space.
- **Improved Validation:** Jackson JSON parsing failures (like extra commas or unknown fields) cleanly map to HTTP 400 Bad Request via `GlobalExceptionHandler`.
- **Initial Seed Data:** Flyway migration automatically generates an initial Admin user and dummy pitches.
- **Strong Password Validation:** Enforced complexity requirements for user registration.
- **Professional Swagger Documentation:** Clean error responses with examples and proper schema annotations.
- **Production-Ready Error Handling:** Consistent ApiError responses across all endpoints with proper logging.

## 🚀 Getting Started

1. Configure your PostgreSQL settings in `backend/src/main/resources/application.yaml` if your local database differs from the defaults.
2. Create `backend/.env` from `backend/.env.example`.
   Required keys are `PGUSER`, `PGPASSWORD`, and `JWT_SECRET`.
   `JWT_SECRET` must be a Base64-encoded signing key. `JWT_EXPIRATION` is optional.
3. Run `./mvnw spring-boot:run` in the backend directory.
4. Run `./mvnw test` in `backend/` to execute the isolated test suite against the H2 test profile.

## 📘 API Documentation

Swagger/OpenAPI is available locally after starting the application:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI 3.0 JSON: `http://localhost:8080/v3/api-docs`
- OpenAPI 3.0 YAML: `http://localhost:8080/v3/api-docs.yaml`

For Postman, one import is enough:

1. Start the backend locally.
2. In Postman, choose `Import`.
3. Paste `http://localhost:8080/v3/api-docs` as the source URL, or download that JSON once and import the file.
4. Postman will generate a collection covering all current auth and reservation endpoints from the same OpenAPI contract used by Swagger UI.

Swagger UI is configured with JWT bearer support, so after logging in or registering you can paste the returned token into the `Authorize` dialog and test protected endpoints directly from the browser.

## 🔐 Reservation And Auth Safeguards

- Reservation detail and cancellation access is restricted to the owner or an admin.
- Reservation creation now rejects past bookings, invalid time ranges, and overlapping active bookings on the same pitch.
- Partial-hour reservations are priced proportionally instead of being rounded down to zero.
- JWT signing no longer falls back to a hard-coded secret; startup now requires an explicit `JWT_SECRET`.
- Authentication, validation, and reservation-access error contracts are covered by integration tests before Swagger work begins.
- Swagger/OpenAPI documentation is generated from the live Spring controllers, so the UI and exported JSON stay aligned with the implemented API contract.

- **Access Control:** User interaction (requests, joining) is strictly regulated by blocking status and join policies.
- **Anti-Spam:** Manual and automatic blocking mechanisms ensure a clean social environment.
- **Data Integrity:** SQL-level constraints and transaction management for bidirectional contacts and strikes.
- **Unified Error Contract:** All failures (Validation, Auth, Business Logic) return the same `ApiError` structure with descriptive codes.

## 🗺️ Project Status

(Social & Match Networking) is now complete. The backend is at version `1.2.0`.

For detailed development phases and setup instructions, please refer to the [ROADMAP.md](./ROADMAP.md) file.
