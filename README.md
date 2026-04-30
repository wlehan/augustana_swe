# Augustana SWE Golf App

A full-stack web app for playing the Golf card game. The frontend is a React/Vite single-page app, and the backend is a Spring Boot REST API with authentication, game state, tutorial support, and database migrations.

## What The App Does

- Lets players create accounts and log in with JWT-based authentication.
- Lets authenticated users create a game, join a game by code, wait in a lobby, leave a game, and start play.
- Runs the Golf card-game loop: initial card flips, drawing from the deck or discard pile, swapping, discarding, revealing cards, ending rounds, and tracking scores.
- Includes a tutorial mode with a bot player for guided practice.
- Stores profile images and simple stats in browser local storage.
- Uses the backend database for users, games, players, rounds, cards, and scores.

## Game Rules

The goal is to have the lowest score after 9 holes, where each hole is one round.

- Each player has a 2x3 grid of cards.
- Players start by flipping two cards.
- On a turn, a player draws from the deck or discard pile.
- The drawn card can be swapped into the grid, or discarded.
- If the drawn card is discarded without swapping, the player flips one face-down card.
- A round ends after a player has revealed all cards and the remaining players finish their final turns.

Scoring:

- Number cards count as face value.
- 2s are worth -2.
- Aces are worth 1.
- Jacks and Queens are worth 10.
- Kings are worth 0.
- Matching cards in a column cancel to 0.

## Repository Layout

```text
augustana_swe/
├── backend/    # Spring Boot API, game logic, auth, migrations, tests
├── frontend/   # React + Vite client, pages, components, services, tests
└── README.md
```

## Tech Stack

- Frontend: React 19, React Router, Axios, Vite, Vitest
- Backend: Spring Boot 3.4, Spring Security, Spring Data JPA, Flyway, JJWT
- Local database: H2 in-memory database in MSSQL compatibility mode
- Non-local database: Azure SQL

## Requirements

- Node.js 20 or newer
- npm
- Java 21 or newer

The backend uses the Maven wrapper in `backend/`, so a separate Maven install is not required.

## First-Time Setup

Clone the repo and install frontend dependencies:

```bash
git clone <repo-url>
cd augustana_swe/frontend
npm install
```

The backend dependencies are downloaded automatically the first time you run `./mvnw`.

## Run Locally

Use two terminals from the repo root.

Terminal 1, start the backend with the local H2 database profile:

```bash
cd backend
SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
```

Backend URL:

```text
http://localhost:8080
```

Terminal 2, start the frontend:

```bash
cd frontend
npm run dev
```

Frontend URL:

```text
http://localhost:5173
```

Open `http://localhost:5173` in the browser. The frontend defaults to calling the backend at the same hostname on port `8080`, so `localhost:5173` talks to `localhost:8080`.

## Environment Variables

Frontend:

- `VITE_API_BASE_URL`: optional API base URL override.
- Default: `http://<current-browser-hostname>:8080`

Example `frontend/.env`:

```bash
VITE_API_BASE_URL=http://localhost:8080
```

Backend:

- `SPRING_PROFILES_ACTIVE=local`: uses an in-memory H2 database and local Flyway migrations.
- `JWT_SECRET`: optional override for JWT signing.
- `JWT_EXPIRATION_MS`: optional token lifetime override. Defaults to one day.

The default non-local backend configuration points at Azure SQL. For local development, always use the `local` Spring profile unless you are intentionally connecting to Azure.

## App Routes

- `/`: home page
- `/signup`: create an account
- `/login`: log in
- `/game-selection`: create a game or choose another game flow
- `/join`: join an existing game by code
- `/play`: active multiplayer game screen
- `/tutorial`: tutorial game screen

Most game routes require a logged-in user.

## API Overview

Base URL for local development:

```text
http://localhost:8080
```

Public endpoints:

- `POST /api/auth/signup`
- `POST /api/auth/login`
- `GET /health`
- `GET /health/db`

Authentication response:

```json
{
  "userId": 1,
  "username": "player1",
  "token": "jwt-token",
  "message": "Login successful."
}
```

Authenticated game endpoints:

- `POST /api/games`
- `POST /api/games/join`
- `POST /api/games/{gameId}/leave`
- `GET /api/games/{gameId}`
- `POST /api/games/{gameId}/start`
- `GET /api/games/{gameId}/state`
- `POST /api/games/{gameId}/actions/flip-initial`
- `POST /api/games/{gameId}/actions/draw`
- `POST /api/games/{gameId}/actions/swap`
- `POST /api/games/{gameId}/actions/discard`

Authenticated tutorial endpoints:

- `POST /api/tutorial/start`
- `GET /api/tutorial/{gameId}/state`
- `POST /api/tutorial/{gameId}/bot-flip`
- `POST /api/tutorial/{gameId}/bot-turn`

The frontend stores the JWT after signup/login and sends it as:

```text
Authorization: Bearer <token>
```

## Database

Local development uses:

```text
jdbc:h2:mem:golfdb;MODE=MSSQLServer
```

Local data is in memory, so it resets when the backend stops.

Migration folders:

- `backend/src/main/resources/db/migration/local`: local H2 migrations and tutorial bot seed data
- `backend/src/main/resources/db/migration`: non-local SQL Server migrations

## Useful Commands

Frontend commands from `frontend/`:

```bash
npm run dev            # Start Vite dev server
npm run build          # Build production assets
npm run preview        # Preview the production build
npm run lint           # Run ESLint
npm run test           # Run Vitest in watch mode
npm run test:run       # Run Vitest once
npm run test:coverage  # Run Vitest with coverage
```

Backend commands from `backend/`:

```bash
SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
./mvnw test
./mvnw clean test
```

## Testing

Run frontend unit tests:

```bash
cd frontend
npm run test:run
```

Run frontend coverage:

```bash
cd frontend
npm run test:coverage
```

Run backend tests:

```bash
cd backend
./mvnw clean test
```

View backend coverage after tests:

```bash
cd backend
open target/site/jacoco/index.html
```

Playwright is installed as a frontend dev dependency. If you are running browser tests, start the backend first with the local profile, then run Playwright from `frontend/`:

```bash
cd backend
SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
```

```bash
cd frontend
npx playwright test
```

## Troubleshooting

If the frontend cannot reach the backend:

- Make sure the backend is running on `http://localhost:8080`.
- Visit `http://localhost:8080/health`; it should return `OK`.
- Check `VITE_API_BASE_URL` if you are using a custom frontend `.env`.

If login or game requests return `401 Unauthorized`:

- Log in again so the browser has a fresh JWT.
- Make sure you are using the frontend app, or manually include `Authorization: Bearer <token>` for direct API calls.

If the backend tries to connect to Azure SQL locally:

- Stop it and restart with `SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run`.

If ports are already in use:

- Backend default: `8080`
- Frontend default: `5173`
- Stop the old process or run one of the apps on a different port.
