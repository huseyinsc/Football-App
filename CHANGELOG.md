# [1.2.0](https://github.com/huseyinsc/Football-App/compare/v1.1.0...v1.2.0) (2026-04-17)


### Bug Fixes

* **ci:** remove duplicate GitHub release creation ([65165d1](https://github.com/huseyinsc/Football-App/commit/65165d1691b369d532afb70fb83e3abe8d427916))


### Features

* **api:** add contacts and match-requests endpoints with join policy enforcement ([492e779](https://github.com/huseyinsc/Football-App/commit/492e779c81a1ee65b79bd410d65c82ca773c27fa))
* **domain:** implement anti-spam contact system and match request schema ([f516268](https://github.com/huseyinsc/Football-App/commit/f51626856ef2c937e467a7c56ae8bc03b220c746))

## [1.1.1](https://github.com/huseyinsc/Football-App/compare/v1.1.0...v1.1.1) (2026-04-15)


### Bug Fixes

* **ci:** remove duplicate GitHub release creation ([65165d1](https://github.com/huseyinsc/Football-App/commit/65165d1691b369d532afb70fb83e3abe8d427916))

# [1.1.0](https://github.com/huseyinsc/Football-App/compare/v1.0.1...v1.1.0) (2026-04-15)


### Features

* **reservation:** finalize lifecycle, update endpoints and add seed data ([499fb02](https://github.com/huseyinsc/Football-App/commit/499fb02827a640b43abe22d7830a0d83a25f8063))

## [1.0.1](https://github.com/huseyinsc/Football-App/compare/v1.0.0...v1.0.1) (2026-04-15)


### Bug Fixes

* **security:** properly expose public pitch endpoints and resolve related auth/swagger issues ([2c788b2](https://github.com/huseyinsc/Football-App/commit/2c788b2fbbc223ab85a951364a7356f4ed54cf2b))

# [1.0.0](https://github.com/huseyinsc/Football-App/compare/v0.9.2...v1.0.0) (2026-04-10)


### Features

* complete v1.0.0 release with multi-user reservations and enhanced API ([03d4e43](https://github.com/huseyinsc/Football-App/commit/03d4e43557564f3b1ddbab1922cdc08183703b53))


### BREAKING CHANGES

* Reservation domain now uses organizer + participants instead of single user
* Password validation now enforces complexity requirements
* New required endpoints: /api/v1/pitches/* and /api/v1/users/*"

## [0.9.2](https://github.com/huseyinsc/Football-App/compare/v0.9.1...v0.9.2) (2026-04-09)

### Bug Fixes

- **api:** unify auth error responses before swagger ([4eb20d4](https://github.com/huseyinsc/Football-App/commit/4eb20d411d28a2ac81dc3a5f167ed28f03d3605c))

## [0.9.1](https://github.com/huseyinsc/Football-App/compare/v0.9.0...v0.9.1) (2026-04-09)

### Bug Fixes

- harden reservation booking and auth flows ([0be8658](https://github.com/huseyinsc/Football-App/commit/0be86589a611d758c3900b91d2ba0a527b8c432f))

# [0.9.0](https://github.com/huseyinsc/Football-App/compare/v0.8.0...v0.9.0) (2026-04-07)

### Features

- **Security & JWT:** implement role-based access control with JWT ([634b687](https://github.com/huseyinsc/Football-App/commit/634b687ba2de1179b2760bd6920245c576965d6b))

# [0.8.0](https://github.com/huseyinsc/Football-App/compare/v0.7.1...v0.8.0) (2026-04-07)

### Features

- **Scheduled Jobs:** implement automated tasks for expired reservations ([d90fa33](https://github.com/huseyinsc/Football-App/commit/d90fa33d2c1992795c209c61515a41fd73b8516d))

## [0.7.1](https://github.com/huseyinsc/Football-App/compare/v0.7.0...v0.7.1) (2026-04-07)

# [0.7.0](https://github.com/huseyinsc/Football-App/compare/v0.6.0...v0.7.0) (2026-04-07)

### Features

- **Pagination & Sorting:** implement pagination and sorting for pitch listings and history ([e41541e](https://github.com/huseyinsc/Football-App/commit/e41541e7773270a177bdbbc3d6f4fe7f51a4ec17))

# [0.6.0](https://github.com/huseyinsc/Football-App/compare/v0.5.0...v0.6.0) (2026-04-07)

### Features

- **DTO & Service Layer:** implement business logic and DTO mapping ([3006bd2](https://github.com/huseyinsc/Football-App/commit/3006bd2369655b8ceb8e8c7c598b9e90f525589d))

# 1.0.0 (2026-04-07)

### Features

- **Database & Flyway:** add initial flyway migration for users table ([674a488](https://github.com/huseyinsc/Football-App/commit/674a488f7fed95c4f26dc9aacbe54cac0b769475))
- **Domain Entities:** implement comprehensive database schema and domain entities ([c316a5a](https://github.com/huseyinsc/Football-App/commit/c316a5aa11e2bdd70e302164fb2bdd801c83f565))
- **Global Exception Handling:** implement global exception handling with generic api error and sealed exceptions ([3d9a92e](https://github.com/huseyinsc/Football-App/commit/3d9a92e467da034c0ad6a2269cb2fb2a40a4587c))
- **Profiles:** updated configuration for db connection and documentation ([e67eed1](https://github.com/huseyinsc/Football-App/commit/e67eed1e81b073a1124b287629f37be28825ed70))
