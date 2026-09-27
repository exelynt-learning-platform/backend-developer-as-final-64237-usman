# Resource Booking System API - Assignment 2026

A robust, enterprise-grade RESTful API for managing bookable resources and reservations. Built strictly following the 12-Factor App methodology using Spring Boot, Java 17, Spring Security (JWT), and PostgreSQL.

## Features & Assignment Compliance
This project fully satisfies all requirements and Evaluation Criteria outlined in the backend developer assignment:

* **Authentication & Security:** Implements JWT-based stateless login via `POST /auth/login`, token validation, and secure BCrypt password handling.
* **Authorization & RBAC:** Distinct `ADMIN` and `USER` roles. `ADMIN` has full CRUD access to resources and reservations. `USER` has read-only access to resources and can only create, view, update, and cancel their own reservations. Standard users cannot self-confirm reservations.
* **Identity Management:** User identity is strictly extracted from the JWT token (`Principal`), never trusted from the request body.
* **Business Validation:** 
  * Reservation statuses explicitly implemented: `PENDING`, `CONFIRMED`, `CANCELLED`.
  * Reservation price is strictly stored and handled as a decimal value (`BigDecimal`).
  * Comprehensive validation prevents past-date bookings and stops overlapping time slots.
* **Advanced Querying:** 
  * Reservation filtering by `status`, `minPrice`, and `maxPrice`.
  * Pagination utilizing `page` and `size` parameters.
  * Optional sorting parameters supported across endpoints.
* **Database:** Connected to PostgreSQL using Spring Data JPA / Hibernate with correct entity relationships.
* **Robust Error Handling:** Global Exception Handler appropriately captures and responds to invalid requests, auth errors, and missing data.

## Setup Instructions (Docker)
The application is fully containerized. The easiest way to run the backend and the PostgreSQL database together is using Docker Compose.

1. Ensure **Docker** and **Docker Compose** are installed and running on your machine.
2. Open a terminal in the root directory of the project.
3. Run the following command to build and start the containers:
    ```bash
    docker-compose up -d --build
    ```
4. The REST API will be accessible at: `http://localhost:8080`


## API Documentation
The API is documented and testable via two methods:
1. **Swagger/OpenAPI UI:** Accessible at `http://localhost:8080/swagger-ui.html` once the server is running.
2. **Postman Collection:** A `postman_collection.json` is included in the repository root. It includes pre-configured requests and automated token extraction scripts.

## Database Configuration & Environment Variables
The application uses a single `application.yml` file configured for PostgreSQL. It dynamically injects the following environment variables (which fall back to local Docker defaults if not explicitly set):

* `DATABASE_URL`: `jdbc:postgresql://postgres:5432/resource_booking`
* `DATABASE_USER`: `rb_admin`
* `DATABASE_PASSWORD`: `<YOUR_SECURE_PASSWORD>`
* `JWT_SECRET`: `<GENERATE_BASE64_256BIT_SECRET>` (Base64 encoded secret key for signing JWT tokens. Fails fast if missing in production).
* `JWT_EXPIRATION_MS`: Token validity duration in milliseconds (default: 86400000).

## Seed Users for Testing
The application automatically provisions the following seed users on startup to easily test Authentication and RBAC functionality:

**1. Administrator (ADMIN Role)**
* **Username:** `admin`
* **Password:** `admin123`
* **Access:** Full CRUD access to all resources and all reservations globally.

**2. Standard User (USER Role)**
* **Username:** `user1`
* **Password:** `user123`
* **Access:** Read-only access to resources. Can only create, view, update, and cancel their own reservations.

## Design Decisions & Future Enhancements

**1. Resource Pricing vs. Reservation Pricing (Assignment Compliance):**
In a real-world SaaS booking system, a `Resource` (e.g., Room, Vehicle, Equipment) would typically possess a `basePrice` or `hourlyRate` attribute. However, to strictly align with the provided assignment requirements and Evaluation Criteria (specifically Criteria 6 & 7, which mandate storing and filtering prices exclusively at the `Reservation` level), the `Resource` entity was intentionally designed without a price field. The final booking cost is tracked and persistently stored within the `Reservation` entity. In a production environment, this would be expanded by implementing a dynamic pricing engine computing `Resource.baseRate * bookingDuration`.

**2. Concurrency Control (Future Enhancement):**
Currently, double-booking prevention relies on strict database queries validating overlapping time slots before saving a reservation. For a high-traffic production system, we would introduce **Pessimistic Write Locks** at the database level or distributed locking mechanisms (e.g., Redis) to guarantee 100% thread safety when two users attempt to book the same resource at the exact same millisecond.

**3. Soft Deletes & Audit Trails (Future Enhancement):**
The current application uses "Hard Deletes" for resources and reservations as per standard CRUD requirements. In an enterprise system involving financial transactions, records are rarely physically deleted. A future enhancement would involve implementing "Soft Deletes" (e.g., `isActive = false`) and using Hibernate Envers to maintain a comprehensive audit log of who changed a reservation's status and when.

**4. Caching & Performance Optimization (Future Enhancement):**
To handle high read-throughput (e.g., users frequently browsing available resources), a distributed caching layer like **Redis** could be integrated. Caching the results of `GET /resources` and implementing cache eviction policies upon resource updates would drastically reduce database I/O and improve overall API latency.

**5. Rate Limiting & API Protection (Future Enhancement):**
While the API is currently secured with stateless JWTs and BCrypt, a production-grade system would implement **Rate Limiting** (e.g., using Bucket4j). This is absolutely crucial for protecting sensitive endpoints like `/auth/login` from brute-force attacks and preventing denial-of-service (DoS) on resource-intensive queries.

**6. Asynchronous Processing & Infrastructure (Future Enhancement):**
For a fully scalable architecture, the application would typically be deployed behind a Reverse Proxy / Load Balancer like **Nginx**. Furthermore, time-sensitive business logic—such as automatically expiring unpaid `PENDING` reservations after 15 minutes, or sending email notifications—would be decoupled from the main HTTP request threads and handled via **Cron Jobs** or asynchronous message brokers (e.g., RabbitMQ, Kafka).