# 🧭 TourPlanner

TourPlanner is a two-tier web application for planning and reviewing personal tours
(hiking, biking, running, vacation). Users self-register, create tours whose distance,
duration and route map are computed from **OpenRouteService**, record **tour logs**, and
explore everything through full-text search and an aggregate **statistics dashboard**.

- **Git repository:** https://github.com/lukifi1/SWEN2
- **Team:** Lukas Fink, Franziska Brandfellner

---

## Tech stack

| Layer       | Technology                                                            |
|-------------|-----------------------------------------------------------------------|
| Frontend    | Angular 19 (standalone components, signals), TypeScript, Leaflet      |
| Backend     | Java 21, Spring Boot 4, Spring Security (JWT), Spring Data JPA/Hibernate |
| Database    | PostgreSQL (OR-mapper: Hibernate)                                      |
| Logging     | Log4j2 (`log4j2-spring.xml`)                                          |
| Auth        | Stateless JWT (jjwt) + BCrypt password hashing                        |
| External    | OpenRouteService (geocoding + directions), OpenStreetMap tiles        |
| Tests       | JUnit 5, Mockito, AssertJ, Spring MockMvc (77 backend tests, 20 frontend tests)          |

---

## Architecture

```
Angular (Presentation, MVVM)  ──HTTP/JSON──>  Spring Boot  ──>  PostgreSQL
                                              │
                                              └──REST──>  OpenRouteService
```

### Backend — strict layered architecture
- **presentation** (`presentation/`, `dto/`) — REST controllers + DTOs. Resolves the
  authenticated user and translates requests; never touches the database directly.
- **business** (`business/`) — services with all domain logic, the ORS client, computed
  attributes, search, import/export, JWT. Defines its **own exception hierarchy**
  (`business/exceptions/`) so persistence/framework exceptions never leak upward.
- **dal** (`dal/`) — Spring Data repositories. All queries are user-scoped and
  parameterized (no SQL injection).

Each layer only calls the layer directly below it. `DataAccessException` from the DAL is
caught in services and rethrown as a `PersistenceFailedException` (business exception).

### Frontend — MVVM
- **Model** — `core/models` (interfaces) + `core/api` (HttpClient services).
- **ViewModel** — `features/**/**.viewmodel.ts` (`@Injectable`, signal-based state +
  commands; e.g. `ToursViewModel`, `TourLogsViewModel`, `StatsViewModel`, `AuthService`).
- **View** — components that bind to view-model signals and contain no business logic.

### Design patterns
- **Strategy** — `ImportExportStrategy` with `GpxImportExportStrategy` as the active
  implementation. `JsonImportExportStrategy` is also available, and the
  `ImportExportService` depends on the abstraction, so another format can be added without
  touching it.
- **Repository** (Spring Data), **Dependency Injection** (Spring / Angular DI),
  **DTO + Mapper** (`TourMapper`, `TourLogMapper`), and the **Observer** pattern on the
  frontend (RxJS observables + Angular signals).

### Reusable UI components
`ActionButtonComponent`, `StatCardComponent`, and a dependency-free `BarChartComponent`.

---

## Features

- Self-registration & JWT login; all tours/logs are private to their owner.
- Tour CRUD; distance, estimated time and route geometry are fetched from ORS on save.
- Tour-log CRUD; logs drive the computed attributes.
- **Computed attributes**: *popularity* (number of logs) and *child-friendliness*
  (0–100 score derived from difficulty, time and distance), recomputed on every log change.
- **Full-text search** across tour fields, log comments, and the textual labels of the
  computed values (e.g. searching “child friendly” or “popular”).
- Import / export of tour data as GPX.
- Tour image upload, stored on the **filesystem** (not in the DB).
- Leaflet route map drawn from the ORS geometry.
- Input validation on every form (client and server side).
- Responsive layout.

### Unique feature — Statistics Dashboard
An aggregate dashboard (`/stats`) with summary cards and charts: difficulty & rating
distributions, tours per transport type, logged distance over time, and a popularity ranking.

---

## Configuration (kept out of the source)

All secrets/paths live in `server/.env` (git-ignored) and are read by
`application.properties`. Copy and adjust:

```dotenv
DB_URL=jdbc:postgresql://localhost:5432/tourplanner
DB_USERNAME=touruser
DB_PASSWORD=tourpassword
IMAGE_BASE_PATH=/absolute/path/to/images
OPENROUTE_API_KEY=your_real_ors_key
OPENROUTE_BASE_URL=https://api.openrouteservice.org
JWT_SECRET=change-me-to-a-long-random-string-min-32-chars
JWT_EXPIRATION_MS=86400000
LOG_PATH=logs
CORS_ALLOWED_ORIGINS=http://localhost:4200
```

> **OpenRouteService key:** get a free key at https://openrouteservice.org/dev/#/signup and
> put it in `OPENROUTE_API_KEY`. Creating a tour calls the geocoding + directions APIs; with
> a missing/invalid key the API returns `502` with a clear message (import still works, as it
> carries its own geometry).

---

## Setup & run

### Prerequisites
Java 21, Maven (wrapper included), Node 20+, PostgreSQL 14+.

### 1. Database
```bash
createdb tourplanner
psql -d tourplanner -c "CREATE USER touruser WITH PASSWORD 'tourpassword';"
# PostgreSQL 15+ revokes CREATE on the public schema by default — grant it:
psql -d tourplanner -c "GRANT ALL ON SCHEMA public TO touruser;"
psql -d tourplanner -c "ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO touruser;"
psql -d tourplanner -c "ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO touruser;"
```
Hibernate creates the schema automatically (`ddl-auto=update`).

### 2. Backend (http://localhost:8080)
```bash
cd server
# on macOS
set -a && . ./.env && set +a   # load configuration into the environment
# on Windows
Get-Content .env | ForEach-Object {
  if ($_ -and $_ -notmatch '^\s*#') {
    $name, $value = $_ -split '=', 2
    [Environment]::SetEnvironmentVariable($name, $value, 'Process')
  }
}
./mvnw spring-boot:run
```

### 3. Frontend (http://localhost:4200)
```bash
cd frontend
npm install
npm start
```

---

## Testing

77 backend tests (unit + Mockito + a MockMvc integration test) covering computed attributes,
JWT, auth, tour & log services, search, stats, import/export and the ORS client.

```bash
cd server
./mvnw test
```

---

## REST API (all under `/api`, JWT required except `/auth/**` and image GET)

| Method | Path                                | Purpose                          |
|--------|-------------------------------------|----------------------------------|
| POST   | `/auth/register`, `/auth/login`     | Self-register / login → JWT      |
| GET/POST/PUT/DELETE | `/tours`, `/tours/{id}` | Tour CRUD (ORS on create/update) |
| GET    | `/tours/search?q=`                  | Full-text search                 |
| GET/POST/PUT/DELETE | `/tours/{id}/logs/...`  | Tour-log CRUD                    |
| GET    | `/tours/export`, POST `/tours/import` | GPX export / import            |
| POST   | `/images`, GET `/images/{file}`     | Image upload / serve             |
| GET    | `/stats`                            | Statistics dashboard data        |

---

## Project structure
```
tourplanner/
├── server/     # Spring Boot backend (presentation / business / dal layers)
├── frontend/   # Angular frontend (core / features (MVVM) / shared)
└── README.md
```
