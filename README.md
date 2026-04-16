# Augustana SWE Golf App

RAIK 284H Software Engineering IV project: a golf-themed card game app with a React frontend and Spring Boot backend.

## What This Website Does

This website is a digital version of the Golf card game, designed for multiple players.

- Players can create accounts and log in securely.
- Users can join or start game sessions from the game selection flow.
- The game interface is intended to guide players through rounds, track player state, and keep score.
- The backend handles authentication, core game data, and persistent storage so sessions can be managed reliably.

The goal is to provide a clean, accessible way to play Golf with regular playing-card rules without needing manual scorekeeping.

## How The Game Works (Golf Card Game)

The objective is to finish with the lowest total score after all rounds.

- Each player has a personal grid of face-down cards (commonly 6 cards in a 2x3 layout).
- On a turn, a player draws from the deck or discard pile.
- The player may swap the drawn card with one card in their grid, then discards the replaced card.
- If they do not want the drawn card, they discard it and usually flip one of their face-down cards (depending on variant rules).
- A round ends when a player has all cards revealed; other players get one final turn.
- Scores are added at the end of each round, and the game continues for a set number of rounds.

Typical scoring (can vary by house rules):

- Number cards count as face value
- Aces count as 1
- Face cards (J, Q, K) are typically 10
- Some variants treat Kings as 0
- Matching columns may cancel out (variant-dependent)

This app is built to support the core Golf loop: draw, replace/discard, reveal, and automatic score tracking across rounds.

## Repository Structure

```text
augustana_swe/
├── frontend/   # React + Vite client
└── backend/    # Spring Boot API + persistence
```

## Tech Stack

- Frontend: React 19, React Router, Axios, Vite
- Backend: Spring Boot 3.4, Spring Web, Spring Data JPA, Flyway
- Database (local profile): H2 in-memory (MSSQL compatibility mode)
- Database (non-local profiles): Azure SQL via Entra ID token auth

## Prerequisites

- Node.js 20+ and npm
- Java 21

## Quick Start (Local Development)

1. Start the backend

```bash
cd backend
SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
```

Backend runs on `http://localhost:8080`.

2. Start the frontend (new terminal)

```bash
cd frontend
npm install
npm run dev
```

Frontend runs on `http://localhost:5173`.

## Environment Variables

Frontend:

- `VITE_API_BASE_URL` (optional)
- Default: `http://localhost:8080`

Example (`frontend/.env`):

```bash
VITE_API_BASE_URL=http://localhost:8080
```

Backend:

- Local development uses `backend/src/main/resources/application-local.properties`
- Azure SQL/Entra config is enabled for profiles other than `local` and `test`

## Scripts and Commands

Frontend (`frontend/`):

```bash
npm run dev      # Start Vite dev server
npm run build    # Production build
npm run preview  # Preview production build
npm run lint     # ESLint
```

Backend (`backend/`):

```bash
./mvnw spring-boot:run           # Run app
./mvnw test                      # Run tests
SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
```

## API (Current)

Base URL: `http://localhost:8080`

- `POST /api/auth/signup`
- Signup body: `{ "username": "string", "password": "string", "email": "string | null" }`
- Signup validation: username required
- Signup validation: password required (minimum 6 characters)
- Signup validation: username must be unique
- `POST /api/auth/login`
- Login body: `{ "username": "string", "password": "string" }`
- `GET /health` -> `OK`
- `GET /health/db` -> `DB OK` when datasource is healthy

Auth success response shape:

```json
{
  "userId": 1,
  "username": "player1",
  "email": "player1@example.com",
  "message": "Login successful."
}
```

## Database

Flyway migration `V1__init.sql` creates:

- `users`
- `games`
- `game_players`
- `rounds`
- `cards`

## Notes

- CORS allowed origin: `http://localhost:5173`
- CORS allowed origin: `http://127.0.0.1:5173`
- CORS allowed origin: `http://localhost:3000`
- CORS allowed origin: `http://127.0.0.1:3000`
- Backend tests run with `test` profile and Flyway disabled.

## Testing

Frontend testing done using vitest and Playwright

To run vitest:
  In terminal, from directory /frontend use command:
    npm run test:run
To run Playwright:
  In terminal, from directory /backend use command:
    SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
  Keep this backend terminal open
  In new terminal, from directory /frontend use command:
    npx playwright test
Don't forget to close backend using ctrl C

To view frontend testing code coverage:
  In terminal, from directory /frontend use command:
    npm run test:coverage

Backend testing done using Java SpringBoot

To run backend tests:
  In terminal, from directory /backend use command:
  ./mvnw clean test

To view backend testing code coverage:
  In terminal, from directory /backend use command:
    open target/site/jacoco/index.html
