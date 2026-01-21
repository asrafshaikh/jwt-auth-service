

# JWT Auth Service

Lightweight Spring Boot service that issues and validates JWTs with token caching (Caffeine). Useful as a small authentication microservice for local development and testing.

## Quick start

Build the project:

```powershell
mvn clean package
```

Run the service:

```powershell
mvn spring-boot:run
# or
java -jar target/jwt-auth-service-1.0.0.jar
```

The application starts on port 8080 by default.

## Configuration

Edit `src/main/resources/application.properties` or override properties with environment variables.

- `jwt.secret` — HMAC secret used to sign tokens (keep this secret out of source control)
- `jwt.expiration` — token lifetime in milliseconds
- `jwt.refresh-buffer` — milliseconds before expiry during which the token is allowed to be refreshed

Example (PowerShell):

```powershell
$env:JWT_SECRET="your-secret"; mvn spring-boot:run
```

## HTTP API

Base path: `/api`

- POST `/api/auth/login`
	- Body JSON: `{ "userId": "...", "password": "..." }`
	- Response: `LoginResponse` (contains token, expiration, etc.)

- POST `/api/auth/refresh-token`
	- Requires `Authorization: Bearer <token>`
	- Returns a refreshed token when within the configured refresh window

- POST `/api/auth/logout`
	- Requires `Authorization: Bearer <token>`
	- Invalidates the cached token for the user

- GET `/api/protected/data` (and other `/api/protected/*` endpoints)
	- Require a valid Bearer token

### Example: login (curl)

```bash
curl --location --request POST 'http://localhost:8080/api/auth/login' \
	--header 'Content-Type: application/json' \
	--data-raw '{
		"userId": "asraf",
		"password": "mypassword"
	}'
```

### Example: access protected endpoint

```bash
curl http://localhost:8080/api/protected/data \
	-H "Authorization: Bearer <TOKEN>"
```

## Key files

- `src/main/java/com/example/jwtauth/security/JwtTokenProvider.java` — token generation and validation
- `src/main/java/com/example/jwtauth/service/TokenCacheService.java` — token caching logic
- `src/main/java/com/example/jwtauth/service/AuthenticationService.java` — login/refresh/logout flows
- `src/main/java/com/example/jwtauth/controller/AuthController.java` — auth endpoints
- `src/main/java/com/example/jwtauth/controller/ProtectedController.java` — protected API endpoints
- `src/main/java/com/example/jwtauth/service/UserDetailsServiceImpl.java` — in-memory user store (for dev)
- `src/main/resources/application.properties` — runtime configuration
- `pom.xml` — Maven build configuration

## Architecture

This section describes the high-level architecture, components, and token lifecycle used by this service.

### Overview

- Lightweight Spring Boot application that issues and validates JWTs.
- Uses a token cache (Caffeine) to track valid tokens and support logout / quick invalidation.
- Authentication flow is handled by `AuthenticationService` and exposed via `AuthController`.
- `JwtTokenProvider` is responsible for token creation and validation; `JwtAuthenticationFilter` enforces JWT validation on protected endpoints.

### Components

- JwtTokenProvider — generate/validate JWTs, compute expiry and claims.
- TokenCacheService — stores token metadata (e.g. issuedAt, expiresAt, userId) in an in-memory cache.
- AuthenticationService — orchestrates login, refresh and logout operations and interacts with the cache.
- JwtAuthenticationFilter — validates incoming Bearer tokens, sets SecurityContext when valid.
- UserDetailsServiceImpl — provides user details (in-memory for this project).

### Token lifecycle (summary)

1. Client POSTs credentials to `/api/auth/login`.
2. `AuthenticationService` validates credentials via `UserDetailsServiceImpl`.
3. `JwtTokenProvider` creates a signed JWT containing standard claims and expiry.
4. `TokenCacheService` stores token metadata keyed by token ID (or token string) to enable quick invalidation.
5. Client uses the JWT in `Authorization: Bearer <token>` header for protected endpoints.
6. `JwtAuthenticationFilter` validates signature and expiry on each request and checks the cache for token presence.
7. On logout, `AuthenticationService` removes the token from the cache so subsequent requests are rejected even if the JWT is not expired.
8. Refresh flow issues a new token (if within `jwt.refresh-buffer` window) and updates the cache entry.

### Sequence (mermaid)

```mermaid
sequenceDiagram
	Client->>+AuthController: POST /api/auth/login (credentials)
	AuthController->>AuthenticationService: authenticate()
	AuthenticationService->>JwtTokenProvider: generateToken(user)
	JwtTokenProvider-->>AuthenticationService: token
	AuthenticationService->>TokenCacheService: cacheToken(token, metadata)
	AuthenticationService-->>-AuthController: LoginResponse(token)
	Client->>+ProtectedEndpoint: GET /api/protected/data (Authorization: Bearer)
	ProtectedEndpoint->>JwtAuthenticationFilter: validate token
	JwtAuthenticationFilter->>JwtTokenProvider: parse/verify
	JwtAuthenticationFilter->>TokenCacheService: isTokenCached(token)
	JwtAuthenticationFilter-->>ProtectedEndpoint: proceed (SecurityContext)
```

### Deployment notes

- For single-node deployments the local Caffeine cache is sufficient for quick invalidation.
- For multi-node or cloud deployments replace the cache with a distributed store (Redis) so logout/invalidations propagate.
- Keep `jwt.secret` in a secrets manager (Vault/Cloud KMS) and rotate regularly.

## Notes & recommendations

- The included `UserDetailsServiceImpl` uses an in-memory user store for convenience. Replace it with a persistent user store for production.
- Caffeine is used for local token caching. For distributed deployments consider using Redis or another shared store.
- Keep `jwt.secret` secure and rotate regularly.

## Troubleshooting

- Authentication failures: verify `jwt.secret` and user credentials configured in `UserDetailsServiceImpl`.
- Token refresh problems: inspect `jwt.refresh-buffer` and `jwt.expiration` settings.
- Enable debug logging for `com.example` to trace authentication/token flows.

## Testing

- Add unit tests under `src/test/java` for `JwtTokenProvider` and `TokenCacheService`.
- Use integration tests to validate end-to-end login/refresh/logout behavior.

## License

Add a `LICENSE` file with your preferred license.

<!-- filepath: g:\jwt-auth-service\README.md -->