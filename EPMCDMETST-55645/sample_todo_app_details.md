# sample_todo_app Codebase Details

## Repository
- Source: https://github.com/soumava05/sample-app-capstone
- Application: java 21 Spring Boot Thymeleaf Todo app
- Architecture: layered moduhith web, service, repository, dto, domain, config packages.

## Application Type
Server-rendered Spring Boot monolithic web application for personal Todo management. It uses Spring MVC controllers, Thymeleaf templates, Spring Security form login, BCrypt password hashing, and local JSON file persistence.

## Key Findings
- Functionality: register, login, view user-scoped tasks, create tasks, and mark tasks completed.
- Tech stack: Java 21, Spring Boot 3.3.2, Spring Security, Thymeleaf, Jackson, Maven, TestNG, Mockito.
- Pattern: layered modular monolith with Repository, Service Layer, DTO, and Configuration patterns.
- Persistence: storage/users.json and storage/tasks/{username}.json.

## Security and Scalability
BCrypt, authenticated routes, DLO validation, and user-scoped repository lookups are used. Local JSON persistence is appropriate for sample/single-node use but should be replaced by a database for production horizontal scaling.
