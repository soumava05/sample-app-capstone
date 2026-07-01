# Sample Todo App --- Architecture & Requirements Analysis

## 1. Application Overview

| Attribute | Value |
|-----------|-------|
| App Name | Todo App |
| Type | Monolithic Web Application (MVC) |
| Language | Java 21 |
| Framework | Spring Boot 3.3.x |
| UI Engine | Thymeleaf (Server-Side Rendering) |
| Security | Spring Security (BCrypt, Form Login, Remember-Me) |
| Persistence | File-System (JSON files via Jackson) |
| Build Tool | Maven |
| Test Frameworks | TestNG + Mockito |
| Server Port | 8090 |

---

## 2. Architecture Pattern

Layered Architecture (N-Tier MVC)

```
web (Controllers)
    service / service.impl (Business Logic)
        repository / repository.impl (Data Access)
            storage (File I/O Utility)
                Local Filesystem (JSON Files)
```

---

## 3. Domain Entities

### User
| Field | Type | Constraint |
|-------|------|-----------|
| username | String | PK, unique, normalized |
| fullName | String | required |
| passwordHash | String | BCrypt encoded |
| createdAt | LocalDateTime | set on creation |

### TodoTask
| Field | Type | Constraint |
|-------|------|-----------|
| id | String (UUID) | PK, auto-generated |
| username | String | FK User, normalized |
| title | String | required, 3-120 chars |
| description | String | optional, max 500 chars |
| taskDate | LocalDate | required |
| plannedFinishDate | LocalDate | required, >= taskDate |
| status | TaskStatus | OPEN (default) / COMPLETED |
| createdAt | LocalDateTime | set on creation |

---

## 4. Business Rules

| ID | Rule |
|----|-----|
| BR-1 | Username must be unique (case-insensitive) |
| BR-2 | Usernames normalized to lowercase before storage and lookup |
| BR-3 | Password and confirmPassword must match |
| BR-4| Passwords stored as BCrypt hashes (never plaintext) |
| BR-5 | plannedFinishDate must not be before taskDate |
| BR-6 | Tasks are scoped per user - no cross-user data access |
| BR-7 | Tasks identified by UUID, preventing collision |
| BR-8 | Task status transitions: OPEN to COMPLETED only |
| BR-9 | Task lists sorted by taskDate ASC, then createdAt ASC |

---

## 5. Technology Stack

| Layer | Technology |
|-------|------------|
| Language | Java 21 |
| Framework | Spring Boot 3.3.x |
| Web MVC | Spring MVC |
| Security | Spring Security |
| Templating | Thymeleaf |
| JSON | Jackson + JavaTimeModule |
| Validation | Jakarta Bean Validation |
| Testing | TestNG + Mockito |
| Build | Maven 3.9+ |
| Persistence | Local JSON files (no database) |
