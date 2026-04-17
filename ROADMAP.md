# Project Roadmap & Setup Guide

This document outlines the development phases and initial configuration for the Football App project.

## Initial Setup (Spring Initializr)

To recreate or initialize the backend, use the following settings:

- **Project:** Maven
- **Language:** Java 25
- **Spring Boot:** 4.x.x
- **Dependencies:**
    - `Spring Web`: For RESTful APIs.
    - `Spring Data JPA`: For database interactions.
    - `PostgreSQL Driver`: To connect to the DB.
    - `Lombok`: To reduce boilerplate code.
    - `Validation`: For input data constraints.
    - `Flyway Migration`: For version-controlled DB schemas.
    - `Spring Security`: For authentication and authorization.

## Development Phases

| Phase  | Title                            | Description                                                                                                |
| :----- | :------------------------------- | :--------------------------------------------------------------------------------------------------------- |
| **01** | **Project Architecture** ✅      | Setting up README, Roadmap, and directory structure.                                                       |
| **02** | **Profiles:** ✅                 | Using `application.yml` with profiles (`default`, `dev`, `test`) and `.env` integration for security.      |
| **03** | **Database & Flyway** ✅         | PostgreSQL integration and `V1__init_schema.sql` (Fields, Users).                                          |
| **04** | **Domain Entities** ✅           | Creating `Pitch`, `Reservation`, and `User` entities with relationships.                                   |
| **05** | **Global Exception Handling** ✅ | Implementing `@RestControllerAdvice`, sealed classes, and generic error wrappers for dynamic status codes. |
| **06** | **DTO & Service Layer** ✅       | Implementing business logic and DTO mapping.                                                               |
| **07** | **Pagination & Sorting** ✅      | Adding filtering and paging for pitch listings and history.                                                |
| **08** | **Scheduled Jobs** ✅            | Automated tasks for expired reservations and notifications.                                                |
| **09** | **Security & JWT** ✅            | Implementing Role-Based Access Control (Admin/User) with JWT.                                              |
| **10** | **Swagger & OpenAPI** ✅         | Swagger UI, OpenAPI 3.0 JSON/YAML export, JWT-aware documentation, and Postman-importable API specs.       |

## Testing & TDD Approach

While unit testing was not explicitly separated as its own roadmap phase, **testing is an integral, continuous process applied from Phase 06 onwards**. We employ an iterative approach to write tests for the core business logic (Service Layer) and continue to enforce test coverage for all subsequent features (Scheduled Jobs, Security, etc.) to maintain software quality without breaking the established phase order.

The current test setup now uses:

- Mockito unit tests configured to avoid Java 25 agent-attachment failures.
- An isolated Spring Boot test profile backed by H2 instead of a developer-managed PostgreSQL instance.
- Integration coverage for API error contracts around authentication, authorization, and request validation.
- Integration checks for `/v3/api-docs` and `/swagger-ui.html` so the documentation endpoints stay available after future changes.

## Current Delivery Status

The roadmap is now complete through all 10 phases. The backend currently ships with:

- A stable release baseline updated to version `1.1.0`.
- PostgreSQL + Flyway schema management for production data, including initial data seed migration for admins and pitches.
- JWT-secured auth and reservation APIs.
- A unified `ApiError` response contract for validation, authorization, and authentication failures.
- Generated Swagger UI for local exploration at `http://localhost:8080/swagger-ui.html`.
- OpenAPI 3.0 JSON and YAML exports that can be imported directly into Postman.

## 🚀 v1.2.0 Coming Enhancements:
- **Friends & Contacts Network:** Users can now discover, request, and add each other as friends to build their personal football network.
- **Strict Anti-Spam System:** Bidirectional contacts flow that automatically 2-strike blocks users for unaccepted friend requests to prevent spam.
- **Match Request Workflow:** Proper isolated requests & invites system via `match_requests` separating confirmed players from pending ones.
- **Join Policies:** Configurations mapping reservations to be `PUBLIC`, `FRIENDS_ONLY` or `INVITE_ONLY`.

- **v1.1.0 Release Enhancements:**
    - Multi-user reservation support with endpoint to join existing bookings.
    - Complete API coverage with PUT endpoints for Pitch, User, and Reservation (including Pitch & Organizer hand-off).
    - Intelligent `ReservationCleanupJob` auto-deletes inactive 30-day old data to conserve schema size.
    - Robust `GlobalExceptionHandler` intercepting previously unhandled JSON parse exceptions as 400 responses.
    - Strong password validation and enhanced security.
    - Professional Swagger documentation with clean error responses.
    - Production-ready error handling with consistent logging.
