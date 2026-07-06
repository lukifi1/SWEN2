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
| Tests       | JUnit 5, Mockito, AssertJ, Spring MockMvc, Jasmine/Karma, Angular TestBed |

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
- **Model** — `core/models` contains TypeScript interfaces, while `core/api` contains
  injectable HttpClient services for backend access.
- **ViewModel** — `features/**/*.viewmodel.ts` contains injectable, signal-based UI state,
  computed state and commands. Examples: `AuthViewModel`, `ToursViewModel`,
  `TourFormViewModel`, `TourLogsViewModel`, `StatsViewModel`.
- **View** — standalone components and templates bind to view-model signals and forward UI
  events to view-model commands. Service subscriptions, loading/error handling, import/export,
  autocomplete, image upload and DTO mapping are kept in view-models.
- **Session state** — `AuthService` is intentionally not a view-model. It stores the global
  JWT/session signals and persists them in `localStorage`; `AuthViewModel` owns login and
  registration flow orchestration.

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

On Windows, install:
- Java 21 JDK
- Node.js LTS
- PostgreSQL for Windows, including pgAdmin or the `psql` command-line tool

Docker is not required. The application expects a running PostgreSQL server and connects to
it via the `DB_URL` configured in `server/.env`.

#### Installing PostgreSQL on Windows
1. Download the official Windows installer from
   https://www.postgresql.org/download/windows/
2. Run the installer and keep the default components selected:
   - PostgreSQL Server
   - pgAdmin 4
   - Command Line Tools
3. Keep the default port:
   ```text
   5432
   ```
4. Set and remember the password for the default PostgreSQL user:
   ```text
   postgres
   ```
5. Finish the installation.
6. Open **Windows Services** and check that the PostgreSQL service is running. It is usually
   named like:
   ```text
   postgresql-x64-16
   ```
   If it is not running, right-click it and choose **Start**.
7. Open **pgAdmin 4**, connect to the local server and enter the password chosen during
   installation.

### 1. Database

The PostgreSQL server must be running before the backend starts. Spring Boot/Hibernate can
create the application tables, but it does not create or start the PostgreSQL server itself.

#### Option A: Windows / pgAdmin
1. Open **pgAdmin**.
2. Connect to the local PostgreSQL server with the password chosen during installation.
3. Right-click **Databases** → **Create** → **Database...**.
4. Set the database name to:
   ```text
   tourplanner
   ```
5. Use the default `postgres` user, or create a dedicated user:
   - Login/Group Roles → Create → Login/Group Role...
   - Name: `touruser`
   - Password: `tourpassword`
   - Enable "Can login?"
6. If you created `touruser`, open pgAdmin's Query Tool on the `tourplanner` database and run:
   ```sql
   GRANT ALL PRIVILEGES ON DATABASE tourplanner TO touruser;
   GRANT ALL ON SCHEMA public TO touruser;
   ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO touruser;
   ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO touruser;
   ```

Then use these values in `server/.env`:

```dotenv
DB_URL=jdbc:postgresql://localhost:5432/tourplanner
DB_USERNAME=touruser
DB_PASSWORD=tourpassword
```

If you use the default `postgres` user instead:

```dotenv
DB_URL=jdbc:postgresql://localhost:5432/tourplanner
DB_USERNAME=postgres
DB_PASSWORD=the_password_chosen_during_postgresql_installation
```

#### Option B: Command line / psql
```bash
createdb tourplanner
psql -d tourplanner -c "CREATE USER touruser WITH PASSWORD 'tourpassword';"
psql -d tourplanner -c "GRANT ALL PRIVILEGES ON DATABASE tourplanner TO touruser;"
# PostgreSQL 15+ revokes CREATE on the public schema by default — grant it:
psql -d tourplanner -c "GRANT ALL ON SCHEMA public TO touruser;"
psql -d tourplanner -c "ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO touruser;"
psql -d tourplanner -c "ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO touruser;"
```
Hibernate creates the schema automatically (`ddl-auto=update`).

On Windows PowerShell, if `createdb` is not in the PATH, use the full PostgreSQL bin path,
for example:

```powershell
& "C:\Program Files\PostgreSQL\16\bin\createdb.exe" -U postgres tourplanner
& "C:\Program Files\PostgreSQL\16\bin\psql.exe" -U postgres -d tourplanner -c "CREATE USER touruser WITH PASSWORD 'tourpassword';"
& "C:\Program Files\PostgreSQL\16\bin\psql.exe" -U postgres -d tourplanner -c "GRANT ALL PRIVILEGES ON DATABASE tourplanner TO touruser;"
& "C:\Program Files\PostgreSQL\16\bin\psql.exe" -U postgres -d tourplanner -c "GRANT ALL ON SCHEMA public TO touruser;"
& "C:\Program Files\PostgreSQL\16\bin\psql.exe" -U postgres -d tourplanner -c "ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO touruser;"
& "C:\Program Files\PostgreSQL\16\bin\psql.exe" -U postgres -d tourplanner -c "ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO touruser;"
```

If PostgreSQL is not running, start the Windows service via **Services** or pgAdmin. The
backend will fail with a connection error if nothing is listening on `localhost:5432`.

### 2. Backend (http://localhost:8080)
Create `server/.env` first. Minimal working example:

```dotenv
DB_URL=jdbc:postgresql://localhost:5432/tourplanner
DB_USERNAME=touruser
DB_PASSWORD=tourpassword
IMAGE_BASE_PATH=./uploads
OPENROUTE_API_KEY=your_real_ors_key
OPENROUTE_BASE_URL=https://api.openrouteservice.org
JWT_SECRET=change-me-to-a-long-random-string-min-32-chars
JWT_EXPIRATION_MS=86400000
LOG_PATH=logs
CORS_ALLOWED_ORIGINS=http://localhost:4200
```

Start the backend:

```bash
cd server
# on macOS
set -a && . ./.env && set +a   # load configuration into the environment
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
cd server
Get-Content .env | ForEach-Object {
  if ($_ -and $_ -notmatch '^\s*#') {
    $name, $value = $_ -split '=', 2
    [Environment]::SetEnvironmentVariable($name, $value, 'Process')
  }
}
.\mvnw.cmd spring-boot:run
```

If the backend starts successfully, it listens on:

```text
http://localhost:8080
```

Common database startup errors:
- `Connection refused` / `localhost:5432` — PostgreSQL is not running.
- `database "tourplanner" does not exist` — create the `tourplanner` database first.
- `password authentication failed` — `DB_USERNAME` or `DB_PASSWORD` in `.env` is wrong.
- `permission denied for schema public` — run the schema grants shown above.

### 3. Frontend (http://localhost:4200)
```bash
cd frontend
npm install
npm start
```

On Windows PowerShell the same commands work:

```powershell
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

Frontend tests cover API services, auth/session state, view-model commands, autocomplete,
image upload, DTO mapping and formatting helpers.

```bash
cd frontend
npm test -- --watch=false --browsers=ChromeHeadless
```

For fast frontend compile checks without launching a browser:

```bash
cd frontend
npx tsc -p tsconfig.app.json --noEmit
npx tsc -p tsconfig.spec.json --noEmit
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
