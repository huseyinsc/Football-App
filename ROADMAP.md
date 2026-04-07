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

| Phase  | Title                         | Description                                                                                                |
| :----- | :---------------------------- | :--------------------------------------------------------------------------------------------------------- |
| **01** | **Project Architecture** ✅    | Setting up README, Roadmap, and directory structure.                                                       |
| **02** | **Profiles:** ✅               | Using `application.yml` with profiles (`default`, `dev`, `test`) and `.env` integration for security.      |
| **03** | **Database & Flyway** ✅       | PostgreSQL integration and `V1__init_schema.sql` (Fields, Users).                                          |
| **04** | **Domain Entities** ✅         | Creating `Pitch`, `Reservation`, and `User` entities with relationships.                                   |
| **05** | **Global Exception Handling** ✅| Implementing `@RestControllerAdvice`, sealed classes, and generic error wrappers for dynamic status codes. |
| **06** | **DTO & Service Layer** ✅     | Implementing business logic and DTO mapping.                                                               |
| **07** | **Pagination & Sorting** ✅    | Adding filtering and paging for pitch listings and history.                                                |
| **08** | **Scheduled Jobs**            | Automated tasks for expired reservations and notifications.                                                |
| **09** | **Security & JWT**            | Implementing Role-Based Access Control (Admin/User) with JWT.                                              |
| **10** | **Advanced Features**         | Swagger documentation and performance optimizations.                                                       |

## Testing & TDD Approach

While unit testing was not explicitly separated as its own roadmap phase, **testing is an integral, continuous process applied from Phase 06 onwards**. We employ an iterative approach to write tests for the core business logic (Service Layer) and continue to enforce test coverage for all subsequent features (Scheduled Jobs, Security, etc.) to maintain software quality without breaking the established phase order.
