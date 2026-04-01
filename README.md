Football App is an application for organizing matches. full-stack application designed to manage bookings, user memberships, and reservation schedules.

## 🛠️ Tech Stack

- **Backend:** Java 25, Spring Boot 4.x, Spring Security (JWT)
- **Database:** PostgreSQL, Flyway (Database Migration)
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

1. Configure your PostgreSQL settings in `application.yml`.
2. To connect database, `application.yml` uses environment variables defined in `.env` file.
   You will need to add a `.env` file in the root directory. See `.env.example` for the required keys
   and please be sure to add `.env` to `.gitignore` before committing any change.
3. Run `./mvnw spring-boot:run` in the backend directory.

## 🗺️ Project Status

For detailed development phases and setup instructions, please refer to the [ROADMAP.md](./ROADMAP.md) file.
