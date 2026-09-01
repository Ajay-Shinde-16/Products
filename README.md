# Product API

A RESTful API for managing Products and their Items, built with Java 17 and Spring Boot.
It supports full CRUD operations, JWT authentication with refresh token rotation,
role-based access, pagination, validation and Swagger documentation.

## Tech Stack

- Java 17
- Spring Boot 3
- Spring Data JPA (Hibernate)
- 'MySQL (H2 for tests)
- Spring Security with JWT and Refresh Tokens
- JUnit 5 and Mockito
- Swagger / OpenAPI
- Docker and Docker Compose

## Project Structure

```
src/main/java/com/zest/productapi
├── config       # swagger + data seeder
├── controller   # REST controllers
├── dto          # request / response objects
├── entity       # JPA entities
├── exception    # custom exceptions + global handler
├── repository   # Spring Data JPA repositories
├── security     # JWT util, filter, security config
└── service      # business logic
```

The app is split into layers: controllers handle HTTP, services hold the logic,
repositories talk to the database and DTOs are used so entities are never exposed directly.

## How to Run

### Option 1: Docker Compose (easiest)

```bash
docker-compose up --build
```

This starts PostgreSQL and the application together.
The API will be available at `http://localhost:8080`.

### Option 2: Run locally

1. Make sure PostgreSQL is running and a database `productdb` exists.
2. Update the credentials in `src/main/resources/application.yml` if needed.
3. Run:

```bash
mvn spring-boot:run
```

## Default Users

On first run two users are created automatically:

| Username | Password | Role  |
|----------|----------|-------|
| admin    | admin123 | ADMIN |
| user     | user123  | USER  |

Only ADMIN can create, update or delete products. Any logged-in user can read.

## Authentication

1. Login to get an access token and a refresh token:

```
POST /api/v1/auth/login
{
  "username": "admin",
  "password": "admin123"
}
```

2. Send the access token on every request:

```
Authorization: Bearer <accessToken>
```

3. When the access token expires, get a new pair using the refresh token
(the old refresh token is rotated and becomes invalid):

```
POST /api/v1/auth/refresh
{
  "refreshToken": "<refreshToken>"
}
```

## API Endpoints

| Method | Endpoint                     | Access        | Description                 |
|--------|------------------------------|---------------|-----------------------------|
| POST   | /api/v1/auth/register        | Public        | Register a new user         |
| POST   | /api/v1/auth/login           | Public        | Login and get tokens        |
| POST   | /api/v1/auth/refresh         | Public        | Rotate refresh token        |
| GET    | /api/v1/products             | Authenticated | List products (paginated)   |
| GET    | /api/v1/products/{id}        | Authenticated | Get one product             |
| POST   | /api/v1/products             | ADMIN         | Create a product            |
| PUT    | /api/v1/products/{id}        | ADMIN         | Update a product            |
| DELETE | /api/v1/products/{id}        | ADMIN         | Delete a product            |
| GET    | /api/v1/products/{id}/items  | Authenticated | List items of a product     |

### Pagination

`GET /api/v1/products?page=0&size=10`

## API Documentation (Swagger)

Once the app is running, open:

```
http://localhost:8080/swagger-ui.html
```

Use the **Authorize** button to paste your access token.

## Running Tests

```bash
mvn test
```

Tests use an in-memory H2 database, so no setup is needed.
There are unit tests (Mockito) for the service layer and integration tests
(Spring Boot Test + MockMvc) for the full request flow.
