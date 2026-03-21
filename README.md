# Secrets Manager Backend

A secure credential management system built for engineering teams. Stores encrypted secrets with multi-party approval, role-based access control, Shamir's Secret Sharing for the master key, and a full audit trail on every sensitive action.

Built with **Spring Boot 3**, **PostgreSQL**, and **Java 21**.

---

## Features

- **AES-256-GCM encryption**: all credential values are encrypted at rest using a master key protected by Shamir's Secret Sharing
- **Multi-party approval**: developers must request access and wait for quorum before a credential can be revealed
- **Role-based access control**: Admin, Team Lead, Project Manager, and Developer each have different permissions and approval powers
- **Shamir's Secret Sharing**: the master encryption key is split across all admins and must be reconstructed cooperatively for destructive operations
- **TTL-based access**: approved credential access expires after a configurable window; users must re-request after expiry
- **Async AOP audit logging**: every credential access, approval vote, and admin action is recorded automatically via a custom `@Audited` annotation
- **Caffeine in-memory cache**: membership checks, credential details, and approval status are cached to reduce database load

---

## API Documentation

The full API is documented via **Swagger UI**. Once the application is running, open:

```
http://localhost:8080/swagger-ui.html
```

All endpoints, request bodies, response schemas, and error codes are described there.

---

## Requirements

- Java 21+
- PostgreSQL 14+
- Gradle 8+ (wrapper included, no installation needed)

---

## Getting Started

**1. Clone the repository**

```bash
git clone https://github.com/RoaTheDev/SecretManger
cd SecretManger
```

**2. Create the database**

```sql
CREATE DATABASE secret_manager;
```

**3. Configure environment variables**

The application reads the following variables from your environment. Set them in your shell, in a `.env` file loaded by your IDE, or directly in `application-local.yaml` for local development.

| Variable         | Description                                                                            |
|------------------|----------------------------------------------------------------------------------------|
| `DB_URL`         | PostgreSQL JDBC connection URL, e.g. `jdbc:postgresql://localhost:5432/secret_manager` |
| `DB_USERNAME`    | Database username                                                                      |
| `DB_PASSWORD`    | Database password                                                                      |
| `JWT_SECRET`     | HS512 signing secret, must be at least 512 bits, Base64-encoded                        |
| `APP_MASTER_KEY` | AES-256 master key used to encrypt all credential values, Base64-encoded 32-byte key   |

Generating a JWT secret (HS512, minimum 512 bits):

```bash
openssl rand -base64 64
```

Generating an AES-256 master key:

```bash
openssl rand -base64 32
```

> Never commit real values for `JWT_SECRET` or `APP_MASTER_KEY` to version control.

**4. Run the application**

```bash
# Local profile, enables DataSeeder with demo data
./gradlew bootRun --args='--spring.profiles.active=local'

# Production profile
./gradlew bootRun --args='--spring.profiles.active=prod'
```

The server starts on `http://localhost:8080`.

---

## Demo Data (local profile only)

When running with the `local` profile, the `DataSeeder` automatically creates demo users, projects, and credentials on startup.

| Name   | Email           | Password     | Role            |
|--------|-----------------|--------------|-----------------|
| Roa    | roa@demo.com    | Password123! | Admin           |
| Alice  | alice@demo.com  | Password123! | Admin           |
| Rem    | rem@demo.com    | Password123! | Team Lead       |
| Anna   | anna@demo.com   | Password123! | Project Manager |
| Tiamat | tiamat@demo.com | Password123! | Developer       |
| Gwen   | gwen@demo.com   | Password123! | Developer       |

---

## Flows

### Authentication

1. User submits email and password to `POST /api/auth/login`
2. Spring Security authenticates via `CustomUserDetailsService`
3. On success, the server generates a short-lived JWT access token and a long-lived refresh token
4. The refresh token is stored in an HttpOnly cookie; the access token and expiry timestamp are returned in the response body
5. The client attaches the access token as a `Bearer` header on subsequent requests
6. When the access token nears expiry, the client calls `POST /api/auth/refresh` using the cookie to get a new token pair silently
7. On logout, `POST /api/auth/logout` clears the cookie server-side

---

### Credential Access (Developer)

Developers cannot read a credential value directly. They must go through an approval flow:

1. Developer calls `POST /api/credentials/{id}/request-access` to open an approval request
2. The request is stored with status `PENDING` and a quorum requirement based on the credential's `ApprovalPolicy`:
  - `RELAXED` requires 1 approver
  - `STANDARD` requires 2 approvers
  - `STRICT` requires 3 approvers
3. Eligible voters (Team Lead, Project Manager, Admin) see the pending request and cast votes via `POST /api/approvals/{id}/vote`
4. When the approve count reaches the quorum, the request status is set to `APPROVED` and an expiry timestamp (`expiresAt`) is written
5. The developer calls `GET /api/credentials/{id}/reveal` while the approval is active
6. The server decrypts the credential value using the AES-256-GCM master key and returns it along with the expiry time
7. Once `expiresAt` passes, the reveal endpoint rejects the request and the developer must submit a new access request

---

### Credential Access (Privileged Roles)

Admin, Team Lead, and Project Manager skip the approval flow entirely. They call `GET /api/credentials/{id}/reveal` directly and receive the decrypted value immediately. No approval request is created and no TTL is applied.

---

### Shamir Key Initialisation

The AES-256-GCM master key that encrypts all credentials is itself protected using Shamir's Secret Sharing:

1. An admin calls `POST /api/admin/shamir/init`
2. The server splits the master key into `n` shares where `n` is the number of admins currently registered
3. Each share is encrypted with the corresponding admin's credentials and stored in the `shamir_shares` table
4. This operation can only be performed once. Calling it again throws `ShamirAlreadyInitializedException`

---

### Project Deletion (Shamir Quorum)

Deleting a project is a destructive operation that removes all credentials and member associations. It requires all admins to agree:

1. Each admin calls `POST /api/admin/projects/{id}/deletion-vote` independently
2. The vote is stored in a Caffeine cache entry keyed by `projectId:adminId` with a 24-hour TTL
3. After each vote the server checks how many admins have voted by querying all admin accounts and checking for a cache entry for each
4. When every admin has voted, the server executes the deletion and evicts all related cache entries
5. Every individual vote is recorded by `@Audited` with action `PROJECT_DELETION_VOTED`; the final deletion is recorded as `PROJECT_DELETION_APPROVED`
6. If quorum is not reached within 24 hours the cache entries expire and the vote resets automatically

---

### Audit Logging

Every sensitive action is captured automatically without any manual service calls. A `@Audited` annotation on a method triggers an AOP aspect (`AuditAspect`) that fires asynchronously after the method returns. It records the actor, action name, target type, and timestamp to the `audit_logs` table. Admins can query the full log via `GET /api/admin/audit-logs` with optional filters.

---

## Project Structure


```
src/main/java/io/roa/secretmanger/
├── Annotation/       # @Audited marks methods for automatic audit logging
├── Config/           # Security, caching (Caffeine), async, Swagger config
├── Controller/       # REST controllers
│   └── docs/         # Swagger/OpenAPI interface definitions per controller
├── DTO/
│   ├── projection/   # Spring Data projection interfaces for query results
│   ├── request/      # Validated request records (auth, project, approval)
│   └── response/     # Response records (API wrapper, pagination, domain DTOs)
├── Exception/        # Domain exceptions mapped to HTTP responses globally
├── Filter/           # JWT auth filter and global exception handler
├── Mapper/           # MapStruct mappers between entities and DTOs
├── Model/
│   ├── Entity/       # JPA entities (User, Project, Credential, Approval, Audit, Shamir)
│   └── Value/        # Enums (UserRole, CredentialType, AccessTier, ApprovalPolicy, etc.)
├── Repo/             # Spring Data JPA repositories
│   └── seeder/       # DataSeeder seeds demo data on local profile startup
├── Service/          # Business logic interfaces
│   └── Impl/         # Service implementations
└── Util/             # JWT, cookie, AOP audit aspect, security context helpers
```



---

## Tech Stack

| Layer         | Technology                     |
|---------------|--------------------------------|
| Framework     | Spring Boot 3                  |
| Language      | Java 21                        |
| Database      | PostgreSQL                     |
| ORM           | Spring Data JPA / Hibernate    |
| Security      | Spring Security, JWT (HS512)   |
| Encryption    | AES-256-GCM                    |
| Key Splitting | Shamir's Secret Sharing        |
| Caching       | Caffeine                       |
| Mapping       | MapStruct                      |
| API Docs      | SpringDoc OpenAPI / Swagger UI |
| Build         | Gradle (Kotlin DSL)            |