# 🚀 TourPlanner


TourPlanner is an application that helps users plan, organize, and track their personal tours such as hiking trips, bike rides, runs, or vacations.

Users can create and manage tours in advance, view route information, and record their experiences through tour logs. Each tour can be enriched with details like distance, duration, and difficulty, making it easy to keep an overview of past activities and compare different tours.

The goal of TourPlanner is to provide a simple and intuitive way to organize outdoor activities and reflect on them afterwards.

---

## 🧱 Tech Stack

### Frontend
- Angular
- TypeScript
- HTML / CSS

### Backend
- Java (Spring Boot)
- Spring Data JPA
- REST API

### Database
- PostgreSQL

### Other
- JSON (client-server communication)
- Git (version control)

---

## 🏗️ Architecture

```text
Angular (Frontend)  <--HTTP/JSON-->  Spring Boot (Backend)  --> PostgreSQL
```

Backend follows a **layered architecture**:
- Controller (presentation layer)
- Service (business logic)
- Repository (data access)

---

## ✨ Features

### Core Features
- User registration & login
- Create, update, delete tours (CRUD)
- Manage tour logs
- Full-text search

### TourModel Data
Each tour includes:
- Name
- Description
- Start & destination
- Distance & estimated time
- Transport type
- Route information (map)

### TourModel Logs
Each log includes:
- Date/time
- Comment
- Difficulty
- Distance & time
- Rating

---

## 🔌 External APIs

- OpenRouteService (route & distance calculation)
- Leaflet (map visualization)

---

## ⚙️ Setup Instructions

### 1. Clone Repository

```bash
git clone https://github.com/lukifi1/SWEN2.git
cd SWEN2
```

---

### 2. Backend (Spring Boot)

```bash
cd server
./mvnw spring-boot:run
```

Backend runs on:
```
http://localhost:8080
```

---

### 3. Frontend (Angular)

```bash
cd frontend
npm install
ng serve
```

Frontend runs on:
```
http://localhost:4200
```

---

### 4. Database

Make sure PostgreSQL is running.

Example configuration (`application.yml`):

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/tourdb
    username: postgres
    password: yourpassword
```

---

## 🧪 Testing

- Backend: JUnit
- Minimum: 20+ unit tests

Run tests:

```bash
./mvnw test
```

---

## 📂 Project Structure

```text
tourplanner/
├── server/        # Spring Boot backend
├── frontend/      # Angular frontend
└── README.md
```

---

## 📌 Development Notes

- Configuration is externalized (no secrets in code)
- Git history documents development progress
- Design patterns are applied where appropriate

---

## 👥 Team

- Lukas Fink
- Franziska Brandfellner

---

## 📊 Status

🚧 Work in progress


---
**AI generated**