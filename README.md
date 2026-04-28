# startingAIDev

This repository now contains a Spring Boot prototype backend for a stock trading application.

Structure:
- `pom.xml` - Maven build file with Spring Boot, JPA, Redis, Security, and PostgreSQL dependencies.
- `docker-compose.yml` - Local PostgreSQL and Redis setup.
- `src/main/java/com/app/stock/` - Backend source code.
  - `config/` - Security and Redis configuration.
  - `controller/` - REST API endpoints for users and trades.
  - `entity/` - JPA entities for User, Portfolio, and Transaction.
  - `repository/` - JPA repositories.
  - `service/` - Trading business logic and AI summary cache integration.
  - `dto/` - Request and response objects.
- `src/main/resources/application.yml` - Database and Redis connection settings.

Run the project:
1. Start local services with `docker-compose up -d`.
2. Run with `mvn spring-boot:run`.
